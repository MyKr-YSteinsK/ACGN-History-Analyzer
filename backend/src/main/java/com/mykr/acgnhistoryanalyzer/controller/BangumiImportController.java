package com.mykr.acgnhistoryanalyzer.controller;

import com.mykr.acgnhistoryanalyzer.common.response.ApiResponse;
import com.mykr.acgnhistoryanalyzer.request.BangumiSeasonImportRequest;
import com.mykr.acgnhistoryanalyzer.response.SubjectImportPreviewResponse;
import com.mykr.acgnhistoryanalyzer.response.SubjectImportResponse;
import com.mykr.acgnhistoryanalyzer.service.BangumiSeasonImportService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/import/bangumi")
public class BangumiImportController {

    private final BangumiSeasonImportService bangumiSeasonImportService;

    public BangumiImportController(BangumiSeasonImportService bangumiSeasonImportService) {
        this.bangumiSeasonImportService = bangumiSeasonImportService;
    }

    @PostMapping("/season/preview")
    public ApiResponse<SubjectImportPreviewResponse> previewSeason(
            @Valid @RequestBody BangumiSeasonImportRequest request
    ) {
        return ApiResponse.success(bangumiSeasonImportService.previewSeason(request));
    }

    @PostMapping("/season/import")
    public ApiResponse<SubjectImportResponse> importSeason(
            @Valid @RequestBody BangumiSeasonImportRequest request
    ) {
        return ApiResponse.success(bangumiSeasonImportService.importSeason(request));
    }
}
