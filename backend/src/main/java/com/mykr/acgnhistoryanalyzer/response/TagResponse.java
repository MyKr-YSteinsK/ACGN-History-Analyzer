package com.mykr.acgnhistoryanalyzer.response;

import java.time.LocalDateTime;

public class TagResponse {

    private Long id;
    private String name;
    private String tagType;
    private String colorKey;
    private Boolean builtin;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public TagResponse(Long id, String name, String tagType, String colorKey,
                       Boolean builtin, String status,
                       LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.tagType = tagType;
        this.colorKey = colorKey;
        this.builtin = builtin;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }

    public String getName() { return name; }

    public String getTagType() { return tagType; }

    public String getColorKey() { return colorKey; }

    public Boolean getBuiltin() { return builtin; }

    public String getStatus() { return status; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
