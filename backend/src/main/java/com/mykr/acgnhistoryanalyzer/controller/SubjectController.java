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
            @RequestParam(required = false) String category
    ) {
        List<SubjectResponse> subjects = subjectService.getSubjects(category);
        return ApiResponse.success(subjects);
    }
}