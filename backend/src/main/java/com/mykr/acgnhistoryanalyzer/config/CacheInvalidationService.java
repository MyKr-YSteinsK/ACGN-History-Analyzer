package com.mykr.acgnhistoryanalyzer.config;

import com.mykr.acgnhistoryanalyzer.llm.cache.LlmAnalysisResultCacheService;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CacheInvalidationService {

    private static final List<String> READ_CACHE_NAMES = List.of(
            "subjectPage",
            "subjectList",
            "homeQuarterDashboard",
            "homeSearch",
            "franchiseList",
            "recordPage",
            "tagList"
    );

    private final CacheManager cacheManager;
    private final LlmAnalysisResultCacheService llmAnalysisResultCacheService;

    public CacheInvalidationService(
            CacheManager cacheManager,
            LlmAnalysisResultCacheService llmAnalysisResultCacheService
    ) {
        this.cacheManager = cacheManager;
        this.llmAnalysisResultCacheService = llmAnalysisResultCacheService;
    }

    @Transactional
    public void invalidateAfterDataMutation() {
        for (String name : READ_CACHE_NAMES) {
            var cache = cacheManager.getCache(name);
            if (cache != null) {
                cache.clear();
            }
        }
        llmAnalysisResultCacheService.clearAll();
    }
}
