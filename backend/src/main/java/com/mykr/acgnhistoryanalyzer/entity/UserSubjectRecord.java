package com.mykr.acgnhistoryanalyzer.entity;

import com.mykr.acgnhistoryanalyzer.common.enums.RecordStatus;
import jakarta.persistence.*;

@Entity
@Table(name = "user_subject_record")
public class UserSubjectRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "subject_id")
    private Long subjectId;

    @Column(name = "subject_title", nullable = false, length = 255)
    private String subjectTitle;

    @Enumerated(EnumType.STRING)
    @Column(name = "record_status", nullable = false, length = 32)
    private RecordStatus recordStatus;

    @Column(name = "score_value")
    private Integer scoreValue;

    @Column(name = "record_year")
    private Integer recordYear;

    @Column(name = "record_quarter", length = 8)
    private String recordQuarter;

    @Column(name = "note", length = 500)
    private String note;

    public UserSubjectRecord() {
    }

    public UserSubjectRecord(Long subjectId, String subjectTitle, RecordStatus recordStatus,
                             Integer scoreValue, Integer recordYear, String recordQuarter, String note) {
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

    public String getRecordQuarter() {
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

    public void setRecordQuarter(String recordQuarter) {
        this.recordQuarter = recordQuarter;
    }

    public void setNote(String note) {
        this.note = note;
    }
}