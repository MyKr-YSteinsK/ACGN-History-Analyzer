package com.mykr.acgnhistoryanalyzer.response;

import java.util.List;

public class TopMasterpieceResponse {

    private Integer threshold;
    private int totalCount;
    private List<UserSubjectRecordResponse> records;

    public TopMasterpieceResponse(Integer threshold, int totalCount, List<UserSubjectRecordResponse> records) {
        this.threshold = threshold;
        this.totalCount = totalCount;
        this.records = records;
    }

    public Integer getThreshold() {
        return threshold;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public List<UserSubjectRecordResponse> getRecords() {
        return records;
    }
}