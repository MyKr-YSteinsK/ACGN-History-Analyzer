package com.mykr.acgnhistoryanalyzer.request;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public class UserSubjectRecordBatchImportRequest {

    @NotEmpty(message = "导入列表不能为空")
    private List<UserSubjectRecordImportItemRequest> records;

    public UserSubjectRecordBatchImportRequest() {
    }

    public List<UserSubjectRecordImportItemRequest> getRecords() {
        return records;
    }

    public void setRecords(List<UserSubjectRecordImportItemRequest> records) {
        this.records = records;
    }
}
