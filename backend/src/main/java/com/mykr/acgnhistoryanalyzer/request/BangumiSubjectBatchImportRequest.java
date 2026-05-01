package com.mykr.acgnhistoryanalyzer.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public class BangumiSubjectBatchImportRequest {

    @NotEmpty(message = "subjects 不能为空")
    @Valid
    private List<BangumiSubjectImportItemRequest> subjects;

    public List<BangumiSubjectImportItemRequest> getSubjects() {
        return subjects;
    }

    public void setSubjects(List<BangumiSubjectImportItemRequest> subjects) {
        this.subjects = subjects;
    }
}
