package com.mykr.acgnhistoryanalyzer.controller;

import com.mykr.acgnhistoryanalyzer.common.error.BusinessErrors;
import com.mykr.acgnhistoryanalyzer.common.response.ApiResponse;
import com.mykr.acgnhistoryanalyzer.request.UserSubjectRecordBatchImportRequest;
import com.mykr.acgnhistoryanalyzer.request.UserSubjectRecordCreateRequest;
import com.mykr.acgnhistoryanalyzer.response.*;
import com.mykr.acgnhistoryanalyzer.service.UserSubjectRecordService;
import jakarta.validation.Valid;
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
    public ApiResponse<?> createRecord(@Valid @RequestBody UserSubjectRecordCreateRequest request) {
        if (!userSubjectRecordService.subjectExists(request.getSubjectId())) {
            return ApiResponse.fail(
                    BusinessErrors.SUBJECT_NOT_FOUND.getCode(),
                    BusinessErrors.SUBJECT_NOT_FOUND.getMessage()
            );
        }

        if (userSubjectRecordService.recordExistsBySubjectId(request.getSubjectId())) {
            return ApiResponse.fail(
                    BusinessErrors.SUBJECT_ALREADY_HAS_RECORD.getCode(),
                    BusinessErrors.SUBJECT_ALREADY_HAS_RECORD.getMessage()
            );
        }

        UserSubjectRecordResponse savedRecord = userSubjectRecordService.createRecord(request);
        return ApiResponse.success(savedRecord);
    }

    @PostMapping("/import")
    public ApiResponse<UserSubjectRecordImportResponse> importRecords(
            @Valid @RequestBody UserSubjectRecordBatchImportRequest request
    ) {
        UserSubjectRecordImportResponse response = userSubjectRecordService.importRecords(request);
        return ApiResponse.success(response);
    }

    @PostMapping("/import/preview")
    public ApiResponse<UserSubjectRecordImportPreviewResponse> previewImportRecords(
            @Valid @RequestBody UserSubjectRecordBatchImportRequest request
    ) {
        UserSubjectRecordImportPreviewResponse response = userSubjectRecordService.previewImportRecords(request);
        return ApiResponse.success(response);
    }

    @GetMapping
    public ApiResponse<List<UserSubjectRecordResponse>> getRecords(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer quarter,
            @RequestParam(required = false) Integer minScore,
            @RequestParam(required = false) Integer maxScore
    ) {
        List<UserSubjectRecordResponse> records =
                userSubjectRecordService.getRecords(year, quarter, minScore, maxScore);
        return ApiResponse.success(records);
    }

    @GetMapping("/page")
    public ApiResponse<PageResponse<UserSubjectRecordResponse>> getRecordPage(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer quarter,
            @RequestParam(required = false) Integer minScore,
            @RequestParam(required = false) Integer maxScore,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sort
    ) {
        PageResponse<UserSubjectRecordResponse> response =
                userSubjectRecordService.getRecordPage(
                        year, quarter, minScore, maxScore,
                        category, keyword, page, size, sort
                );
        return ApiResponse.success(response);
    }

    @GetMapping("/stats/score-bands")
    public ApiResponse<RecordScoreBandStatsResponse> getScoreBandStats(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer quarter
    ) {
        RecordScoreBandStatsResponse stats = userSubjectRecordService.getScoreBandStats(year, quarter);
        return ApiResponse.success(stats);
    }

    @GetMapping("/stats/quarter-overview")
    public ApiResponse<RecordQuarterOverviewResponse> getQuarterOverview(
            @RequestParam Integer year,
            @RequestParam Integer quarter
    ) {
        RecordQuarterOverviewResponse overview = userSubjectRecordService.getQuarterOverview(year, quarter);
        return ApiResponse.success(overview);
    }

    @GetMapping("/stats/year-overview")
    public ApiResponse<RecordYearOverviewResponse> getYearOverview(
            @RequestParam Integer year
    ) {
        RecordYearOverviewResponse overview = userSubjectRecordService.getYearOverview(year);
        return ApiResponse.success(overview);
    }

    @GetMapping("/high-score")
    public ApiResponse<List<UserSubjectRecordResponse>> getHighScoreRecords(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer quarter,
            @RequestParam(required = false) Integer minScore
    ) {
        List<UserSubjectRecordResponse> records = userSubjectRecordService.getHighScoreRecords(year, quarter, minScore);
        return ApiResponse.success(records);
    }

    @GetMapping("/{id}")
    public ApiResponse<?> getRecordById(@PathVariable Long id) {
        UserSubjectRecordResponse record = userSubjectRecordService.getRecordById(id);
        if (record == null) {
            return ApiResponse.fail(
                    BusinessErrors.RECORD_NOT_FOUND.getCode(),
                    BusinessErrors.RECORD_NOT_FOUND.getMessage()
            );
        }
        return ApiResponse.success(record);
    }

    @PutMapping("/{id}")
    public ApiResponse<?> updateRecord(
            @PathVariable Long id,
            @Valid @RequestBody UserSubjectRecordCreateRequest request
    ) {
        if (!userSubjectRecordService.subjectExists(request.getSubjectId())) {
            return ApiResponse.fail(
                    BusinessErrors.RELATED_SUBJECT_NOT_FOUND.getCode(),
                    BusinessErrors.RELATED_SUBJECT_NOT_FOUND.getMessage()
            );
        }

        if (userSubjectRecordService.recordExistsBySubjectIdExcludingCurrent(request.getSubjectId(), id)) {
            return ApiResponse.fail(
                    BusinessErrors.SUBJECT_ALREADY_HAS_RECORD.getCode(),
                    BusinessErrors.SUBJECT_ALREADY_HAS_RECORD.getMessage()
            );
        }

        UserSubjectRecordResponse updatedRecord = userSubjectRecordService.updateRecord(id, request);
        if (updatedRecord == null) {
            return ApiResponse.fail(
                    BusinessErrors.RECORD_NOT_FOUND.getCode(),
                    BusinessErrors.RECORD_NOT_FOUND.getMessage()
            );
        }
        return ApiResponse.success(updatedRecord);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<?> deleteRecord(@PathVariable Long id) {
        boolean deleted = userSubjectRecordService.deleteRecord(id);
        if (!deleted) {
            return ApiResponse.fail(
                    BusinessErrors.RECORD_NOT_FOUND.getCode(),
                    BusinessErrors.RECORD_NOT_FOUND.getMessage()
            );
        }
        return ApiResponse.success("删除成功");
    }
}
