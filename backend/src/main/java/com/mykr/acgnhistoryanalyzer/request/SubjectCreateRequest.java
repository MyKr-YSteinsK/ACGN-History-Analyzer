package com.mykr.acgnhistoryanalyzer.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

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

    @NotNull(message = "发行月份档期不能为空")
    @Min(value = 1, message = "发行月份档期只能是1到4：1一月，2四月，3七月，4十月")
    @Max(value = 4, message = "发行月份档期只能是1到4：1一月，2四月，3七月，4十月")
    private Integer releaseQuarter;

    @NotBlank(message = "主类型不能为空")
    @Pattern(regexp = "ANIME|COMIC|NOVEL|GAME", message = "主类型只能是 ANIME、COMIC、NOVEL 或 GAME")
    private String category;

    @Size(max = 255, message = "制作公司名称不能超过255个字符")
    private String studioName;

    @Size(max = 500, message = "平台链接不能超过500个字符")
    private String platformLink;

    @DecimalMin(value = "0.00", message = "平台评分不能小于0")
    @DecimalMax(value = "10.00", message = "平台评分不能大于10")
    private BigDecimal platformScore;

    @Min(value = 0, message = "集数不能小于0")
    private Integer episodeCount;

    @Size(max = 12, message = "单个作品最多设置12个标签")
    private List<@Size(max = 32, message = "单个标签不能超过32个字符") String> tagNames;

    @Size(max = 64, message = "来源平台不能超过64个字符")
    private String sourcePlatform;

    @Size(max = 128, message = "来源ID不能超过128个字符")
    private String sourceId;

    @NotBlank(message = "状态不能为空")
    @Pattern(regexp = "NORMAL|HIDDEN", message = "状态只能是 NORMAL 或 HIDDEN")
    private String status;

    @Size(max = 200, message = "系列名称不能超过200个字符")
    private String franchiseName;

    public SubjectCreateRequest() {
    }

    public String getTitleCn() { return titleCn; }

    public String getSubtitle() { return subtitle; }

    public String getDisplayTitle() { return displayTitle; }

    public Integer getSeasonIndex() { return seasonIndex; }

    public Integer getPartIndex() { return partIndex; }

    public Long getFranchiseId() { return franchiseId; }

    public String getCoverUrl() { return coverUrl; }

    public String getSummary() { return summary; }

    public Integer getReleaseYear() { return releaseYear; }

    public Integer getReleaseQuarter() { return releaseQuarter; }

    public String getCategory() { return category; }

    public String getStudioName() { return studioName; }

    public String getPlatformLink() { return platformLink; }

    public BigDecimal getPlatformScore() { return platformScore; }

    public Integer getEpisodeCount() { return episodeCount; }

    public List<String> getTagNames() { return tagNames; }

    public String getSourcePlatform() { return sourcePlatform; }

    public String getSourceId() { return sourceId; }

    public String getStatus() { return status; }

    public String getFranchiseName() { return franchiseName; }

    public void setTitleCn(String titleCn) { this.titleCn = titleCn; }

    public void setSubtitle(String subtitle) { this.subtitle = subtitle; }

    public void setDisplayTitle(String displayTitle) { this.displayTitle = displayTitle; }

    public void setSeasonIndex(Integer seasonIndex) { this.seasonIndex = seasonIndex; }

    public void setPartIndex(Integer partIndex) { this.partIndex = partIndex; }

    public void setFranchiseId(Long franchiseId) { this.franchiseId = franchiseId; }

    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }

    public void setSummary(String summary) { this.summary = summary; }

    public void setReleaseYear(Integer releaseYear) { this.releaseYear = releaseYear; }

    public void setReleaseQuarter(Integer releaseQuarter) { this.releaseQuarter = releaseQuarter; }

    public void setCategory(String category) { this.category = category; }

    public void setStudioName(String studioName) { this.studioName = studioName; }

    public void setPlatformLink(String platformLink) { this.platformLink = platformLink; }

    public void setPlatformScore(BigDecimal platformScore) { this.platformScore = platformScore; }

    public void setEpisodeCount(Integer episodeCount) { this.episodeCount = episodeCount; }

    public void setTagNames(List<String> tagNames) { this.tagNames = tagNames; }

    public void setSourcePlatform(String sourcePlatform) { this.sourcePlatform = sourcePlatform; }

    public void setSourceId(String sourceId) { this.sourceId = sourceId; }

    public void setStatus(String status) { this.status = status; }

    public void setFranchiseName(String franchiseName) { this.franchiseName = franchiseName; }
}
