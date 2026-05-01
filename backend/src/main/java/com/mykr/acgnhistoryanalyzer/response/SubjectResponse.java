package com.mykr.acgnhistoryanalyzer.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class SubjectResponse {

    private Long id;
    private String titleCn;
    private String subtitle;
    private String displayTitle;
    private Integer seasonIndex;
    private Integer partIndex;
    private Long franchiseId;
    private String coverUrl;
    private String summary;
    private Integer releaseYear;
    private Integer releaseQuarter;
    private String category;
    private String studioName;
    private String platformLink;
    private BigDecimal platformScore;
    private Integer personalScore;
    private Integer episodeCount;
    private List<TagResponse> tagList;
    private String sourcePlatform;
    private String sourceId;
    private String status;
    private String libraryStatus;
    private String libraryStatusLabel;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public SubjectResponse(Long id, String titleCn, String subtitle, String displayTitle,
                           Integer seasonIndex, Integer partIndex, Long franchiseId,
                           String coverUrl, String summary, Integer releaseYear,
                           Integer releaseQuarter, String category, String studioName,
                           String platformLink, BigDecimal platformScore, Integer personalScore, Integer episodeCount,
                           List<TagResponse> tagList, String sourcePlatform,
                           String sourceId, String status,
                           String libraryStatus, String libraryStatusLabel,
                           LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
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
        this.personalScore = personalScore;
        this.episodeCount = episodeCount;
        this.tagList = tagList;
        this.sourcePlatform = sourcePlatform;
        this.sourceId = sourceId;
        this.status = status;
        this.libraryStatus = libraryStatus;
        this.libraryStatusLabel = libraryStatusLabel;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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

    public Integer getPersonalScore() { return personalScore; }

    public Integer getEpisodeCount() { return episodeCount; }

    public List<TagResponse> getTagList() { return tagList; }

    public String getSourcePlatform() { return sourcePlatform; }

    public String getSourceId() { return sourceId; }

    public String getStatus() { return status; }

    public String getLibraryStatus() { return libraryStatus; }

    public String getLibraryStatusLabel() { return libraryStatusLabel; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
