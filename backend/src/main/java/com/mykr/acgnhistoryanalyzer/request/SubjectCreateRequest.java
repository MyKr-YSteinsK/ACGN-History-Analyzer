package com.mykr.acgnhistoryanalyzer.request;

import jakarta.validation.constraints.*;

public class SubjectCreateRequest {

    @NotBlank(message = "中文标题不能为空")
    @Size(max = 255, message = "中文标题不能超过255个字符")
    private String titleCn;

    @Size(max = 255, message = "副标题不能超过255个字符")
    private String subtitle;

    @NotBlank(message = "展示标题不能为空")
    @Size(max = 500, message = "展示标题不能超过500个字符")
    private String displayTitle;

    @Min(value = 1, message = "季序号必须大于等于1")
    private Integer seasonIndex;

    @Min(value = 1, message = "Part序号必须大于等于1")
    private Integer partIndex;

    @Positive(message = "系列ID必须大于0")
    private Long franchiseId;

    @Size(max = 500, message = "封面地址不能超过500个字符")
    private String coverUrl;

    @Size(max = 2000, message = "简介不能超过2000个字符")
    private String summary;

    @NotNull(message = "发行年份不能为空")
    @Min(value = 1900, message = "发行年份不合法")
    @Max(value = 2100, message = "发行年份不合法")
    private Integer releaseYear;

    @NotNull(message = "发行季度不能为空")
    @Min(value = 1, message = "发行季度只能是1到4")
    @Max(value = 4, message = "发行季度只能是1到4")
    private Integer releaseQuarter;

    @NotBlank(message = "主类型不能为空")
    @Pattern(regexp = "ANIME|COMIC|NOVEL|GAME", message = "主类型只能是 ANIME、COMIC、NOVEL 或 GAME")
    private String category;

    @Size(max = 255, message = "制作公司名称不能超过255个字符")
    private String studioName;

    @Size(max = 500, message = "平台链接不能超过500个字符")
    private String platformLink;

    @NotBlank(message = "状态不能为空")
    @Pattern(regexp = "NORMAL|HIDDEN", message = "状态只能是 NORMAL 或 HIDDEN")
    private String status;

    public SubjectCreateRequest() {
    }

    public String getTitleCn() {
        return titleCn;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public String getDisplayTitle() {
        return displayTitle;
    }

    public Integer getSeasonIndex() {
        return seasonIndex;
    }

    public Integer getPartIndex() {
        return partIndex;
    }

    public Long getFranchiseId() {
        return franchiseId;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public String getSummary() {
        return summary;
    }

    public Integer getReleaseYear() {
        return releaseYear;
    }

    public Integer getReleaseQuarter() {
        return releaseQuarter;
    }

    public String getCategory() {
        return category;
    }

    public String getStudioName() {
        return studioName;
    }

    public String getPlatformLink() {
        return platformLink;
    }

    public String getStatus() {
        return status;
    }

    public void setTitleCn(String titleCn) {
        this.titleCn = titleCn;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public void setDisplayTitle(String displayTitle) {
        this.displayTitle = displayTitle;
    }

    public void setSeasonIndex(Integer seasonIndex) {
        this.seasonIndex = seasonIndex;
    }

    public void setPartIndex(Integer partIndex) {
        this.partIndex = partIndex;
    }

    public void setFranchiseId(Long franchiseId) {
        this.franchiseId = franchiseId;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public void setReleaseYear(Integer releaseYear) {
        this.releaseYear = releaseYear;
    }

    public void setReleaseQuarter(Integer releaseQuarter) {
        this.releaseQuarter = releaseQuarter;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setStudioName(String studioName) {
        this.studioName = studioName;
    }

    public void setPlatformLink(String platformLink) {
        this.platformLink = platformLink;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}