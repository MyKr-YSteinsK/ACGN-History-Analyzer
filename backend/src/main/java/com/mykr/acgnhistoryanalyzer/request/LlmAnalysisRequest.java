package com.mykr.acgnhistoryanalyzer.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public class LlmAnalysisRequest {

    @Min(value = 1900, message = "年份不能小于1900")
    @Max(value = 2100, message = "年份不能大于2100")
    private Integer year;

    @Min(value = 1, message = "月份档期只能是1到4")
    @Max(value = 4, message = "月份档期只能是1到4")
    private Integer quarter;

    @Size(max = 32, message = "类型不能超过32个字符")
    private String category;

    @Size(max = 2000, message = "问题不能超过2000个字符")
    private String question;

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Integer getQuarter() {
        return quarter;
    }

    public void setQuarter(Integer quarter) {
        this.quarter = quarter;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }
}
