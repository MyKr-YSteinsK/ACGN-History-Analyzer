package com.mykr.acgnhistoryanalyzer.llm;

import com.mykr.acgnhistoryanalyzer.response.LlmAnalysisResponse;
import com.mykr.acgnhistoryanalyzer.response.LlmQuestionAnswerResponse;

public interface LlmClient {

    LlmAnalysisResponse generate(LlmGenerateRequest request);

    LlmQuestionAnswerResponse answer(LlmGenerateRequest request);

    String testConnection();
}
