package com.mykr.acgnhistoryanalyzer.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "tag",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_tag_normalized_name", columnNames = "normalized_name")
        }
)
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 64)
    private String name;

    @Column(name = "normalized_name", nullable = false, length = 64)
    private String normalizedName;

    @Column(name = "tag_type", nullable = false, length = 32)
    private String tagType;

    @Column(name = "color_key", nullable = false, length = 32)
    private String colorKey;

    @Column(name = "builtin", nullable = false)
    private Boolean builtin;

    @Column(name = "status", nullable = false, length = 32)
    private String status;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Tag() {
    }

    public Tag(String name, String normalizedName, String tagType,
               String colorKey, Boolean builtin, String status) {
        this.name = name;
        this.normalizedName = normalizedName;
        this.tagType = tagType;
        this.colorKey = colorKey;
        this.builtin = builtin;
        this.status = status;
    }

    public Long getId() { return id; }

    public String getName() { return name; }

    public String getNormalizedName() { return normalizedName; }

    public String getTagType() { return tagType; }

    public String getColorKey() { return colorKey; }

    public Boolean getBuiltin() { return builtin; }

    public String getStatus() { return status; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void setName(String name) { this.name = name; }

    public void setNormalizedName(String normalizedName) { this.normalizedName = normalizedName; }

    public void setTagType(String tagType) { this.tagType = tagType; }

    public void setColorKey(String colorKey) { this.colorKey = colorKey; }

    public void setBuiltin(Boolean builtin) { this.builtin = builtin; }

    public void setStatus(String status) { this.status = status; }
}
