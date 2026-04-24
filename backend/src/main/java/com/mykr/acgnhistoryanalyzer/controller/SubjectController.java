package com.mykr.acgnhistoryanalyzer.controller;

import com.mykr.acgnhistoryanalyzer.common.response.ApiResponse;
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
        if (request.getFranchiseId() != null
                && !franchiseService.franchiseExists(request.getFranchiseId())) {
            return ApiResponse.fail(4044, "系列不存在");
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
            @RequestParam(required = false) String status
    ) {
        List<SubjectResponse> subjects =
                subjectService.getSubjects(year, quarter, category, keyword, status);
        return ApiResponse.success(subjects);
    }

    @GetMapping("/page")
    public ApiResponse<PageResponse<SubjectResponse>> getSubjectPage(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer quarter,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sort
    ) {
        PageResponse<SubjectResponse> response =
                subjectService.getSubjectPage(
                        year, quarter, category, keyword, status, page, size, sort
                );
        return ApiResponse.success(response);
    }

    @GetMapping("/{id}")
    public ApiResponse<?> getSubjectById(@PathVariable Long id) {
        SubjectDetailResponse subject = subjectService.getSubjectDetailById(id);

        if (subject == null) {
            return ApiResponse.fail(4043, "作品不存在");
        }

        return ApiResponse.success(subject);
    }

    @PutMapping("/{id}")
    public ApiResponse<?> updateSubject(
            @PathVariable Long id,
            @Valid @RequestBody SubjectCreateRequest request
    ) {
        if (request.getFranchiseId() != null
                && !franchiseService.franchiseExists(request.getFranchiseId())) {
            return ApiResponse.fail(4044, "系列不存在");
        }

        SubjectResponse updatedSubject = subjectService.updateSubject(id, request);

        if (updatedSubject == null) {
            return ApiResponse.fail(4043, "作品不存在");
        }

        return ApiResponse.success(updatedSubject);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<?> deleteSubject(@PathVariable Long id) {
        if (!subjectService.subjectExists(id)) {
            return ApiResponse.fail(4043, "作品不存在");
        }

        if (subjectService.isSubjectReferenced(id)) {
            return ApiResponse.fail(4092, "作品已被用户记录引用，不能删除");
        }

        subjectService.deleteSubject(id);
        return ApiResponse.success("删除成功");
    }
}