package com.mykr.acgnhistoryanalyzer.llm;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mykr.acgnhistoryanalyzer.common.exception.LlmAnalysisException;
import com.mykr.acgnhistoryanalyzer.response.LlmAnalysisResponse;
import com.mykr.acgnhistoryanalyzer.response.LlmQuestionAnswerResponse;

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
            throw new LlmAnalysisException("智能分析生成失败：" + error.getMessage(), error);
        }
    }

    @Override
    public LlmQuestionAnswerResponse answer(LlmGenerateRequest request) {
        try {
            return currentClient().answer(request);
        } catch (IllegalStateException error) {
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
}
