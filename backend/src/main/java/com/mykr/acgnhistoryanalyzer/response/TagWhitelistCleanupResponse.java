package com.mykr.acgnhistoryanalyzer.response;

import java.util.List;

public class TagWhitelistCleanupResponse {

    private final int totalBefore;
    private final int keptCount;
    private final int deletedCount;
    private final List<String> deletedTagNames;

    public TagWhitelistCleanupResponse(int totalBefore,
                                       int keptCount,
                                       int deletedCount,
                                       List<String> deletedTagNames) {
        this.totalBefore = totalBefore;
        this.keptCount = keptCount;
        this.deletedCount = deletedCount;
        this.deletedTagNames = deletedTagNames;
    }

    public int getTotalBefore() {
        return totalBefore;
    }

    public int getKeptCount() {
        return keptCount;
    }

    public int getDeletedCount() {
        return deletedCount;
    }

    public List<String> getDeletedTagNames() {
        return deletedTagNames;
    }
}
