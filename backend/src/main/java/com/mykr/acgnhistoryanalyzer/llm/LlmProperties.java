package com.mykr.acgnhistoryanalyzer.llm;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.llm")
public class LlmProperties {

    private boolean enabled = true;

    private String provider = "mock";

    private String baseUrl = "";

    private String apiKey = "";

    private String model = "mock-acgn-analyst";

    private Integer timeoutSeconds = 30;

    private Double temperature = 0.4;

    private Integer maxTokens = 1200;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
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

    public boolean isMockMode() {
        return !enabled || provider == null || provider.isBlank() || "mock".equalsIgnoreCase(provider);
    }

    public boolean isApiKeyConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }

    public synchronized void switchToMockPreview() {
        this.enabled = true;
        this.provider = "mock";
        this.model = "mock-acgn-analyst";
    }

    public synchronized void switchToDeepSeek(
            String baseUrl,
            String apiKey,
            String model,
            Integer timeoutSeconds,
            Double temperature,
            Integer maxTokens
    ) {
        this.enabled = true;
        this.provider = "deepseek";
        this.baseUrl = normalizeOrDefault(baseUrl, "https://api.deepseek.com");
        if (apiKey != null && !apiKey.isBlank()) {
            this.apiKey = apiKey.trim();
        }
        this.model = normalizeOrDefault(model, "deepseek-v4-flash");
        this.timeoutSeconds = timeoutSeconds == null ? 30 : timeoutSeconds;
        this.temperature = temperature == null ? 0.4 : temperature;
        this.maxTokens = maxTokens == null ? 1200 : maxTokens;

        validateForRealClient();
    }

    public void validateForRealClient() {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalStateException("app.llm.base-url 不能为空");
        }

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("app.llm.api-key 不能为空");
        }

        if (model == null || model.isBlank()) {
            throw new IllegalStateException("app.llm.model 不能为空");
        }
    }

    private String normalizeOrDefault(String value, String defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }

        return value.trim();
    }
}
