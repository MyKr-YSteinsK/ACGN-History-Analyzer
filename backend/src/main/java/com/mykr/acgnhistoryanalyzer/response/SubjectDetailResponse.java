package com.mykr.acgnhistoryanalyzer.response;

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
    private String status;

    private boolean recorded;
    private int recordCount;
    private List<UserSubjectRecordResponse> records;
    private FranchiseSimpleResponse franchise;

    public SubjectDetailResponse(Long id, String titleCn, String subtitle, String displayTitle,
                                 Integer seasonIndex, Integer partIndex, Long franchiseId,
                                 String coverUrl, String summary, Integer releaseYear,
                                 Integer releaseQuarter, String category, String studioName,
                                 String platformLink, String status,
                                 boolean recorded, int recordCount,
                                 List<UserSubjectRecordResponse> records,
                                 FranchiseSimpleResponse franchise){
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
        this.status = status;
        this.recorded = recorded;
        this.recordCount = recordCount;
        this.records = records;
        this.franchise = franchise;
    }

    public Long getId() {
        return id;
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

    public boolean isRecorded() {
        return recorded;
    }

    public int getRecordCount() {
        return recordCount;
    }

    public List<UserSubjectRecordResponse> getRecords() {
        return records;
    }

    public FranchiseSimpleResponse getFranchise() { return franchise; }
}