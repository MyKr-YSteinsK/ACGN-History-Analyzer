package com.mykr.acgnhistoryanalyzer.response;

public class RecordQuarterOverviewResponse {

    private Integer year;
    private String quarter;
    private int totalCount;
    private int highScoreCount;
    private int watchedCount;
    private int onHoldCount;
    private int wantToWatchCount;
    private int excellentCount;
    private int normalCount;
    private int badCount;
    private int unratedCount;

    public RecordQuarterOverviewResponse(Integer year, String quarter,
                                         int totalCount, int highScoreCount,
                                         int watchedCount, int onHoldCount, int wantToWatchCount,
                                         int excellentCount, int normalCount,
                                         int badCount, int unratedCount) {
        this.year = year;
        this.quarter = quarter;
        this.totalCount = totalCount;
        this.highScoreCount = highScoreCount;
        this.watchedCount = watchedCount;
        this.onHoldCount = onHoldCount;
        this.wantToWatchCount = wantToWatchCount;
        this.excellentCount = excellentCount;
        this.normalCount = normalCount;
        this.badCount = badCount;
        this.unratedCount = unratedCount;
    }

    public Integer getYear() {
        return year;
    }

    public String getQuarter() {
        return quarter;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public int getHighScoreCount() {
        return highScoreCount;
    }

    public int getWatchedCount() {
        return watchedCount;
    }

    public int getOnHoldCount() {
        return onHoldCount;
    }

    public int getWantToWatchCount() {
        return wantToWatchCount;
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