package com.mykr.acgnhistoryanalyzer.llm;

import com.mykr.acgnhistoryanalyzer.response.LlmAnalysisResponse;

public interface LlmClient {

    LlmAnalysisResponse generate(LlmGenerateRequest request);
}
