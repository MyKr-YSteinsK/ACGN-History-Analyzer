package com.mykr.acgnhistoryanalyzer.response;

import java.util.List;

public class TopQuarterGroupResponse {

    private Integer quarter;
    private int count;
    private List<UserSubjectRecordResponse> records;

    public TopQuarterGroupResponse(Integer quarter, int count, List<UserSubjectRecordResponse> records) {
        this.quarter = quarter;
        this.count = count;
        this.records = records;
    }

    public Integer getQuarter() {
        return quarter;
    }

    public int getCount() {
        return count;
    }

    public List<UserSubjectRecordResponse> getRecords() {
        return records;
    }
}