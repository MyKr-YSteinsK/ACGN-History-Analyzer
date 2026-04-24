package com.mykr.acgnhistoryanalyzer.response;

public class SubjectImportItemResponse {

    private Long subjectId;
    private String displayTitle;
    private String result;
    private String message;

    public SubjectImportItemResponse(Long subjectId, String displayTitle, String result, String message) {
        this.subjectId = subjectId;
        this.displayTitle = displayTitle;
        this.result = result;
        this.message = message;
    }

    public Long getSubjectId() {
        return subjectId;
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