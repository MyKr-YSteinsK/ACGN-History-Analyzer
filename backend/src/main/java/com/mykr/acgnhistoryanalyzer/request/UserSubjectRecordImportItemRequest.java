package com.mykr.acgnhistoryanalyzer.request;

import com.mykr.acgnhistoryanalyzer.common.enums.RecordStatus;
import jakarta.validation.constraints.*;

public class UserSubjectRecordImportItemRequest {

    @NotBlank(message = "作品主标题不能为空")
    @Size(max = 255, message = "作品主标题不能超过255个字符")
    private String subjectTitleCn;

    @Size(max = 255, message = "作品副标题不能超过255个字符")
    private String subjectSubtitle;

    private Integer subjectSeasonIndex;

    private Integer subjectPartIndex;

    @NotBlank(message = "作品分类不能为空")
    @Size(max = 32, message = "作品分类不能超过32个字符")
    private String subjectCategory;

    @NotNull(message = "记录状态不能为空")
    private RecordStatus recordStatus;

    @Min(value = 0, message = "评分不能小于0")
    @Max(value = 50, message = "评分不能大于50")
    private Integer scoreValue;

    @NotNull(message = "记录年份不能为空")
    @Min(value = 1900, message = "记录年份不合法")
    @Max(value = 2100, message = "记录年份不合法")
    private Integer recordYear;

    @NotNull(message = "记录季度不能为空")
    @Min(value = 1, message = "记录季度只能是1到4")
    @Max(value = 4, message = "记录季度只能是1到4")
    private Integer recordQuarter;

    @Size(max = 500, message = "备注不能超过500个字符")
    private String note;

    public UserSubjectRecordImportItemRequest() {
    }
    
    public String getSubjectCategory() {
        return subjectCategory;
    }

    public RecordStatus getRecordStatus() {
        return recordStatus;
    }

    public Integer getScoreValue() {
        return scoreValue;
    }

    public Integer getRecordYear() {
        return recordYear;
    }

    public Integer getRecordQuarter() {
        return recordQuarter;
    }

    public String getNote() {
        return note;
    }

    public void setSubjectCategory(String subjectCategory) {
        this.subjectCategory = subjectCategory;
    }

    public void setRecordStatus(RecordStatus recordStatus) {
        this.recordStatus = recordStatus;
    }

    public void setScoreValue(Integer scoreValue) {
        this.scoreValue = scoreValue;
    }

    public void setRecordYear(Integer recordYear) {
        this.recordYear = recordYear;
    }

    public void setRecordQuarter(Integer recordQuarter) {
        this.recordQuarter = recordQuarter;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getSubjectTitleCn() {
        return subjectTitleCn;
    }

    public void setSubjectTitleCn(String subjectTitleCn) {
        this.subjectTitleCn = subjectTitleCn;
    }

    public String getSubjectSubtitle() {
        return subjectSubtitle;
    }

    public void setSubjectSubtitle(String subjectSubtitle) {
        this.subjectSubtitle = subjectSubtitle;
    }

    public Integer getSubjectSeasonIndex() {
        return subjectSeasonIndex;
    }

    public void setSubjectSeasonIndex(Integer subjectSeasonIndex) {
        this.subjectSeasonIndex = subjectSeasonIndex;
    }

    public Integer getSubjectPartIndex() {
        return subjectPartIndex;
    }

    public void setSubjectPartIndex(Integer subjectPartIndex) {
        this.subjectPartIndex = subjectPartIndex;
    }

}