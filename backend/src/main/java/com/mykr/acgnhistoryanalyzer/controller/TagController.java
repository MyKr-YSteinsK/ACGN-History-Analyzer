package com.mykr.acgnhistoryanalyzer.controller;

import com.mykr.acgnhistoryanalyzer.common.response.ApiResponse;
import com.mykr.acgnhistoryanalyzer.response.TagResponse;
import com.mykr.acgnhistoryanalyzer.response.TagWhitelistCleanupResponse;
import com.mykr.acgnhistoryanalyzer.service.TagService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
public class TagController {

    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @GetMapping
    public ApiResponse<List<TagResponse>> getTags(
            @RequestParam(required = false) String keyword
    ) {
        return ApiResponse.success(tagService.getTags(keyword));
    }

    @PostMapping("/cleanup/whitelist")
    public ApiResponse<TagWhitelistCleanupResponse> cleanupToWhitelist() {
        return ApiResponse.success(tagService.cleanupToSubjectWhitelist());
    }
}
