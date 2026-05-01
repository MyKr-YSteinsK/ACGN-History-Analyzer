package com.mykr.acgnhistoryanalyzer.controller;

import com.mykr.acgnhistoryanalyzer.common.response.ApiResponse;
import com.mykr.acgnhistoryanalyzer.llm.LlmProperties;
import com.mykr.acgnhistoryanalyzer.request.LlmAnalysisRequest;
import com.mykr.acgnhistoryanalyzer.request.LlmConfigUpdateRequest;
import com.mykr.acgnhistoryanalyzer.response.LlmAnalysisResponse;
import com.mykr.acgnhistoryanalyzer.response.LlmConnectionTestResponse;
import com.mykr.acgnhistoryanalyzer.response.LlmQuestionAnswerResponse;
import com.mykr.acgnhistoryanalyzer.response.LlmStatusResponse;
import com.mykr.acgnhistoryanalyzer.service.LlmAnalysisService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
        return ApiResponse.success(buildStatusResponse());
    }

    @PostMapping("/config")
    public ApiResponse<LlmStatusResponse> updateConfig(@Valid @RequestBody LlmConfigUpdateRequest request) {
        try {
            if (request.isPreviewMode()) {
                llmProperties.switchToMockPreview();
            } else {
                llmProperties.switchToDeepSeek(
                        request.getBaseUrl(),
                        request.getApiKey(),
                        request.getModel(),
                        request.getTimeoutSeconds(),
                        request.getTemperature(),
                        request.getMaxTokens()
                );
            }

            return ApiResponse.success(buildStatusResponse());
        } catch (IllegalStateException error) {
            return ApiResponse.fail(4002, error.getMessage());
        }
    }

    @PostMapping("/test-connection")
    public ApiResponse<LlmConnectionTestResponse> testConnection() {
        return ApiResponse.success(llmAnalysisService.testConnection());
    }

    @GetMapping("/analysis/yearly")
    public ApiResponse<LlmAnalysisResponse> generateYearlySummary(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String category
    ) {
        return ApiResponse.success(llmAnalysisService.generateYearlySummary(year, category));
    }

    @PostMapping("/analysis/yearly")
    public ApiResponse<LlmAnalysisResponse> generateYearlySummaryPost(
            @Valid @RequestBody LlmAnalysisRequest request
    ) {
        return ApiResponse.success(llmAnalysisService.generateYearlySummary(request.getYear(), request.getCategory()));
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

    @PostMapping("/analysis/quarterly")
    public ApiResponse<LlmAnalysisResponse> generateQuarterlySummaryPost(
            @Valid @RequestBody LlmAnalysisRequest request
    ) {
        return ApiResponse.success(
                llmAnalysisService.generateQuarterlySummary(
                        request.getYear(),
                        request.getQuarter(),
                        request.getCategory()
                )
        );
    }

    @GetMapping("/analysis/type-preference")
    public ApiResponse<LlmAnalysisResponse> generateTypePreference(
            @RequestParam(required = false) String category
    ) {
        return ApiResponse.success(llmAnalysisService.generateTypePreference(category));
    }

    @PostMapping("/analysis/type-preference")
    public ApiResponse<LlmAnalysisResponse> generateTypePreferencePost(
            @Valid @RequestBody LlmAnalysisRequest request
    ) {
        return ApiResponse.success(llmAnalysisService.generateTypePreference(request.getCategory()));
    }

    @GetMapping("/analysis/score-structure")
    public ApiResponse<LlmAnalysisResponse> generateScoreStructure(
            @RequestParam(required = false) String category
    ) {
        return ApiResponse.success(llmAnalysisService.generateScoreStructure(category));
    }

    @PostMapping("/analysis/score-structure")
    public ApiResponse<LlmAnalysisResponse> generateScoreStructurePost(
            @Valid @RequestBody LlmAnalysisRequest request
    ) {
        return ApiResponse.success(llmAnalysisService.generateScoreStructure(request.getCategory()));
    }

    @GetMapping("/analysis/tag-preference")
    public ApiResponse<LlmAnalysisResponse> generateTagPreference(
            @RequestParam(required = false) String category
    ) {
        return ApiResponse.success(llmAnalysisService.generateTagPreference(category));
    }

    @PostMapping("/analysis/tag-preference")
    public ApiResponse<LlmAnalysisResponse> generateTagPreferencePost(
            @Valid @RequestBody LlmAnalysisRequest request
    ) {
        return ApiResponse.success(llmAnalysisService.generateTagPreference(request.getCategory()));
    }

    @GetMapping("/analysis/recommendations")
    public ApiResponse<LlmAnalysisResponse> generateRecommendations(
            @RequestParam(required = false) String category
    ) {
        return ApiResponse.success(llmAnalysisService.generateRecommendations(category));
    }

    @PostMapping("/analysis/recommendations")
    public ApiResponse<LlmAnalysisResponse> generateRecommendationsPost(
            @Valid @RequestBody LlmAnalysisRequest request
    ) {
        return ApiResponse.success(llmAnalysisService.generateRecommendations(request.getCategory()));
    }

    @GetMapping("/analysis/collection-backlog")
    public ApiResponse<LlmAnalysisResponse> generateCollectionBacklog(
            @RequestParam(required = false) String category
    ) {
        return ApiResponse.success(llmAnalysisService.generateCollectionBacklog(category));
    }

    @PostMapping("/analysis/collection-backlog")
    public ApiResponse<LlmAnalysisResponse> generateCollectionBacklogPost(
            @Valid @RequestBody LlmAnalysisRequest request
    ) {
        return ApiResponse.success(llmAnalysisService.generateCollectionBacklog(request.getCategory()));
    }

    @GetMapping("/analysis/taste-profile")
    public ApiResponse<LlmAnalysisResponse> generateTasteProfile(
            @RequestParam(required = false) String category
    ) {
        return ApiResponse.success(llmAnalysisService.generateTasteProfile(category));
    }

    @PostMapping("/analysis/taste-profile")
    public ApiResponse<LlmAnalysisResponse> generateTasteProfilePost(
            @Valid @RequestBody LlmAnalysisRequest request
    ) {
        return ApiResponse.success(llmAnalysisService.generateTasteProfile(request.getCategory()));
    }

    @GetMapping("/analysis/preference-contradictions")
    public ApiResponse<LlmAnalysisResponse> generatePreferenceContradictions(
            @RequestParam(required = false) String category
    ) {
        return ApiResponse.success(llmAnalysisService.generatePreferenceContradictions(category));
    }

    @PostMapping("/analysis/preference-contradictions")
    public ApiResponse<LlmAnalysisResponse> generatePreferenceContradictionsPost(
            @Valid @RequestBody LlmAnalysisRequest request
    ) {
        return ApiResponse.success(llmAnalysisService.generatePreferenceContradictions(request.getCategory()));
    }

    @GetMapping("/analysis/interest-trend")
    public ApiResponse<LlmAnalysisResponse> generateInterestTrend(
            @RequestParam(required = false) String category
    ) {
        return ApiResponse.success(llmAnalysisService.generateInterestTrend(category));
    }

    @PostMapping("/analysis/interest-trend")
    public ApiResponse<LlmAnalysisResponse> generateInterestTrendPost(
            @Valid @RequestBody LlmAnalysisRequest request
    ) {
        return ApiResponse.success(llmAnalysisService.generateInterestTrend(request.getCategory()));
    }

    @GetMapping("/analysis/franchise-completion")
    public ApiResponse<LlmAnalysisResponse> generateFranchiseCompletion(
            @RequestParam(required = false) String category
    ) {
        return ApiResponse.success(llmAnalysisService.generateFranchiseCompletion(category));
    }

    @PostMapping("/analysis/franchise-completion")
    public ApiResponse<LlmAnalysisResponse> generateFranchiseCompletionPost(
            @Valid @RequestBody LlmAnalysisRequest request
    ) {
        return ApiResponse.success(llmAnalysisService.generateFranchiseCompletion(request.getCategory()));
    }

    @GetMapping("/analysis/comprehensive-report")
    public ApiResponse<LlmAnalysisResponse> generateComprehensiveReport(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String category
    ) {
        return ApiResponse.success(llmAnalysisService.generateComprehensiveReport(year, category));
    }

    @PostMapping("/analysis/comprehensive-report")
    public ApiResponse<LlmAnalysisResponse> generateComprehensiveReportPost(
            @Valid @RequestBody LlmAnalysisRequest request
    ) {
        return ApiResponse.success(llmAnalysisService.generateComprehensiveReport(request.getYear(), request.getCategory()));
    }

    @GetMapping("/qa")
    public ApiResponse<LlmQuestionAnswerResponse> answerQuestion(@RequestParam String question) {
        if (question == null || question.isBlank()) {
            return ApiResponse.fail(4001, "问题不能为空");
        }

        return ApiResponse.success(llmAnalysisService.answerQuestion(question));
    }

    @PostMapping("/qa")
    public ApiResponse<LlmQuestionAnswerResponse> answerQuestionPost(
            @Valid @RequestBody LlmAnalysisRequest request
    ) {
        String question = request.getQuestion();
        if (question == null || question.isBlank()) {
            return ApiResponse.fail(4001, "问题不能为空");
        }

        return ApiResponse.success(llmAnalysisService.answerQuestion(question));
    }

    @GetMapping("/analysis/home-insight")
    public ApiResponse<LlmAnalysisResponse> generateHomeInsight(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer quarter,
            @RequestParam(required = false) String category
    ) {
        return ApiResponse.success(llmAnalysisService.generateHomeInsight(year, quarter, category));
    }

    @PostMapping("/analysis/home-insight")
    public ApiResponse<LlmAnalysisResponse> generateHomeInsightPost(
            @Valid @RequestBody LlmAnalysisRequest request
    ) {
        return ApiResponse.success(
                llmAnalysisService.generateHomeInsight(
                        request.getYear(),
                        request.getQuarter(),
                        request.getCategory()
                )
        );
    }

    private LlmStatusResponse buildStatusResponse() {
        return new LlmStatusResponse(
                llmProperties.isEnabled(),
                llmProperties.getProvider(),
                llmProperties.getBaseUrl(),
                llmProperties.getModel(),
                llmProperties.isMockMode(),
                llmProperties.isApiKeyConfigured()
        );
    }
}
