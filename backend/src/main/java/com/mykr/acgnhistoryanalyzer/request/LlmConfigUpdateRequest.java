package com.mykr.acgnhistoryanalyzer.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public class LlmConfigUpdateRequest {

    private boolean previewMode;

    @Size(max = 500, message = "LLM baseUrl 不能超过500个字符")
    private String baseUrl;

    @Size(max = 255, message = "LLM API Key 不能超过255个字符")
    private String apiKey;

    @Size(max = 128, message = "LLM 模型名不能超过128个字符")
    private String model;

    @Min(value = 5, message = "LLM 超时时间不能小于5秒")
    @Max(value = 120, message = "LLM 超时时间不能超过120秒")
    private Integer timeoutSeconds;

    @DecimalMin(value = "0.0", message = "LLM temperature 不能小于0")
    @DecimalMax(value = "2.0", message = "LLM temperature 不能大于2")
    private Double temperature;

    @Min(value = 256, message = "LLM maxTokens 不能小于256")
    @Max(value = 8000, message = "LLM maxTokens 不能超过8000")
    private Integer maxTokens;

    public boolean isPreviewMode() {
        return previewMode;
    }

    public void setPreviewMode(boolean previewMode) {
        this.previewMode = previewMode;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Integer getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(Integer timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public Integer getMaxTokens() {
        return maxTokens;
    }

    public void setMaxTokens(Integer maxTokens) {
        this.maxTokens = maxTokens;
    }
}
