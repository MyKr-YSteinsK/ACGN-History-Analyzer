package com.mykr.acgnhistoryanalyzer.service;

import com.mykr.acgnhistoryanalyzer.llm.LlmClient;
import com.mykr.acgnhistoryanalyzer.llm.LlmGenerateRequest;
import com.mykr.acgnhistoryanalyzer.llm.LlmPromptService;
import com.mykr.acgnhistoryanalyzer.response.LlmAnalysisResponse;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Service
public class LlmAnalysisService {

    private final LlmClient llmClient;
    private final LlmPromptService llmPromptService;

    public LlmAnalysisService(LlmClient llmClient, LlmPromptService llmPromptService) {
        this.llmClient = llmClient;
        this.llmPromptService = llmPromptService;
    }

    public LlmAnalysisResponse generateYearlySummary(Integer year, String category) {
        Integer effectiveYear = year == null ? LocalDate.now().getYear() : year;
        String normalizedCategory = normalizeBlankToNull(category);
        String prompt = llmPromptService.buildYearlySummaryPrompt(effectiveYear, normalizedCategory);

        Map<String, Object> context = new HashMap<>();
        context.put("year", effectiveYear);
        context.put("category", normalizedCategory);

        return llmClient.generate(new LlmGenerateRequest(
                "YEARLY",
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
}
