package com.mykr.acgnhistoryanalyzer.llm.cache;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "llm_analysis_result_cache")
public class LlmAnalysisResultCacheEntity {

    @Id
    @Column(length = 128, nullable = false)
    private String cacheKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private LlmResultKind resultKind;

    @Lob
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String responseJson;

    @Column(nullable = false)
    private Instant expiresAt;

    public String getCacheKey() {
        return cacheKey;
    }

    public void setCacheKey(String cacheKey) {
        this.cacheKey = cacheKey;
    }

    public LlmResultKind getResultKind() {
        return resultKind;
    }

    public void setResultKind(LlmResultKind resultKind) {
        this.resultKind = resultKind;
    }

    public String getResponseJson() {
        return responseJson;
    }

    public void setResponseJson(String responseJson) {
        this.responseJson = responseJson;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }
}
