package com.mykr.acgnhistoryanalyzer.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class BangumiSeasonImportRequest {

    @NotNull(message = "年份不能为空")
    @Min(value = 1900, message = "年份不合法")
    @Max(value = 2100, message = "年份不合法")
    private Integer year;

    @NotNull(message = "月份档期不能为空")
    @Min(value = 1, message = "月份档期只能是1到4：1一月，2四月，3七月，4十月")
    @Max(value = 4, message = "月份档期只能是1到4：1一月，2四月，3七月，4十月")
    private Integer quarter;

    private Boolean includeWeb = false;

    private Boolean japaneseOnly = true;

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

    public Boolean getIncludeWeb() {
        return includeWeb;
    }

    public void setIncludeWeb(Boolean includeWeb) {
        this.includeWeb = includeWeb;
    }

    public Boolean getJapaneseOnly() {
        return japaneseOnly;
    }

    public void setJapaneseOnly(Boolean japaneseOnly) {
        this.japaneseOnly = japaneseOnly;
    }
}
