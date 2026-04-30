package com.mykr.acgnhistoryanalyzer.response;

import java.time.LocalDateTime;

public class BangumiImportJobStatusResponse {

    private final String jobId;
    private final String status;
    private final String stage;
    private final String message;
    private final int percent;
    private final SubjectImportResponse result;
    private final String errorMessage;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public BangumiImportJobStatusResponse(String jobId,
                                          String status,
                                          String stage,
                                          String message,
                                          int percent,
                                          SubjectImportResponse result,
                                          String errorMessage,
                                          LocalDateTime createdAt,
                                          LocalDateTime updatedAt) {
        this.jobId = jobId;
        this.status = status;
        this.stage = stage;
        this.message = message;
        this.percent = percent;
        this.result = result;
        this.errorMessage = errorMessage;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getJobId() {
        return jobId;
    }

    public String getStatus() {
        return status;
    }

    public String getStage() {
        return stage;
    }

    public String getMessage() {
        return message;
    }

    public int getPercent() {
        return percent;
    }

    public SubjectImportResponse getResult() {
        return result;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
