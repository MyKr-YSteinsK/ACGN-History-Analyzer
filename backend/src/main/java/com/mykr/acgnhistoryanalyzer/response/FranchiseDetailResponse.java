package com.mykr.acgnhistoryanalyzer.response;

import java.time.LocalDateTime;
import java.util.List;

public class FranchiseDetailResponse {

    private Long id;
    private String name;
    private String nameCn;
    private String description;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<SubjectResponse> subjects;

    public FranchiseDetailResponse(Long id, String name, String nameCn, String description,
                                   String status, LocalDateTime createdAt,
                                   LocalDateTime updatedAt, List<SubjectResponse> subjects) {
        this.id = id;
        this.name = name;
        this.nameCn = nameCn;
        this.description = description;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.subjects = subjects;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getNameCn() {
        return nameCn;
    }

    public String getDescription() {
        return description;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public List<SubjectResponse> getSubjects() {
        return subjects;
    }
}