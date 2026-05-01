package com.mykr.acgnhistoryanalyzer.llm;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mykr.acgnhistoryanalyzer.response.LlmAnalysisResponse;
import com.mykr.acgnhistoryanalyzer.response.LlmAnalysisSectionResponse;
import com.mykr.acgnhistoryanalyzer.response.LlmAnalysisScopeResponse;
import com.mykr.acgnhistoryanalyzer.response.LlmQuestionAnswerResponse;
import com.mykr.acgnhistoryanalyzer.response.LlmRecommendationResponse;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RealLlmClient implements LlmClient {

    private final LlmProperties properties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public RealLlmClient(LlmProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(Math.max(5, properties.getTimeoutSeconds())))
                .build();
    }

    @Override
    public LlmAnalysisResponse generate(LlmGenerateRequest request) {
        try {
            String body = objectMapper.writeValueAsString(Map.of(
                    "model", properties.getModel(),
                    "temperature", properties.getTemperature(),
                    "max_tokens", properties.getMaxTokens(),
                    "messages", List.of(
                            Map.of("role", "system", "content", request.prompt()),
                            Map.of("role", "user", "content", buildUserContent(request))
                    ),
                    "response_format", Map.of("type", "json_object")
            ));

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(resolveEndpoint()))
                    .timeout(Duration.ofSeconds(Math.max(5, properties.getTimeoutSeconds())))
                    .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                    .header("Authorization", "Bearer " + properties.getApiKey())
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("LLM 请求失败，状态码：" + response.statusCode()
                        + "，响应：" + abbreviate(response.body()));
            }

            JsonNode root = objectMapper.readTree(response.body());
            String content = extractContent(root);
            JsonNode data = parseAnalysisPayload(content);
            return mapResponse(request, data);
        } catch (InterruptedException error) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("LLM 调用被中断", error);
        } catch (IOException error) {
            throw new IllegalStateException("LLM 调用失败", error);
        }
    }

    private String buildUserContent(LlmGenerateRequest request) {
        try {
            return objectMapper.writeValueAsString(Map.of(
                    "analysisType", request.analysisType(),
                    "context", request.context()
            ));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("LLM 请求上下文序列化失败", exception);
        }
    }

    private String resolveEndpoint() {
        String baseUrl = properties.getBaseUrl();
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalStateException("LLM baseUrl 不能为空");
        }
        return baseUrl.endsWith("/") ? baseUrl + "chat/completions" : baseUrl + "/chat/completions";
    }

    private String extractContent(JsonNode root) {
        JsonNode choices = root.path("choices");
        if (!choices.isArray() || choices.isEmpty()) {
            throw new IllegalStateException("LLM 返回缺少 choices");
        }
        return choices.get(0).path("message").path("content").asText();
    }

    private JsonNode parseAnalysisPayload(String content) throws JsonProcessingException {
        String trimmed = content == null ? "" : content.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalStateException("LLM 返回内容为空");
        }

        if (trimmed.startsWith("```")) {
            trimmed = trimmed
                    .replaceFirst("^```(?:json)?\\s*", "")
                    .replaceFirst("\\s*```$", "")
                    .trim();
        }

        try {
            return objectMapper.readTree(trimmed);
        } catch (JsonProcessingException firstError) {
            int start = trimmed.indexOf('{');
            int end = trimmed.lastIndexOf('}');
            if (start >= 0 && end > start) {
                String candidate = trimmed.substring(start, end + 1);
                return objectMapper.readTree(candidate);
            }
            throw firstError;
        }
    }

    @Override
    public LlmQuestionAnswerResponse answer(LlmGenerateRequest request) {
        try {
            String body = objectMapper.writeValueAsString(Map.of(
                    "model", properties.getModel(),
                    "temperature", properties.getTemperature(),
                    "max_tokens", properties.getMaxTokens(),
                    "messages", List.of(
                            Map.of("role", "system", "content", request.prompt()),
                            Map.of("role", "user", "content", buildUserContent(request))
                    )
            ));

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(resolveEndpoint()))
                    .timeout(Duration.ofSeconds(Math.max(5, properties.getTimeoutSeconds())))
                    .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                    .header("Authorization", "Bearer " + properties.getApiKey())
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("LLM 问答请求失败，状态码：" + response.statusCode()
                        + "，响应：" + abbreviate(response.body()));
            }

            JsonNode root = objectMapper.readTree(response.body());
            String answer = extractContent(root).trim();
            return new LlmQuestionAnswerResponse(
                    textFromContext(request.context(), "question", ""),
                    answer,
                    false,
                    properties.getModel(),
                    LocalDateTime.now()
            );
        } catch (InterruptedException error) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("LLM 问答调用被中断", error);
        } catch (IOException error) {
            throw new IllegalStateException("LLM 问答调用失败", error);
        }
    }

    @Override
    public String testConnection() {
        try {
            String body = objectMapper.writeValueAsString(Map.of(
                    "model", properties.getModel(),
                    "temperature", 0,
                    "max_tokens", 32,
                    "messages", List.of(
                            Map.of("role", "system", "content", "你只需要用中文简短回复连接正常。"),
                            Map.of("role", "user", "content", "请回复：连接正常")
                    )
            ));

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(resolveEndpoint()))
                    .timeout(Duration.ofSeconds(Math.max(5, properties.getTimeoutSeconds())))
                    .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                    .header("Authorization", "Bearer " + properties.getApiKey())
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("LLM 连接测试失败，状态码：" + response.statusCode()
                        + "，响应：" + abbreviate(response.body()));
            }

            JsonNode root = objectMapper.readTree(response.body());
            String answer = extractContent(root).trim();
            return answer.isBlank() ? "连接正常" : answer;
        } catch (InterruptedException error) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("LLM 连接测试被中断", error);
        } catch (IOException error) {
            throw new IllegalStateException("LLM 连接测试失败", error);
        }
    }

    private LlmAnalysisResponse mapResponse(LlmGenerateRequest request, JsonNode data) {
        String analysisType = text(data, "analysisType", request.analysisType());
        String title = text(data, "title", analysisType);
        String summary = text(data, "summary", "暂无摘要");
        List<String> keywords = textList(data, "keywords");
        List<LlmAnalysisSectionResponse> sections = sectionList(data.path("sections"));
        List<LlmRecommendationResponse> recommendations = recommendationList(data.path("recommendations"));
        List<String> evidence = textList(data, "evidence");
        List<String> warnings = textList(data, "warnings");
        JsonNode scopeNode = data.path("scope");
        LlmAnalysisScopeResponse scope = new LlmAnalysisScopeResponse(
                nullableInteger(scopeNode, "year"),
                nullableInteger(scopeNode, "quarter"),
                text(scopeNode, "category", null)
        );
        boolean mockMode = data.path("mockMode").asBoolean(false);
        String modelName = text(data, "modelName", properties.getModel());
        LocalDateTime generatedAt = LocalDateTime.now();

        return new LlmAnalysisResponse(
                analysisType,
                title,
                summary,
                keywords,
                sections,
                recommendations,
                evidence,
                warnings,
                scope,
                mockMode,
                modelName,
                generatedAt
        );
    }

    private List<LlmAnalysisSectionResponse> sectionList(JsonNode node) {
        List<LlmAnalysisSectionResponse> sections = new ArrayList<>();
        if (!node.isArray()) {
            return sections;
        }
        for (JsonNode item : node) {
            sections.add(new LlmAnalysisSectionResponse(
                    text(item, "title", ""),
                    text(item, "content", "")
            ));
        }
        return sections;
    }

    private List<LlmRecommendationResponse> recommendationList(JsonNode node) {
        List<LlmRecommendationResponse> items = new ArrayList<>();
        if (!node.isArray()) {
            return items;
        }
        for (JsonNode item : node) {
            items.add(new LlmRecommendationResponse(
                    nullableLong(item, "subjectId"),
                    text(item, "title", ""),
                    text(item, "coverUrl", null),
                    text(item, "category", "ANIME"),
                    textList(item, "tags"),
                    text(item, "franchiseName", null),
                    text(item, "source", "LIBRARY"),
                    text(item, "reason", ""),
                    text(item, "risk", ""),
                    text(item, "evidence", ""),
                    text(item, "priority", "LOW"),
                    nullableInteger(item, "matchScore"),
                    nullableBigDecimal(item, "platformScore")
            ));
        }
        return items;
    }

    private String text(JsonNode node, String field, String defaultValue) {
        JsonNode value = node.get(field);
        if (value == null || value.isNull()) {
            return defaultValue;
        }
        return value.asText(defaultValue);
    }

    private List<String> textList(JsonNode node, String field) {
        List<String> values = new ArrayList<>();
        JsonNode array = node.get(field);
        if (array == null || !array.isArray()) {
            return values;
        }
        for (JsonNode item : array) {
            if (!item.isNull()) {
                values.add(item.asText());
            }
        }
        return values;
    }

    private String textFromContext(Map<String, Object> context, String field, String defaultValue) {
        Object value = context.get(field);
        if (value == null) {
            return defaultValue;
        }
        return String.valueOf(value);
    }

    private Integer nullableInteger(JsonNode node, String field) {
        JsonNode value = node.get(field);
        if (value == null || value.isNull() || value.isMissingNode()) {
            return null;
        }

        return value.asInt();
    }

    private Long nullableLong(JsonNode node, String field) {
        JsonNode value = node.get(field);
        if (value == null || value.isNull() || value.isMissingNode()) {
            return null;
        }

        return value.asLong();
    }

    private BigDecimal nullableBigDecimal(JsonNode node, String field) {
        JsonNode value = node.get(field);
        if (value == null || value.isNull() || value.isMissingNode()) {
            return null;
        }

        return value.decimalValue();
    }

    private String abbreviate(String value) {
        if (value == null || value.isBlank()) {
            return "空响应";
        }

        String compact = value.replaceAll("\\s+", " ").trim();
        if (compact.length() <= 300) {
            return compact;
        }

        return compact.substring(0, 300) + "...";
    }
}
