package com.mykr.acgnhistoryanalyzer.service;

import com.mykr.acgnhistoryanalyzer.request.BangumiSeasonImportRequest;
import com.mykr.acgnhistoryanalyzer.response.BangumiImportJobStartResponse;
import com.mykr.acgnhistoryanalyzer.response.BangumiImportJobStatusResponse;
import com.mykr.acgnhistoryanalyzer.response.SubjectImportResponse;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class BangumiImportJobService {

    private static final int JOB_RETENTION_HOURS = 6;

    private final BangumiSeasonImportService bangumiSeasonImportService;
    private final Map<String, BangumiImportJob> jobs = new ConcurrentHashMap<>();

    public BangumiImportJobService(BangumiSeasonImportService bangumiSeasonImportService) {
        this.bangumiSeasonImportService = bangumiSeasonImportService;
    }

    public BangumiImportJobStartResponse startImportJob(BangumiSeasonImportRequest request) {
        cleanupFinishedJobs();
        String jobId = UUID.randomUUID().toString();
        BangumiImportJob job = new BangumiImportJob(jobId);
        jobs.put(jobId, job);

        CompletableFuture.runAsync(() -> runImportJob(job, request));

        return new BangumiImportJobStartResponse(jobId);
    }

    public BangumiImportJobStatusResponse getJobStatus(String jobId) {
        cleanupFinishedJobs();
        BangumiImportJob job = jobs.get(jobId);
        if (job == null) {
            return null;
        }
        return job.toResponse();
    }

    private void runImportJob(BangumiImportJob job, BangumiSeasonImportRequest request) {
        try {
            job.markRunning("STARTING", "导入任务已启动，正在准备请求参数", 1);
            SubjectImportResponse result = bangumiSeasonImportService.importSeason(
                    request,
                    (percent, stage, message) -> job.markRunning(stage, message, percent)
            );
            job.markSucceeded(result);
        } catch (Exception ex) {
            job.markFailed(buildFailureMessage(ex));
        }
    }

    private void cleanupFinishedJobs() {
        LocalDateTime expiredAt = LocalDateTime.now().minusHours(JOB_RETENTION_HOURS);
        jobs.entrySet().removeIf(entry -> entry.getValue().isFinishedBefore(expiredAt));
    }

    private String buildFailureMessage(Exception ex) {
        String message = ex.getMessage();
        if (message == null || message.isBlank()) {
            message = "未知错误";
        }
        return "Bangumi 导入失败：" + message + "。可以稍后重试；如果连续失败，请检查网络、Bangumi 可用性或先关闭 WEB 作品后再导入。";
    }

    private static class BangumiImportJob {

        private final String jobId;
        private final LocalDateTime createdAt;
        private volatile LocalDateTime updatedAt;
        private volatile String status;
        private volatile String stage;
        private volatile String message;
        private volatile int percent;
        private volatile SubjectImportResponse result;
        private volatile String errorMessage;

        BangumiImportJob(String jobId) {
            this.jobId = jobId;
            this.createdAt = LocalDateTime.now();
            this.updatedAt = this.createdAt;
            this.status = "PENDING";
            this.stage = "PENDING";
            this.message = "等待导入任务开始";
            this.percent = 0;
        }

        void markRunning(String stage, String message, int percent) {
            this.status = "RUNNING";
            this.stage = stage;
            this.message = message;
            this.percent = Math.max(0, Math.min(percent, 99));
            this.updatedAt = LocalDateTime.now();
        }

        void markSucceeded(SubjectImportResponse result) {
            this.status = "SUCCESS";
            this.stage = "DONE";
            this.message = "导入完成";
            this.percent = 100;
            this.result = result;
            this.updatedAt = LocalDateTime.now();
        }

        void markFailed(String errorMessage) {
            this.status = "FAILED";
            this.stage = "FAILED";
            this.message = "导入失败";
            this.errorMessage = errorMessage;
            this.percent = Math.max(1, this.percent);
            this.updatedAt = LocalDateTime.now();
        }

        boolean isFinishedBefore(LocalDateTime expiredAt) {
            return ("SUCCESS".equals(status) || "FAILED".equals(status))
                    && updatedAt != null
                    && updatedAt.isBefore(expiredAt);
        }

        BangumiImportJobStatusResponse toResponse() {
            return new BangumiImportJobStatusResponse(
                    jobId,
                    status,
                    stage,
                    message,
                    percent,
                    result,
                    errorMessage,
                    createdAt,
                    updatedAt
            );
        }
    }
}
