package com.mykr.acgnhistoryanalyzer.service;

import com.mykr.acgnhistoryanalyzer.llm.LlmClient;
import com.mykr.acgnhistoryanalyzer.llm.LlmContextBuilder;
import com.mykr.acgnhistoryanalyzer.llm.LlmGenerateRequest;
import com.mykr.acgnhistoryanalyzer.llm.LlmPromptService;
import com.mykr.acgnhistoryanalyzer.response.LlmAnalysisResponse;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Map;

@Service
public class LlmAnalysisService {

    private final LlmClient llmClient;
    private final LlmPromptService llmPromptService;
    private final LlmContextBuilder llmContextBuilder;

    public LlmAnalysisService(
            LlmClient llmClient,
            LlmPromptService llmPromptService,
            LlmContextBuilder llmContextBuilder
    ) {
        this.llmClient = llmClient;
        this.llmPromptService = llmPromptService;
        this.llmContextBuilder = llmContextBuilder;
    }

    public LlmAnalysisResponse generateYearlySummary(Integer year, String category) {
        Integer effectiveYear = year == null ? LocalDate.now().getYear() : year;
        String normalizedCategory = normalizeBlankToNull(category);
        String prompt = llmPromptService.buildYearlySummaryPrompt(effectiveYear, normalizedCategory);

        Map<String, Object> context = llmContextBuilder.buildYearlyContext(effectiveYear, normalizedCategory);

        return llmClient.generate(new LlmGenerateRequest(
                "YEARLY",
                prompt,
                context
        ));
    }

    public LlmAnalysisResponse generateQuarterlySummary(Integer year, Integer quarter, String category) {
        Integer effectiveYear = year == null ? LocalDate.now().getYear() : year;
        Integer effectiveQuarter = normalizeQuarter(quarter);
        String normalizedCategory = normalizeBlankToNull(category);
        String prompt = llmPromptService.buildQuarterlySummaryPrompt(
                effectiveYear,
                effectiveQuarter,
                normalizedCategory
        );

        Map<String, Object> context = llmContextBuilder.buildQuarterlyContext(
                effectiveYear,
                effectiveQuarter,
                normalizedCategory
        );

        return llmClient.generate(new LlmGenerateRequest(
                "QUARTERLY",
                prompt,
                context
        ));
    }

    public LlmAnalysisResponse generateTypePreference(String category) {
        String normalizedCategory = normalizeBlankToNull(category);
        if (normalizedCategory == null) {
            normalizedCategory = "ANIME";
        }

        String prompt = llmPromptService.buildTypePreferencePrompt(normalizedCategory);
        Map<String, Object> context = llmContextBuilder.buildTypePreferenceContext(normalizedCategory);

        return llmClient.generate(new LlmGenerateRequest(
                "TYPE_PREFERENCE",
                prompt,
                context
        ));
    }

    public LlmAnalysisResponse generateScoreStructure(String category) {
        String normalizedCategory = normalizeBlankToNull(category);
        String prompt = llmPromptService.buildScoreStructurePrompt(normalizedCategory);
        Map<String, Object> context = llmContextBuilder.buildScoreStructureContext(normalizedCategory);

        return llmClient.generate(new LlmGenerateRequest(
                "SCORE_STRUCTURE",
                prompt,
                context
        ));
    }

    public LlmAnalysisResponse generateTagPreference(String category) {
        String normalizedCategory = normalizeBlankToNull(category);
        String prompt = llmPromptService.buildTagPreferencePrompt(normalizedCategory);
        Map<String, Object> context = llmContextBuilder.buildTagPreferenceContext(normalizedCategory);

        return llmClient.generate(new LlmGenerateRequest(
                "TAG_PREFERENCE",
                prompt,
                context
        ));
    }

    public LlmAnalysisResponse generateRecommendations(String category) {
        String normalizedCategory = normalizeBlankToNull(category);
        String prompt = llmPromptService.buildRecommendationPrompt(normalizedCategory);
        Map<String, Object> context = llmContextBuilder.buildRecommendationContext(normalizedCategory);

        return llmClient.generate(new LlmGenerateRequest(
                "RECOMMENDATION",
                prompt,
                context
        ));
    }

    public LlmAnalysisResponse generateCollectionBacklog(String category) {
        String normalizedCategory = normalizeBlankToNull(category);
        String prompt = llmPromptService.buildCollectionBacklogPrompt(normalizedCategory);
        Map<String, Object> context = llmContextBuilder.buildCollectionBacklogContext(normalizedCategory);

        return llmClient.generate(new LlmGenerateRequest(
                "COLLECTION_BACKLOG",
                prompt,
                context
        ));
    }

    public LlmAnalysisResponse generateTasteProfile(String category) {
        String normalizedCategory = normalizeBlankToNull(category);
        String prompt = llmPromptService.buildTasteProfilePrompt(normalizedCategory);
        Map<String, Object> context = llmContextBuilder.buildTasteProfileContext(normalizedCategory);

        return llmClient.generate(new LlmGenerateRequest(
                "TASTE_PROFILE",
                prompt,
                context
        ));
    }

    public LlmAnalysisResponse generatePreferenceContradictions(String category) {
        String normalizedCategory = normalizeBlankToNull(category);
        String prompt = llmPromptService.buildPreferenceContradictionPrompt(normalizedCategory);
        Map<String, Object> context = llmContextBuilder.buildPreferenceContradictionContext(normalizedCategory);

        return llmClient.generate(new LlmGenerateRequest(
                "PREFERENCE_CONTRADICTION",
                prompt,
                context
        ));
    }

    public LlmAnalysisResponse generateComprehensiveReport(Integer year, String category) {
        Integer effectiveYear = year == null ? LocalDate.now().getYear() : year;
        String normalizedCategory = normalizeBlankToNull(category);
        String prompt = llmPromptService.buildComprehensiveReportPrompt(effectiveYear, normalizedCategory);
        Map<String, Object> context = llmContextBuilder.buildComprehensiveReportContext(
                effectiveYear,
                normalizedCategory
        );

        return llmClient.generate(new LlmGenerateRequest(
                "COMPREHENSIVE_REPORT",
                prompt,
                context
        ));
    }

    private String normalizeBlankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim().toUpperCase();
    }

    private Integer normalizeQuarter(Integer quarter) {
        if (quarter == null || quarter < 1 || quarter > 4) {
            return 1;
        }

        return quarter;
    }
}
