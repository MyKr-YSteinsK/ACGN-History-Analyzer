package com.mykr.acgnhistoryanalyzer.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class FranchiseCreateRequest {

    @NotBlank(message = "系列名称不能为空")
    @Size(max = 200, message = "系列名称不能超过200个字符")
    private String name;

    @Size(max = 200, message = "系列中文名不能超过200个字符")
    private String nameCn;

    @Size(max = 1000, message = "系列简介不能超过1000个字符")
    private String description;

    @NotBlank(message = "状态不能为空")
    @Pattern(regexp = "NORMAL|HIDDEN", message = "状态只能是 NORMAL 或 HIDDEN")
    private String status;

    public FranchiseCreateRequest() {
    }

    public String getName() {
        return name;
    }

    public String getNameCn() {
        return nameCn;
    }

    public String getDescription() {
        return description;
    }

    public String getStatus() {
        return status;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setNameCn(String nameCn) {
        this.nameCn = nameCn;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}