package com.mykr.acgnhistoryanalyzer.response;

public class UserSubjectCollectionImportItemResponse {

    private Long collectionId;
    private String subjectDisplayTitle;
    private String result;
    private String message;

    public UserSubjectCollectionImportItemResponse(Long collectionId,
                                                   String subjectDisplayTitle,
                                                   String result,
                                                   String message) {
        this.collectionId = collectionId;
        this.subjectDisplayTitle = subjectDisplayTitle;
        this.result = result;
        this.message = message;
    }

    public Long getCollectionId() {
        return collectionId;
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
