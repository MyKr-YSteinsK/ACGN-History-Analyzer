package com.mykr.acgnhistoryanalyzer.request;

import jakarta.validation.constraints.*;

public class UserSubjectRecordImportItemRequest {

    @NotBlank(message = "作品主标题不能为空")
    private String subjectTitleCn;

    private String subjectSubtitle;

    private Integer subjectSeasonIndex;

    private Integer subjectPartIndex;

    @NotBlank(message = "作品类型不能为空")
    @Pattern(regexp = "ANIME|COMIC|NOVEL|GAME", message = "作品类型只能是 ANIME、COMIC、NOVEL 或 GAME")
    private String subjectCategory;

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

    public String getSubjectTitleCn() { return subjectTitleCn; }

    public String getSubjectSubtitle() { return subjectSubtitle; }

    public Integer getSubjectSeasonIndex() { return subjectSeasonIndex; }

    public Integer getSubjectPartIndex() { return subjectPartIndex; }

    public String getSubjectCategory() { return subjectCategory; }

    public Integer getScoreValue() { return scoreValue; }

    public Integer getRecordYear() { return recordYear; }

    public Integer getRecordQuarter() { return recordQuarter; }

    public String getNote() { return note; }

    public void setSubjectTitleCn(String subjectTitleCn) { this.subjectTitleCn = subjectTitleCn; }

    public void setSubjectSubtitle(String subjectSubtitle) { this.subjectSubtitle = subjectSubtitle; }

    public void setSubjectSeasonIndex(Integer subjectSeasonIndex) { this.subjectSeasonIndex = subjectSeasonIndex; }

    public void setSubjectPartIndex(Integer subjectPartIndex) { this.subjectPartIndex = subjectPartIndex; }

    public void setSubjectCategory(String subjectCategory) { this.subjectCategory = subjectCategory; }

    public void setScoreValue(Integer scoreValue) { this.scoreValue = scoreValue; }

    public void setRecordYear(Integer recordYear) { this.recordYear = recordYear; }

    public void setRecordQuarter(Integer recordQuarter) { this.recordQuarter = recordQuarter; }

    public void setNote(String note) { this.note = note; }
}
