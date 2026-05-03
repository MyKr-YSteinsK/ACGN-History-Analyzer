package com.mykr.acgnhistoryanalyzer.controller;

import com.mykr.acgnhistoryanalyzer.common.response.ApiResponse;
import com.mykr.acgnhistoryanalyzer.response.BackupExportResponse;
import com.mykr.acgnhistoryanalyzer.service.BackupExportService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/export")
public class BackupExportController {

    private final BackupExportService backupExportService;

    public BackupExportController(BackupExportService backupExportService) {
        this.backupExportService = backupExportService;
    }

    @GetMapping("/bangumi-roundtrip")
    public ApiResponse<BackupExportResponse> bangumiRoundtrip() {
        return ApiResponse.success(backupExportService.buildBangumiRoundtripExport());
    }
}
