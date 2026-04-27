package com.mykr.acgnhistoryanalyzer.response;

import java.time.LocalDateTime;

public class UserSubjectCollectionResponse {

    private Long id;
    private Long subjectId;
    private String subjectTitle;
    private String subjectCategory;
    private Integer subjectReleaseYear;
    private Integer subjectReleaseQuarter;
    private String subjectCoverUrl;
    private String collectionType;
    private String collectionTypeLabel;
    private String note;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public UserSubjectCollectionResponse(Long id, Long subjectId, String subjectTitle,
                                         String subjectCategory, Integer subjectReleaseYear,
                                         Integer subjectReleaseQuarter, String subjectCoverUrl,
                                         String collectionType, String collectionTypeLabel,
                                         String note, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.subjectId = subjectId;
        this.subjectTitle = subjectTitle;
        this.subjectCategory = subjectCategory;
        this.subjectReleaseYear = subjectReleaseYear;
        this.subjectReleaseQuarter = subjectReleaseQuarter;
        this.subjectCoverUrl = subjectCoverUrl;
        this.collectionType = collectionType;
        this.collectionTypeLabel = collectionTypeLabel;
        this.note = note;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }

    public Long getSubjectId() { return subjectId; }

    public String getSubjectTitle() { return subjectTitle; }

    public String getSubjectCategory() { return subjectCategory; }

    public Integer getSubjectReleaseYear() { return subjectReleaseYear; }

    public Integer getSubjectReleaseQuarter() { return subjectReleaseQuarter; }

    public String getSubjectCoverUrl() { return subjectCoverUrl; }

    public String getCollectionType() { return collectionType; }

    public String getCollectionTypeLabel() { return collectionTypeLabel; }

    public String getNote() { return note; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
