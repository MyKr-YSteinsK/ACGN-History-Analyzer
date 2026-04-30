package com.mykr.acgnhistoryanalyzer.response;

public class BangumiImportJobStartResponse {

    private final String jobId;

    public BangumiImportJobStartResponse(String jobId) {
        this.jobId = jobId;
    }

    public String getJobId() {
        return jobId;
    }
}
