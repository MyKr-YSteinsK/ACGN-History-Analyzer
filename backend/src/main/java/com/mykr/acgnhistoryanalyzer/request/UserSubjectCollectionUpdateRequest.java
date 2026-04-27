package com.mykr.acgnhistoryanalyzer.request;

import com.mykr.acgnhistoryanalyzer.common.enums.CollectionType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class UserSubjectCollectionUpdateRequest {

    @NotNull(message = "收藏类型不能为空")
    private CollectionType collectionType;

    @Size(max = 500, message = "备注不能超过500个字符")
    private String note;

    public CollectionType getCollectionType() { return collectionType; }

    public String getNote() { return note; }

    public void setCollectionType(CollectionType collectionType) { this.collectionType = collectionType; }

    public void setNote(String note) { this.note = note; }
}
