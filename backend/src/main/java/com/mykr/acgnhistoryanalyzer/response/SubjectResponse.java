package com.mykr.acgnhistoryanalyzer.response;

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
    private Integer releaseMonth;
    private String category;
    private String studioName;
    private String platformLink;
    private String status;

    public SubjectResponse(Long id, String titleCn, String subtitle, String displayTitle,
                           Integer seasonIndex, Integer partIndex, Long franchiseId,
                           String coverUrl, String summary, Integer releaseYear,
                           Integer releaseMonth, String category, String studioName,
                           String platformLink, String status) {
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
        this.releaseMonth = releaseMonth;
        this.category = category;
        this.studioName = studioName;
        this.platformLink = platformLink;
        this.status = status;
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

    public Integer getReleaseMonth() {
        return releaseMonth;
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
}