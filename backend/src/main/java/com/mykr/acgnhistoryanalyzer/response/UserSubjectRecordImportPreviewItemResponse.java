package com.mykr.acgnhistoryanalyzer.response;

public class UserSubjectRecordImportPreviewItemResponse {

    private String subjectDisplayTitle;
    private String result;
    private String message;

    public UserSubjectRecordImportPreviewItemResponse(String subjectDisplayTitle,
                                                      String result,
                                                      String message) {
        this.subjectDisplayTitle = subjectDisplayTitle;
        this.result = result;
        this.message = message;
    }

    public String getSubjectDisplayTitle() {
        return subjectDisplayTitle;
    }

    public String getResult() {
        return result;
    }

    public String getMessage() {
        return message;
    }
}