package com.mykr.acgnhistoryanalyzer.response;

public class RecordScoreBandStatsResponse {

    private int totalCount;
    private int excellentCount;
    private int normalCount;
    private int badCount;
    private int unratedCount;

    public RecordScoreBandStatsResponse(int totalCount, int excellentCount,
                                        int normalCount, int badCount, int unratedCount) {
        this.totalCount = totalCount;
        this.excellentCount = excellentCount;
        this.normalCount = normalCount;
        this.badCount = badCount;
        this.unratedCount = unratedCount;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public int getExcellentCount() {
        return excellentCount;
    }

    public int getNormalCount() {
        return normalCount;
    }

    public int getBadCount() {
        return badCount;
    }

    public int getUnratedCount() {
        return unratedCount;
    }
}