package com.mykr.acgnhistoryanalyzer.response;

import java.time.LocalDateTime;

public record LlmQuestionAnswerResponse(
        String question,
        String answer,
        boolean mockMode,
        String modelName,
        LocalDateTime generatedAt
) {
}
