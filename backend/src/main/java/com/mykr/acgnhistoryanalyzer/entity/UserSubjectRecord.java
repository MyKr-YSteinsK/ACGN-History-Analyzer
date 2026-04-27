package com.mykr.acgnhistoryanalyzer.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "user_subject_record",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_subject_record_subject_id",
                        columnNames = "subject_id"
                )
        }
)
public class UserSubjectRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "subject_id", nullable = false)
    private Long subjectId;

    @Column(name = "subject_title", nullable = false, length = 255)
    private String subjectTitle;

    @Column(name = "score_value")
    private Integer scoreValue;

    @Column(name = "record_year", nullable = false)
    private Integer recordYear;

    @Column(name = "record_quarter", nullable = false)
    private Integer recordQuarter;

    @Column(name = "note", length = 500)
    private String note;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public UserSubjectRecord() {
    }

    public UserSubjectRecord(Long subjectId, String subjectTitle,
                             Integer scoreValue, Integer recordYear,
                             Integer recordQuarter, String note) {
        this.subjectId = subjectId;
        this.subjectTitle = subjectTitle;
        this.scoreValue = scoreValue;
        this.recordYear = recordYear;
        this.recordQuarter = recordQuarter;
        this.note = note;
    }

    public Long getId() { return id; }

    public Long getSubjectId() { return subjectId; }

    public String getSubjectTitle() { return subjectTitle; }

    public Integer getScoreValue() { return scoreValue; }

    public Integer getRecordYear() { return recordYear; }

    public Integer getRecordQuarter() { return recordQuarter; }

    public String getNote() { return note; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void setSubjectId(Long subjectId) { this.subjectId = subjectId; }

    public void setSubjectTitle(String subjectTitle) { this.subjectTitle = subjectTitle; }

    public void setScoreValue(Integer scoreValue) { this.scoreValue = scoreValue; }

    public void setRecordYear(Integer recordYear) { this.recordYear = recordYear; }

    public void setRecordQuarter(Integer recordQuarter) { this.recordQuarter = recordQuarter; }

    public void setNote(String note) { this.note = note; }
}
