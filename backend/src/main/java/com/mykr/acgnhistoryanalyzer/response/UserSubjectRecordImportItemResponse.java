package com.mykr.acgnhistoryanalyzer.response;

public class UserSubjectRecordImportItemResponse {

    private Long recordId;
    private String subjectDisplayTitle;
    private String result;
    private String message;

    public UserSubjectRecordImportItemResponse(Long recordId, String subjectDisplayTitle,
                                               String result, String message) {
        this.recordId = recordId;
        this.subjectDisplayTitle = subjectDisplayTitle;
        this.result = result;
        this.message = message;
    }

    public Long getRecordId() {
        return recordId;
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