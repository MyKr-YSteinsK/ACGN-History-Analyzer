package com.mykr.acgnhistoryanalyzer.response;

import java.util.List;

public class HomeQuarterDashboardResponse {

    private Integer year;
    private Integer quarter;
    private String category;
    private RecordQuarterOverviewResponse quarterOverview;
    private List<UserSubjectRecordResponse> recordList;
    private List<UserSubjectRecordResponse> highScoreRecordList;
    private List<SubjectResponse> subjectLibraryList;

    public HomeQuarterDashboardResponse(Integer year, Integer quarter, String category,
                                        RecordQuarterOverviewResponse quarterOverview,
                                        List<UserSubjectRecordResponse> recordList,
                                        List<UserSubjectRecordResponse> highScoreRecordList,
                                        List<SubjectResponse> subjectLibraryList) {
        this.year = year;
        this.quarter = quarter;
        this.category = category;
        this.quarterOverview = quarterOverview;
        this.recordList = recordList;
        this.highScoreRecordList = highScoreRecordList;
        this.subjectLibraryList = subjectLibraryList;
    }

    public Integer getYear() {
        return year;
    }

    public Integer getQuarter() {
        return quarter;
    }

    public String getCategory() {
        return category;
    }

    public RecordQuarterOverviewResponse getQuarterOverview() {
        return quarterOverview;
    }

    public List<UserSubjectRecordResponse> getRecordList() {
        return recordList;
    }

    public List<UserSubjectRecordResponse> getHighScoreRecordList() {
        return highScoreRecordList;
    }

    public List<SubjectResponse> getSubjectLibraryList() {
        return subjectLibraryList;
    }
}