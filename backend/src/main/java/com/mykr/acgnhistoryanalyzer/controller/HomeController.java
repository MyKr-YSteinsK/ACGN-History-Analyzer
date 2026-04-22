package com.mykr.acgnhistoryanalyzer.controller;

import com.mykr.acgnhistoryanalyzer.common.enums.HomeSearchScope;
import com.mykr.acgnhistoryanalyzer.common.response.ApiResponse;
import com.mykr.acgnhistoryanalyzer.response.HomeQuarterDashboardResponse;
import com.mykr.acgnhistoryanalyzer.response.HomeSearchResponse;
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
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String recordSort,
            @RequestParam(required = false) String librarySort
    ) {
        HomeQuarterDashboardResponse dashboard =
                homeService.getQuarterDashboard(year, quarter, category, status, keyword, recordSort, librarySort);
        return ApiResponse.success(dashboard);
    }

    @GetMapping("/search")
    public ApiResponse<?> searchHomeData(
            @RequestParam Integer year,
            @RequestParam Integer quarter,
            @RequestParam(required = false) String category,
            @RequestParam String keyword,
            @RequestParam(required = false) HomeSearchScope scope
    ) {
        if (keyword == null || keyword.isBlank()) {
            return ApiResponse.fail(4001, "搜索关键词不能为空");
        }

        HomeSearchResponse response =
                homeService.searchHomeData(year, quarter, category, keyword, scope);
        return ApiResponse.success(response);
    }
}