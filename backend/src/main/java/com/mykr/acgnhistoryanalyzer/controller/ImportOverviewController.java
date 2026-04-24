package com.mykr.acgnhistoryanalyzer.controller;

import com.mykr.acgnhistoryanalyzer.common.response.ApiResponse;
import com.mykr.acgnhistoryanalyzer.response.ImportOverviewResponse;
import com.mykr.acgnhistoryanalyzer.service.ImportOverviewService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/import")
public class ImportOverviewController {

    private final ImportOverviewService importOverviewService;

    public ImportOverviewController(ImportOverviewService importOverviewService) {
        this.importOverviewService = importOverviewService;
    }

    @GetMapping("/overview")
    public ApiResponse<ImportOverviewResponse> getImportOverview() {
        return ApiResponse.success(importOverviewService.getImportOverview());
    }
}