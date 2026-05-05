package com.mykr.acgnhistoryanalyzer.service;

import com.mykr.acgnhistoryanalyzer.common.enums.CollectionType;
import com.mykr.acgnhistoryanalyzer.common.enums.HomeLibrarySortType;
import com.mykr.acgnhistoryanalyzer.common.enums.LibraryStatus;
import com.mykr.acgnhistoryanalyzer.common.enums.SubjectPageSortType;
import com.mykr.acgnhistoryanalyzer.entity.Franchise;
import com.mykr.acgnhistoryanalyzer.entity.Subject;
import com.mykr.acgnhistoryanalyzer.entity.Tag;
import com.mykr.acgnhistoryanalyzer.entity.UserSubjectRecord;
import com.mykr.acgnhistoryanalyzer.repository.FranchiseRepository;
import com.mykr.acgnhistoryanalyzer.repository.SubjectRepository;
import com.mykr.acgnhistoryanalyzer.repository.UserSubjectCollectionRepository;
import com.mykr.acgnhistoryanalyzer.config.CacheInvalidationService;
import com.mykr.acgnhistoryanalyzer.repository.UserSubjectRecordRepository;
import com.mykr.acgnhistoryanalyzer.request.SubjectBatchImportRequest;
import com.mykr.acgnhistoryanalyzer.request.SubjectCreateRequest;
import com.mykr.acgnhistoryanalyzer.response.*;
import com.mykr.acgnhistoryanalyzer.specification.SubjectSpecifications;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.persistence.criteria.Expression;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SubjectService {

    private static final Logger log = LoggerFactory.getLogger(SubjectService.class);

    private final SubjectRepository subjectRepository;
    private final UserSubjectRecordRepository userSubjectRecordRepository;
    private final UserSubjectCollectionRepository userSubjectCollectionRepository;
    private final UserSubjectRecordService userSubjectRecordService;
    private final UserSubjectCollectionService userSubjectCollectionService;
    private final FranchiseRepository franchiseRepository;
    private final TagService tagService;
    private final CoverStorageService coverStorageService;
    private final Validator validator;
    private final CacheInvalidationService cacheInvalidationService;

    public SubjectService(SubjectRepository subjectRepository,
                          UserSubjectRecordRepository userSubjectRecordRepository,
                          UserSubjectCollectionRepository userSubjectCollectionRepository,
                          UserSubjectRecordService userSubjectRecordService,
                          UserSubjectCollectionService userSubjectCollectionService,
                          FranchiseRepository franchiseRepository,
                          TagService tagService,
                          CoverStorageService coverStorageService,
                          Validator validator,
                          CacheInvalidationService cacheInvalidationService) {
        this.subjectRepository = subjectRepository;
        this.userSubjectRecordRepository = userSubjectRecordRepository;
        this.userSubjectCollectionRepository = userSubjectCollectionRepository;
        this.userSubjectRecordService = userSubjectRecordService;
        this.userSubjectCollectionService = userSubjectCollectionService;
        this.franchiseRepository = franchiseRepository;
        this.tagService = tagService;
        this.coverStorageService = coverStorageService;
        this.validator = validator;
        this.cacheInvalidationService = cacheInvalidationService;
    }

    public SubjectResponse createSubject(SubjectCreateRequest request) {
        Subject subject = buildSubjectFromRequest(request, resolveFranchiseIdForUpdate(request));
        tagService.syncSubjectTags(subject, tagService.resolveAllowedSubjectTagNames(request.getTagNames()));
        Subject savedSubject = subjectRepository.save(subject);
        cacheInvalidationService.invalidateAfterDataMutation();
        return toResponse(savedSubject);
    }

    public List<SubjectResponse> getSubjects(Integer year, Integer quarter,
                                             String category, String keyword, String status) {
        return getSubjects(year, null, quarter, category, keyword, status, null, null);
    }

    public List<SubjectResponse> getSubjects(Integer year, Integer quarter,
                                             String category, String keyword,
                                             String status, String sortType) {
        return getSubjects(year, null, quarter, category, keyword, status, sortType, null);
    }

    public List<SubjectResponse> getSubjects(Integer year, Integer quarter,
                                             String category, String keyword,
                                             String status, String sortType, String tagName) {
        return getSubjects(year, null, quarter, category, keyword, status, sortType, tagName);
    }

    @Cacheable(
            cacheNames = "subjectList",
            key = "#year + '_' + #releaseYearBefore + '_' + #quarter + '_' + #category + '_' + #keyword + '_' + #status + '_' + #sortType + '_' + #tagName"
    )
    public List<SubjectResponse> getSubjects(Integer year, Integer releaseYearBefore, Integer quarter,
                                             String category, String keyword,
                                             String status, String sortType,
                                             String tagName) {
        String subjectStatus = isLibraryStatus(status) ? "NORMAL" : status;
        LibraryStatus libraryStatus = parseLibraryStatus(status);

        Specification<Subject> specification =
                buildSubjectSpecification(year, releaseYearBefore, quarter, category, keyword, subjectStatus, tagName);

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
        return getSubjectPage(year, null, quarter, category, keyword, status, page, size, sortType, null);
    }

    public PageResponse<SubjectResponse> getSubjectPage(Integer year, Integer quarter,
                                                        String category, String keyword,
                                                        String status, Integer page,
                                                        Integer size, String sortType,
                                                        String tagName) {
        return getSubjectPage(year, null, quarter, category, keyword, status, page, size, sortType, tagName);
    }

    @Cacheable(
            cacheNames = "subjectPage",
            key = "#year + '_' + #releaseYearBefore + '_' + #quarter + '_' + #category + '_' + #keyword + '_' + #status + '_' + #page + '_' + #size + '_' + #sortType + '_' + #tagName"
    )
    public PageResponse<SubjectResponse> getSubjectPage(Integer year, Integer releaseYearBefore, Integer quarter,
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
                buildSubjectSpecification(year, releaseYearBefore, quarter, category, keyword, subjectStatus, tagName);
        Specification<Subject> querySpecification = usesPlatformScoreSort(effectiveSortType)
                ? specification.and(buildPlatformScoreOrderSpecification(effectiveSortType))
                : specification;

        if (libraryStatus != null) {
            List<SubjectResponse> filtered = subjectRepository
                    .findAll(
                            querySpecification,
                            usesPlatformScoreSort(effectiveSortType)
                                    ? Sort.unsorted()
                                    : buildSubjectPageSort(effectiveSortType)
                    )
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
                usesPlatformScoreSort(effectiveSortType)
                        ? Sort.unsorted()
                        : buildSubjectPageSort(effectiveSortType)
        );

        Page<Subject> subjectPage = subjectRepository.findAll(querySpecification, pageable);

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
        tagService.syncSubjectTags(existingSubject, tagService.resolveAllowedSubjectTagNames(request.getTagNames()));
        existingSubject.setSourcePlatform(request.getSourcePlatform());
        existingSubject.setSourceId(request.getSourceId());
        existingSubject.setStatus(request.getStatus());

        Subject updatedSubject = subjectRepository.save(existingSubject);
        cacheInvalidationService.invalidateAfterDataMutation();
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
        cacheInvalidationService.invalidateAfterDataMutation();
    }

    public SubjectImportResponse importSubjects(SubjectBatchImportRequest request) {
        return importSubjects(request, null);
    }

    public SubjectImportResponse importSubjects(SubjectBatchImportRequest request,
                                                SubjectImportProgressListener progressListener) {
        List<SubjectImportItemResponse> items = new ArrayList<>();
        Set<String> batchSourceKeys = new HashSet<>();

        int importedCount = 0;
        int skippedCount = 0;

        int rowNumber = 1;
        int totalCount = request.getSubjects().size();
        for (SubjectCreateRequest item : request.getSubjects()) {
            if (progressListener != null) {
                progressListener.onItemProcessing(
                        rowNumber,
                        totalCount,
                        buildSubjectImportLabel(item, rowNumber)
                );
            }

            try {
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

                String sourceUniqueKey = buildSourceUniqueKey(item);
                if (sourceUniqueKey != null && !batchSourceKeys.add(sourceUniqueKey)) {
                    skippedCount++;
                    items.add(new SubjectImportItemResponse(
                            null,
                            item.getDisplayTitle(),
                            "SKIPPED",
                            "同一批次内来源作品重复，已跳过"
                    ));
                    rowNumber++;
                    continue;
                }

                Long resolvedFranchiseId = resolveFranchiseIdForUpdate(item);

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

                List<Subject> matchedSubjects = findImportMatches(item);
                Subject existingSubject = chooseImportTarget(matchedSubjects);
                if (existingSubject != null) {
                    if (hasSubjectChanged(existingSubject, item, resolvedFranchiseId)) {
                        Subject updatedSubject = applyImportUpdate(existingSubject, item, resolvedFranchiseId);
                        tagService.syncSubjectTags(updatedSubject, tagService.resolveAllowedSubjectTagNames(item.getTagNames()));
                        Subject savedSubject = subjectRepository.save(updatedSubject);
                        int mergedDuplicateCount = removeUnreferencedDuplicateMatches(matchedSubjects, savedSubject.getId());
                        importedCount++;

                        items.add(new SubjectImportItemResponse(
                                savedSubject.getId(),
                                savedSubject.getDisplayTitle(),
                                "UPDATED",
                                appendDuplicateMergeMessage(
                                        buildSubjectImportUpdateMessage(
                                                item,
                                                resolvedFranchiseId,
                                                buildSubjectPreviewChangeSummary(existingSubject, item, resolvedFranchiseId)
                                        ),
                                        mergedDuplicateCount
                                )
                        ));
                    } else {
                        int mergedDuplicateCount = removeUnreferencedDuplicateMatches(matchedSubjects, existingSubject.getId());
                        if (mergedDuplicateCount > 0) {
                            importedCount++;
                            items.add(new SubjectImportItemResponse(
                                    existingSubject.getId(),
                                    existingSubject.getDisplayTitle(),
                                    "UPDATED",
                                    appendDuplicateMergeMessage("作品已存在且字段一致", mergedDuplicateCount)
                            ));
                            rowNumber++;
                            continue;
                        }
                        skippedCount++;
                        items.add(new SubjectImportItemResponse(
                                existingSubject.getId(),
                                existingSubject.getDisplayTitle(),
                                "SKIPPED",
                                "作品已存在且标签、评分、集数一致，已跳过"
                        ));
                    }
                    rowNumber++;
                    continue;
                }

                Subject subject = buildSubjectFromRequest(item, resolvedFranchiseId);
                tagService.syncSubjectTags(subject, tagService.resolveAllowedSubjectTagNames(item.getTagNames()));
                Subject savedSubject = subjectRepository.save(subject);
                importedCount++;

                items.add(new SubjectImportItemResponse(
                        savedSubject.getId(),
                        savedSubject.getDisplayTitle(),
                        "IMPORTED",
                        buildSubjectImportSuccessMessage(item, resolvedFranchiseId)
                ));
                rowNumber++;
            } catch (Exception ex) {
                skippedCount++;
                items.add(new SubjectImportItemResponse(
                        null,
                        buildSubjectImportLabel(item, rowNumber),
                        "ERROR",
                        buildUnexpectedImportErrorMessage(ex)
                ));
                rowNumber++;
            }
        }

        SubjectImportResponse response = new SubjectImportResponse(
                request.getSubjects().size(),
                importedCount,
                skippedCount,
                items
        );
        cacheInvalidationService.invalidateAfterDataMutation();
        return response;
    }

    public SubjectImportPreviewResponse previewImportSubjects(SubjectBatchImportRequest request) {
        List<SubjectImportPreviewItemResponse> items = new ArrayList<>();
        Set<String> batchSourceKeys = new HashSet<>();

        int canImportCount = 0;
        int skippedCount = 0;
        int updatedCount = 0;

        int rowNumber = 1;
        for (SubjectCreateRequest item : request.getSubjects()) {
            try {
                String validationMessage = validateSubjectImportItem(item);
                if (validationMessage != null) {
                    skippedCount++;
                    items.add(new SubjectImportPreviewItemResponse(
                            buildSubjectImportLabel(item, rowNumber),
                            "ERROR",
                            validationMessage,
                            item == null ? null : item.getSourcePlatform(),
                            item == null ? null : item.getSourceId()
                    ));
                    rowNumber++;
                    continue;
                }

                String sourceUniqueKey = buildSourceUniqueKey(item);
                if (sourceUniqueKey != null && !batchSourceKeys.add(sourceUniqueKey)) {
                    skippedCount++;
                    items.add(new SubjectImportPreviewItemResponse(
                            item.getDisplayTitle(),
                            "SKIPPED",
                            "同一批次内来源作品重复，已跳过",
                            item.getSourcePlatform(),
                            item.getSourceId()
                    ));
                    rowNumber++;
                    continue;
                }

                if (item.getFranchiseId() != null && !franchiseRepository.existsById(item.getFranchiseId())) {
                    skippedCount++;
                    items.add(new SubjectImportPreviewItemResponse(
                            item.getDisplayTitle(),
                            "SKIPPED",
                            "franchiseId 对应的系列不存在，正式导入时会跳过",
                            item.getSourcePlatform(),
                            item.getSourceId()
                    ));
                    rowNumber++;
                    continue;
                }

                List<Subject> matchedSubjects = findImportMatches(item);
                Subject existingSubject = chooseImportTarget(matchedSubjects);
                Long resolvedFranchiseId = resolveFranchiseIdForUpdate(item);
                if (existingSubject != null) {
                    if (hasSubjectChanged(existingSubject, item, resolvedFranchiseId)) {
                        updatedCount++;
                        items.add(new SubjectImportPreviewItemResponse(
                                item.getDisplayTitle(),
                                "UPDATED",
                                buildSubjectImportUpdatePreviewMessage(item, resolvedFranchiseId),
                                item.getSourcePlatform(),
                                item.getSourceId(),
                                appendDuplicatePreviewSummary(
                                        buildSubjectPreviewChangeSummary(existingSubject, item, resolvedFranchiseId),
                                        matchedSubjects,
                                        existingSubject
                                )
                        ));
                    } else {
                        skippedCount++;
                        items.add(new SubjectImportPreviewItemResponse(
                                item.getDisplayTitle(),
                                "SKIPPED",
                                "作品已存在且标签、评分、集数一致，正式导入时会跳过",
                                item.getSourcePlatform(),
                                item.getSourceId(),
                                appendDuplicatePreviewSummary(
                                        "标签、评分、集数均无变化",
                                        matchedSubjects,
                                        existingSubject
                                )
                        ));
                    }
                    rowNumber++;
                    continue;
                }

                canImportCount++;
                items.add(new SubjectImportPreviewItemResponse(
                        item.getDisplayTitle(),
                        "CAN_IMPORT",
                        buildSubjectImportPreviewMessage(item, resolvedFranchiseId),
                        item.getSourcePlatform(),
                        item.getSourceId()
                ));
                rowNumber++;
            } catch (Exception ex) {
                skippedCount++;
                items.add(new SubjectImportPreviewItemResponse(
                        buildSubjectImportLabel(item, rowNumber),
                        "ERROR",
                        buildUnexpectedImportErrorMessage(ex),
                        item == null ? null : item.getSourcePlatform(),
                        item == null ? null : item.getSourceId()
                ));
                rowNumber++;
            }
        }

        return new SubjectImportPreviewResponse(
                request.getSubjects().size(),
                canImportCount,
                skippedCount,
                updatedCount,
                items
        );
    }

    public boolean subjectSourceUniqueExistsForCreate(SubjectCreateRequest request) {
        if (!hasText(request.getSourceId())) {
            return false;
        }

        return subjectRepository.existsBySourceIdentity(normalizeTextKey(request.getSourceId()));
    }

    public boolean subjectSourceUniqueExistsForUpdate(Long id, SubjectCreateRequest request) {
        if (!hasText(request.getSourceId())) {
            return false;
        }

        return subjectRepository.existsBySourceIdentityAndIdNot(
                normalizeTextKey(request.getSourceId()),
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

    private Long resolveFranchiseIdForUpdate(SubjectCreateRequest request) {
        if (request.getFranchiseId() != null) {
            return request.getFranchiseId();
        }
        if (hasText(request.getFranchiseName())) {
            return resolveFranchiseIdByName(request.getFranchiseName());
        }
        return null;
    }

    private List<Subject> findExistingBySourceIdentity(SubjectCreateRequest request) {
        if (!hasText(request.getSourceId())) {
            return List.of();
        }

        return subjectRepository.findAllBySourceIdentity(normalizeTextKey(request.getSourceId()));
    }

    private List<Subject> findImportMatches(SubjectCreateRequest request) {
        Map<Long, Subject> matches = new LinkedHashMap<>();

        for (Subject subject : findExistingBySourceIdentity(request)) {
            matches.put(subject.getId(), subject);
        }

        return new ArrayList<>(matches.values());
    }

    private Subject chooseImportTarget(List<Subject> matchedSubjects) {
        return matchedSubjects.stream()
                .min(Comparator
                        .comparing((Subject subject) -> !isSubjectReferenced(subject.getId()))
                        .thenComparing(Subject::getId))
                .orElse(null);
    }

    private int removeUnreferencedDuplicateMatches(List<Subject> matchedSubjects, Long targetSubjectId) {
        int removedCount = 0;
        for (Subject subject : matchedSubjects) {
            if (subject.getId().equals(targetSubjectId) || isSubjectReferenced(subject.getId())) {
                continue;
            }
            try {
                subjectRepository.delete(subject);
                removedCount++;
            } catch (DataIntegrityViolationException ex) {
                log.warn("Skipped deleting unreferenced duplicate subject id={} (FK constraint): {}",
                        subject.getId(), ex.getMostSpecificCause().getMessage());
            }
        }
        return removedCount;
    }

    private String appendDuplicateMergeMessage(String message, int mergedDuplicateCount) {
        if (mergedDuplicateCount <= 0) {
            return message;
        }
        return message + "；已合并清理 " + mergedDuplicateCount + " 条未被记录/收藏引用的重复作品";
    }

    private String appendDuplicatePreviewSummary(
            String summary,
            List<Subject> matchedSubjects,
            Subject selectedSubject
    ) {
        long removableDuplicateCount = matchedSubjects.stream()
                .filter(subject -> !subject.getId().equals(selectedSubject.getId()))
                .filter(subject -> !isSubjectReferenced(subject.getId()))
                .count();
        long protectedDuplicateCount = matchedSubjects.stream()
                .filter(subject -> !subject.getId().equals(selectedSubject.getId()))
                .filter(subject -> isSubjectReferenced(subject.getId()))
                .count();

        List<String> parts = new ArrayList<>();
        parts.add(summary);
        if (removableDuplicateCount > 0) {
            parts.add("可自动合并未引用重复项 " + removableDuplicateCount + " 条");
        }
        if (protectedDuplicateCount > 0) {
            parts.add("另有已被记录/收藏引用的重复项 " + protectedDuplicateCount + " 条，需人工确认");
        }
        return String.join("；", parts);
    }

    private boolean hasSubjectChanged(Subject subject, SubjectCreateRequest request, Long franchiseId) {
        if (subject == null || request == null) {
            return false;
        }

        // 作品库导入“重复更新”仅允许三项变更：标签、平台评分、集数。
        return !hasSameTags(subject.getStructuredTags(), request.getTagNames())
                || !equalsNumber(subject.getPlatformScore(), request.getPlatformScore())
                || !equalsNumber(subject.getEpisodeCount(), request.getEpisodeCount());
    }

    private Subject applyImportUpdate(Subject target, SubjectCreateRequest request, Long franchiseId) {
        // 与 hasSubjectChanged 保持一致：仅同步标签、平台评分、集数。
        target.setPlatformScore(request.getPlatformScore());
        target.setEpisodeCount(request.getEpisodeCount());
        return target;
    }

    private boolean hasSameTags(Set<Tag> structuredTags, List<String> tagNames) {
        List<String> existing = structuredTags == null ? List.of() : structuredTags.stream()
                .map(Tag::getName)
                .map(this::normalizeTextKey)
                .sorted()
                .toList();
        List<String> incoming = tagService.resolveAllowedSubjectTagNames(tagNames).stream()
                .map(this::normalizeTextKey)
                .sorted()
                .toList();
        return existing.equals(incoming);
    }

    private String buildSubjectPreviewChangeSummary(Subject subject,
                                                    SubjectCreateRequest request,
                                                    Long franchiseId) {
        List<String> changes = new ArrayList<>();

        if (!hasSameTags(subject.getStructuredTags(), request.getTagNames())) {
            changes.add("标签: " + formatTagValues(subject.getStructuredTags())
                    + " -> " + formatTagNames(request.getTagNames()));
        }
        if (!equalsNumber(subject.getPlatformScore(), request.getPlatformScore())) {
            changes.add("平台评分: " + formatNumber(subject.getPlatformScore()) + " -> " + formatNumber(request.getPlatformScore()));
        }
        if (!equalsNumber(subject.getEpisodeCount(), request.getEpisodeCount())) {
            changes.add("集数: " + formatNumber(subject.getEpisodeCount()) + " -> " + formatNumber(request.getEpisodeCount()));
        }

        if (changes.isEmpty()) {
            return "无字段变化";
        }

        return String.join("；", changes);
    }

    private boolean equalsNumber(Number left, Number right) {
        if (left == null && right == null) {
            return true;
        }
        if (left == null || right == null) {
            return false;
        }
        return left.doubleValue() == right.doubleValue();
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

    private String buildSubjectImportUpdatePreviewMessage(SubjectCreateRequest request, Long resolvedFranchiseId) {
        return "作品已存在，将同步标签、平台评分、集数";
    }

    private String buildSubjectImportSuccessMessage(SubjectCreateRequest request, Long resolvedFranchiseId) {
        if (request.getFranchiseId() == null && hasText(request.getFranchiseName()) && resolvedFranchiseId == null) {
            return "导入成功；franchiseName 未匹配到已有系列，已按无系列作品导入";
        }
        return "导入成功";
    }

    private String buildSubjectImportUpdateMessage(SubjectCreateRequest request,
                                                   Long resolvedFranchiseId,
                                                   String changeSummary) {
        String message = "更新成功（仅同步标签、平台评分、集数）";
        if (changeSummary == null || changeSummary.isBlank()) {
            return message;
        }
        return message + "；变更：" + changeSummary;
    }

    private String validateSubjectImportItem(SubjectCreateRequest item) {
        if (item == null) {
            return "字段校验失败：导入行不能为空";
        }

        if (!hasText(item.getSourceId())) {
            return "字段校验失败：sourceId 不能为空（作品库仅支持 Bangumi 来源唯一标识）";
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

    private String buildUnexpectedImportErrorMessage(Exception ex) {
        String message = ex.getMessage();
        if (message == null || message.isBlank()) {
            return "导入该行时发生未知异常，已自动跳过";
        }
        return "导入该行时发生异常，已自动跳过：" + message;
    }

    private String buildSourceUniqueKey(SubjectCreateRequest request) {
        if (!hasText(request.getSourceId())) {
            return null;
        }

        String normalizedId = normalizeTextKey(request.getSourceId());
        if (normalizedId.isBlank()) {
            return null;
        }

        return normalizedId;
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
            case PLATFORM_SCORE_DESC -> Sort.by("platformScore").descending()
                    .and(Sort.by("titleCn").ascending())
                    .and(Sort.by("seasonIndex").ascending())
                    .and(Sort.by("partIndex").ascending())
                    .and(Sort.by("displayTitle").ascending())
                    .and(Sort.by("id").ascending());
            case PLATFORM_SCORE_ASC -> Sort.by("platformScore").ascending()
                    .and(Sort.by("titleCn").ascending())
                    .and(Sort.by("seasonIndex").ascending())
                    .and(Sort.by("partIndex").ascending())
                    .and(Sort.by("displayTitle").ascending())
                    .and(Sort.by("id").ascending());
            case RELEASE_NEWEST -> Sort.by("releaseYear").descending()
                    .and(Sort.by("releaseQuarter").descending())
                    .and(Sort.by("displayTitle").ascending());
            case RELEASE_OLDEST -> Sort.by("releaseYear").ascending()
                    .and(Sort.by("releaseQuarter").ascending())
                    .and(Sort.by("displayTitle").ascending());
        };
    }

    private boolean usesPlatformScoreSort(SubjectPageSortType sortType) {
        return sortType == SubjectPageSortType.PLATFORM_SCORE_DESC
                || sortType == SubjectPageSortType.PLATFORM_SCORE_ASC;
    }

    private Specification<Subject> buildPlatformScoreOrderSpecification(SubjectPageSortType sortType) {
        return (root, query, criteriaBuilder) -> {
            if (query.getResultType() == Long.class || query.getResultType() == long.class) {
                return criteriaBuilder.conjunction();
            }

            Expression<Integer> nullRank = criteriaBuilder.<Integer>selectCase()
                    .when(criteriaBuilder.isNull(root.get("platformScore")), 1)
                    .otherwise(0);

            query.orderBy(
                    criteriaBuilder.asc(nullRank),
                    sortType == SubjectPageSortType.PLATFORM_SCORE_DESC
                            ? criteriaBuilder.desc(root.get("platformScore"))
                            : criteriaBuilder.asc(root.get("platformScore")),
                    criteriaBuilder.asc(root.get("titleCn")),
                    criteriaBuilder.asc(root.get("seasonIndex")),
                    criteriaBuilder.asc(root.get("partIndex")),
                    criteriaBuilder.asc(root.get("displayTitle")),
                    criteriaBuilder.asc(root.get("id"))
            );

            return criteriaBuilder.conjunction();
        };
    }

    private Specification<Subject> buildSubjectSpecification(Integer year, Integer releaseYearBefore,
                                                             Integer quarter,
                                                             String category, String keyword,
                                                             String status, String tagName) {
        return Specification.allOf(
                releaseYearBefore != null
                        ? SubjectSpecifications.releaseYearStrictlyBefore(releaseYearBefore)
                        : SubjectSpecifications.hasReleaseYear(year),
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
        Integer personalScore = userSubjectRecordRepository.findBySubjectId(subject.getId()).stream()
                .findFirst()
                .map(UserSubjectRecord::getScoreValue)
                .orElse(null);
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
                personalScore,
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

    private String formatNumber(Number value) {
        return value == null ? "-" : String.valueOf(value);
    }

    private String formatTagValues(Set<Tag> tags) {
        if (tags == null || tags.isEmpty()) {
            return "-";
        }
        return tags.stream()
                .map(Tag::getName)
                .sorted()
                .collect(Collectors.joining("|"));
    }

    private String formatTagNames(List<String> tagNames) {
        List<String> resolved = tagService.resolveAllowedSubjectTagNames(tagNames);
        if (resolved.isEmpty()) {
            return "-";
        }
        return resolved.stream().sorted().collect(Collectors.joining("|"));
    }
}
