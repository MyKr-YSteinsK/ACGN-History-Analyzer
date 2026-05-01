package com.mykr.acgnhistoryanalyzer.controller;

import com.mykr.acgnhistoryanalyzer.common.response.ApiResponse;
import com.mykr.acgnhistoryanalyzer.request.BangumiSeasonImportRequest;
import com.mykr.acgnhistoryanalyzer.request.BangumiSubjectBatchImportRequest;
import com.mykr.acgnhistoryanalyzer.response.BangumiImportJobStartResponse;
import com.mykr.acgnhistoryanalyzer.response.BangumiImportJobStatusResponse;
import com.mykr.acgnhistoryanalyzer.response.SubjectImportPreviewResponse;
import com.mykr.acgnhistoryanalyzer.response.SubjectImportResponse;
import com.mykr.acgnhistoryanalyzer.service.BangumiImportJobService;
import com.mykr.acgnhistoryanalyzer.service.BangumiSeasonImportService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/import/bangumi")
public class BangumiImportController {

    private final BangumiSeasonImportService bangumiSeasonImportService;
    private final BangumiImportJobService bangumiImportJobService;

    public BangumiImportController(BangumiSeasonImportService bangumiSeasonImportService,
                                   BangumiImportJobService bangumiImportJobService) {
        this.bangumiSeasonImportService = bangumiSeasonImportService;
        this.bangumiImportJobService = bangumiImportJobService;
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

    @PostMapping("/season/import-jobs")
    public ApiResponse<BangumiImportJobStartResponse> startImportJob(
            @Valid @RequestBody BangumiSeasonImportRequest request
    ) {
        return ApiResponse.success(bangumiImportJobService.startImportJob(request));
    }

    @PostMapping("/subject/preview")
    public ApiResponse<SubjectImportPreviewResponse> previewBySource(
            @Valid @RequestBody BangumiSubjectBatchImportRequest request
    ) {
        return ApiResponse.success(bangumiSeasonImportService.previewSubjectsBySource(request));
    }

    @PostMapping("/subject/import")
    public ApiResponse<SubjectImportResponse> importBySource(
            @Valid @RequestBody BangumiSubjectBatchImportRequest request
    ) {
        return ApiResponse.success(bangumiSeasonImportService.importSubjectsBySource(request));
    }

    @GetMapping("/season/import-jobs/{jobId}")
    public ApiResponse<?> getImportJobStatus(@PathVariable String jobId) {
        BangumiImportJobStatusResponse response = bangumiImportJobService.getJobStatus(jobId);
        if (response == null) {
            return ApiResponse.fail(4046, "Bangumi 导入任务不存在");
        }
        return ApiResponse.success(response);
    }
}
