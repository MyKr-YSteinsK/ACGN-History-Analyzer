package com.mykr.acgnhistoryanalyzer.service;

import com.mykr.acgnhistoryanalyzer.common.enums.CollectionType;
import com.mykr.acgnhistoryanalyzer.entity.Subject;
import com.mykr.acgnhistoryanalyzer.entity.UserSubjectCollection;
import com.mykr.acgnhistoryanalyzer.repository.SubjectRepository;
import com.mykr.acgnhistoryanalyzer.config.CacheInvalidationService;
import com.mykr.acgnhistoryanalyzer.repository.UserSubjectCollectionRepository;
import com.mykr.acgnhistoryanalyzer.repository.UserSubjectRecordRepository;
import com.mykr.acgnhistoryanalyzer.request.UserSubjectCollectionBatchImportRequest;
import com.mykr.acgnhistoryanalyzer.request.UserSubjectCollectionCreateRequest;
import com.mykr.acgnhistoryanalyzer.request.UserSubjectCollectionImportItemRequest;
import com.mykr.acgnhistoryanalyzer.request.UserSubjectCollectionUpdateRequest;
import com.mykr.acgnhistoryanalyzer.response.PageResponse;
import com.mykr.acgnhistoryanalyzer.response.UserSubjectCollectionImportItemResponse;
import com.mykr.acgnhistoryanalyzer.response.UserSubjectCollectionImportPreviewItemResponse;
import com.mykr.acgnhistoryanalyzer.response.UserSubjectCollectionImportPreviewResponse;
import com.mykr.acgnhistoryanalyzer.response.UserSubjectCollectionImportResponse;
import com.mykr.acgnhistoryanalyzer.response.UserSubjectCollectionResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserSubjectCollectionService {

    private final UserSubjectCollectionRepository collectionRepository;
    private final UserSubjectRecordRepository recordRepository;
    private final SubjectRepository subjectRepository;
    private final ImportSubjectMatchService importSubjectMatchService;
    private final Validator validator;
    private final CacheInvalidationService cacheInvalidationService;

    public UserSubjectCollectionService(UserSubjectCollectionRepository collectionRepository,
                                        UserSubjectRecordRepository recordRepository,
                                        SubjectRepository subjectRepository,
                                        ImportSubjectMatchService importSubjectMatchService,
                                        Validator validator,
                                        CacheInvalidationService cacheInvalidationService) {
        this.collectionRepository = collectionRepository;
        this.recordRepository = recordRepository;
        this.subjectRepository = subjectRepository;
        this.importSubjectMatchService = importSubjectMatchService;
        this.validator = validator;
        this.cacheInvalidationService = cacheInvalidationService;
    }

    public boolean subjectExists(Long subjectId) {
        return subjectRepository.existsById(subjectId);
    }

    public boolean recordExistsBySubjectId(Long subjectId) {
        return recordRepository.existsBySubjectId(subjectId);
    }

    @Transactional
    public UserSubjectCollectionResponse createOrMoveCollection(UserSubjectCollectionCreateRequest request) {
        Subject subject = subjectRepository.findById(request.getSubjectId()).orElse(null);
        if (subject == null) {
            return null;
        }

        UserSubjectCollection collection = collectionRepository.findBySubjectId(subject.getId())
                .orElseGet(() -> new UserSubjectCollection(
                        subject.getId(),
                        subject.getDisplayTitle(),
                        request.getCollectionType(),
                        request.getNote()
                ));

        collection.setSubjectId(subject.getId());
        collection.setSubjectTitle(subject.getDisplayTitle());
        collection.setCollectionType(request.getCollectionType());
        collection.setNote(request.getNote());

        UserSubjectCollection saved = collectionRepository.save(collection);
        cacheInvalidationService.invalidateAfterDataMutation();
        return toResponse(saved);
    }

    public PageResponse<UserSubjectCollectionResponse> getCollectionPage(CollectionType type,
                                                                        String category,
                                                                        String keyword,
                                                                        Integer page,
                                                                        Integer size) {
        int effectivePage = normalizePage(page);
        int effectiveSize = normalizeSize(size);
        String normalizedCategory = normalizeBlankToNull(category);
        String normalizedKeyword = normalizeBlankToNull(keyword);

        Pageable pageable = PageRequest.of(
                effectivePage,
                effectiveSize,
                Sort.by("createdAt").descending().and(Sort.by("id").descending())
        );

        Page<UserSubjectCollection> collectionPage = collectionRepository.findCollectionPageWithSubjectFilters(
                type,
                normalizedCategory,
                normalizedKeyword,
                pageable
        );

        return new PageResponse<>(
                collectionPage.getContent().stream().map(this::toResponse).toList(),
                collectionPage.getNumber(),
                collectionPage.getSize(),
                collectionPage.getTotalElements(),
                collectionPage.getTotalPages(),
                collectionPage.isFirst(),
                collectionPage.isLast()
        );
    }

    public UserSubjectCollectionResponse getCollectionById(Long id) {
        return collectionRepository.findById(id).map(this::toResponse).orElse(null);
    }

    public UserSubjectCollectionResponse getCollectionBySubjectId(Long subjectId) {
        return collectionRepository.findBySubjectId(subjectId).map(this::toResponse).orElse(null);
    }

    @Transactional
    public UserSubjectCollectionImportResponse importCollections(
            UserSubjectCollectionBatchImportRequest request
    ) {
        List<UserSubjectCollectionImportItemResponse> items = new java.util.ArrayList<>();

        int importedCount = 0;
        int skippedCount = 0;
        int rowNumber = 1;

        for (UserSubjectCollectionImportItemRequest item : request.getCollections()) {
            String validationMessage = validateCollectionImportItem(item);
            if (validationMessage != null) {
                skippedCount++;
                items.add(new UserSubjectCollectionImportItemResponse(
                        null,
                        buildImportSubjectLabel(item, rowNumber),
                        "ERROR",
                        validationMessage
                ));
                rowNumber++;
                continue;
            }

            ImportSubjectResolveResult resolveResult = resolveSubjectForCollectionImport(item);
            if (resolveResult.hasAmbiguousCandidates()) {
                skippedCount++;
                items.add(new UserSubjectCollectionImportItemResponse(
                        null,
                        buildImportSubjectLabel(item, rowNumber),
                        "SKIPPED",
                        "匹配到多个作品，请先在预览中选择一个候选作品后再导入"
                ));
                rowNumber++;
                continue;
            }
            if (resolveResult.hasError()) {
                skippedCount++;
                items.add(new UserSubjectCollectionImportItemResponse(
                        null,
                        buildImportSubjectLabel(item, rowNumber),
                        "SKIPPED",
                        resolveResult.getErrorMessage()
                ));
                rowNumber++;
                continue;
            }

            Subject subject = resolveResult.getSubject();
            if (recordRepository.existsBySubjectId(subject.getId())) {
                skippedCount++;
                items.add(new UserSubjectCollectionImportItemResponse(
                        null,
                        subject.getDisplayTitle(),
                        "SKIPPED",
                        "该作品已有正式记录，不再加入想看或搁置"
                ));
                rowNumber++;
                continue;
            }

            UserSubjectCollectionCreateRequest createRequest = new UserSubjectCollectionCreateRequest();
            createRequest.setSubjectId(subject.getId());
            createRequest.setCollectionType(CollectionType.valueOf(item.getCollectionType()));
            createRequest.setNote(item.getNote());

            UserSubjectCollectionResponse savedCollection = createOrMoveCollection(createRequest);
            importedCount++;
            items.add(new UserSubjectCollectionImportItemResponse(
                    savedCollection.getId(),
                    savedCollection.getSubjectTitle(),
                    "IMPORTED",
                    "导入成功；如原本已有收藏，则已更新清单类型和备注"
            ));
            rowNumber++;
        }

        UserSubjectCollectionImportResponse response = new UserSubjectCollectionImportResponse(
                request.getCollections().size(),
                importedCount,
                skippedCount,
                items
        );
        cacheInvalidationService.invalidateAfterDataMutation();
        return response;
    }

    public UserSubjectCollectionImportPreviewResponse previewImportCollections(
            UserSubjectCollectionBatchImportRequest request
    ) {
        List<UserSubjectCollectionImportPreviewItemResponse> items = new java.util.ArrayList<>();

        int canImportCount = 0;
        int skippedCount = 0;
        int rowNumber = 1;

        for (UserSubjectCollectionImportItemRequest item : request.getCollections()) {
            String validationMessage = validateCollectionImportItem(item);
            if (validationMessage != null) {
                skippedCount++;
                items.add(new UserSubjectCollectionImportPreviewItemResponse(
                        buildImportSubjectLabel(item, rowNumber),
                        "ERROR",
                        validationMessage
                ));
                rowNumber++;
                continue;
            }

            ImportSubjectResolveResult resolveResult = resolveSubjectForCollectionImport(item);
            if (resolveResult.hasAmbiguousCandidates()) {
                skippedCount++;
                items.add(new UserSubjectCollectionImportPreviewItemResponse(
                        buildImportSubjectLabel(item, rowNumber),
                        "AMBIGUOUS",
                        "匹配到多个作品，请在前端选择一个候选作品后再导入",
                        resolveResult.getCandidates()
                ));
                rowNumber++;
                continue;
            }
            if (resolveResult.hasError()) {
                skippedCount++;
                items.add(new UserSubjectCollectionImportPreviewItemResponse(
                        buildImportSubjectLabel(item, rowNumber),
                        "SKIPPED",
                        resolveResult.getErrorMessage()
                ));
                rowNumber++;
                continue;
            }

            Subject subject = resolveResult.getSubject();
            if (recordRepository.existsBySubjectId(subject.getId())) {
                skippedCount++;
                items.add(new UserSubjectCollectionImportPreviewItemResponse(
                        subject.getDisplayTitle(),
                        "SKIPPED",
                        "该作品已有正式记录，正式导入时会跳过收藏"
                ));
                rowNumber++;
                continue;
            }

            canImportCount++;
            items.add(new UserSubjectCollectionImportPreviewItemResponse(
                    subject.getDisplayTitle(),
                    "CAN_IMPORT",
                    collectionRepository.existsBySubjectId(subject.getId())
                            ? "可以导入；已有收藏将更新为当前清单类型和备注"
                            : "可以导入收藏"
            ));
            rowNumber++;
        }

        return new UserSubjectCollectionImportPreviewResponse(
                request.getCollections().size(),
                canImportCount,
                skippedCount,
                items
        );
    }

    @Transactional
    public UserSubjectCollectionResponse updateCollection(Long id, UserSubjectCollectionUpdateRequest request) {
        UserSubjectCollection collection = collectionRepository.findById(id).orElse(null);
        if (collection == null) {
            return null;
        }

        collection.setCollectionType(request.getCollectionType());
        collection.setNote(request.getNote());

        UserSubjectCollection saved = collectionRepository.save(collection);
        cacheInvalidationService.invalidateAfterDataMutation();
        return toResponse(saved);
    }

    @Transactional
    public boolean deleteCollection(Long id) {
        if (!collectionRepository.existsById(id)) {
            return false;
        }
        collectionRepository.deleteById(id);
        cacheInvalidationService.invalidateAfterDataMutation();
        return true;
    }

    @Transactional
    public void removeCollectionBySubjectId(Long subjectId) {
        if (subjectId != null && collectionRepository.existsBySubjectId(subjectId)) {
            collectionRepository.deleteBySubjectId(subjectId);
            cacheInvalidationService.invalidateAfterDataMutation();
        }
    }

    private String validateCollectionImportItem(UserSubjectCollectionImportItemRequest item) {
        if (item == null) {
            return "字段校验失败：导入行不能为空";
        }

        Set<ConstraintViolation<UserSubjectCollectionImportItemRequest>> violations =
                validator.validate(item);
        if (!violations.isEmpty()) {
            return "字段校验失败：" + violations.stream()
                    .sorted((left, right) -> left.getPropertyPath().toString()
                            .compareTo(right.getPropertyPath().toString()))
                    .map(violation -> violation.getPropertyPath() + " " + violation.getMessage())
                    .collect(Collectors.joining("；"));
        }

        if (!hasText(item.getCollectionType())) {
            return "字段校验失败：collectionType 不能为空";
        }

        if (item.getSubjectId() == null && !hasText(item.getSourceId()) && !hasText(item.getSubjectTitle())) {
            return "字段校验失败：subjectId、sourceId、subjectTitle 至少提供一个";
        }

        return null;
    }

    private ImportSubjectResolveResult resolveSubjectForCollectionImport(
            UserSubjectCollectionImportItemRequest item
    ) {
        return importSubjectMatchService.resolve(
                item.getSubjectId(),
                item.getSourceId(),
                item.getSubjectTitle(),
                item.getSubjectSeasonIndex(),
                item.getSubjectPartIndex()
        );
    }

    private String buildImportSubjectLabel(UserSubjectCollectionImportItemRequest item, int rowNumber) {
        if (item == null) {
            return "第 " + rowNumber + " 行";
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
        return "第 " + rowNumber + " 行";
    }

    private UserSubjectCollectionResponse toResponse(UserSubjectCollection collection) {
        String subjectTitle = collection.getSubjectTitle();
        String subjectCategory = null;
        Integer subjectReleaseYear = null;
        Integer subjectReleaseQuarter = null;
        String subjectCoverUrl = null;

        if (collection.getSubjectId() != null) {
            Subject subject = subjectRepository.findById(collection.getSubjectId()).orElse(null);
            if (subject != null) {
                subjectTitle = subject.getDisplayTitle();
                subjectCategory = subject.getCategory();
                subjectReleaseYear = subject.getReleaseYear();
                subjectReleaseQuarter = subject.getReleaseQuarter();
                subjectCoverUrl = subject.getCoverUrl();
            }
        }

        return new UserSubjectCollectionResponse(
                collection.getId(),
                collection.getSubjectId(),
                subjectTitle,
                subjectCategory,
                subjectReleaseYear,
                subjectReleaseQuarter,
                subjectCoverUrl,
                collection.getCollectionType().name(),
                collection.getCollectionType().getLabel(),
                collection.getNote(),
                collection.getCreatedAt(),
                collection.getUpdatedAt()
        );
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

    private String normalizeBlankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
