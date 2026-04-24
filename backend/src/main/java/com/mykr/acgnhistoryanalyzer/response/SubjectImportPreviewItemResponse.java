package com.mykr.acgnhistoryanalyzer.response;

public class SubjectImportPreviewItemResponse {

    private String displayTitle;
    private String result;
    private String message;

    public SubjectImportPreviewItemResponse(String displayTitle, String result, String message) {
        this.displayTitle = displayTitle;
        this.result = result;
        this.message = message;
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
}