package com.mykr.acgnhistoryanalyzer.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class SubjectDetailResponse {

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
    private Integer episodeCount;
    private String tags;
    private String sourcePlatform;
    private String sourceId;
    private String status;
    private String libraryStatus;
    private String libraryStatusLabel;
    private boolean recorded;
    private int recordCount;
    private List<UserSubjectRecordResponse> records;
    private UserSubjectRecordResponse record;
    private UserSubjectCollectionResponse collection;
    private FranchiseSimpleResponse franchise;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public SubjectDetailResponse(Long id, String titleCn, String subtitle, String displayTitle,
                                 Integer seasonIndex, Integer partIndex, Long franchiseId,
                                 String coverUrl, String summary, Integer releaseYear,
                                 Integer releaseQuarter, String category, String studioName,
                                 String platformLink, BigDecimal platformScore, Integer episodeCount,
                                 String tags, String sourcePlatform, String sourceId, String status,
                                 String libraryStatus, String libraryStatusLabel,
                                 boolean recorded, int recordCount,
                                 List<UserSubjectRecordResponse> records,
                                 UserSubjectRecordResponse record,
                                 UserSubjectCollectionResponse collection,
                                 FranchiseSimpleResponse franchise,
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
        this.episodeCount = episodeCount;
        this.tags = tags;
        this.sourcePlatform = sourcePlatform;
        this.sourceId = sourceId;
        this.status = status;
        this.libraryStatus = libraryStatus;
        this.libraryStatusLabel = libraryStatusLabel;
        this.recorded = recorded;
        this.recordCount = recordCount;
        this.records = records;
        this.record = record;
        this.collection = collection;
        this.franchise = franchise;
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

    public Integer getEpisodeCount() { return episodeCount; }

    public String getTags() { return tags; }

    public String getSourcePlatform() { return sourcePlatform; }

    public String getSourceId() { return sourceId; }

    public String getStatus() { return status; }

    public String getLibraryStatus() { return libraryStatus; }

    public String getLibraryStatusLabel() { return libraryStatusLabel; }

    public boolean isRecorded() { return recorded; }

    public int getRecordCount() { return recordCount; }

    public List<UserSubjectRecordResponse> getRecords() { return records; }

    public UserSubjectRecordResponse getRecord() { return record; }

    public UserSubjectCollectionResponse getCollection() { return collection; }

    public FranchiseSimpleResponse getFranchise() { return franchise; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
