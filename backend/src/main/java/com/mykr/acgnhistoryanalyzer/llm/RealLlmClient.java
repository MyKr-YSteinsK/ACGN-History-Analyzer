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

    private static final int MAX_ATTEMPTS = 2;

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

            JsonNode root = sendChatCompletion(body, "LLM 请求");
            String content = extractContent(root);
            try {
                JsonNode data = parseAnalysisPayload(content);
                return mapResponse(request, data);
            } catch (JsonProcessingException parseError) {
                return mapTextFallbackResponse(request, content, parseError);
            }
        } catch (InterruptedException error) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("LLM 调用被中断", error);
        } catch (IOException error) {
            throw new IllegalStateException("LLM 调用失败：" + describeException(error), error);
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

            JsonNode root = sendChatCompletion(body, "LLM 问答请求");
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
            throw new IllegalStateException("LLM 问答调用失败：" + describeException(error), error);
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

            JsonNode root = sendChatCompletion(body, "LLM 连接测试");
            String answer = extractContent(root).trim();
            return answer.isBlank() ? "连接正常" : answer;
        } catch (InterruptedException error) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("LLM 连接测试被中断", error);
        } catch (IOException error) {
            throw new IllegalStateException("LLM 连接测试失败：" + describeException(error), error);
        }
    }

    private JsonNode sendChatCompletion(String body, String operation) throws IOException, InterruptedException {
        IOException lastIoError = null;
        IllegalStateException lastHttpError = null;

        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                HttpResponse<String> response = httpClient.send(
                        buildHttpRequest(body),
                        HttpResponse.BodyHandlers.ofString()
                );

                if (response.statusCode() >= 200 && response.statusCode() < 300) {
                    return objectMapper.readTree(response.body());
                }

                IllegalStateException httpError = new IllegalStateException(
                        operation + "失败，状态码：" + response.statusCode()
                                + "，响应：" + abbreviate(response.body())
                );
                if (!shouldRetryStatus(response.statusCode()) || attempt == MAX_ATTEMPTS) {
                    throw httpError;
                }
                lastHttpError = httpError;
            } catch (IOException error) {
                if (attempt == MAX_ATTEMPTS) {
                    throw error;
                }
                lastIoError = error;
            }

            sleepBeforeRetry();
        }

        if (lastHttpError != null) {
            throw lastHttpError;
        }
        if (lastIoError != null) {
            throw lastIoError;
        }

        throw new IllegalStateException(operation + "失败：未知错误");
    }

    private HttpRequest buildHttpRequest(String body) {
        return HttpRequest.newBuilder()
                .uri(URI.create(resolveEndpoint()))
                .timeout(Duration.ofSeconds(Math.max(5, properties.getTimeoutSeconds())))
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", "Bearer " + properties.getApiKey())
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
    }

    private boolean shouldRetryStatus(int statusCode) {
        return statusCode == 429 || statusCode >= 500;
    }

    private void sleepBeforeRetry() throws InterruptedException {
        Thread.sleep(450);
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

    private LlmAnalysisResponse mapTextFallbackResponse(
            LlmGenerateRequest request,
            String content,
            JsonProcessingException parseError
    ) {
        String text = normalizeModelContent(content);
        boolean looksLikeJson = text.startsWith("{");
        String title = firstNonBlank(extractStringField(text, "title"), "智能分析报告");
        String summary = firstNonBlank(
                extractStringField(text, "summary"),
                looksLikeJson
                        ? "模型返回了结构化报告，但内容没有完整闭合。系统已隐藏原始输出，并尽量保留可读的标题、摘要和段落。"
                        : abbreviate(text)
        );
        List<String> keywords = extractStringArrayField(text, "keywords");
        List<LlmAnalysisSectionResponse> sections = extractSectionArrayField(text, "sections");
        List<LlmRecommendationResponse> recommendations = extractRecommendationArrayField(text, "recommendations");
        List<String> evidence = extractStringArrayField(text, "evidence");
        List<String> warnings = new ArrayList<>(extractStringArrayField(text, "warnings"));

        if (keywords.isEmpty()) {
            keywords = looksLikeJson ? List.of("格式整理", "可读报告") : List.of("自然语言报告");
        }
        if (sections.isEmpty()) {
            sections = List.of(new LlmAnalysisSectionResponse(
                    looksLikeJson ? "报告内容整理说明" : "模型自然语言回答",
                    looksLikeJson
                            ? "DeepSeek 已经返回报告内容，但 JSON 在传输或长度限制处被截断。系统没有把原始 JSON 直接塞进正文，而是保留可解析的摘要和字段；建议重新生成以补齐推荐候选。"
                            : text
            ));
        }
        if (evidence.isEmpty()) {
            evidence = List.of("DeepSeek 已返回内容，系统已保留其中可解析、可展示的字段");
        }
        warnings.add("模型输出被截断或不是严格 JSON，系统已自动隐藏原始输出并整理为可读报告。");
        warnings.add("如果推荐候选不完整，请点击重新生成；系统已提高默认输出长度以降低再次截断概率。");
        warnings.add("解析状态：原始输出没有完整闭合，已保留可解析的正文内容。");

        return new LlmAnalysisResponse(
                request.analysisType(),
                title,
                summary,
                keywords,
                sections,
                recommendations,
                evidence,
                warnings,
                new LlmAnalysisScopeResponse(null, null, null),
                false,
                properties.getModel(),
                LocalDateTime.now()
        );
    }

    private String normalizeModelContent(String content) {
        String trimmed = content == null ? "" : content.trim();
        if (trimmed.startsWith("```")) {
            trimmed = trimmed
                    .replaceFirst("^```(?:json)?\\s*", "")
                    .replaceFirst("\\s*```$", "")
                    .trim();
        }
        return trimmed.isBlank() ? "模型返回了非标准结构，且没有可展示的正文。" : trimmed;
    }

    private String firstNonBlank(String value, String defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return value;
    }

    private String extractStringField(String content, String field) {
        String marker = "\"" + field + "\"";
        int fieldIndex = content.indexOf(marker);
        if (fieldIndex < 0) {
            return null;
        }

        int colonIndex = content.indexOf(':', fieldIndex + marker.length());
        if (colonIndex < 0) {
            return null;
        }

        int quoteIndex = content.indexOf('"', colonIndex + 1);
        if (quoteIndex < 0) {
            return null;
        }

        StringBuilder escapedValue = new StringBuilder();
        boolean escaped = false;
        for (int index = quoteIndex + 1; index < content.length(); index++) {
            char current = content.charAt(index);
            if (escaped) {
                escapedValue.append('\\').append(current);
                escaped = false;
                continue;
            }
            if (current == '\\') {
                escaped = true;
                continue;
            }
            if (current == '"') {
                return decodeJsonString(escapedValue.toString());
            }
            escapedValue.append(current);
        }

        return null;
    }

    private String decodeJsonString(String escapedValue) {
        try {
            return objectMapper.readValue("\"" + escapedValue + "\"", String.class);
        } catch (JsonProcessingException error) {
            return escapedValue;
        }
    }

    private List<String> extractStringArrayField(String content, String field) {
        String arrayPayload = extractCompleteJsonArray(content, field);
        if (arrayPayload == null) {
            return List.of();
        }

        try {
            JsonNode node = objectMapper.readTree(arrayPayload);
            List<String> values = new ArrayList<>();
            if (node.isArray()) {
                for (JsonNode item : node) {
                    if (!item.isNull()) {
                        values.add(item.asText());
                    }
                }
            }
            return values;
        } catch (JsonProcessingException error) {
            return List.of();
        }
    }

    private List<LlmAnalysisSectionResponse> extractSectionArrayField(String content, String field) {
        String arrayPayload = extractCompleteJsonArray(content, field);
        if (arrayPayload == null) {
            return List.of();
        }

        try {
            return sectionList(objectMapper.readTree(arrayPayload));
        } catch (JsonProcessingException error) {
            return List.of();
        }
    }

    private List<LlmRecommendationResponse> extractRecommendationArrayField(String content, String field) {
        String arrayPayload = extractCompleteJsonArray(content, field);
        if (arrayPayload == null) {
            return List.of();
        }

        try {
            return recommendationList(objectMapper.readTree(arrayPayload));
        } catch (JsonProcessingException error) {
            return List.of();
        }
    }

    private String extractCompleteJsonArray(String content, String field) {
        String marker = "\"" + field + "\"";
        int fieldIndex = content.indexOf(marker);
        if (fieldIndex < 0) {
            return null;
        }

        int colonIndex = content.indexOf(':', fieldIndex + marker.length());
        int startIndex = colonIndex < 0 ? -1 : content.indexOf('[', colonIndex + 1);
        if (startIndex < 0) {
            return null;
        }

        int depth = 0;
        boolean inString = false;
        boolean escaped = false;
        for (int index = startIndex; index < content.length(); index++) {
            char current = content.charAt(index);
            if (escaped) {
                escaped = false;
                continue;
            }
            if (current == '\\') {
                escaped = inString;
                continue;
            }
            if (current == '"') {
                inString = !inString;
                continue;
            }
            if (inString) {
                continue;
            }
            if (current == '[') {
                depth++;
            } else if (current == ']') {
                depth--;
                if (depth == 0) {
                    return content.substring(startIndex, index + 1);
                }
            }
        }

        return null;
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

    private String describeException(Exception error) {
        String message = error.getMessage();
        if (message == null || message.isBlank()) {
            return error.getClass().getSimpleName();
        }

        return message;
    }
}
