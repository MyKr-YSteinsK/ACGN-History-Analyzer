package com.mykr.acgnhistoryanalyzer.service;

import com.mykr.acgnhistoryanalyzer.common.enums.CollectionType;
import com.mykr.acgnhistoryanalyzer.common.enums.HomeLibrarySortType;
import com.mykr.acgnhistoryanalyzer.common.enums.LibraryStatus;
import com.mykr.acgnhistoryanalyzer.common.enums.SubjectPageSortType;
import com.mykr.acgnhistoryanalyzer.entity.Franchise;
import com.mykr.acgnhistoryanalyzer.entity.Subject;
import com.mykr.acgnhistoryanalyzer.repository.FranchiseRepository;
import com.mykr.acgnhistoryanalyzer.repository.SubjectRepository;
import com.mykr.acgnhistoryanalyzer.repository.UserSubjectCollectionRepository;
import com.mykr.acgnhistoryanalyzer.repository.UserSubjectRecordRepository;
import com.mykr.acgnhistoryanalyzer.request.SubjectBatchImportRequest;
import com.mykr.acgnhistoryanalyzer.request.SubjectCreateRequest;
import com.mykr.acgnhistoryanalyzer.response.*;
import com.mykr.acgnhistoryanalyzer.specification.SubjectSpecifications;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SubjectService {

    private final SubjectRepository subjectRepository;
    private final UserSubjectRecordRepository userSubjectRecordRepository;
    private final UserSubjectCollectionRepository userSubjectCollectionRepository;
    private final UserSubjectRecordService userSubjectRecordService;
    private final UserSubjectCollectionService userSubjectCollectionService;
    private final FranchiseRepository franchiseRepository;
    private final TagService tagService;
    private final CoverStorageService coverStorageService;
    private final Validator validator;

    public SubjectService(SubjectRepository subjectRepository,
                          UserSubjectRecordRepository userSubjectRecordRepository,
                          UserSubjectCollectionRepository userSubjectCollectionRepository,
                          UserSubjectRecordService userSubjectRecordService,
                          UserSubjectCollectionService userSubjectCollectionService,
                          FranchiseRepository franchiseRepository,
                          TagService tagService,
                          CoverStorageService coverStorageService,
                          Validator validator) {
        this.subjectRepository = subjectRepository;
        this.userSubjectRecordRepository = userSubjectRecordRepository;
        this.userSubjectCollectionRepository = userSubjectCollectionRepository;
        this.userSubjectRecordService = userSubjectRecordService;
        this.userSubjectCollectionService = userSubjectCollectionService;
        this.franchiseRepository = franchiseRepository;
        this.tagService = tagService;
        this.coverStorageService = coverStorageService;
        this.validator = validator;
    }

    public SubjectResponse createSubject(SubjectCreateRequest request) {
        Subject subject = buildSubjectFromRequest(request, resolveFranchiseIdForCreate(request));
        tagService.syncSubjectTags(subject, request.getTagNames());
        Subject savedSubject = subjectRepository.save(subject);
        return toResponse(savedSubject);
    }

    public List<SubjectResponse> getSubjects(Integer year, Integer quarter,
                                             String category, String keyword, String status) {
        return getSubjects(year, quarter, category, keyword, status, null);
    }

    public List<SubjectResponse> getSubjects(Integer year, Integer quarter,
                                             String category, String keyword,
                                             String status, String sortType) {
        return getSubjects(year, quarter, category, keyword, status, sortType, null);
    }

    public List<SubjectResponse> getSubjects(Integer year, Integer quarter,
                                             String category, String keyword,
                                             String status, String sortType,
                                             String tagName) {
        String subjectStatus = isLibraryStatus(status) ? "NORMAL" : status;
        LibraryStatus libraryStatus = parseLibraryStatus(status);

        Specification<Subject> specification =
                buildSubjectSpecification(year, quarter, category, keyword, subjectStatus, tagName);

        HomeLibrarySortType effectiveSortType = normalizeLibrarySort(sortType);
        Sort sort = buildHomeSubjectSort(effectiveSortType);

        return subjectRepository.findAll(specification, sort).stream()
                .map(this::toResponse)
                .filter(subject -> libraryStatus == null || libraryStatus.name().equals(subject.getLibraryStatus()))
                .toList();
    }

    public PageResponse<SubjectResponse> getSubjectPage(Integer year, Integer quarter,
                                                        String category, String keyword,
                                                        String status, Integer page,
                                                        Integer size, String sortType) {
        return getSubjectPage(year, quarter, category, keyword, status, page, size, sortType, null);
    }

    public PageResponse<SubjectResponse> getSubjectPage(Integer year, Integer quarter,
                                                        String category, String keyword,
                                                        String status, Integer page,
                                                        Integer size, String sortType,
                                                        String tagName) {
        int effectivePage = normalizePage(page);
        int effectiveSize = normalizeSize(size);
        SubjectPageSortType effectiveSortType = normalizeSubjectPageSort(sortType);
        LibraryStatus libraryStatus = parseLibraryStatus(status);
        String subjectStatus = isLibraryStatus(status) ? "NORMAL" : status;

        Specification<Subject> specification =
                buildSubjectSpecification(year, quarter, category, keyword, subjectStatus, tagName);

        if (libraryStatus != null) {
            List<SubjectResponse> filtered = subjectRepository
                    .findAll(specification, buildSubjectPageSort(effectiveSortType))
                    .stream()
                    .map(this::toResponse)
                    .filter(subject -> libraryStatus.name().equals(subject.getLibraryStatus()))
                    .toList();

            int fromIndex = Math.min(effectivePage * effectiveSize, filtered.size());
            int toIndex = Math.min(fromIndex + effectiveSize, filtered.size());
            List<SubjectResponse> content = filtered.subList(fromIndex, toIndex);
            int totalPages = filtered.isEmpty() ? 0 : (int) Math.ceil((double) filtered.size() / effectiveSize);

            return new PageResponse<>(
                    content,
                    effectivePage,
                    effectiveSize,
                    filtered.size(),
                    totalPages,
                    effectivePage == 0,
                    effectivePage >= totalPages - 1
            );
        }

        Pageable pageable = PageRequest.of(
                effectivePage,
                effectiveSize,
                buildSubjectPageSort(effectiveSortType)
        );

        Page<Subject> subjectPage = subjectRepository.findAll(specification, pageable);

        List<SubjectResponse> content = subjectPage.getContent().stream()
                .map(this::toResponse)
                .toList();

        return new PageResponse<>(
                content,
                subjectPage.getNumber(),
                subjectPage.getSize(),
                subjectPage.getTotalElements(),
                subjectPage.getTotalPages(),
                subjectPage.isFirst(),
                subjectPage.isLast()
        );
    }

    public SubjectDetailResponse getSubjectDetailById(Long id) {
        Subject subject = subjectRepository.findById(id).orElse(null);
        if (subject == null) {
            return null;
        }

        List<UserSubjectRecordResponse> records = userSubjectRecordService.getRecordsBySubjectId(id);
        UserSubjectRecordResponse record = records.isEmpty() ? null : records.get(0);
        UserSubjectCollectionResponse collection = userSubjectCollectionService.getCollectionBySubjectId(id);
        LibraryStatus libraryStatus = resolveLibraryStatus(subject.getId());

        FranchiseSimpleResponse franchise = null;
        if (subject.getFranchiseId() != null) {
            Franchise franchiseEntity = franchiseRepository.findById(subject.getFranchiseId()).orElse(null);
            if (franchiseEntity != null) {
                franchise = new FranchiseSimpleResponse(
                        franchiseEntity.getId(),
                        franchiseEntity.getName(),
                        franchiseEntity.getNameCn()
                );
            }
        }

        return new SubjectDetailResponse(
                subject.getId(),
                subject.getTitleCn(),
                subject.getSubtitle(),
                subject.getDisplayTitle(),
                subject.getSeasonIndex(),
                subject.getPartIndex(),
                subject.getFranchiseId(),
                subject.getCoverUrl(),
                subject.getSummary(),
                subject.getReleaseYear(),
                subject.getReleaseQuarter(),
                subject.getCategory(),
                subject.getStudioName(),
                subject.getPlatformLink(),
                subject.getPlatformScore(),
                subject.getEpisodeCount(),
                tagService.toResponses(subject.getStructuredTags()),
                subject.getSourcePlatform(),
                subject.getSourceId(),
                subject.getStatus(),
                libraryStatus.name(),
                libraryStatus.getLabel(),
                !records.isEmpty(),
                records.size(),
                records,
                record,
                collection,
                franchise,
                subject.getCreatedAt(),
                subject.getUpdatedAt()
        );
    }

    public SubjectResponse getSubjectById(Long id) {
        Subject subject = subjectRepository.findById(id).orElse(null);
        if (subject == null) {
            return null;
        }
        return toResponse(subject);
    }

    public SubjectResponse updateSubject(Long id, SubjectCreateRequest request) {
        Subject existingSubject = subjectRepository.findById(id).orElse(null);
        if (existingSubject == null) {
            return null;
        }

        existingSubject.setTitleCn(request.getTitleCn());
        existingSubject.setSubtitle(request.getSubtitle());
        existingSubject.setDisplayTitle(request.getDisplayTitle());
        existingSubject.setSeasonIndex(request.getSeasonIndex());
        existingSubject.setPartIndex(request.getPartIndex());
        existingSubject.setFranchiseId(resolveFranchiseIdForUpdate(request));
        existingSubject.setCoverUrl(resolveCoverUrl(request));
        existingSubject.setSummary(request.getSummary());
        existingSubject.setReleaseYear(request.getReleaseYear());
        existingSubject.setReleaseQuarter(request.getReleaseQuarter());
        existingSubject.setCategory(request.getCategory());
        existingSubject.setStudioName(request.getStudioName());
        existingSubject.setPlatformLink(request.getPlatformLink());
        existingSubject.setPlatformScore(request.getPlatformScore());
        existingSubject.setEpisodeCount(request.getEpisodeCount());
        tagService.syncSubjectTags(existingSubject, request.getTagNames());
        existingSubject.setSourcePlatform(request.getSourcePlatform());
        existingSubject.setSourceId(request.getSourceId());
        existingSubject.setStatus(request.getStatus());

        Subject updatedSubject = subjectRepository.save(existingSubject);
        return toResponse(updatedSubject);
    }

    public boolean subjectExists(Long id) {
        return subjectRepository.existsById(id);
    }

    public boolean isSubjectReferenced(Long id) {
        return userSubjectRecordRepository.existsBySubjectId(id)
                || userSubjectCollectionRepository.existsBySubjectId(id);
    }

    public void deleteSubject(Long id) {
        subjectRepository.deleteById(id);
    }

    public SubjectImportResponse importSubjects(SubjectBatchImportRequest request) {
        List<SubjectImportItemResponse> items = new ArrayList<>();

        int importedCount = 0;
        int skippedCount = 0;

        int rowNumber = 1;
        for (SubjectCreateRequest item : request.getSubjects()) {
            String validationMessage = validateSubjectImportItem(item);
            if (validationMessage != null) {
                skippedCount++;
                items.add(new SubjectImportItemResponse(
                        null,
                        buildSubjectImportLabel(item, rowNumber),
                        "ERROR",
                        validationMessage
                ));
                rowNumber++;
                continue;
            }

            Long resolvedFranchiseId = resolveFranchiseIdForImport(item);

            if (item.getFranchiseId() != null && !franchiseRepository.existsById(item.getFranchiseId())) {
                skippedCount++;
                items.add(new SubjectImportItemResponse(
                        null,
                        item.getDisplayTitle(),
                        "SKIPPED",
                        "franchiseId 对应的系列不存在，已跳过"
                ));
                rowNumber++;
                continue;
            }

            boolean exists = subjectBusinessUniqueExistsForCreate(item);
            if (exists) {
                skippedCount++;
                items.add(new SubjectImportItemResponse(
                        null,
                        item.getDisplayTitle(),
                        "SKIPPED",
                        "作品已存在，已跳过"
                ));
                rowNumber++;
                continue;
            }

            Subject subject = buildSubjectFromRequest(item, resolvedFranchiseId);
            tagService.syncSubjectTags(subject, item.getTagNames());
            Subject savedSubject = subjectRepository.save(subject);
            importedCount++;

            items.add(new SubjectImportItemResponse(
                    savedSubject.getId(),
                    savedSubject.getDisplayTitle(),
                    "IMPORTED",
                    buildSubjectImportSuccessMessage(item, resolvedFranchiseId)
            ));
            rowNumber++;
        }

        return new SubjectImportResponse(
                request.getSubjects().size(),
                importedCount,
                skippedCount,
                items
        );
    }

    public SubjectImportPreviewResponse previewImportSubjects(SubjectBatchImportRequest request) {
        List<SubjectImportPreviewItemResponse> items = new ArrayList<>();

        int canImportCount = 0;
        int skippedCount = 0;

        int rowNumber = 1;
        for (SubjectCreateRequest item : request.getSubjects()) {
            String validationMessage = validateSubjectImportItem(item);
            if (validationMessage != null) {
                skippedCount++;
                items.add(new SubjectImportPreviewItemResponse(
                        buildSubjectImportLabel(item, rowNumber),
                        "ERROR",
                        validationMessage
                ));
                rowNumber++;
                continue;
            }

            if (item.getFranchiseId() != null && !franchiseRepository.existsById(item.getFranchiseId())) {
                skippedCount++;
                items.add(new SubjectImportPreviewItemResponse(
                        item.getDisplayTitle(),
                        "SKIPPED",
                        "franchiseId 对应的系列不存在，正式导入时会跳过"
                ));
                rowNumber++;
                continue;
            }

            boolean exists = subjectBusinessUniqueExistsForCreate(item);
            if (exists) {
                skippedCount++;
                items.add(new SubjectImportPreviewItemResponse(
                        item.getDisplayTitle(),
                        "SKIPPED",
                        "作品已存在，正式导入时会跳过"
                ));
                rowNumber++;
                continue;
            }

            canImportCount++;
            Long resolvedFranchiseId = resolveFranchiseIdForImport(item);
            items.add(new SubjectImportPreviewItemResponse(
                    item.getDisplayTitle(),
                    "CAN_IMPORT",
                    buildSubjectImportPreviewMessage(item, resolvedFranchiseId)
            ));
            rowNumber++;
        }

        return new SubjectImportPreviewResponse(
                request.getSubjects().size(),
                canImportCount,
                skippedCount,
                items
        );
    }

    public boolean subjectBusinessUniqueExistsForCreate(SubjectCreateRequest request) {
        return subjectRepository.existsByBusinessUnique(
                normalizeTextKey(request.getTitleCn()),
                normalizeTextKey(request.getSubtitle()),
                normalizeNumberKey(request.getSeasonIndex()),
                normalizeNumberKey(request.getPartIndex()),
                normalizeTextKey(request.getCategory())
        );
    }

    public boolean subjectBusinessUniqueExistsForUpdate(Long id, SubjectCreateRequest request) {
        return subjectRepository.existsByBusinessUniqueAndIdNot(
                normalizeTextKey(request.getTitleCn()),
                normalizeTextKey(request.getSubtitle()),
                normalizeNumberKey(request.getSeasonIndex()),
                normalizeNumberKey(request.getPartIndex()),
                normalizeTextKey(request.getCategory()),
                id
        );
    }

    private Subject buildSubjectFromRequest(SubjectCreateRequest request, Long franchiseId) {
        return new Subject(
                request.getTitleCn(),
                request.getSubtitle(),
                request.getDisplayTitle(),
                request.getSeasonIndex(),
                request.getPartIndex(),
                franchiseId,
                resolveCoverUrl(request),
                request.getSummary(),
                request.getReleaseYear(),
                request.getReleaseQuarter(),
                request.getCategory(),
                request.getStudioName(),
                request.getPlatformLink(),
                request.getPlatformScore(),
                request.getEpisodeCount(),
                request.getSourcePlatform(),
                request.getSourceId(),
                request.getStatus()
        );
    }

    private String resolveCoverUrl(SubjectCreateRequest request) {
        return coverStorageService.cacheCover(
                request.getCoverUrl(),
                request.getSourcePlatform(),
                request.getSourceId(),
                request.getDisplayTitle()
        );
    }

    private Long resolveFranchiseIdForCreate(SubjectCreateRequest request) {
        return resolveFranchiseIdForUpdate(request);
    }

    private Long resolveFranchiseIdForUpdate(SubjectCreateRequest request) {
        if (request.getFranchiseId() != null) {
            return request.getFranchiseId();
        }
        if (hasText(request.getFranchiseName())) {
            return resolveFranchiseIdByName(request.getFranchiseName());
        }
        return null;
    }

    private Long resolveFranchiseIdForImport(SubjectCreateRequest request) {
        if (request.getFranchiseId() != null) {
            return request.getFranchiseId();
        }
        if (hasText(request.getFranchiseName())) {
            return resolveFranchiseIdByName(request.getFranchiseName());
        }
        return null;
    }

    private Long resolveFranchiseIdByName(String franchiseName) {
        if (!hasText(franchiseName)) {
            return null;
        }

        String businessName = franchiseName.trim();

        List<Franchise> matchedByCn = franchiseRepository.findAllByNormalizedNameCn(businessName);
        if (!matchedByCn.isEmpty()) {
            return matchedByCn.get(0).getId();
        }

        List<Franchise> matchedByName = franchiseRepository.findAllByNormalizedName(businessName);
        if (!matchedByName.isEmpty()) {
            return matchedByName.get(0).getId();
        }

        return null;
    }

    private String buildSubjectImportPreviewMessage(SubjectCreateRequest request, Long resolvedFranchiseId) {
        if (request.getFranchiseId() == null && hasText(request.getFranchiseName()) && resolvedFranchiseId == null) {
            return "可以导入；franchiseName 未匹配到已有系列，将按无系列作品导入";
        }
        return "可以导入";
    }

    private String buildSubjectImportSuccessMessage(SubjectCreateRequest request, Long resolvedFranchiseId) {
        if (request.getFranchiseId() == null && hasText(request.getFranchiseName()) && resolvedFranchiseId == null) {
            return "导入成功；franchiseName 未匹配到已有系列，已按无系列作品导入";
        }
        return "导入成功";
    }

    private String validateSubjectImportItem(SubjectCreateRequest item) {
        if (item == null) {
            return "字段校验失败：导入行不能为空";
        }

        Set<ConstraintViolation<SubjectCreateRequest>> violations = validator.validate(item);
        if (violations.isEmpty()) {
            return null;
        }

        return "字段校验失败：" + violations.stream()
                .sorted((left, right) -> left.getPropertyPath().toString()
                        .compareTo(right.getPropertyPath().toString()))
                .map(violation -> violation.getPropertyPath() + " " + violation.getMessage())
                .collect(Collectors.joining("；"));
    }

    private String buildSubjectImportLabel(SubjectCreateRequest item, int rowNumber) {
        if (item == null) {
            return "第 " + rowNumber + " 行";
        }
        if (hasText(item.getDisplayTitle())) {
            return item.getDisplayTitle().trim();
        }
        if (hasText(item.getTitleCn())) {
            return item.getTitleCn().trim();
        }
        return "第 " + rowNumber + " 行";
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
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

    private int normalizePage(Integer page) {
        if (page == null || page < 0) {
            return 0;
        }
        return page;
    }

    private int normalizeSize(Integer size) {
        if (size == null) {
            return 12;
        }
        if (size < 1) {
            return 1;
        }
        return Math.min(size, 50);
    }

    private HomeLibrarySortType normalizeLibrarySort(String sortType) {
        if (sortType == null || sortType.isBlank()) {
            return HomeLibrarySortType.TITLE_ASC;
        }
        return HomeLibrarySortType.valueOf(sortType.toUpperCase(Locale.ROOT));
    }

    private SubjectPageSortType normalizeSubjectPageSort(String sortType) {
        if (sortType == null || sortType.isBlank()) {
            return SubjectPageSortType.RELEASE_NEWEST;
        }
        return SubjectPageSortType.valueOf(sortType.toUpperCase(Locale.ROOT));
    }

    private Sort buildHomeSubjectSort(HomeLibrarySortType sortType) {
        return switch (sortType) {
            case TITLE_ASC -> Sort.by("displayTitle").ascending();
            case TITLE_DESC -> Sort.by("displayTitle").descending();
            case RELEASE_NEWEST -> Sort.by("releaseYear").descending()
                    .and(Sort.by("releaseQuarter").descending())
                    .and(Sort.by("displayTitle").ascending());
            case RELEASE_OLDEST -> Sort.by("releaseYear").ascending()
                    .and(Sort.by("releaseQuarter").ascending())
                    .and(Sort.by("displayTitle").ascending());
        };
    }

    private Sort buildSubjectPageSort(SubjectPageSortType sortType) {
        return switch (sortType) {
            case TITLE_ASC -> Sort.by("titleCn").ascending()
                    .and(Sort.by("seasonIndex").ascending())
                    .and(Sort.by("partIndex").ascending())
                    .and(Sort.by("displayTitle").ascending())
                    .and(Sort.by("id").ascending());
            case TITLE_DESC -> Sort.by("titleCn").descending()
                    .and(Sort.by("seasonIndex").descending())
                    .and(Sort.by("partIndex").descending())
                    .and(Sort.by("displayTitle").descending())
                    .and(Sort.by("id").descending());
            case RELEASE_NEWEST -> Sort.by("releaseYear").descending()
                    .and(Sort.by("releaseQuarter").descending())
                    .and(Sort.by("displayTitle").ascending());
            case RELEASE_OLDEST -> Sort.by("releaseYear").ascending()
                    .and(Sort.by("releaseQuarter").ascending())
                    .and(Sort.by("displayTitle").ascending());
        };
    }

    private Specification<Subject> buildSubjectSpecification(Integer year, Integer quarter,
                                                             String category, String keyword,
                                                             String status, String tagName) {
        return Specification.allOf(
                SubjectSpecifications.hasReleaseYear(year),
                SubjectSpecifications.hasReleaseQuarter(quarter),
                SubjectSpecifications.hasCategory(category),
                SubjectSpecifications.titleContainsKeyword(keyword),
                SubjectSpecifications.hasStatus(status),
                SubjectSpecifications.hasTagName(tagName)
        );
    }

    private boolean isLibraryStatus(String value) {
        return parseLibraryStatus(value) != null;
    }

    private LibraryStatus parseLibraryStatus(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LibraryStatus.valueOf(value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private LibraryStatus resolveLibraryStatus(Long subjectId) {
        if (userSubjectRecordRepository.existsBySubjectId(subjectId)) {
            return LibraryStatus.WATCHED;
        }

        return userSubjectCollectionRepository.findBySubjectId(subjectId)
                .map(collection -> collection.getCollectionType() == CollectionType.WANT
                        ? LibraryStatus.WANT
                        : LibraryStatus.PAUSED)
                .orElse(LibraryStatus.NONE);
    }

    private SubjectResponse toResponse(Subject subject) {
        LibraryStatus libraryStatus = resolveLibraryStatus(subject.getId());
        return new SubjectResponse(
                subject.getId(),
                subject.getTitleCn(),
                subject.getSubtitle(),
                subject.getDisplayTitle(),
                subject.getSeasonIndex(),
                subject.getPartIndex(),
                subject.getFranchiseId(),
                subject.getCoverUrl(),
                subject.getSummary(),
                subject.getReleaseYear(),
                subject.getReleaseQuarter(),
                subject.getCategory(),
                subject.getStudioName(),
                subject.getPlatformLink(),
                subject.getPlatformScore(),
                subject.getEpisodeCount(),
                tagService.toResponses(subject.getStructuredTags()),
                subject.getSourcePlatform(),
                subject.getSourceId(),
                subject.getStatus(),
                libraryStatus.name(),
                libraryStatus.getLabel(),
                subject.getCreatedAt(),
                subject.getUpdatedAt()
        );
    }
}
