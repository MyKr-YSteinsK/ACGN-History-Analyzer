package com.mykr.acgnhistoryanalyzer.controller;

import com.mykr.acgnhistoryanalyzer.common.enums.CollectionType;
import com.mykr.acgnhistoryanalyzer.common.error.BusinessErrors;
import com.mykr.acgnhistoryanalyzer.common.response.ApiResponse;
import com.mykr.acgnhistoryanalyzer.request.UserSubjectCollectionBatchImportRequest;
import com.mykr.acgnhistoryanalyzer.request.UserSubjectCollectionCreateRequest;
import com.mykr.acgnhistoryanalyzer.request.UserSubjectCollectionUpdateRequest;
import com.mykr.acgnhistoryanalyzer.response.PageResponse;
import com.mykr.acgnhistoryanalyzer.response.UserSubjectCollectionImportPreviewResponse;
import com.mykr.acgnhistoryanalyzer.response.UserSubjectCollectionImportResponse;
import com.mykr.acgnhistoryanalyzer.response.UserSubjectCollectionResponse;
import com.mykr.acgnhistoryanalyzer.service.UserSubjectCollectionService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/collections")
public class UserSubjectCollectionController {

    private final UserSubjectCollectionService collectionService;

    public UserSubjectCollectionController(UserSubjectCollectionService collectionService) {
        this.collectionService = collectionService;
    }

    @PostMapping
    public ApiResponse<?> createOrMoveCollection(@Valid @RequestBody UserSubjectCollectionCreateRequest request) {
        if (!collectionService.subjectExists(request.getSubjectId())) {
            return ApiResponse.fail(
                    BusinessErrors.SUBJECT_NOT_FOUND.getCode(),
                    BusinessErrors.SUBJECT_NOT_FOUND.getMessage()
            );
        }

        if (collectionService.recordExistsBySubjectId(request.getSubjectId())) {
            return ApiResponse.fail(
                    BusinessErrors.SUBJECT_ALREADY_HAS_RECORD.getCode(),
                    "该作品已有正式记录，不需要再加入想看或搁置"
            );
        }

        UserSubjectCollectionResponse response = collectionService.createOrMoveCollection(request);
        return ApiResponse.success(response);
    }

    @PostMapping("/want/{subjectId}")
    public ApiResponse<?> addToWant(@PathVariable Long subjectId) {
        UserSubjectCollectionCreateRequest request = new UserSubjectCollectionCreateRequest();
        request.setSubjectId(subjectId);
        request.setCollectionType(CollectionType.WANT);
        return createOrMoveCollection(request);
    }

    @PostMapping("/paused/{subjectId}")
    public ApiResponse<?> addToPaused(@PathVariable Long subjectId) {
        UserSubjectCollectionCreateRequest request = new UserSubjectCollectionCreateRequest();
        request.setSubjectId(subjectId);
        request.setCollectionType(CollectionType.PAUSED);
        return createOrMoveCollection(request);
    }

    @PostMapping("/import")
    public ApiResponse<UserSubjectCollectionImportResponse> importCollections(
            @Valid @RequestBody UserSubjectCollectionBatchImportRequest request
    ) {
        return ApiResponse.success(collectionService.importCollections(request));
    }

    @PostMapping("/import/preview")
    public ApiResponse<UserSubjectCollectionImportPreviewResponse> previewImportCollections(
            @Valid @RequestBody UserSubjectCollectionBatchImportRequest request
    ) {
        return ApiResponse.success(collectionService.previewImportCollections(request));
    }

    @GetMapping
    public ApiResponse<PageResponse<UserSubjectCollectionResponse>> getCollections(
            @RequestParam(required = false) CollectionType type,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        return ApiResponse.success(collectionService.getCollectionPage(type, category, keyword, page, size));
    }

    @GetMapping("/{id}")
    public ApiResponse<?> getCollectionById(@PathVariable Long id) {
        UserSubjectCollectionResponse collection = collectionService.getCollectionById(id);
        if (collection == null) {
            return ApiResponse.fail(
                    BusinessErrors.COLLECTION_NOT_FOUND.getCode(),
                    BusinessErrors.COLLECTION_NOT_FOUND.getMessage()
            );
        }
        return ApiResponse.success(collection);
    }

    @PutMapping("/{id}")
    public ApiResponse<?> updateCollection(
            @PathVariable Long id,
            @Valid @RequestBody UserSubjectCollectionUpdateRequest request
    ) {
        UserSubjectCollectionResponse collection = collectionService.updateCollection(id, request);
        if (collection == null) {
            return ApiResponse.fail(
                    BusinessErrors.COLLECTION_NOT_FOUND.getCode(),
                    BusinessErrors.COLLECTION_NOT_FOUND.getMessage()
            );
        }
        return ApiResponse.success(collection);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<?> deleteCollection(@PathVariable Long id) {
        boolean deleted = collectionService.deleteCollection(id);
        if (!deleted) {
            return ApiResponse.fail(
                    BusinessErrors.COLLECTION_NOT_FOUND.getCode(),
                    BusinessErrors.COLLECTION_NOT_FOUND.getMessage()
            );
        }
        return ApiResponse.success("删除成功");
    }
}
