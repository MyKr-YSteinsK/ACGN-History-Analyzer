package com.mykr.acgnhistoryanalyzer.entity;

import com.mykr.acgnhistoryanalyzer.common.enums.CollectionType;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "user_subject_collection",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_subject_collection_subject_id",
                        columnNames = "subject_id"
                )
        }
)
public class UserSubjectCollection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "subject_id", nullable = false)
    private Long subjectId;

    @Column(name = "subject_title", nullable = false, length = 255)
    private String subjectTitle;

    @Enumerated(EnumType.STRING)
    @Column(name = "collection_type", nullable = false, length = 32)
    private CollectionType collectionType;

    @Column(name = "note", length = 500)
    private String note;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public UserSubjectCollection() {
    }

    public UserSubjectCollection(Long subjectId, String subjectTitle,
                                 CollectionType collectionType, String note) {
        this.subjectId = subjectId;
        this.subjectTitle = subjectTitle;
        this.collectionType = collectionType;
        this.note = note;
    }

    public Long getId() { return id; }

    public Long getSubjectId() { return subjectId; }

    public String getSubjectTitle() { return subjectTitle; }

    public CollectionType getCollectionType() { return collectionType; }

    public String getNote() { return note; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void setSubjectId(Long subjectId) { this.subjectId = subjectId; }

    public void setSubjectTitle(String subjectTitle) { this.subjectTitle = subjectTitle; }

    public void setCollectionType(CollectionType collectionType) { this.collectionType = collectionType; }

    public void setNote(String note) { this.note = note; }
}
