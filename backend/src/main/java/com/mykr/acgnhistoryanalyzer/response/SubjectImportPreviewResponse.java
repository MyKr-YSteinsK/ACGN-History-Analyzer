package com.mykr.acgnhistoryanalyzer.response;

import java.util.List;

public class SubjectImportPreviewResponse {

    private int totalCount;
    private int canImportCount;
    private int skippedCount;
    private int updatedCount;
    private List<SubjectImportPreviewItemResponse> items;

    public SubjectImportPreviewResponse(int totalCount, int canImportCount, int skippedCount,
                                        int updatedCount,
                                        List<SubjectImportPreviewItemResponse> items) {
        this.totalCount = totalCount;
        this.canImportCount = canImportCount;
        this.skippedCount = skippedCount;
        this.updatedCount = updatedCount;
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

    public int getUpdatedCount() {
        return updatedCount;
    }

    public List<SubjectImportPreviewItemResponse> getItems() {
        return items;
    }
}