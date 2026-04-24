package com.mykr.acgnhistoryanalyzer.service;

import com.mykr.acgnhistoryanalyzer.common.enums.RecordStatus;
import com.mykr.acgnhistoryanalyzer.common.enums.RecordPageSortType;
import com.mykr.acgnhistoryanalyzer.entity.Subject;
import com.mykr.acgnhistoryanalyzer.entity.UserSubjectRecord;
import com.mykr.acgnhistoryanalyzer.repository.SubjectRepository;
import com.mykr.acgnhistoryanalyzer.repository.UserSubjectRecordRepository;
import com.mykr.acgnhistoryanalyzer.request.UserSubjectRecordCreateRequest;
import com.mykr.acgnhistoryanalyzer.request.UserSubjectRecordBatchImportRequest;
import com.mykr.acgnhistoryanalyzer.request.UserSubjectRecordImportItemRequest;
import com.mykr.acgnhistoryanalyzer.response.RecordQuarterOverviewResponse;
import com.mykr.acgnhistoryanalyzer.response.RecordScoreBandStatsResponse;
import com.mykr.acgnhistoryanalyzer.response.RecordYearOverviewResponse;
import com.mykr.acgnhistoryanalyzer.response.UserSubjectRecordResponse;
import com.mykr.acgnhistoryanalyzer.response.PageResponse;
import com.mykr.acgnhistoryanalyzer.response.UserSubjectRecordImportItemResponse;
import com.mykr.acgnhistoryanalyzer.response.UserSubjectRecordImportResponse;
import com.mykr.acgnhistoryanalyzer.response.UserSubjectRecordImportPreviewItemResponse;
import com.mykr.acgnhistoryanalyzer.response.UserSubjectRecordImportPreviewResponse;
import com.mykr.acgnhistoryanalyzer.specification.UserSubjectRecordSpecifications;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

@Service
public class UserSubjectRecordService {

    private final UserSubjectRecordRepository userSubjectRecordRepository;
    private final SubjectRepository subjectRepository;

    public UserSubjectRecordService(UserSubjectRecordRepository userSubjectRecordRepository,
                                    SubjectRepository subjectRepository) {
        this.userSubjectRecordRepository = userSubjectRecordRepository;
        this.subjectRepository = subjectRepository;
    }

    public boolean subjectExists(Long subjectId) {
        return subjectRepository.existsById(subjectId);
    }

    public UserSubjectRecordResponse createRecord(UserSubjectRecordCreateRequest request) {
        Subject subject = subjectRepository.findById(request.getSubjectId()).orElse(null);

        if (subject == null) {
            return null;
        }

        UserSubjectRecord record = new UserSubjectRecord(
                subject.getId(),
                subject.getDisplayTitle(),
                request.getRecordStatus(),
                request.getScoreValue(),
                request.getRecordYear(),
                request.getRecordQuarter(),
                request.getNote()
        );

        UserSubjectRecord savedRecord = userSubjectRecordRepository.save(record);
        return toResponse(savedRecord);
    }

