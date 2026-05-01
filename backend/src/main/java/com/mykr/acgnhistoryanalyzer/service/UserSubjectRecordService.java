package com.mykr.acgnhistoryanalyzer.service;

import com.mykr.acgnhistoryanalyzer.common.enums.RecordPageSortType;
import com.mykr.acgnhistoryanalyzer.entity.Subject;
import com.mykr.acgnhistoryanalyzer.entity.UserSubjectRecord;
import com.mykr.acgnhistoryanalyzer.repository.SubjectRepository;
import com.mykr.acgnhistoryanalyzer.repository.UserSubjectCollectionRepository;
import com.mykr.acgnhistoryanalyzer.repository.UserSubjectRecordRepository;
import com.mykr.acgnhistoryanalyzer.request.UserSubjectRecordBatchImportRequest;
import com.mykr.acgnhistoryanalyzer.request.UserSubjectRecordCreateRequest;
import com.mykr.acgnhistoryanalyzer.request.UserSubjectRecordImportItemRequest;
import com.mykr.acgnhistoryanalyzer.response.*;
import com.mykr.acgnhistoryanalyzer.specification.UserSubjectRecordSpecifications;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserSubjectRecordService {

    private final UserSubjectRecordRepository userSubjectRecordRepository;
    private final UserSubjectCollectionRepository userSubjectCollectionRepository;
    private final SubjectRepository subjectRepository;
    private final TagService tagService;
    private final FranchiseAutoLinkService franchiseAutoLinkService;
    private final Validator validator;

    public UserSubjectRecordService(UserSubjectRecordRepository userSubjectRecordRepository,
                                    UserSubjectCollectionRepository userSubjectCollectionRepository,
                                    SubjectRepository subjectRepository,
                                    TagService tagService,
                                    FranchiseAutoLinkService franchiseAutoLinkService,
                                    Validator validator) {
        this.userSubjectRecordRepository = userSubjectRecordRepository;
        this.userSubjectCollectionRepository = userSubjectCollectionRepository;
        this.subjectRepository = subjectRepository;
        this.tagService = tagService;
        this.franchiseAutoLinkService = franchiseAutoLinkService;
        this.validator = validator;
    }

    public boolean subjectExists(Long subjectId) {
        return subjectRepository.existsById(subjectId);
    }

    @Transactional
    public UserSubjectRecordResponse createRecord(UserSubjectRecordCreateRequest request) {
        Subject subject = subjectRepository.findById(request.getSubjectId()).orElse(null);
        if (subject == null) {
            return null;
        }

        UserSubjectRecord record = new UserSubjectRecord(
                subject.getId(),
                subject.getDisplayTitle(),
                request.getScoreValue(),
                request.getRecordYear(),
                request.getRecordQuarter(),
                request.getNote()
        );

        UserSubjectRecord savedRecord = userSubjectRecordRepository.save(record);

        // 作品进入正式记录后，不再保留“想看/搁置”收藏状态。
        if (userSubjectCollectionRepository.existsBySubjectId(subject.getId())) {
            userSubjectCollectionRepository.deleteBySubjectId(subject.getId());
        }

        franchiseAutoLinkService.detectAndLinkForRecordedSubject(subject.getId());

        return toResponse(savedRecord);
    }

    public UserSubjectRecordImportResponse importRecords(UserSubjectRecordBatchImportRequest request) {
        List<UserSubjectRecordImportItemResponse> items = new ArrayList<>();

        int importedCount = 0;
        int skippedCount = 0;

        int rowNumber = 1;
        for (UserSubjectRecordImportItemRequest item : request.getRecords()) {
            String validationMessage = validateRecordImportItem(item);
            if (validationMessage != null) {
                skippedCount++;
                items.add(new UserSubjectRecordImportItemResponse(
                        null,
                        buildSubjectLabel(item, rowNumber),
                        "ERROR",
                        validationMessage
                ));
                rowNumber++;
                continue;
            }

            SubjectResolveResult resolveResult = resolveSubjectForRecordImport(item);

            if (resolveResult.hasAmbiguousCandidates()) {
                skippedCount++;
                items.add(new UserSubjectRecordImportItemResponse(
                        null,
                        buildSubjectLabel(item),
                        "SKIPPED",
                        "匹配到多个作品，请先在预览中选择一个候选作品后再导入"
                ));
                rowNumber++;
                continue;
            }

            if (resolveResult.hasError()) {
                skippedCount++;
                items.add(new UserSubjectRecordImportItemResponse(
                        null,
                        buildSubjectLabel(item),
                        "SKIPPED",
                        resolveResult.getErrorMessage()
                ));
                rowNumber++;
                continue;
            }

            Subject subject = resolveResult.getSubject();

            if (userSubjectRecordRepository.existsBySubjectId(subject.getId())) {
                skippedCount++;
                items.add(new UserSubjectRecordImportItemResponse(
                        null,
                        subject.getDisplayTitle(),
                        "SKIPPED",
                        "该作品已有正式记录，已跳过"
                ));
                rowNumber++;
                continue;
            }

            UserSubjectRecordCreateRequest createRequest = new UserSubjectRecordCreateRequest();
            createRequest.setSubjectId(subject.getId());
            createRequest.setScoreValue(item.getScoreValue());
            createRequest.setRecordYear(item.getRecordYear());
            createRequest.setRecordQuarter(item.getRecordQuarter());
            createRequest.setNote(item.getNote());

            UserSubjectRecordResponse savedRecord = createRecord(createRequest);
            importedCount++;

            items.add(new UserSubjectRecordImportItemResponse(
                    savedRecord.getId(),
                    subject.getDisplayTitle(),
                    "IMPORTED",
                    "导入成功"
            ));
            rowNumber++;
        }

        return new UserSubjectRecordImportResponse(
                request.getRecords().size(),
                importedCount,
                skippedCount,
                items
        );
    }

    public UserSubjectRecordImportPreviewResponse previewImportRecords(UserSubjectRecordBatchImportRequest request) {
        List<UserSubjectRecordImportPreviewItemResponse> items = new ArrayList<>();

        int canImportCount = 0;
        int skippedCount = 0;

        int rowNumber = 1;
        for (UserSubjectRecordImportItemRequest item : request.getRecords()) {
            String validationMessage = validateRecordImportItem(item);
            if (validationMessage != null) {
                skippedCount++;
                items.add(new UserSubjectRecordImportPreviewItemResponse(
                        buildSubjectLabel(item, rowNumber),
                        "ERROR",
                        validationMessage
                ));
                rowNumber++;
                continue;
            }

            SubjectResolveResult resolveResult = resolveSubjectForRecordImport(item);

            if (resolveResult.hasAmbiguousCandidates()) {
                skippedCount++;
                items.add(new UserSubjectRecordImportPreviewItemResponse(
                        buildSubjectLabel(item),
                        "AMBIGUOUS",
                        "匹配到多个作品，请在前端选择一个候选作品后再导入",
                        resolveResult.getCandidates()
                ));
                rowNumber++;
                continue;
            }

            if (resolveResult.hasError()) {
                skippedCount++;
                items.add(new UserSubjectRecordImportPreviewItemResponse(
                        buildSubjectLabel(item),
                        "SKIPPED",
                        resolveResult.getErrorMessage()
                ));
                rowNumber++;
                continue;
            }

            Subject subject = resolveResult.getSubject();

            if (userSubjectRecordRepository.existsBySubjectId(subject.getId())) {
                skippedCount++;
                items.add(new UserSubjectRecordImportPreviewItemResponse(
                        subject.getDisplayTitle(),
                        "SKIPPED",
                        "该作品已有正式记录，正式导入时会跳过"
                ));
                rowNumber++;
                continue;
            }

            canImportCount++;
            items.add(new UserSubjectRecordImportPreviewItemResponse(
                    subject.getDisplayTitle(),
                    "CAN_IMPORT",
                    "可以导入正式记录"
            ));
            rowNumber++;
        }

        return new UserSubjectRecordImportPreviewResponse(
                request.getRecords().size(),
                canImportCount,
                skippedCount,
                items
        );
    }

    public boolean recordExistsBySubjectId(Long subjectId) {
        return userSubjectRecordRepository.existsBySubjectId(subjectId);
    }

    public boolean recordExistsBySubjectIdExcludingCurrent(Long subjectId, Long currentRecordId) {
        return userSubjectRecordRepository.existsBySubjectIdAndIdNot(subjectId, currentRecordId);
    }

    public List<UserSubjectRecordResponse> getRecords(Integer year, Integer quarter,
                                                      Integer minScore, Integer maxScore) {
        Specification<UserSubjectRecord> specification =
                buildRecordSpecification(year, quarter, minScore, maxScore);

        List<UserSubjectRecord> records = userSubjectRecordRepository.findAll(specification);

        return records.stream()
                .map(this::toResponse)
                .toList();
    }

    public PageResponse<UserSubjectRecordResponse> getRecordPage(Integer year, Integer quarter,
                                                                 Integer minScore, Integer maxScore,
                                                                 String category, String keyword,
                                                                 Integer page, Integer size,
                                                                 String sortType) {
        int effectivePage = normalizePage(page);
        int effectiveSize = normalizeSize(size);
        RecordPageSortType effectiveSortType = normalizeRecordPageSort(sortType);

        String normalizedCategory = normalizeBlankToNull(category);
        String normalizedKeyword = normalizeBlankToNull(keyword);

        Pageable pageable = PageRequest.of(
                effectivePage,
                effectiveSize,
                buildRecordPageSort(effectiveSortType)
        );

        Page<UserSubjectRecord> recordPage =
                userSubjectRecordRepository.findRecordPageWithSubjectFilters(
                        year,
                        quarter,
                        minScore,
                        maxScore,
                        normalizedCategory,
                        normalizedKeyword,
                        pageable
                );

        List<UserSubjectRecordResponse> content = recordPage.getContent().stream()
                .map(this::toResponse)
                .toList();

        return new PageResponse<>(
                content,
                recordPage.getNumber(),
                recordPage.getSize(),
                recordPage.getTotalElements(),
                recordPage.getTotalPages(),
                recordPage.isFirst(),
                recordPage.isLast()
        );
    }

    public Integer getLatestRecordYear() {
        PageRequest pageRequest = PageRequest.of(
                0,
                1,
                Sort.by("recordYear").descending()
                        .and(Sort.by("recordQuarter").descending())
                        .and(Sort.by("id").descending())
        );

        Page<UserSubjectRecord> page = userSubjectRecordRepository.findAll(pageRequest);

        if (page.isEmpty()) {
            return null;
        }

        return page.getContent().get(0).getRecordYear();
    }

    public RecordScoreBandStatsResponse getScoreBandStats(Integer year, Integer quarter) {
        List<UserSubjectRecord> records = userSubjectRecordRepository.findAll(
                buildRecordSpecification(year, quarter, null, null)
        );

        int excellentCount = 0;
        int normalCount = 0;
        int badCount = 0;
        int unratedCount = 0;

        for (UserSubjectRecord record : records) {
            Integer score = record.getScoreValue();
            if (score == null) {
                unratedCount++;
            } else if (score >= 42 && score <= 50) {
                excellentCount++;
            } else if (score >= 35 && score <= 41) {
                normalCount++;
            } else if (score >= 0 && score <= 34) {
                badCount++;
            }
        }

        return new RecordScoreBandStatsResponse(
                records.size(),
                excellentCount,
                normalCount,
                badCount,
                unratedCount
        );
    }

    public RecordQuarterOverviewResponse getQuarterOverview(Integer year, Integer quarter) {
        List<UserSubjectRecord> records = userSubjectRecordRepository.findAll(
                buildRecordSpecification(year, quarter, null, null)
        );

        int totalCount = records.size();
        int highScoreCount = 0;
        int excellentCount = 0;
        int normalCount = 0;
        int badCount = 0;
        int unratedCount = 0;

        for (UserSubjectRecord record : records) {
            Integer score = record.getScoreValue();
            if (score == null) {
                unratedCount++;
            } else {
                if (score >= 45) {
                    highScoreCount++;
                }
                if (score >= 42 && score <= 50) {
                    excellentCount++;
                } else if (score >= 35 && score <= 41) {
                    normalCount++;
                } else if (score >= 0 && score <= 34) {
                    badCount++;
                }
            }
        }

        return new RecordQuarterOverviewResponse(
                year,
                quarter,
                totalCount,
                highScoreCount,
                totalCount,
                0,
                0,
                excellentCount,
                normalCount,
                badCount,
                unratedCount
        );
    }

    public RecordYearOverviewResponse getYearOverview(Integer year) {
        List<UserSubjectRecord> records = userSubjectRecordRepository.findAll(
                buildRecordSpecification(year, null, null, null)
        );

        int totalCount = records.size();
        int highScoreCount = 0;
        int excellentCount = 0;
        int normalCount = 0;
        int badCount = 0;
        int unratedCount = 0;
        int q1Count = 0;
        int q2Count = 0;
        int q3Count = 0;
        int q4Count = 0;

        for (UserSubjectRecord record : records) {
            if (record.getRecordQuarter() != null) {
                switch (record.getRecordQuarter()) {
                    case 1 -> q1Count++;
                    case 2 -> q2Count++;
                    case 3 -> q3Count++;
                    case 4 -> q4Count++;
                    default -> {
                    }
                }
            }

            Integer score = record.getScoreValue();
            if (score == null) {
                unratedCount++;
            } else {
                if (score >= 45) {
                    highScoreCount++;
                }
                if (score >= 42 && score <= 50) {
                    excellentCount++;
                } else if (score >= 35 && score <= 41) {
                    normalCount++;
                } else if (score >= 0 && score <= 34) {
                    badCount++;
                }
            }
        }

        return new RecordYearOverviewResponse(
                year,
                totalCount,
                highScoreCount,
                totalCount,
                0,
                0,
                excellentCount,
                normalCount,
                badCount,
                unratedCount,
                q1Count,
                q2Count,
                q3Count,
                q4Count
        );
    }

    public List<UserSubjectRecordResponse> getHighScoreRecords(Integer year, Integer quarter, Integer minScore) {
        int effectiveMinScore = (minScore == null) ? 45 : minScore;

        List<UserSubjectRecord> records = userSubjectRecordRepository.findAll(
                buildRecordSpecification(year, quarter, effectiveMinScore, 50)
        );

        return records.stream()
                .map(this::toResponse)
                .sorted((a, b) -> {
                    Integer scoreA = a.getScoreValue();
                    Integer scoreB = b.getScoreValue();

                    if (scoreA == null && scoreB == null) {
                        return 0;
                    }
                    if (scoreA == null) {
                        return 1;
                    }
                    if (scoreB == null) {
                        return -1;
                    }
                    return scoreB.compareTo(scoreA);
                })
                .toList();
    }

    public UserSubjectRecordResponse getRecordById(Long id) {
        UserSubjectRecord record = userSubjectRecordRepository.findById(id).orElse(null);
        if (record == null) {
            return null;
        }
        return toResponse(record);
    }

    public List<UserSubjectRecordResponse> getRecordsBySubjectId(Long subjectId) {
        return userSubjectRecordRepository.findBySubjectId(subjectId).stream()
                .map(this::toResponse)
                .sorted(
                        java.util.Comparator.comparing(UserSubjectRecordResponse::getRecordYear,
                                        java.util.Comparator.nullsLast(Integer::compareTo))
                                .thenComparing(UserSubjectRecordResponse::getRecordQuarter,
                                        java.util.Comparator.nullsLast(Integer::compareTo))
                                .reversed()
                )
                .toList();
    }

    @Transactional
    public UserSubjectRecordResponse updateRecord(Long id, UserSubjectRecordCreateRequest request) {
        UserSubjectRecord existingRecord = userSubjectRecordRepository.findById(id).orElse(null);
        if (existingRecord == null) {
            return null;
        }

        Subject subject = subjectRepository.findById(request.getSubjectId()).orElse(null);
        if (subject == null) {
            return null;
        }

        existingRecord.setSubjectId(subject.getId());
        existingRecord.setSubjectTitle(subject.getDisplayTitle());
        existingRecord.setScoreValue(request.getScoreValue());
        existingRecord.setRecordYear(request.getRecordYear());
        existingRecord.setRecordQuarter(request.getRecordQuarter());
        existingRecord.setNote(request.getNote());

        UserSubjectRecord updatedRecord = userSubjectRecordRepository.save(existingRecord);

        if (userSubjectCollectionRepository.existsBySubjectId(subject.getId())) {
            userSubjectCollectionRepository.deleteBySubjectId(subject.getId());
        }

        return toResponse(updatedRecord);
    }

    public boolean deleteRecord(Long id) {
        if (!userSubjectRecordRepository.existsById(id)) {
            return false;
        }

        userSubjectRecordRepository.deleteById(id);
        return true;
    }

    private Specification<UserSubjectRecord> buildRecordSpecification(Integer year, Integer quarter,
                                                                      Integer minScore, Integer maxScore) {
        return Specification.allOf(
                UserSubjectRecordSpecifications.hasRecordYear(year),
                UserSubjectRecordSpecifications.hasRecordQuarter(quarter),
                UserSubjectRecordSpecifications.hasMinScore(minScore),
                UserSubjectRecordSpecifications.hasMaxScore(maxScore)
        );
    }

    private SubjectResolveResult resolveSubjectForRecordImport(UserSubjectRecordImportItemRequest item) {
        List<Subject> matchedSubjects = resolveSubjectCandidates(item);

        if (matchedSubjects.isEmpty()) {
            return new SubjectResolveResult(null, "作品不存在，请先导入作品库");
        }

        if (matchedSubjects.size() > 1) {
            return new SubjectResolveResult(
                    null,
                    null,
                    toCandidateResponses(matchedSubjects)
            );
        }

        return new SubjectResolveResult(matchedSubjects.get(0), null);
    }

    private List<Subject> resolveSubjectCandidates(UserSubjectRecordImportItemRequest item) {
        if (item.getSubjectId() != null) {
            Subject direct = subjectRepository.findById(item.getSubjectId()).orElse(null);
            return direct == null ? List.of() : List.of(direct);
        }

        if (hasText(item.getSourceId())) {
            return subjectRepository.findAllBySourceIdentity(normalizeTextKey(item.getSourceId()));
        }

        if (!hasText(item.getSubjectTitle())) {
            return List.of();
        }

        return subjectRepository.searchAllNormalByTitleKeyword(normalizeTextKey(item.getSubjectTitle())).stream()
                .filter(subject -> item.getSubjectSeasonIndex() == null
                        || equalsNumber(subject.getSeasonIndex(), item.getSubjectSeasonIndex()))
                .filter(subject -> item.getSubjectPartIndex() == null
                        || equalsNumber(subject.getPartIndex(), item.getSubjectPartIndex()))
                .toList();
    }

    private String normalizeTextKey(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "";
        }
        return value.trim().toLowerCase();
    }

    private String validateRecordImportItem(UserSubjectRecordImportItemRequest item) {
        if (item == null) {
            return "字段校验失败：导入行不能为空";
        }

        if (item.getSubjectId() == null && !hasText(item.getSourceId()) && !hasText(item.getSubjectTitle())) {
            return "字段校验失败：subjectId、sourceId、subjectTitle 至少提供一个";
        }

        Set<ConstraintViolation<UserSubjectRecordImportItemRequest>> violations =
                validator.validate(item);
        if (violations.isEmpty()) {
            return null;
        }

        return "字段校验失败：" + violations.stream()
                .sorted((left, right) -> left.getPropertyPath().toString()
                        .compareTo(right.getPropertyPath().toString()))
                .map(violation -> violation.getPropertyPath() + " " + violation.getMessage())
                .collect(Collectors.joining("；"));
    }

    private String buildSubjectLabel(UserSubjectRecordImportItemRequest item, int rowNumber) {
        if (item == null) {
            return "第 " + rowNumber + " 行";
        }

        String label = buildSubjectLabel(item);
        return label.isBlank() ? "第 " + rowNumber + " 行" : label;
    }

    private String buildSubjectLabel(UserSubjectRecordImportItemRequest item) {
        if (item == null) {
            return "";
        }

        if (item.getSubjectId() != null) {
            return "subjectId=" + item.getSubjectId();
        }
        if (hasText(item.getSourceId())) {
            return "sourceId=" + item.getSourceId().trim();
        }
        if (hasText(item.getSubjectTitle())) {
            String title = item.getSubjectTitle().trim();
            String season = item.getSubjectSeasonIndex() == null ? "" : " S" + item.getSubjectSeasonIndex();
            String part = item.getSubjectPartIndex() == null ? "" : " P" + item.getSubjectPartIndex();
            return title + season + part;
        }
        return "";
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private int normalizePage(Integer page) {
        if (page == null || page < 0) {
            return 0;
        }
        return page;
    }

    private int normalizeSize(Integer size) {
        if (size == null) {
            return 10;
        }
        if (size < 1) {
            return 1;
        }
        return Math.min(size, 50);
    }

    private RecordPageSortType normalizeRecordPageSort(String sortType) {
        if (sortType == null || sortType.isBlank()) {
            return RecordPageSortType.TITLE_ASC;
        }
        return RecordPageSortType.valueOf(sortType.toUpperCase());
    }

    private String normalizeBlankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private Sort buildRecordPageSort(RecordPageSortType sortType) {
        return switch (sortType) {
            case RECORD_TIME_DESC -> Sort.by("recordYear").descending()
                    .and(Sort.by("recordQuarter").descending())
                    .and(Sort.by("id").descending());
            case RECORD_TIME_ASC -> Sort.by("recordYear").ascending()
                    .and(Sort.by("recordQuarter").ascending())
                    .and(Sort.by("id").ascending());
            case SCORE_DESC -> Sort.by("scoreValue").descending()
                    .and(Sort.by("subjectTitle").ascending())
                    .and(Sort.by("id").descending());
            case SCORE_ASC -> Sort.by("scoreValue").ascending()
                    .and(Sort.by("subjectTitle").ascending())
                    .and(Sort.by("id").ascending());
            case TITLE_ASC -> Sort.by("subjectTitle").ascending()
                    .and(Sort.by("id").ascending());
            case TITLE_DESC -> Sort.by("subjectTitle").descending()
                    .and(Sort.by("id").descending());
        };
    }

    private static class SubjectResolveResult {
        private final Subject subject;
        private final String errorMessage;
        private final List<SubjectMatchCandidateResponse> candidates;

        private SubjectResolveResult(Subject subject, String errorMessage) {
            this(subject, errorMessage, List.of());
        }

        private SubjectResolveResult(Subject subject,
                                     String errorMessage,
                                     List<SubjectMatchCandidateResponse> candidates) {
            this.subject = subject;
            this.errorMessage = errorMessage;
            this.candidates = candidates == null ? List.of() : candidates;
        }

        public Subject getSubject() { return subject; }

        public String getErrorMessage() { return errorMessage; }

        public List<SubjectMatchCandidateResponse> getCandidates() { return candidates; }

        public boolean hasAmbiguousCandidates() { return !candidates.isEmpty(); }

        public boolean hasError() { return errorMessage != null; }
    }

    private boolean equalsNumber(Number left, Number right) {
        if (left == null && right == null) {
            return true;
        }
        if (left == null || right == null) {
            return false;
        }
        return left.longValue() == right.longValue();
    }

    private List<SubjectMatchCandidateResponse> toCandidateResponses(List<Subject> matchedSubjects) {
        return matchedSubjects.stream()
                .sorted(Comparator.comparing(Subject::getId))
                .map(subject -> new SubjectMatchCandidateResponse(
                        subject.getId(),
                        subject.getDisplayTitle(),
                        subject.getSeasonIndex(),
                        subject.getPartIndex(),
                        subject.getReleaseYear(),
                        subject.getReleaseQuarter(),
                        subject.getCategory()
                ))
                .toList();
    }

    private UserSubjectRecordResponse toResponse(UserSubjectRecord record) {
        String subjectTitle = record.getSubjectTitle();
        String subjectCategory = null;
        Integer subjectReleaseYear = null;
        Integer subjectReleaseQuarter = null;
        String subjectCoverUrl = null;
        List<TagResponse> tagList = List.of();

        if (record.getSubjectId() != null) {
            Subject subject = subjectRepository.findById(record.getSubjectId()).orElse(null);
            if (subject != null) {
                subjectTitle = subject.getDisplayTitle();
                subjectCategory = subject.getCategory();
                subjectReleaseYear = subject.getReleaseYear();
                subjectReleaseQuarter = subject.getReleaseQuarter();
                subjectCoverUrl = subject.getCoverUrl();
                tagList = tagService.toResponses(subject.getStructuredTags());
            }
        }

        return new UserSubjectRecordResponse(
                record.getId(),
                record.getSubjectId(),
                subjectTitle,
                subjectCategory,
                subjectReleaseYear,
                subjectReleaseQuarter,
                subjectCoverUrl,
                tagList,
                record.getScoreValue(),
                record.getRecordYear(),
                record.getRecordQuarter(),
                record.getNote(),
                record.getCreatedAt(),
                record.getUpdatedAt()
        );
    }
}
