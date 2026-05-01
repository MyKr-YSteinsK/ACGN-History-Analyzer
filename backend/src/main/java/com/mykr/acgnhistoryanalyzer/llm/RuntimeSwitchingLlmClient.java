package com.mykr.acgnhistoryanalyzer.llm;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mykr.acgnhistoryanalyzer.common.exception.LlmAnalysisException;
import com.mykr.acgnhistoryanalyzer.response.LlmAnalysisResponse;
import com.mykr.acgnhistoryanalyzer.response.LlmQuestionAnswerResponse;

import java.util.ArrayList;
import java.util.List;

public class RuntimeSwitchingLlmClient implements LlmClient {

    private final LlmProperties properties;
    private final ObjectMapper objectMapper;

    public RuntimeSwitchingLlmClient(LlmProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Override
    public LlmAnalysisResponse generate(LlmGenerateRequest request) {
        try {
            return currentClient().generate(request);
        } catch (IllegalStateException error) {
            if (!properties.isMockMode()) {
                return withFallbackWarning(
                        new MockLlmClient(properties).generate(request),
                        "DeepSeek 调用失败，已自动降级为本地预览结果：" + error.getMessage()
                );
            }
            throw new LlmAnalysisException("智能分析生成失败：" + error.getMessage(), error);
        }
    }

    @Override
    public LlmQuestionAnswerResponse answer(LlmGenerateRequest request) {
        try {
            return currentClient().answer(request);
        } catch (IllegalStateException error) {
            if (!properties.isMockMode()) {
                LlmQuestionAnswerResponse fallback = new MockLlmClient(properties).answer(request);
                return new LlmQuestionAnswerResponse(
                        fallback.question(),
                        fallback.answer() + "\n\nDeepSeek 调用失败，已自动降级为本地预览回答：" + error.getMessage(),
                        true,
                        fallback.modelName(),
                        fallback.generatedAt()
                );
            }
            throw new LlmAnalysisException("智能问答生成失败：" + error.getMessage(), error);
        }
    }

    @Override
    public String testConnection() {
        try {
            return currentClient().testConnection();
        } catch (IllegalStateException error) {
            throw new LlmAnalysisException("大模型连接测试失败：" + error.getMessage(), error);
        }
    }

    private LlmClient currentClient() {
        if (properties.isMockMode()) {
            return new MockLlmClient(properties);
        }

        properties.validateForRealClient();
        return new RealLlmClient(properties, objectMapper);
    }

    private LlmAnalysisResponse withFallbackWarning(LlmAnalysisResponse response, String warning) {
        List<String> warnings = new ArrayList<>(response.warnings());
        warnings.add(warning);

        return new LlmAnalysisResponse(
                response.analysisType(),
                response.title(),
                response.summary(),
                response.keywords(),
                response.sections(),
                response.recommendations(),
                response.evidence(),
                warnings,
                response.scope(),
                true,
                response.modelName(),
                response.generatedAt()
        );
    }
}
