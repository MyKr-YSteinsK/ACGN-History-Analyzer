package com.mykr.acgnhistoryanalyzer.response;

import java.util.List;

public class RecordStatusResponse {

    private List<String> statuses;

    public RecordStatusResponse(List<String> statuses) {
        this.statuses = statuses;
    }

    public List<String> getStatuses() {
        return statuses;
    }
}