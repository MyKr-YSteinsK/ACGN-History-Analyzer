package com.mykr.acgnhistoryanalyzer.request;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public class UserSubjectCollectionBatchImportRequest {

    @NotEmpty(message = "导入列表不能为空")
    private List<UserSubjectCollectionImportItemRequest> collections;

    public UserSubjectCollectionBatchImportRequest() {
    }

    public List<UserSubjectCollectionImportItemRequest> getCollections() {
        return collections;
    }

    public void setCollections(List<UserSubjectCollectionImportItemRequest> collections) {
        this.collections = collections;
    }
}
