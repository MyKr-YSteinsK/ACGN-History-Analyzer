package com.mykr.acgnhistoryanalyzer.response;

import java.math.BigDecimal;
import java.util.List;

public record LlmRecommendationResponse(
        Long subjectId,
        String title,
        String coverUrl,
        String category,
        List<String> tags,
        String franchiseName,
        String source,
        String reason,
        String risk,
        String evidence,
        String priority,
        Integer matchScore,
        BigDecimal platformScore
) {
}
