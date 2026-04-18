package com.mykr.acgnhistoryanalyzer.request;

import com.mykr.acgnhistoryanalyzer.common.enums.RecordStatus;
import jakarta.validation.constraints.*;

public class UserSubjectRecordCreateRequest {

    @NotBlank(message = "作品标题不能为空")
    private String subjectTitle;

    @NotNull(message = "记录状态不能为空")
    private RecordStatus recordStatus;

    @Min(value = 10, message = "评分不能小于10")
    @Max(value = 50, message = "评分不能大于50")
    private Integer scoreValue;

    @NotNull(message = "记录年份不能为空")
    @Min(value = 1900, message = "记录年份不合法")
    @Max(value = 2100, message = "记录年份不合法")
    private Integer recordYear;

    @NotBlank(message = "记录季度不能为空")
    @Pattern(regexp = "Q[1-4]", message = "记录季度只能是 Q1、Q2、Q3 或 Q4")
    private String recordQuarter;

    @Size(max = 500, message = "备注不能超过500个字符")
    private String note;

    public UserSubjectRecordCreateRequest() {
    }

    public String getSubjectTitle() {
        return subjectTitle;
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

    public String getRecordQuarter() {
        return recordQuarter;
    }

    public String getNote() {
        return note;
    }

    public void setSubjectTitle(String subjectTitle) {
        this.subjectTitle = subjectTitle;
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

    public void setRecordQuarter(String recordQuarter) {
        this.recordQuarter = recordQuarter;
    }

    public void setNote(String note) {
        this.note = note;
    }
}