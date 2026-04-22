package com.mykr.acgnhistoryanalyzer.response;

import java.util.List;

public class HomeSearchResponse {

    private Integer year;
    private Integer quarter;
    private String category;
    private String keyword;
    private String scope;
    private List<UserSubjectRecordResponse> recordedResults;
    private List<SubjectResponse> libraryResults;

    public HomeSearchResponse(Integer year, Integer quarter, String category,
                              String keyword, String scope,
                              List<UserSubjectRecordResponse> recordedResults,
                              List<SubjectResponse> libraryResults) {
        this.year = year;
        this.quarter = quarter;
        this.category = category;
        this.keyword = keyword;
        this.scope = scope;
        this.recordedResults = recordedResults;
        this.libraryResults = libraryResults;
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

    public String getKeyword() {
        return keyword;
    }

    public String getScope() {
        return scope;
    }

    public List<UserSubjectRecordResponse> getRecordedResults() {
        return recordedResults;
    }

    public List<SubjectResponse> getLibraryResults() {
        return libraryResults;
    }
}