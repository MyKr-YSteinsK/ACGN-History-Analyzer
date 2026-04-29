package com.mykr.acgnhistoryanalyzer.response;

public record LlmAnalysisScopeResponse(
        Integer year,
        Integer quarter,
        String category
) {
}
