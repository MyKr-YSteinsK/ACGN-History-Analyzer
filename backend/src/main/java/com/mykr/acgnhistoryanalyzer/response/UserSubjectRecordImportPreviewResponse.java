package com.mykr.acgnhistoryanalyzer.response;

import java.util.List;

public class UserSubjectRecordImportPreviewResponse {

    private int totalCount;
    private int canImportCount;
    private int skippedCount;
    private List<UserSubjectRecordImportPreviewItemResponse> items;

    public UserSubjectRecordImportPreviewResponse(int totalCount,
                                                  int canImportCount,
                                                  int skippedCount,
                                                  List<UserSubjectRecordImportPreviewItemResponse> items) {
        this.totalCount = totalCount;
        this.canImportCount = canImportCount;
        this.skippedCount = skippedCount;
        this.items = items;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public int getCanImportCount() {
        return canImportCount;
    }

    public int getSkippedCount() {
        return skippedCount;
    }

    public List<UserSubjectRecordImportPreviewItemResponse> getItems() {
        return items;
    }
}