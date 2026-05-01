package com.mykr.acgnhistoryanalyzer.response;

import java.time.LocalDateTime;

public record LlmConnectionTestResponse(
        boolean available,
        boolean previewMode,
        String provider,
        String model,
        String message,
        LocalDateTime checkedAt
) {
}
