package com.mykr.acgnhistoryanalyzer.response;

public record LlmStatusResponse(
        boolean enabled,
        String provider,
        String baseUrl,
        String model,
        boolean mockMode,
        boolean apiKeyConfigured
) {
}
