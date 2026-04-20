package com.mykr.acgnhistoryanalyzer.controller;

import com.mykr.acgnhistoryanalyzer.common.response.ApiResponse;
import com.mykr.acgnhistoryanalyzer.request.SubjectCreateRequest;
import com.mykr.acgnhistoryanalyzer.response.SubjectResponse;
import com.mykr.acgnhistoryanalyzer.service.SubjectService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subjects")
public class SubjectController {

    private final SubjectService subjectService;

    public SubjectController(SubjectService subjectService) {
        this.subjectService = subjectService;
    }

    @PostMapping
    public ApiResponse<SubjectResponse> createSubject(@Valid @RequestBody SubjectCreateRequest request) {
        SubjectResponse savedSubject = subjectService.createSubject(request);
        return ApiResponse.success(savedSubject);
    }

    @GetMapping
    public ApiResponse<List<SubjectResponse>> getSubjects(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword
    ) {
        List<SubjectResponse> subjects = subjectService.getSubjects(category, keyword);
        return ApiResponse.success(subjects);
    }

    @GetMapping("/{id}")
    public ApiResponse<?> getSubjectById(@PathVariable Long id) {
        SubjectResponse subject = subjectService.getSubjectById(id);

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