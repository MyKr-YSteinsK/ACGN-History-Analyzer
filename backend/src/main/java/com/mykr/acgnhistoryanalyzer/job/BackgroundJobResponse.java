package com.mykr.acgnhistoryanalyzer.job;

import java.time.LocalDateTime;

public class BackgroundJobResponse {

    private String id;
    private String jobType;
    private String status;
    private int progress;
    private String message;
    private String errorMessage;
    private String resultJson;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static BackgroundJobResponse fromEntity(BackgroundJob job) {
        BackgroundJobResponse response = new BackgroundJobResponse();
        response.setId(job.getId());
        response.setJobType(job.getJobType().name());
        response.setStatus(job.getStatus().name());
        response.setProgress(job.getProgress() == null ? 0 : job.getProgress());
        response.setMessage(job.getMessage());
        response.setErrorMessage(job.getErrorMessage());
        response.setResultJson(job.getResultJson());
        response.setCreatedAt(job.getCreatedAt());
        response.setUpdatedAt(job.getUpdatedAt());
        return response;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getJobType() {
        return jobType;
    }

    public void setJobType(String jobType) {
        this.jobType = jobType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getResultJson() {
        return resultJson;
    }

    public void setResultJson(String resultJson) {
        this.resultJson = resultJson;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
