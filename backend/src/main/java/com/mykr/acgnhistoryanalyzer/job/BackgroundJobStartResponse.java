package com.mykr.acgnhistoryanalyzer.job;

public class BackgroundJobStartResponse {

    private final String jobId;

    public BackgroundJobStartResponse(String jobId) {
        this.jobId = jobId;
    }

    public String getJobId() {
        return jobId;
    }
}
