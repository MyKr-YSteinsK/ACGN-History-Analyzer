package com.mykr.acgnhistoryanalyzer.llm.cache;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.Optional;

public interface LlmAnalysisResultCacheRepository extends JpaRepository<LlmAnalysisResultCacheEntity, String> {

    Optional<LlmAnalysisResultCacheEntity> findByCacheKeyAndExpiresAtAfter(String cacheKey, Instant now);

    @Modifying
    @Query("delete from LlmAnalysisResultCacheEntity")
    void deleteAllEntries();
}
