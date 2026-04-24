package com.mykr.acgnhistoryanalyzer.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public class SubjectBatchImportRequest {

    @NotEmpty(message = "导入列表不能为空")
    @Valid
    private List<SubjectCreateRequest> subjects;

    public SubjectBatchImportRequest() {
    }

    public List<SubjectCreateRequest> getSubjects() {
        return subjects;
    }

    public void setSubjects(List<SubjectCreateRequest> subjects) {
        this.subjects = subjects;
    }
}