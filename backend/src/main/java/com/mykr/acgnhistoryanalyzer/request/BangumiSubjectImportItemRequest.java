package com.mykr.acgnhistoryanalyzer.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class BangumiSubjectImportItemRequest {

    @NotBlank(message = "sourceId 不能为空")
    @Size(max = 128, message = "sourceId 不能超过128个字符")
    private String sourceId;

    @NotNull(message = "releaseYear 不能为空")
    @Min(value = 1900, message = "releaseYear 不合法")
    @Max(value = 2100, message = "releaseYear 不合法")
    private Integer releaseYear;

    @NotNull(message = "releaseQuarter 不能为空")
    @Min(value = 1, message = "releaseQuarter 只能是1到4：1一月，2四月，3七月，4十月")
    @Max(value = 4, message = "releaseQuarter 只能是1到4：1一月，2四月，3七月，4十月")
    private Integer releaseQuarter;

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public Integer getReleaseYear() {
        return releaseYear;
    }

    public void setReleaseYear(Integer releaseYear) {
        this.releaseYear = releaseYear;
    }

    public Integer getReleaseQuarter() {
        return releaseQuarter;
    }

    public void setReleaseQuarter(Integer releaseQuarter) {
        this.releaseQuarter = releaseQuarter;
    }
}
