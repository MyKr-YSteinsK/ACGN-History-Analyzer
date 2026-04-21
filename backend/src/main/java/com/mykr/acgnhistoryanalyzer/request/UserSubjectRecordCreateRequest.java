package com.mykr.acgnhistoryanalyzer.request;

import com.mykr.acgnhistoryanalyzer.common.enums.RecordStatus;
import jakarta.validation.constraints.*;

public class UserSubjectRecordCreateRequest {

    @NotNull(message = "作品ID不能为空")
    @Positive(message = "作品ID必须大于0")
    private Long subjectId;

    @NotNull(message = "记录状态不能为空")
    private RecordStatus recordStatus;

    @Min(value = 20, message = "评分不能小于20")
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

    public UserSubjectRecordCreateRequest() {
    }

    public Long getSubjectId() {
        return subjectId;
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

    public void setSubjectId(Long subjectId) {
        this.subjectId = subjectId;
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
}