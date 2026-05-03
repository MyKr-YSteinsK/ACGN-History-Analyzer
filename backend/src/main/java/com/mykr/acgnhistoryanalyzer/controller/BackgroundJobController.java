package com.mykr.acgnhistoryanalyzer.controller;

import com.mykr.acgnhistoryanalyzer.common.response.ApiResponse;
import com.mykr.acgnhistoryanalyzer.job.BackgroundJobEnqueueRequest;
import com.mykr.acgnhistoryanalyzer.job.BackgroundJobResponse;
import com.mykr.acgnhistoryanalyzer.job.BackgroundJobService;
import com.mykr.acgnhistoryanalyzer.job.BackgroundJobStartResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/jobs")
public class BackgroundJobController {

    private final BackgroundJobService backgroundJobService;

    public BackgroundJobController(BackgroundJobService backgroundJobService) {
        this.backgroundJobService = backgroundJobService;
    }

    @PostMapping
    public ApiResponse<BackgroundJobStartResponse> enqueue(@Valid @RequestBody BackgroundJobEnqueueRequest request) {
        return ApiResponse.success(backgroundJobService.enqueue(request));
    }

    @GetMapping("/{jobId}")
    public ApiResponse<?> getJob(@PathVariable String jobId) {
        BackgroundJobResponse response = backgroundJobService.get(jobId);
        if (response == null) {
            return ApiResponse.fail(4046, "任务不存在");
        }
        return ApiResponse.success(response);
    }

    @DeleteMapping("/{jobId}")
    public ApiResponse<?> cancelJob(@PathVariable String jobId) {
        if (backgroundJobService.get(jobId) == null) {
            return ApiResponse.fail(4046, "任务不存在");
        }
        if (!backgroundJobService.cancelPending(jobId)) {
            return ApiResponse.fail(4097, "仅排队中的任务可取消");
        }
        return ApiResponse.success("已取消");
    }
}
