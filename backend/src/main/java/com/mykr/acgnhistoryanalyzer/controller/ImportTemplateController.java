package com.mykr.acgnhistoryanalyzer.controller;

import com.mykr.acgnhistoryanalyzer.common.response.ApiResponse;
import com.mykr.acgnhistoryanalyzer.response.ImportTemplateResponse;
import com.mykr.acgnhistoryanalyzer.service.ImportTemplateService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/import/templates")
public class ImportTemplateController {

    private final ImportTemplateService importTemplateService;

    public ImportTemplateController(ImportTemplateService importTemplateService) {
        this.importTemplateService = importTemplateService;
    }

    @GetMapping("/subject")
    public ApiResponse<ImportTemplateResponse> getSubjectImportTemplate() {
        return ApiResponse.success(importTemplateService.getSubjectImportTemplate());
    }

    @GetMapping("/record")
    public ApiResponse<ImportTemplateResponse> getRecordImportTemplate() {
        return ApiResponse.success(importTemplateService.getRecordImportTemplate());
    }
}