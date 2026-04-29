package com.mykr.acgnhistoryanalyzer.response;

import java.time.LocalDateTime;
import java.util.List;

public record LlmAnalysisResponse(
        String analysisType,
        String title,
        String summary,
        List<String> keywords,
        List<LlmAnalysisSectionResponse> sections,
        List<LlmRecommendationResponse> recommendations,
        List<String> evidence,
        List<String> warnings,
        LlmAnalysisScopeResponse scope,
        boolean mockMode,
        String modelName,
        LocalDateTime generatedAt
) {
}
