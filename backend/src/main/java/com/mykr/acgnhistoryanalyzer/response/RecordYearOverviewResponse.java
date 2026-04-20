package com.mykr.acgnhistoryanalyzer.response;

public class RecordYearOverviewResponse {

    private Integer year;
    private int totalCount;
    private int highScoreCount;
    private int watchedCount;
    private int onHoldCount;
    private int wantToWatchCount;
    private int excellentCount;
    private int normalCount;
    private int badCount;
    private int unratedCount;
    private int q1Count;
    private int q2Count;
    private int q3Count;
    private int q4Count;

    public RecordYearOverviewResponse(Integer year,
                                      int totalCount,
                                      int highScoreCount,
                                      int watchedCount,
                                      int onHoldCount,
                                      int wantToWatchCount,
                                      int excellentCount,
                                      int normalCount,
                                      int badCount,
                                      int unratedCount,
                                      int q1Count,
                                      int q2Count,
                                      int q3Count,
                                      int q4Count) {
        this.year = year;
        this.totalCount = totalCount;
        this.highScoreCount = highScoreCount;
        this.watchedCount = watchedCount;
        this.onHoldCount = onHoldCount;
        this.wantToWatchCount = wantToWatchCount;
        this.excellentCount = excellentCount;
        this.normalCount = normalCount;
        this.badCount = badCount;
        this.unratedCount = unratedCount;
        this.q1Count = q1Count;
        this.q2Count = q2Count;
        this.q3Count = q3Count;
        this.q4Count = q4Count;
    }

    public Integer getYear() {
        return year;
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

    public int getQ1Count() {
        return q1Count;
    }

    public int getQ2Count() {
        return q2Count;
    }

    public int getQ3Count() {
        return q3Count;
    }

    public int getQ4Count() {
        return q4Count;
    }
}