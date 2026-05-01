package com.mykr.acgnhistoryanalyzer.controller;

import com.mykr.acgnhistoryanalyzer.common.response.ApiResponse;
import com.mykr.acgnhistoryanalyzer.common.error.BusinessErrors;
import com.mykr.acgnhistoryanalyzer.request.SubjectCreateRequest;
import com.mykr.acgnhistoryanalyzer.request.SubjectBatchImportRequest;
import com.mykr.acgnhistoryanalyzer.response.*;
import com.mykr.acgnhistoryanalyzer.service.FranchiseService;
import com.mykr.acgnhistoryanalyzer.service.SubjectService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subjects")
public class SubjectController {

    private final SubjectService subjectService;
    private final FranchiseService franchiseService;

    public SubjectController(SubjectService subjectService, FranchiseService franchiseService) {
        this.subjectService = subjectService;
        this.franchiseService = franchiseService;
    }
    @PostMapping
    public ApiResponse<SubjectResponse> createSubject(@Valid @RequestBody SubjectCreateRequest request) {
        if (request.getSourceId() == null || request.getSourceId().isBlank()) {
            return ApiResponse.fail(
                    BusinessErrors.SUBJECT_UNIQUE_DUPLICATED.getCode(),
                    "sourceId 不能为空（作品库仅支持 Bangumi 唯一标识）"
            );
        }
        if (request.getFranchiseId() != null
                && !franchiseService.franchiseExists(request.getFranchiseId())) {
            return ApiResponse.fail(
                    BusinessErrors.FRANCHISE_NOT_FOUND.getCode(),
                    BusinessErrors.FRANCHISE_NOT_FOUND.getMessage()
            );
        }
        if (subjectService.subjectSourceUniqueExistsForCreate(request)) {
            return ApiResponse.fail(
                    BusinessErrors.SUBJECT_UNIQUE_DUPLICATED.getCode(),
                    "来源作品已存在，请勿重复添加"
            );
        }
        SubjectResponse savedSubject = subjectService.createSubject(request);
        return ApiResponse.success(savedSubject);
    }

    @PostMapping("/import")
    public ApiResponse<SubjectImportResponse> importSubjects(
            @Valid @RequestBody SubjectBatchImportRequest request
    ) {
        SubjectImportResponse response = subjectService.importSubjects(request);
        return ApiResponse.success(response);
    }

    @PostMapping("/import/preview")
    public ApiResponse<SubjectImportPreviewResponse> previewImportSubjects(
            @Valid @RequestBody SubjectBatchImportRequest request
    ) {
        SubjectImportPreviewResponse response = subjectService.previewImportSubjects(request);
        return ApiResponse.success(response);
    }

    @GetMapping
    public ApiResponse<List<SubjectResponse>> getSubjects(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer quarter,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String tagName
    ) {
        List<SubjectResponse> subjects =
                subjectService.getSubjects(year, quarter, category, keyword, status, null, tagName);
        return ApiResponse.success(subjects);
    }

    @GetMapping("/page")
    public ApiResponse<PageResponse<SubjectResponse>> getSubjectPage(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer quarter,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String tagName,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sort
    ) {
        PageResponse<SubjectResponse> response =
                subjectService.getSubjectPage(
                        year, quarter, category, keyword, status, page, size, sort, tagName
                );
        return ApiResponse.success(response);
    }

    @GetMapping("/{id}")
    public ApiResponse<?> getSubjectById(@PathVariable Long id) {
        SubjectDetailResponse subject = subjectService.getSubjectDetailById(id);

        if (subject == null) {
            return ApiResponse.fail(
        BusinessErrors.SUBJECT_NOT_FOUND.getCode(),
        BusinessErrors.SUBJECT_NOT_FOUND.getMessage()
);
        }

        return ApiResponse.success(subject);
    }

    @PutMapping("/{id}")
    public ApiResponse<?> updateSubject(
            @PathVariable Long id,
            @Valid @RequestBody SubjectCreateRequest request
    ) {
        if (request.getSourceId() == null || request.getSourceId().isBlank()) {
            return ApiResponse.fail(
                    BusinessErrors.SUBJECT_UNIQUE_DUPLICATED.getCode(),
                    "sourceId 不能为空（作品库仅支持 Bangumi 唯一标识）"
            );
        }
        if (!subjectService.subjectExists(id)) {
            return ApiResponse.fail(
                    BusinessErrors.SUBJECT_NOT_FOUND.getCode(),
                    BusinessErrors.SUBJECT_NOT_FOUND.getMessage()
            );
        }

        if (request.getFranchiseId() != null && !franchiseService.franchiseExists(request.getFranchiseId())) {
            return ApiResponse.fail(
                    BusinessErrors.FRANCHISE_NOT_FOUND.getCode(),
                    BusinessErrors.FRANCHISE_NOT_FOUND.getMessage()
            );
        }

        if (subjectService.subjectSourceUniqueExistsForUpdate(id, request)) {
            return ApiResponse.fail(
                    BusinessErrors.SUBJECT_UNIQUE_DUPLICATED.getCode(),
                    "来源作品已存在，请勿重复添加"
            );
        }

        SubjectResponse updatedSubject = subjectService.updateSubject(id, request);
        return ApiResponse.success(updatedSubject);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<?> deleteSubject(@PathVariable Long id) {
        if (!subjectService.subjectExists(id)) {
            return ApiResponse.fail(
        BusinessErrors.SUBJECT_NOT_FOUND.getCode(),
        BusinessErrors.SUBJECT_NOT_FOUND.getMessage()
);
        }

        if (subjectService.isSubjectReferenced(id)) {
            return ApiResponse.fail(
                    BusinessErrors.SUBJECT_DELETE_FORBIDDEN.getCode(),
                    BusinessErrors.SUBJECT_DELETE_FORBIDDEN.getMessage()
            );
        }

        subjectService.deleteSubject(id);
        return ApiResponse.success("删除成功");
    }
}
