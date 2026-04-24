package com.mykr.acgnhistoryanalyzer.response;

import java.util.List;

public class UserSubjectRecordImportResponse {

    private int totalCount;
    private int importedCount;
    private int skippedCount;
    private List<UserSubjectRecordImportItemResponse> items;

    public UserSubjectRecordImportResponse(int totalCount, int importedCount, int skippedCount,
                                           List<UserSubjectRecordImportItemResponse> items) {
        this.totalCount = totalCount;
        this.importedCount = importedCount;
        this.skippedCount = skippedCount;
        this.items = items;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public int getImportedCount() {
        return importedCount;
    }

    public int getSkippedCount() {
        return skippedCount;
    }

    public List<UserSubjectRecordImportItemResponse> getItems() {
        return items;
    }
}