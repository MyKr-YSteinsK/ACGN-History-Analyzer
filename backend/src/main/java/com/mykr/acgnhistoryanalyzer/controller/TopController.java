package com.mykr.acgnhistoryanalyzer.controller;

import com.mykr.acgnhistoryanalyzer.common.response.ApiResponse;
import com.mykr.acgnhistoryanalyzer.response.TopMasterpieceResponse;
import com.mykr.acgnhistoryanalyzer.response.TopYearHighScoreResponse;
import com.mykr.acgnhistoryanalyzer.service.TopService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/top")
public class TopController {

    private final TopService topService;

    public TopController(TopService topService) {
        this.topService = topService;
    }

    @GetMapping("/year-high-score")
    public ApiResponse<TopYearHighScoreResponse> getYearHighScore(
            @RequestParam(required = false) Integer year
    ) {
        TopYearHighScoreResponse response = topService.getYearHighScore(year);
        return ApiResponse.success(response);
    }

    @GetMapping("/masterpieces")
    public ApiResponse<TopMasterpieceResponse> getMasterpieces() {
        TopMasterpieceResponse response = topService.getMasterpieces();
        return ApiResponse.success(response);
    }
}