package com.mykr.acgnhistoryanalyzer.entity;

import com.mykr.acgnhistoryanalyzer.common.enums.RecordStatus;
import jakarta.persistence.*;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "record_status", nullable = false, length = 32)
    private RecordStatus recordStatus;

    @Column(name = "score_value")
    private Integer scoreValue;

    @Column(name = "record_year", nullable = false)
    private Integer recordYear;

    @Column(name = "record_quarter", nullable = false)
    private Integer recordQuarter;

    @Column(name = "note", length = 500)
    private String note;

    public UserSubjectRecord() {
    }

    public UserSubjectRecord(Long subjectId, String subjectTitle, RecordStatus recordStatus,
                             Integer scoreValue, Integer recordYear, Integer recordQuarter, String note) {
        this.subjectId = subjectId;
        this.subjectTitle = subjectTitle;
        this.recordStatus = recordStatus;
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

    public RecordStatus getRecordStatus() {
        return recordStatus;
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

    public void setSubjectId(Long subjectId) {
        this.subjectId = subjectId;
    }

    public void setSubjectTitle(String subjectTitle) {
        this.subjectTitle = subjectTitle;
    }

    public void setRecordStatus(RecordStatus recordStatus) {
        this.recordStatus = recordStatus;
    }

    public void setScoreValue(Integer scoreValue) {
        this.scoreValue = scoreValue;
    }

    public void setRecordYear(Integer recordYear) {
        this.recordYear = recordYear;
    }

    public void setRecordQuarter(Integer recordQuarter) {
        this.recordQuarter = recordQuarter;
    }

    public void setNote(String note) {
        this.note = note;
    }
}