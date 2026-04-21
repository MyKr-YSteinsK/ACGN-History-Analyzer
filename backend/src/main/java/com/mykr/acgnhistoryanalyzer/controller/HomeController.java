package com.mykr.acgnhistoryanalyzer.controller;

import com.mykr.acgnhistoryanalyzer.common.response.ApiResponse;
import com.mykr.acgnhistoryanalyzer.response.HomeQuarterDashboardResponse;
import com.mykr.acgnhistoryanalyzer.service.HomeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/home")
public class HomeController {

    private final HomeService homeService;

    public HomeController(HomeService homeService) {
        this.homeService = homeService;
    }

    @GetMapping("/quarter-dashboard")
    public ApiResponse<HomeQuarterDashboardResponse> getQuarterDashboard(
            @RequestParam Integer year,
            @RequestParam Integer quarter,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword
    ) {
        HomeQuarterDashboardResponse dashboard =
                homeService.getQuarterDashboard(year, quarter, category, status, keyword);
        return ApiResponse.success(dashboard);
    }
}