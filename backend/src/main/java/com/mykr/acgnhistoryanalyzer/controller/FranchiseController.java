package com.mykr.acgnhistoryanalyzer.controller;

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
        FranchiseResponse savedFranchise = franchiseService.createFranchise(request);
        return ApiResponse.success(savedFranchise);
    }

    @GetMapping
    public ApiResponse<List<FranchiseResponse>> getFranchises() {
        List<FranchiseResponse> franchises = franchiseService.getFranchises();
        return ApiResponse.success(franchises);
    }

    @GetMapping("/{id}")
    public ApiResponse<?> getFranchiseById(@PathVariable Long id) {
        FranchiseDetailResponse franchise = franchiseService.getFranchiseDetailById(id);

        if (franchise == null) {
            return ApiResponse.fail(4044, "系列不存在");
        }

        return ApiResponse.success(franchise);
    }

    @PutMapping("/{id}")
    public ApiResponse<?> updateFranchise(
            @PathVariable Long id,
            @Valid @RequestBody FranchiseCreateRequest request
    ) {
        FranchiseResponse updatedFranchise = franchiseService.updateFranchise(id, request);

        if (updatedFranchise == null) {
            return ApiResponse.fail(4044, "系列不存在");
        }

        return ApiResponse.success(updatedFranchise);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<?> deleteFranchise(@PathVariable Long id) {
        if (!franchiseService.franchiseExists(id)) {
            return ApiResponse.fail(4044, "系列不存在");
        }

        if (franchiseService.isFranchiseReferenced(id)) {
            return ApiResponse.fail(4093, "该系列下仍有关联作品，不能删除");
        }

        franchiseService.deleteFranchise(id);
        return ApiResponse.success("删除成功");
    }
}