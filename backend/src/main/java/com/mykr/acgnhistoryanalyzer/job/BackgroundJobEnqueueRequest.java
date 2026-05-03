package com.mykr.acgnhistoryanalyzer.job;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotNull;

public class BackgroundJobEnqueueRequest {

    @NotNull
    private BackgroundJobType jobType;

    @NotNull
    private JsonNode payload;

    public BackgroundJobType getJobType() {
        return jobType;
    }

    public void setJobType(BackgroundJobType jobType) {
        this.jobType = jobType;
    }

    public JsonNode getPayload() {
        return payload;
    }

    public void setPayload(JsonNode payload) {
        this.payload = payload;
    }
}
