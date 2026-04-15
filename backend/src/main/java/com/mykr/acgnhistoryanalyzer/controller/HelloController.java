package com.mykr.acgnhistoryanalyzer.controller;

import com.mykr.acgnhistoryanalyzer.common.response.ApiResponse;
import com.mykr.acgnhistoryanalyzer.response.HealthResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/api/hello")
    public ApiResponse<String> hello() {
        return ApiResponse.success("ACGN History Analyzer backend is running.");
    }

    @GetMapping("/api/health")
    public ApiResponse<HealthResponse> health() {
        HealthResponse response = new HealthResponse(
                "ACGN History Analyzer",
                "running",
                "Backend is healthy."
        );
        return ApiResponse.success(response);
    }
}