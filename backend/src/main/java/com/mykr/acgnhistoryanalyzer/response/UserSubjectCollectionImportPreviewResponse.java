package com.mykr.acgnhistoryanalyzer.response;

import java.util.List;

public class UserSubjectCollectionImportPreviewResponse {

    private int totalCount;
    private int canImportCount;
    private int skippedCount;
    private List<UserSubjectCollectionImportPreviewItemResponse> items;

    public UserSubjectCollectionImportPreviewResponse(int totalCount,
                                                      int canImportCount,
                                                      int skippedCount,
                                                      List<UserSubjectCollectionImportPreviewItemResponse> items) {
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

    public List<UserSubjectCollectionImportPreviewItemResponse> getItems() {
        return items;
    }
}
