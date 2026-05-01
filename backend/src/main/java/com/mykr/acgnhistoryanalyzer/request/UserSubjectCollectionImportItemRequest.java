package com.mykr.acgnhistoryanalyzer.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class UserSubjectCollectionImportItemRequest {

    @Positive(message = "subjectId 必须大于0")
    private Long subjectId;

    @Size(max = 255, message = "subjectTitle 不能超过255个字符")
    private String subjectTitle;

    @Min(value = 1, message = "subjectSeasonIndex 必须大于等于1")
    private Integer subjectSeasonIndex;

    @Min(value = 1, message = "subjectPartIndex 必须大于等于1")
    private Integer subjectPartIndex;

    @Size(max = 255, message = "sourceId 不能超过255个字符")
    private String sourceId;

    @Pattern(regexp = "WANT|PAUSED", message = "收藏类型只能是 WANT 或 PAUSED")
    private String collectionType;

    @Size(max = 500, message = "备注不能超过500个字符")
    private String note;

    public Long getSubjectId() { return subjectId; }

    public String getSubjectTitle() { return subjectTitle; }

    public Integer getSubjectSeasonIndex() { return subjectSeasonIndex; }

    public Integer getSubjectPartIndex() { return subjectPartIndex; }

    public String getSourceId() { return sourceId; }

    public String getCollectionType() { return collectionType; }

    public String getNote() { return note; }

    public void setSubjectId(Long subjectId) { this.subjectId = subjectId; }

    public void setSubjectTitle(String subjectTitle) { this.subjectTitle = subjectTitle; }

    public void setSubjectSeasonIndex(Integer subjectSeasonIndex) { this.subjectSeasonIndex = subjectSeasonIndex; }

    public void setSubjectPartIndex(Integer subjectPartIndex) { this.subjectPartIndex = subjectPartIndex; }

    public void setSourceId(String sourceId) { this.sourceId = sourceId; }

    public void setCollectionType(String collectionType) { this.collectionType = collectionType; }

    public void setNote(String note) { this.note = note; }
}
