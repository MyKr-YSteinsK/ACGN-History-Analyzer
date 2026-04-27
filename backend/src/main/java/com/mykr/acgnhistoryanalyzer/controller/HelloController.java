package com.mykr.acgnhistoryanalyzer.controller;

import com.mykr.acgnhistoryanalyzer.common.enums.CollectionType;
import com.mykr.acgnhistoryanalyzer.common.enums.LibraryStatus;
import com.mykr.acgnhistoryanalyzer.common.response.ApiResponse;
import com.mykr.acgnhistoryanalyzer.response.EnumOptionResponse;
import com.mykr.acgnhistoryanalyzer.response.HealthResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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

    /**
     * 前端下拉选项聚合入口。
     * 当前阶段不接入 AI / LLM；这里只返回稳定业务枚举，方便后续前端统一渲染筛选项。
     */
    @RestController
    @RequestMapping("/api/options")
    public static class OptionController {

        @GetMapping("/categories")
        public ApiResponse<List<EnumOptionResponse>> categories() {
            return ApiResponse.success(List.of(
                    new EnumOptionResponse("ANIME", "动画"),
                    new EnumOptionResponse("COMIC", "漫画"),
                    new EnumOptionResponse("GAME", "游戏"),
                    new EnumOptionResponse("NOVEL", "小说")
            ));
        }

        @GetMapping("/quarters")
        public ApiResponse<List<EnumOptionResponse>> quarters() {
            return ApiResponse.success(List.of(
                    new EnumOptionResponse("1", "春"),
                    new EnumOptionResponse("2", "夏"),
                    new EnumOptionResponse("3", "秋"),
                    new EnumOptionResponse("4", "冬")
            ));
        }

        @GetMapping("/collection-types")
        public ApiResponse<List<EnumOptionResponse>> collectionTypes() {
            return ApiResponse.success(List.of(
                    new EnumOptionResponse(CollectionType.WANT.name(), CollectionType.WANT.getLabel()),
                    new EnumOptionResponse(CollectionType.PAUSED.name(), CollectionType.PAUSED.getLabel())
            ));
        }

        @GetMapping("/library-statuses")
        public ApiResponse<List<EnumOptionResponse>> libraryStatuses() {
            return ApiResponse.success(List.of(
                    new EnumOptionResponse(LibraryStatus.WATCHED.name(), LibraryStatus.WATCHED.getLabel()),
                    new EnumOptionResponse(LibraryStatus.WANT.name(), LibraryStatus.WANT.getLabel()),
                    new EnumOptionResponse(LibraryStatus.PAUSED.name(), LibraryStatus.PAUSED.getLabel()),
                    new EnumOptionResponse(LibraryStatus.NONE.name(), LibraryStatus.NONE.getLabel())
            ));
        }
    }
}
