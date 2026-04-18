package com.mykr.acgnhistoryanalyzer.controller;

import com.mykr.acgnhistoryanalyzer.common.enums.RecordStatus;
import com.mykr.acgnhistoryanalyzer.common.response.ApiResponse;
import com.mykr.acgnhistoryanalyzer.request.UserSubjectRecordCreateRequest;
import com.mykr.acgnhistoryanalyzer.response.UserSubjectRecordResponse;
import com.mykr.acgnhistoryanalyzer.service.UserSubjectRecordService;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/records")
public class UserSubjectRecordController {

    private final UserSubjectRecordService userSubjectRecordService;

    public UserSubjectRecordController(UserSubjectRecordService userSubjectRecordService) {
        this.userSubjectRecordService = userSubjectRecordService;
    }

    @PostMapping
    public ApiResponse<UserSubjectRecordResponse> createRecord(@Valid @RequestBody UserSubjectRecordCreateRequest request) {
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

    @GetMapping("/{id}")
    public ApiResponse<?> getRecordById(@PathVariable Long id) {
        UserSubjectRecordResponse record = userSubjectRecordService.getRecordById(id);

        if (record == null) {
            return ApiResponse.fail(4041, "记录不存在");
        }

        return ApiResponse.success(record);
    }

    @PutMapping("/{id}")
    public ApiResponse<?> updateRecord(
            @PathVariable Long id,
            @Valid @RequestBody UserSubjectRecordCreateRequest request
    ) {
        UserSubjectRecordResponse updatedRecord = userSubjectRecordService.updateRecord(id, request);

        if (updatedRecord == null) {
            return ApiResponse.fail(4041, "记录不存在");
        }

        return ApiResponse.success(updatedRecord);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<?> deleteRecord(@PathVariable Long id) {
        boolean deleted = userSubjectRecordService.deleteRecord(id);

        if (!deleted) {
            return ApiResponse.fail(4041, "记录不存在");
        }

        return ApiResponse.success("删除成功");
    }
}