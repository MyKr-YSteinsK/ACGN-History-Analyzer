package com.mykr.acgnhistoryanalyzer.llm.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mykr.acgnhistoryanalyzer.llm.LlmProperties;
import com.mykr.acgnhistoryanalyzer.response.LlmAnalysisResponse;
import com.mykr.acgnhistoryanalyzer.response.LlmQuestionAnswerResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.Optional;

@Service
public class LlmAnalysisResultCacheService {

    private final LlmAnalysisResultCacheRepository repository;
    private final ObjectMapper objectMapper;
    private final LlmProperties llmProperties;

    public LlmAnalysisResultCacheService(
            LlmAnalysisResultCacheRepository repository,
            ObjectMapper objectMapper,
            LlmProperties llmProperties
    ) {
        this.repository = repository;
        this.objectMapper = objectMapper;
        this.llmProperties = llmProperties;
    }

    @Transactional
    public void clearAll() {
        repository.deleteAllEntries();
    }

    public Optional<LlmAnalysisResponse> getAnalysis(String logicalKey) {
        String cacheKey = buildCacheKey(logicalKey);
        return repository.findByCacheKeyAndExpiresAtAfter(cacheKey, Instant.now())
                .filter(e -> e.getResultKind() == LlmResultKind.ANALYSIS)
                .map(entity -> {
                    try {
                        return objectMapper.readValue(entity.getResponseJson(), LlmAnalysisResponse.class);
                    } catch (Exception ex) {
                        return null;
                    }
                })
                .filter(java.util.Objects::nonNull);
    }

    public void putAnalysis(String logicalKey, LlmAnalysisResponse response) {
        try {
            LlmAnalysisResultCacheEntity entity = new LlmAnalysisResultCacheEntity();
            entity.setCacheKey(buildCacheKey(logicalKey));
            entity.setResultKind(LlmResultKind.ANALYSIS);
            entity.setResponseJson(objectMapper.writeValueAsString(response));
            entity.setExpiresAt(Instant.now().plus(llmProperties.getResultCacheDays(), ChronoUnit.DAYS));
            repository.save(entity);
        } catch (Exception ignored) {
        }
    }

    public Optional<LlmQuestionAnswerResponse> getQuestionAnswer(String logicalKey) {
        String cacheKey = buildCacheKey(logicalKey);
        return repository.findByCacheKeyAndExpiresAtAfter(cacheKey, Instant.now())
                .filter(e -> e.getResultKind() == LlmResultKind.QUESTION_ANSWER)
                .map(entity -> {
                    try {
                        return objectMapper.readValue(entity.getResponseJson(), LlmQuestionAnswerResponse.class);
                    } catch (Exception ex) {
                        return null;
                    }
                })
                .filter(java.util.Objects::nonNull);
    }

    public void putQuestionAnswer(String logicalKey, LlmQuestionAnswerResponse response) {
        try {
            LlmAnalysisResultCacheEntity entity = new LlmAnalysisResultCacheEntity();
            entity.setCacheKey(buildCacheKey(logicalKey));
            entity.setResultKind(LlmResultKind.QUESTION_ANSWER);
            entity.setResponseJson(objectMapper.writeValueAsString(response));
            entity.setExpiresAt(Instant.now().plus(llmProperties.getResultCacheDays(), ChronoUnit.DAYS));
            repository.save(entity);
        } catch (Exception ignored) {
        }
    }

    private String buildCacheKey(String logicalKey) {
        String raw = llmProperties.getCacheContentVersion() + "::" + logicalKey;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException ex) {
            return String.valueOf(raw.hashCode());
        }
    }
}
