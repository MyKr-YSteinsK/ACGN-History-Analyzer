package com.mykr.acgnhistoryanalyzer.request;

import com.mykr.acgnhistoryanalyzer.common.enums.CollectionType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class UserSubjectCollectionCreateRequest {

    @NotNull(message = "作品ID不能为空")
    @Positive(message = "作品ID必须大于0")
    private Long subjectId;

    @NotNull(message = "收藏类型不能为空")
    private CollectionType collectionType;

    @Size(max = 500, message = "备注不能超过500个字符")
    private String note;

    public Long getSubjectId() { return subjectId; }

    public CollectionType getCollectionType() { return collectionType; }

    public String getNote() { return note; }

    public void setSubjectId(Long subjectId) { this.subjectId = subjectId; }

    public void setCollectionType(CollectionType collectionType) { this.collectionType = collectionType; }

    public void setNote(String note) { this.note = note; }
}
