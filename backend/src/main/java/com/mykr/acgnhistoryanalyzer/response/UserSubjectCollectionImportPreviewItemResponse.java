package com.mykr.acgnhistoryanalyzer.response;

public class UserSubjectCollectionImportPreviewItemResponse {

    private String subjectDisplayTitle;
    private String result;
    private String message;
    private java.util.List<SubjectMatchCandidateResponse> candidates;

    public UserSubjectCollectionImportPreviewItemResponse(String subjectDisplayTitle,
                                                          String result,
                                                          String message) {
        this(subjectDisplayTitle, result, message, java.util.List.of());
    }

    public UserSubjectCollectionImportPreviewItemResponse(String subjectDisplayTitle,
                                                          String result,
                                                          String message,
                                                          java.util.List<SubjectMatchCandidateResponse> candidates) {
        this.subjectDisplayTitle = subjectDisplayTitle;
        this.result = result;
        this.message = message;
        this.candidates = candidates == null ? java.util.List.of() : candidates;
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

    public java.util.List<SubjectMatchCandidateResponse> getCandidates() {
        return candidates;
    }
}
