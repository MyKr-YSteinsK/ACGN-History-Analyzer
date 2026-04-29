package com.mykr.acgnhistoryanalyzer.response;

import java.util.List;

public class UserSubjectCollectionImportResponse {

    private int totalCount;
    private int importedCount;
    private int skippedCount;
    private List<UserSubjectCollectionImportItemResponse> items;

    public UserSubjectCollectionImportResponse(int totalCount,
                                               int importedCount,
                                               int skippedCount,
                                               List<UserSubjectCollectionImportItemResponse> items) {
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

    public List<UserSubjectCollectionImportItemResponse> getItems() {
        return items;
    }
}
