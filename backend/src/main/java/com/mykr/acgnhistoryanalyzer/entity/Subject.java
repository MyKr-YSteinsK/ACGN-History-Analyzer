package com.mykr.acgnhistoryanalyzer.entity;

import jakarta.persistence.*;

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

    @Column(name = "display_title", length = 500)
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

    @Column(name = "release_year")
    private Integer releaseYear;

    @Column(name = "release_month")
    private Integer releaseMonth;

    @Column(name = "category", nullable = false, length = 32)
    private String category;

    @Column(name = "studio_name", length = 255)
    private String studioName;

    @Column(name = "platform_link", length = 500)
    private String platformLink;

    @Column(name = "status", nullable = false, length = 32)
    private String status;

    public Subject() {
    }

    public Subject(String titleCn, String subtitle, String displayTitle,
                   Integer seasonIndex, Integer partIndex, Long franchiseId,
                   String coverUrl, String summary, Integer releaseYear,
                   Integer releaseMonth, String category, String studioName,
                   String platformLink, String status) {
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

    public void setReleaseMonth(Integer releaseMonth) {
        this.releaseMonth = releaseMonth;
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