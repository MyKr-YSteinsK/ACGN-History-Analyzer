package com.mykr.acgnhistoryanalyzer.response;

public record LlmStatusResponse(
        boolean enabled,
        String provider,
        String model,
        boolean mockMode
) {
}
