package com.mykr.acgnhistoryanalyzer.response;

import java.util.List;

public class HomeQuarterDashboardResponse {

    private Integer year;
    private Integer quarter;
    private String category;
    private String recordStatus;
    private String keyword;
    private String recordSort;
    private String librarySort;
    private RecordQuarterOverviewResponse baseQuarterOverview;
    private RecordQuarterOverviewResponse viewQuarterOverview;
    private List<UserSubjectRecordResponse> recordList;
    private List<UserSubjectRecordResponse> highScoreRecordList;
    private List<SubjectResponse> subjectLibraryList;

    public HomeQuarterDashboardResponse(Integer year, Integer quarter, String category,
                                        String recordStatus, String keyword, String recordSort,
                                        String librarySort,
                                        RecordQuarterOverviewResponse baseQuarterOverview,
                                        RecordQuarterOverviewResponse viewQuarterOverview,
                                        List<UserSubjectRecordResponse> recordList,
                                        List<UserSubjectRecordResponse> highScoreRecordList,
                                        List<SubjectResponse> subjectLibraryList) {
        this.year = year;
        this.quarter = quarter;
        this.category = category;
        this.recordStatus = recordStatus;
        this.keyword = keyword;
        this.recordSort = recordSort;
        this.librarySort = librarySort;
        this.baseQuarterOverview = baseQuarterOverview;
        this.viewQuarterOverview = viewQuarterOverview;
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

    public String getRecordStatus() {
        return recordStatus;
    }

    public String getKeyword() {
        return keyword;
    }

    public String getRecordSort() {
        return recordSort;
    }

    public String getLibrarySort() {
        return librarySort;
    }

    public RecordQuarterOverviewResponse getBaseQuarterOverview() {
        return baseQuarterOverview;
    }

    public RecordQuarterOverviewResponse getViewQuarterOverview() {
        return viewQuarterOverview;
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