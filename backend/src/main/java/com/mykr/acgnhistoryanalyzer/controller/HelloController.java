package com.mykr.acgnhistoryanalyzer.controller;

import com.mykr.acgnhistoryanalyzer.common.enums.CollectionType;
import com.mykr.acgnhistoryanalyzer.common.response.ApiResponse;
import com.mykr.acgnhistoryanalyzer.response.HealthResponse;
import com.mykr.acgnhistoryanalyzer.response.RecordStatusResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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

    @GetMapping("/api/record-statuses")
    public ApiResponse<RecordStatusResponse> recordStatuses() {
        RecordStatusResponse response = new RecordStatusResponse(List.of("看过"));
        return ApiResponse.success(response);
    }

    @GetMapping("/api/collection-types")
    public ApiResponse<RecordStatusResponse> collectionTypes() {
        RecordStatusResponse response = new RecordStatusResponse(
                List.of(CollectionType.WANT.getLabel(), CollectionType.PAUSED.getLabel())
        );
        return ApiResponse.success(response);
    }
}
