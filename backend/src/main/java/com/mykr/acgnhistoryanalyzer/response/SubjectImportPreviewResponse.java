package com.mykr.acgnhistoryanalyzer.response;

import java.util.List;

public class SubjectImportPreviewResponse {

    private int totalCount;
    private int canImportCount;
    private int skippedCount;
    private List<SubjectImportPreviewItemResponse> items;

    public SubjectImportPreviewResponse(int totalCount, int canImportCount, int skippedCount,
                                        List<SubjectImportPreviewItemResponse> items) {
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

    public List<SubjectImportPreviewItemResponse> getItems() {
        return items;
    }
}