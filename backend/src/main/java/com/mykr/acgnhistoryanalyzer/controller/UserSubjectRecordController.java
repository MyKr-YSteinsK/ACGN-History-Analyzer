package com.mykr.acgnhistoryanalyzer.controller;

import com.mykr.acgnhistoryanalyzer.common.enums.RecordStatus;
import com.mykr.acgnhistoryanalyzer.common.response.ApiResponse;
import com.mykr.acgnhistoryanalyzer.request.UserSubjectRecordCreateRequest;
import com.mykr.acgnhistoryanalyzer.response.UserSubjectRecordResponse;
import com.mykr.acgnhistoryanalyzer.service.UserSubjectRecordService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/records")
public class UserSubjectRecordController {

    private final UserSubjectRecordService userSubjectRecordService;

    public UserSubjectRecordController(UserSubjectRecordService userSubjectRecordService) {
        this.userSubjectRecordService = userSubjectRecordService;
    }

    @PostMapping
    public ApiResponse<UserSubjectRecordResponse> createRecord(@RequestBody UserSubjectRecordCreateRequest request) {
        UserSubjectRecordResponse savedRecord = userSubjectRecordService.createRecord(request);
        return ApiResponse.success(savedRecord);
    }

    @GetMapping
    public ApiResponse<List<UserSubjectRecordResponse>> getRecords(
            @RequestParam(required = false) RecordStatus status
    ) {
        List<UserSubjectRecordResponse> records = userSubjectRecordService.getRecords(status);
        return ApiResponse.success(records);
    }
}