package com.mykr.acgnhistoryanalyzer.response;

public class SubjectImportPreviewItemResponse {

    private String displayTitle;
    private String result;
    private String message;
    private String sourcePlatform;
    private String sourceId;

    public SubjectImportPreviewItemResponse(String displayTitle, String result, String message) {
        this(displayTitle, result, message, null, null);
    }

    public SubjectImportPreviewItemResponse(String displayTitle, String result, String message,
                                            String sourcePlatform, String sourceId) {
        this.displayTitle = displayTitle;
        this.result = result;
        this.message = message;
        this.sourcePlatform = sourcePlatform;
        this.sourceId = sourceId;
    }

    public String getDisplayTitle() {
        return displayTitle;
    }

    public String getResult() {
        return result;
    }

    public String getMessage() {
        return message;
    }

    public String getSourcePlatform() {
        return sourcePlatform;
    }

    public String getSourceId() {
        return sourceId;
    }
}
