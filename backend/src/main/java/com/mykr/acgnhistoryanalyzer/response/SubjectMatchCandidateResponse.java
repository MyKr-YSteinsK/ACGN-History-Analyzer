package com.mykr.acgnhistoryanalyzer.response;

public class SubjectMatchCandidateResponse {

    private Long subjectId;
    private String displayTitle;
    private Integer seasonIndex;
    private Integer partIndex;
    private Integer releaseYear;
    private Integer releaseQuarter;
    private String category;

    public SubjectMatchCandidateResponse(Long subjectId,
                                         String displayTitle,
                                         Integer seasonIndex,
                                         Integer partIndex,
                                         Integer releaseYear,
                                         Integer releaseQuarter,
                                         String category) {
        this.subjectId = subjectId;
        this.displayTitle = displayTitle;
        this.seasonIndex = seasonIndex;
        this.partIndex = partIndex;
        this.releaseYear = releaseYear;
        this.releaseQuarter = releaseQuarter;
        this.category = category;
    }

    public Long getSubjectId() { return subjectId; }

    public String getDisplayTitle() { return displayTitle; }

    public Integer getSeasonIndex() { return seasonIndex; }

    public Integer getPartIndex() { return partIndex; }

    public Integer getReleaseYear() { return releaseYear; }

    public Integer getReleaseQuarter() { return releaseQuarter; }

    public String getCategory() { return category; }
}
