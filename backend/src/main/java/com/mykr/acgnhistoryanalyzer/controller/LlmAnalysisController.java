package com.mykr.acgnhistoryanalyzer.controller;

import com.mykr.acgnhistoryanalyzer.common.response.ApiResponse;
import com.mykr.acgnhistoryanalyzer.llm.LlmProperties;
import com.mykr.acgnhistoryanalyzer.response.LlmAnalysisResponse;
import com.mykr.acgnhistoryanalyzer.response.LlmStatusResponse;
import com.mykr.acgnhistoryanalyzer.service.LlmAnalysisService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/llm")
public class LlmAnalysisController {

    private final LlmAnalysisService llmAnalysisService;
    private final LlmProperties llmProperties;

    public LlmAnalysisController(
            LlmAnalysisService llmAnalysisService,
            LlmProperties llmProperties
    ) {
        this.llmAnalysisService = llmAnalysisService;
        this.llmProperties = llmProperties;
    }

    @GetMapping("/status")
    public ApiResponse<LlmStatusResponse> getStatus() {
        return ApiResponse.success(new LlmStatusResponse(
                llmProperties.isEnabled(),
                llmProperties.getProvider(),
                llmProperties.getModel(),
                llmProperties.isMockMode()
        ));
    }

    @GetMapping("/analysis/yearly")
    public ApiResponse<LlmAnalysisResponse> generateYearlySummary(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String category
    ) {
        return ApiResponse.success(llmAnalysisService.generateYearlySummary(year, category));
    }

    @GetMapping("/analysis/quarterly")
    public ApiResponse<LlmAnalysisResponse> generateQuarterlySummary(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer quarter,
            @RequestParam(required = false) String category
    ) {
        return ApiResponse.success(
                llmAnalysisService.generateQuarterlySummary(year, quarter, category)
        );
    }

    @GetMapping("/analysis/type-preference")
    public ApiResponse<LlmAnalysisResponse> generateTypePreference(
            @RequestParam(required = false) String category
    ) {
        return ApiResponse.success(llmAnalysisService.generateTypePreference(category));
    }

    @GetMapping("/analysis/score-structure")
    public ApiResponse<LlmAnalysisResponse> generateScoreStructure(
            @RequestParam(required = false) String category
    ) {
        return ApiResponse.success(llmAnalysisService.generateScoreStructure(category));
    }

    @GetMapping("/analysis/tag-preference")
    public ApiResponse<LlmAnalysisResponse> generateTagPreference(
            @RequestParam(required = false) String category
    ) {
        return ApiResponse.success(llmAnalysisService.generateTagPreference(category));
    }

    @GetMapping("/analysis/recommendations")
    public ApiResponse<LlmAnalysisResponse> generateRecommendations(
            @RequestParam(required = false) String category
    ) {
        return ApiResponse.success(llmAnalysisService.generateRecommendations(category));
    }

    @GetMapping("/analysis/collection-backlog")
    public ApiResponse<LlmAnalysisResponse> generateCollectionBacklog(
            @RequestParam(required = false) String category
    ) {
        return ApiResponse.success(llmAnalysisService.generateCollectionBacklog(category));
    }

    @GetMapping("/analysis/taste-profile")
    public ApiResponse<LlmAnalysisResponse> generateTasteProfile(
            @RequestParam(required = false) String category
    ) {
        return ApiResponse.success(llmAnalysisService.generateTasteProfile(category));
    }

    @GetMapping("/analysis/preference-contradictions")
    public ApiResponse<LlmAnalysisResponse> generatePreferenceContradictions(
            @RequestParam(required = false) String category
    ) {
        return ApiResponse.success(llmAnalysisService.generatePreferenceContradictions(category));
    }

    @GetMapping("/analysis/comprehensive-report")
    public ApiResponse<LlmAnalysisResponse> generateComprehensiveReport(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String category
    ) {
        return ApiResponse.success(llmAnalysisService.generateComprehensiveReport(year, category));
    }
}
