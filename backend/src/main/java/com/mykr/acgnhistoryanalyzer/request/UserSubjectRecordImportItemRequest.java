package com.mykr.acgnhistoryanalyzer.request;

import jakarta.validation.constraints.*;

public class UserSubjectRecordImportItemRequest {

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

    @Min(value = 0, message = "评分不能小于0")
    @Max(value = 50, message = "评分不能大于50")
    private Integer scoreValue;

    @NotNull(message = "记录年份不能为空")
    @Min(value = 1900, message = "记录年份不合法")
    @Max(value = 2100, message = "记录年份不合法")
    private Integer recordYear;

    @NotNull(message = "记录月份档期不能为空")
    @Min(value = 1, message = "记录月份档期只能是1到4：1一月，2四月，3七月，4十月")
    @Max(value = 4, message = "记录月份档期只能是1到4：1一月，2四月，3七月，4十月")
    private Integer recordQuarter;

    @Size(max = 500, message = "备注不能超过500个字符")
    private String note;

    public Long getSubjectId() { return subjectId; }

    public String getSubjectTitle() { return subjectTitle; }

    public Integer getSubjectSeasonIndex() { return subjectSeasonIndex; }

    public Integer getSubjectPartIndex() { return subjectPartIndex; }

    public String getSourceId() { return sourceId; }

    public Integer getScoreValue() { return scoreValue; }

    public Integer getRecordYear() { return recordYear; }

    public Integer getRecordQuarter() { return recordQuarter; }

    public String getNote() { return note; }

    public void setSubjectId(Long subjectId) { this.subjectId = subjectId; }

    public void setSubjectTitle(String subjectTitle) { this.subjectTitle = subjectTitle; }

    public void setSubjectSeasonIndex(Integer subjectSeasonIndex) { this.subjectSeasonIndex = subjectSeasonIndex; }

    public void setSubjectPartIndex(Integer subjectPartIndex) { this.subjectPartIndex = subjectPartIndex; }

    public void setSourceId(String sourceId) { this.sourceId = sourceId; }

    public void setScoreValue(Integer scoreValue) { this.scoreValue = scoreValue; }

    public void setRecordYear(Integer recordYear) { this.recordYear = recordYear; }

    public void setRecordQuarter(Integer recordQuarter) { this.recordQuarter = recordQuarter; }

    public void setNote(String note) { this.note = note; }
}
