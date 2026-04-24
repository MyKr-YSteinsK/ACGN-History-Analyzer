package com.mykr.acgnhistoryanalyzer.response;

import java.util.List;

public class SubjectImportResponse {

    private int totalCount;
    private int importedCount;
    private int skippedCount;
    private List<SubjectImportItemResponse> items;

    public SubjectImportResponse(int totalCount, int importedCount, int skippedCount,
                                 List<SubjectImportItemResponse> items) {
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

    public List<SubjectImportItemResponse> getItems() {
        return items;
    }
}