    public UserSubjectRecordImportResponse importRecords(UserSubjectRecordBatchImportRequest request) {
        List<UserSubjectRecordImportItemResponse> items = new ArrayList<>();

        int importedCount = 0;
        int skippedCount = 0;

        for (UserSubjectRecordImportItemRequest item : request.getRecords()) {
            SubjectResolveResult resolveResult = resolveSubjectForRecordImport(
                    item.getSubjectTitleCn(),
                    item.getSubjectSubtitle(),
                    item.getSubjectSeasonIndex(),
                    item.getSubjectPartIndex(),
                    item.getSubjectCategory()
            );

            if (resolveResult.hasError()) {
                skippedCount++;
                items.add(new UserSubjectRecordImportItemResponse(
                        null,
                        buildSubjectLabel(item),
                        "SKIPPED",
                        resolveResult.getErrorMessage()
                ));
                continue;
            }
            Subject subject = resolveResult.getSubject();

            if (subject == null) {
                skippedCount++;
                items.add(new UserSubjectRecordImportItemResponse(
                        null,
                        buildSubjectLabel(item),
                        "SKIPPED",
                        "作品不存在，请先导入作品库"
                ));
                continue;
            }

            if (userSubjectRecordRepository.existsBySubjectId(subject.getId())) {
                skippedCount++;
                items.add(new UserSubjectRecordImportItemResponse(
                        null,
                        buildSubjectLabel(item),
                        "SKIPPED",
                        "该作品已有记录，已跳过"
                ));
                continue;
            }

            UserSubjectRecordCreateRequest createRequest = new UserSubjectRecordCreateRequest();
            createRequest.setSubjectId(subject.getId());
            createRequest.setRecordStatus(item.getRecordStatus());
            createRequest.setScoreValue(item.getScoreValue());
            createRequest.setRecordYear(item.getRecordYear());
            createRequest.setRecordQuarter(item.getRecordQuarter());
            createRequest.setNote(item.getNote());

            UserSubjectRecordResponse savedRecord = createRecord(createRequest);
            importedCount++;

            items.add(new UserSubjectRecordImportItemResponse(
                    savedRecord.getId(),
                    buildSubjectLabel(item),
                    "IMPORTED",
                    "导入成功"
            ));
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

        for (UserSubjectRecordImportItemRequest item : request.getRecords()) {
            SubjectResolveResult resolveResult = resolveSubjectForRecordImport(
                    item.getSubjectTitleCn(),
                    item.getSubjectSubtitle(),
                    item.getSubjectSeasonIndex(),
                    item.getSubjectPartIndex(),
                    item.getSubjectCategory()
            );

            if (resolveResult.hasError()) {
                skippedCount++;
                items.add(new UserSubjectRecordImportPreviewItemResponse(
                        buildSubjectLabel(item),
                        "SKIPPED",
                        resolveResult.getErrorMessage()
                ));
                continue;
            }
            Subject subject = resolveResult.getSubject();

            if (userSubjectRecordRepository.existsBySubjectId(subject.getId())) {
                skippedCount++;
                items.add(new UserSubjectRecordImportPreviewItemResponse(
                        buildSubjectLabel(item),
                        "SKIPPED",
                        "该作品已有记录，正式导入时会跳过"
                ));
                continue;
            }

            canImportCount++;
            items.add(new UserSubjectRecordImportPreviewItemResponse(
                    buildSubjectLabel(item),
                    "CAN_IMPORT",
                    "可以导入"
            ));
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
                                                      RecordStatus status, Integer minScore, Integer maxScore) {

        Specification<UserSubjectRecord> specification =
                buildRecordSpecification(year, quarter, status, minScore, maxScore);

        List<UserSubjectRecord> records = userSubjectRecordRepository.findAll(specification);

        return records.stream()
                .map(this::toResponse)
                .toList();
    }

    public PageResponse<UserSubjectRecordResponse> getRecordPage(Integer year, Integer quarter,
                                                                 RecordStatus status,
                                                                 Integer minScore, Integer maxScore,
                                                                 String category, String keyword,
                                                                 Integer page, Integer size,
                                                                 String sortType) {

        int effectivePage = normalizePage(page);
        int effectiveSize = normalizeSize(size);
        RecordPageSortType effectiveSortType = normalizeRecordPageSort(sortType);

        Specification<UserSubjectRecord> specification =
                buildRecordSpecification(year, quarter, status, minScore, maxScore);

        Pageable pageable = PageRequest.of(
                effectivePage,
                effectiveSize,
                buildRecordPageSort(effectiveSortType)
        );

        Page<UserSubjectRecord> recordPage = userSubjectRecordRepository.findAll(specification, pageable);

        List<UserSubjectRecordResponse> content = recordPage.getContent().stream()
                .map(this::toResponse)
                .filter(record -> category == null || category.isBlank()
                        || category.equalsIgnoreCase(record.getSubjectCategory()))
                .filter(record -> keyword == null || keyword.isBlank()
                        || matchesKeyword(record.getSubjectTitle(), keyword))
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
            return RecordPageSortType.RECORD_TIME_DESC;
        }
        return RecordPageSortType.valueOf(sortType.toUpperCase());
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
                    .and(Sort.by("id").descending());
            case SCORE_ASC -> Sort.by("scoreValue").ascending()
                    .and(Sort.by("id").ascending());
            case TITLE_ASC -> Sort.by("subjectTitle").ascending()
                    .and(Sort.by("id").ascending());
            case TITLE_DESC -> Sort.by("subjectTitle").descending()
                    .and(Sort.by("id").descending());
        };
    }

