package com.mykr.acgnhistoryanalyzer.controller;

import com.mykr.acgnhistoryanalyzer.common.error.BusinessErrors;
import com.mykr.acgnhistoryanalyzer.common.response.ApiResponse;
import com.mykr.acgnhistoryanalyzer.request.FranchiseCreateRequest;
import com.mykr.acgnhistoryanalyzer.response.FranchiseDetailResponse;
import com.mykr.acgnhistoryanalyzer.response.FranchiseResponse;
import com.mykr.acgnhistoryanalyzer.service.FranchiseService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/franchises")
public class FranchiseController {

    private final FranchiseService franchiseService;

    public FranchiseController(FranchiseService franchiseService) {
        this.franchiseService = franchiseService;
    }

    @PostMapping
    public ApiResponse<FranchiseResponse> createFranchise(
            @Valid @RequestBody FranchiseCreateRequest request
    ) {
        if (franchiseService.franchiseNameExistsForCreate(request)) {
            return ApiResponse.fail(
                    BusinessErrors.FRANCHISE_NAME_DUPLICATED.getCode(),
                    BusinessErrors.FRANCHISE_NAME_DUPLICATED.getMessage()
            );
        }

        FranchiseResponse savedFranchise = franchiseService.createFranchise(request);
        return ApiResponse.success(savedFranchise);
    }

    @GetMapping
    public ApiResponse<List<FranchiseResponse>> getFranchises() {
        List<FranchiseResponse> franchises = franchiseService.getFranchises();
        return ApiResponse.success(franchises);
    }

    @GetMapping("/overview")
    public ApiResponse<List<FranchiseDetailResponse>> getFranchisesOverview() {
        return ApiResponse.success(franchiseService.getAllFranchiseDetailsWithSubjects());
    }

    @GetMapping("/{id}")
    public ApiResponse<?> getFranchiseById(@PathVariable Long id) {
        FranchiseDetailResponse franchise = franchiseService.getFranchiseDetailById(id);

        if (franchise == null) {
            return ApiResponse.fail(
                    BusinessErrors.FRANCHISE_NOT_FOUND.getCode(),
                    BusinessErrors.FRANCHISE_NOT_FOUND.getMessage()
            );
        }

        return ApiResponse.success(franchise);
    }

    @PutMapping("/{id}")
    public ApiResponse<?> updateFranchise(
            @PathVariable Long id,
            @Valid @RequestBody FranchiseCreateRequest request
    ) {
        if (!franchiseService.franchiseExists(id)) {
            return ApiResponse.fail(
                    BusinessErrors.FRANCHISE_NOT_FOUND.getCode(),
                    BusinessErrors.FRANCHISE_NOT_FOUND.getMessage()
            );
        }

        if (franchiseService.franchiseNameExistsForUpdate(id, request)) {
            return ApiResponse.fail(
                    BusinessErrors.FRANCHISE_NAME_DUPLICATED.getCode(),
                    BusinessErrors.FRANCHISE_NAME_DUPLICATED.getMessage()
            );
        }

        FranchiseResponse updatedFranchise = franchiseService.updateFranchise(id, request);
        return ApiResponse.success(updatedFranchise);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<?> deleteFranchise(@PathVariable Long id) {
        if (!franchiseService.franchiseExists(id)) {
            return ApiResponse.fail(
                    BusinessErrors.FRANCHISE_NOT_FOUND.getCode(),
                    BusinessErrors.FRANCHISE_NOT_FOUND.getMessage()
            );
        }

        franchiseService.deleteFranchise(id);
        return ApiResponse.success("删除成功");
    }

    @DeleteMapping("/{franchiseId}/subjects/{subjectId}")
    public ApiResponse<?> removeSubjectFromFranchise(
            @PathVariable Long franchiseId,
            @PathVariable Long subjectId
    ) {
        if (!franchiseService.franchiseExists(franchiseId)) {
            return ApiResponse.fail(
                    BusinessErrors.FRANCHISE_NOT_FOUND.getCode(),
                    BusinessErrors.FRANCHISE_NOT_FOUND.getMessage()
            );
        }

        boolean removed = franchiseService.removeSubjectFromFranchise(franchiseId, subjectId);
        if (!removed) {
            return ApiResponse.fail(
                    BusinessErrors.SUBJECT_NOT_FOUND.getCode(),
                    BusinessErrors.SUBJECT_NOT_FOUND.getMessage()
            );
        }

        return ApiResponse.success("移出成功");
    }
}
