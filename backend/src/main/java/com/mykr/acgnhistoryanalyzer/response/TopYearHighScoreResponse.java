package com.mykr.acgnhistoryanalyzer.response;

import java.util.List;

public class TopYearHighScoreResponse {

    private Integer year;
    private Integer threshold;
    private int totalCount;
    private List<TopQuarterGroupResponse> quarterGroups;

    public TopYearHighScoreResponse(Integer year, Integer threshold, int totalCount,
                                    List<TopQuarterGroupResponse> quarterGroups) {
        this.year = year;
        this.threshold = threshold;
        this.totalCount = totalCount;
        this.quarterGroups = quarterGroups;
    }

    public Integer getYear() {
        return year;
    }

    public Integer getThreshold() {
        return threshold;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public List<TopQuarterGroupResponse> getQuarterGroups() {
        return quarterGroups;
    }
}