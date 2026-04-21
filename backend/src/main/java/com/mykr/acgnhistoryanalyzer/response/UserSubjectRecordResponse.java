package com.mykr.acgnhistoryanalyzer.response;

public class UserSubjectRecordResponse {

    private Long id;
    private Long subjectId;
    private String subjectTitle;
    private String subjectCategory;
    private Integer subjectReleaseYear;
    private Integer subjectReleaseQuarter;
    private String subjectCoverUrl;
    private String recordStatus;
    private String recordStatusLabel;
    private Integer scoreValue;
    private Integer recordYear;
    private Integer recordQuarter;
    private String note;

    public UserSubjectRecordResponse(Long id, Long subjectId, String subjectTitle,
                                     String subjectCategory, Integer subjectReleaseYear,
                                     Integer subjectReleaseQuarter, String subjectCoverUrl,
                                     String recordStatus, String recordStatusLabel,
                                     Integer scoreValue, Integer recordYear,
                                     Integer recordQuarter, String note) {
        this.id = id;
        this.subjectId = subjectId;
        this.subjectTitle = subjectTitle;
        this.subjectCategory = subjectCategory;
        this.subjectReleaseYear = subjectReleaseYear;
        this.subjectReleaseQuarter = subjectReleaseQuarter;
        this.subjectCoverUrl = subjectCoverUrl;
        this.recordStatus = recordStatus;
        this.recordStatusLabel = recordStatusLabel;
        this.scoreValue = scoreValue;
        this.recordYear = recordYear;
        this.recordQuarter = recordQuarter;
        this.note = note;
    }

    public Long getId() {
        return id;
    }

    public Long getSubjectId() {
        return subjectId;
    }

    public String getSubjectTitle() {
        return subjectTitle;
    }

    public String getSubjectCategory() {
        return subjectCategory;
    }

    public Integer getSubjectReleaseYear() {
        return subjectReleaseYear;
    }

    public Integer getSubjectReleaseQuarter() {
        return subjectReleaseQuarter;
    }

    public String getSubjectCoverUrl() {
        return subjectCoverUrl;
    }

    public String getRecordStatus() {
        return recordStatus;
    }

    public String getRecordStatusLabel() {
        return recordStatusLabel;
    }

    public Integer getScoreValue() {
        return scoreValue;
    }

    public Integer getRecordYear() {
        return recordYear;
    }

    public Integer getRecordQuarter() {
        return recordQuarter;
    }

    public String getNote() {
        return note;
    }
}