    private boolean matchesKeyword(String text, String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return true;
        }
        if (text == null) {
            return false;
        }
        return text.toLowerCase().contains(keyword.toLowerCase());
    }

    public RecordScoreBandStatsResponse getScoreBandStats(Integer year, Integer quarter, RecordStatus status) {
        Specification<UserSubjectRecord> specification =
                buildRecordSpecification(year, quarter, status, null, null);

        List<UserSubjectRecord> records = userSubjectRecordRepository.findAll(specification);

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
        Specification<UserSubjectRecord> specification =
                buildRecordSpecification(year, quarter, null, null, null);

        List<UserSubjectRecord> records = userSubjectRecordRepository.findAll(specification);

        int totalCount = records.size();
        int highScoreCount = 0;
        int watchedCount = 0;
        int onHoldCount = 0;
        int wantToWatchCount = 0;
        int excellentCount = 0;
        int normalCount = 0;
        int badCount = 0;
        int unratedCount = 0;

        for (UserSubjectRecord record : records) {
            if (record.getRecordStatus() != null) {
                switch (record.getRecordStatus()) {
                    case WATCHED -> watchedCount++;
                    case ON_HOLD -> onHoldCount++;
                    case WANT_TO_WATCH -> wantToWatchCount++;
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

        return new RecordQuarterOverviewResponse(
                year,
                quarter,
                totalCount,
                highScoreCount,
                watchedCount,
                onHoldCount,
                wantToWatchCount,
                excellentCount,
                normalCount,
                badCount,
                unratedCount
        );
    }

    public RecordYearOverviewResponse getYearOverview(Integer year) {
        Specification<UserSubjectRecord> specification =
                buildRecordSpecification(year, null, null, null, null);

        List<UserSubjectRecord> records = userSubjectRecordRepository.findAll(specification);

        int totalCount = records.size();
        int highScoreCount = 0;
        int watchedCount = 0;
        int onHoldCount = 0;
        int wantToWatchCount = 0;
        int excellentCount = 0;
        int normalCount = 0;
        int badCount = 0;
        int unratedCount = 0;
        int q1Count = 0;
        int q2Count = 0;
        int q3Count = 0;
        int q4Count = 0;

        for (UserSubjectRecord record : records) {
            if (record.getRecordStatus() != null) {
                switch (record.getRecordStatus()) {
                    case WATCHED -> watchedCount++;
                    case ON_HOLD -> onHoldCount++;
                    case WANT_TO_WATCH -> wantToWatchCount++;
                }
            }

            if (record.getRecordQuarter() != null) {
                switch (record.getRecordQuarter()) {
                    case 1 -> q1Count++;
                    case 2 -> q2Count++;
                    case 3 -> q3Count++;
                    case 4 -> q4Count++;
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
                watchedCount,
                onHoldCount,
                wantToWatchCount,
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

    public List<UserSubjectRecordResponse> getHighScoreRecords(Integer year, Integer quarter,
                                                               RecordStatus status, Integer minScore) {

        int effectiveMinScore = (minScore == null) ? 45 : minScore;

        Specification<UserSubjectRecord> specification =
                buildRecordSpecification(year, quarter, status, effectiveMinScore, 50);

        List<UserSubjectRecord> records = userSubjectRecordRepository.findAll(specification);

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
        existingRecord.setRecordStatus(request.getRecordStatus());
        existingRecord.setScoreValue(request.getScoreValue());
        existingRecord.setRecordYear(request.getRecordYear());
        existingRecord.setRecordQuarter(request.getRecordQuarter());
        existingRecord.setNote(request.getNote());

        UserSubjectRecord updatedRecord = userSubjectRecordRepository.save(existingRecord);
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
                                                                      RecordStatus status,
                                                                      Integer minScore, Integer maxScore) {
        return Specification.allOf(
                UserSubjectRecordSpecifications.hasRecordYear(year),
                UserSubjectRecordSpecifications.hasRecordQuarter(quarter),
                UserSubjectRecordSpecifications.hasRecordStatus(status),
                UserSubjectRecordSpecifications.hasMinScore(minScore),
                UserSubjectRecordSpecifications.hasMaxScore(maxScore)
        );
    }

    private SubjectResolveResult resolveSubjectForRecordImport(
            String titleCn,
            String subtitle,
            Integer seasonIndex,
            Integer partIndex,
            String category
    ) {
        List<Subject> matchedSubjects = subjectRepository.findAllByBusinessUnique(
                normalizeTextKey(titleCn),
                normalizeTextKey(subtitle),
                normalizeNumberKey(seasonIndex),
                normalizeNumberKey(partIndex),
                normalizeTextKey(category)
        );

        if (matchedSubjects.isEmpty()) {
            return new SubjectResolveResult(null, "作品不存在，请先导入作品库");
        }

        if (matchedSubjects.size() > 1) {
            return new SubjectResolveResult(null, "匹配到多个作品，无法唯一定位，请先清理重复作品");
        }

        return new SubjectResolveResult(matchedSubjects.get(0), null);
    }

    private String normalizeTextKey(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "";
        }
        return value.trim().toLowerCase();
    }

    private Integer normalizeNumberKey(Integer value) {
        return value == null ? -1 : value;
    }

    private String buildSubjectLabel(UserSubjectRecordImportItemRequest item) {
        String title = item.getSubjectTitleCn() == null ? "" : item.getSubjectTitleCn().trim();
        String subtitle = item.getSubjectSubtitle() == null ? "" : item.getSubjectSubtitle().trim();

        if (!subtitle.isEmpty()) {
            return title + " / " + subtitle;
        }
        return title;
    }

    private static class SubjectResolveResult {
        private final Subject subject;
        private final String errorMessage;

        private SubjectResolveResult(Subject subject, String errorMessage) {
            this.subject = subject;
            this.errorMessage = errorMessage;
        }

        public Subject getSubject() {
            return subject;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public boolean hasError() {
            return errorMessage != null;
        }
    }

    private UserSubjectRecordResponse toResponse(UserSubjectRecord record) {
        String subjectTitle = record.getSubjectTitle();
        String subjectCategory = null;
        Integer subjectReleaseYear = null;
        Integer subjectReleaseQuarter = null;
        String subjectCoverUrl = null;

        if (record.getSubjectId() != null) {
            Subject subject = subjectRepository.findById(record.getSubjectId()).orElse(null);
            if (subject != null) {
                subjectTitle = subject.getDisplayTitle();
                subjectCategory = subject.getCategory();
                subjectReleaseYear = subject.getReleaseYear();
                subjectReleaseQuarter = subject.getReleaseQuarter();
                subjectCoverUrl = subject.getCoverUrl();
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
                record.getRecordStatus().name(),
                record.getRecordStatus().getLabel(),
                record.getScoreValue(),
                record.getRecordYear(),
                record.getRecordQuarter(),
                record.getNote()
        );
    }
}