package com.mykr.acgnhistoryanalyzer.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class UserSubjectCollectionImportItemRequest {

    @Positive(message = "作品ID必须大于0")
    private Long subjectId;

    @Size(max = 255, message = "作品主标题不能超过255个字符")
    private String subjectTitleCn;

    @Size(max = 255, message = "作品副标题不能超过255个字符")
    private String subjectSubtitle;

    @Min(value = 1, message = "季序号必须大于等于1")
    private Integer subjectSeasonIndex;

    @Min(value = 1, message = "Part序号必须大于等于1")
    private Integer subjectPartIndex;

    @Pattern(regexp = "ANIME|COMIC|NOVEL|GAME", message = "作品类型只能是 ANIME、COMIC、NOVEL 或 GAME")
    private String subjectCategory;

    @Pattern(regexp = "WANT|PAUSED", message = "收藏类型只能是 WANT 或 PAUSED")
    private String collectionType;

    @Size(max = 500, message = "备注不能超过500个字符")
    private String note;

    public Long getSubjectId() { return subjectId; }

    public String getSubjectTitleCn() { return subjectTitleCn; }

    public String getSubjectSubtitle() { return subjectSubtitle; }

    public Integer getSubjectSeasonIndex() { return subjectSeasonIndex; }

    public Integer getSubjectPartIndex() { return subjectPartIndex; }

    public String getSubjectCategory() { return subjectCategory; }

    public String getCollectionType() { return collectionType; }

    public String getNote() { return note; }

    public void setSubjectId(Long subjectId) { this.subjectId = subjectId; }

    public void setSubjectTitleCn(String subjectTitleCn) { this.subjectTitleCn = subjectTitleCn; }

    public void setSubjectSubtitle(String subjectSubtitle) { this.subjectSubtitle = subjectSubtitle; }

    public void setSubjectSeasonIndex(Integer subjectSeasonIndex) { this.subjectSeasonIndex = subjectSeasonIndex; }

    public void setSubjectPartIndex(Integer subjectPartIndex) { this.subjectPartIndex = subjectPartIndex; }

    public void setSubjectCategory(String subjectCategory) { this.subjectCategory = subjectCategory; }

    public void setCollectionType(String collectionType) { this.collectionType = collectionType; }

    public void setNote(String note) { this.note = note; }
}
