package com.mykr.acgnhistoryanalyzer.llm;

import java.util.Map;

public record LlmGenerateRequest(
        String analysisType,
        String prompt,
        Map<String, Object> context
) {
}
