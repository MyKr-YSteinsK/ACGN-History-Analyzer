package com.mykr.acgnhistoryanalyzer.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "subject")
public class Subject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title_cn", nullable = false, length = 255)
    private String titleCn;

    @Column(name = "subtitle", length = 255)
    private String subtitle;

    @Column(name = "display_title", nullable = false, length = 500)
    private String displayTitle;

    @Column(name = "season_index")
    private Integer seasonIndex;

    @Column(name = "part_index")
    private Integer partIndex;

    @Column(name = "franchise_id")
    private Long franchiseId;

    @Column(name = "cover_url", length = 500)
    private String coverUrl;

    @Column(name = "summary", length = 2000)
    private String summary;

    @Column(name = "release_year", nullable = false)
    private Integer releaseYear;

    @Column(name = "release_quarter", nullable = false)
    private Integer releaseQuarter;

    @Column(name = "category", nullable = false, length = 32)
    private String category;

    @Column(name = "studio_name", length = 255)
    private String studioName;

    @Column(name = "platform_link", length = 500)
    private String platformLink;

    @Column(name = "platform_score", precision = 4, scale = 2)
    private BigDecimal platformScore;

    @Column(name = "episode_count")
    private Integer episodeCount;

    /**
     * 当前先用逗号分隔字符串保存标签，避免过早拆出标签表。
     * 例如：战斗,奇幻,校园
     */
    @Column(name = "tags", length = 1000)
    private String tags;

    @Column(name = "source_platform", length = 64)
    private String sourcePlatform;

    @Column(name = "source_id", length = 128)
    private String sourceId;

    @Column(name = "status", nullable = false, length = 32)
    private String status;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Subject() {
    }

    public Subject(String titleCn, String subtitle, String displayTitle,
                   Integer seasonIndex, Integer partIndex, Long franchiseId,
                   String coverUrl, String summary, Integer releaseYear,
                   Integer releaseQuarter, String category, String studioName,
                   String platformLink, BigDecimal platformScore, Integer episodeCount,
                   String tags, String sourcePlatform, String sourceId, String status) {
        this.titleCn = titleCn;
        this.subtitle = subtitle;
        this.displayTitle = displayTitle;
        this.seasonIndex = seasonIndex;
        this.partIndex = partIndex;
        this.franchiseId = franchiseId;
        this.coverUrl = coverUrl;
        this.summary = summary;
        this.releaseYear = releaseYear;
        this.releaseQuarter = releaseQuarter;
        this.category = category;
        this.studioName = studioName;
        this.platformLink = platformLink;
        this.platformScore = platformScore;
        this.episodeCount = episodeCount;
        this.tags = tags;
        this.sourcePlatform = sourcePlatform;
        this.sourceId = sourceId;
        this.status = status;
    }

    public Long getId() { return id; }

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

    public String getTags() { return tags; }

    public String getSourcePlatform() { return sourcePlatform; }

    public String getSourceId() { return sourceId; }

    public String getStatus() { return status; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }

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

    public void setTags(String tags) { this.tags = tags; }

    public void setSourcePlatform(String sourcePlatform) { this.sourcePlatform = sourcePlatform; }

    public void setSourceId(String sourceId) { this.sourceId = sourceId; }

    public void setStatus(String status) { this.status = status; }
}
