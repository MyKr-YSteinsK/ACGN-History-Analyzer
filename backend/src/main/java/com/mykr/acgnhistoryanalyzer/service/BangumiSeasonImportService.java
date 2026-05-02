package com.mykr.acgnhistoryanalyzer.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.mykr.acgnhistoryanalyzer.common.exception.BangumiImportException;
import com.mykr.acgnhistoryanalyzer.request.BangumiSubjectBatchImportRequest;
import com.mykr.acgnhistoryanalyzer.request.BangumiSubjectImportItemRequest;
import com.mykr.acgnhistoryanalyzer.request.BangumiSeasonImportRequest;
import com.mykr.acgnhistoryanalyzer.request.SubjectBatchImportRequest;
import com.mykr.acgnhistoryanalyzer.request.SubjectCreateRequest;
import com.mykr.acgnhistoryanalyzer.response.SubjectImportPreviewResponse;
import com.mykr.acgnhistoryanalyzer.response.SubjectImportItemResponse;
import com.mykr.acgnhistoryanalyzer.response.SubjectImportPreviewItemResponse;
import com.mykr.acgnhistoryanalyzer.response.SubjectImportResponse;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
public class BangumiSeasonImportService {

    private static final String BANGUMI_API_BASE = "https://api.bgm.tv/v0";
    private static final String BANGUMI_SUBJECT_BASE = "https://bangumi.tv/subject/";
    private static final String USER_AGENT = "ACGN-History-Analyzer/1.0 (local season import)";
    private static final int SUBJECT_TYPE_BOOK = 1;
    private static final int SUBJECT_TYPE_ANIME = 2;
    private static final int SUBJECT_TYPE_NOVEL = 3;
    private static final int SUBJECT_TYPE_GAME = 4;
    private static final int ANIME_CATEGORY_TV = 1;
    private static final int ANIME_CATEGORY_WEB = 5;
    private static final int PAGE_SIZE = 50;
    private static final int MAX_PAGE_COUNT = 8;
    private static final int MAX_FETCH_ATTEMPTS = 3;

    private final ObjectMapper objectMapper;
    private final SubjectService subjectService;
    private final FranchiseAutoLinkService franchiseAutoLinkService;
    private final HttpClient httpClient;

    public BangumiSeasonImportService(ObjectMapper objectMapper,
                                      SubjectService subjectService,
                                      FranchiseAutoLinkService franchiseAutoLinkService) {
        this.objectMapper = objectMapper;
        this.subjectService = subjectService;
        this.franchiseAutoLinkService = franchiseAutoLinkService;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    public SubjectImportPreviewResponse previewSeason(BangumiSeasonImportRequest request) {
        SubjectBatchImportRequest batchRequest = buildSubjectBatchImportRequest(request);
        return subjectService.previewImportSubjects(batchRequest);
    }

    public SubjectImportResponse importSeason(BangumiSeasonImportRequest request) {
        return importSeason(request, null);
    }

    public SubjectImportPreviewResponse previewSubjectsBySource(BangumiSubjectBatchImportRequest request) {
        PreparedSourceBatch preparedBatch = prepareSourceBatch(request);
        if (preparedBatch.validSubjects.isEmpty()) {
            return new SubjectImportPreviewResponse(
                    preparedBatch.totalCount,
                    0,
                    preparedBatch.errorCount,
                    0,
                    preparedBatch.errorPreviewItems
            );
        }

        SubjectBatchImportRequest batchRequest = new SubjectBatchImportRequest();
        batchRequest.setSubjects(preparedBatch.validSubjects);
        SubjectImportPreviewResponse serviceResponse = subjectService.previewImportSubjects(batchRequest);

        List<SubjectImportPreviewItemResponse> mergedItems = new ArrayList<>(serviceResponse.getItems());
        mergedItems.addAll(preparedBatch.errorPreviewItems);

        int canImportCount = serviceResponse.getCanImportCount();
        int updatedCount = serviceResponse.getUpdatedCount();
        int skippedCount = serviceResponse.getSkippedCount() + preparedBatch.errorCount;

        return new SubjectImportPreviewResponse(
                preparedBatch.totalCount,
                canImportCount,
                skippedCount,
                updatedCount,
                mergedItems
        );
    }

    public SubjectImportResponse importSubjectsBySource(BangumiSubjectBatchImportRequest request) {
        PreparedSourceBatch preparedBatch = prepareSourceBatch(request);
        if (preparedBatch.validSubjects.isEmpty()) {
            return new SubjectImportResponse(
                    preparedBatch.totalCount,
                    0,
                    preparedBatch.errorCount,
                    preparedBatch.errorImportItems
            );
        }

        SubjectBatchImportRequest batchRequest = new SubjectBatchImportRequest();
        batchRequest.setSubjects(preparedBatch.validSubjects);
        SubjectImportResponse serviceResponse = subjectService.importSubjects(batchRequest);

        List<SubjectImportItemResponse> mergedItems = new ArrayList<>(serviceResponse.getItems());
        mergedItems.addAll(preparedBatch.errorImportItems);

        SubjectImportResponse response = new SubjectImportResponse(
                preparedBatch.totalCount,
                serviceResponse.getImportedCount(),
                serviceResponse.getSkippedCount() + preparedBatch.errorCount,
                mergedItems
        );

        response.getItems().stream()
                .filter(item -> "IMPORTED".equals(item.getResult()))
                .map(SubjectImportItemResponse::getSubjectId)
                .filter(id -> id != null)
                .forEach(franchiseAutoLinkService::detectAndLinkForSubject);
        return response;
    }

    private SubjectBatchImportRequest buildSubjectBatchImportRequest(BangumiSeasonImportRequest request) {
        return buildSubjectBatchImportRequest(request, null);
    }

    public synchronized SubjectImportResponse importSeason(BangumiSeasonImportRequest request,
                                                           BangumiImportProgressReporter progressReporter) {
        SubjectBatchImportRequest batchRequest = buildSubjectBatchImportRequest(request, progressReporter);
        int totalCount = batchRequest.getSubjects().size();
        if (progressReporter != null) {
            progressReporter.update(55, "IMPORTING", "准备写入作品并缓存封面，共 " + totalCount + " 条");
        }

        SubjectImportResponse response = subjectService.importSubjects(
                batchRequest,
                (currentIndex, itemTotalCount, displayTitle) -> {
                    if (progressReporter == null) {
                        return;
                    }
                    int percent = itemTotalCount <= 0
                            ? 95
                            : 55 + (int) Math.floor((currentIndex - 1) * 40.0 / itemTotalCount);
                    progressReporter.update(
                            Math.min(95, Math.max(55, percent)),
                            "IMPORTING",
                            "正在写入并缓存封面 " + currentIndex + "/" + itemTotalCount + "：" + displayTitle
                    );
                }
        );

        if (progressReporter != null) {
            progressReporter.update(100, "DONE", "导入完成");
        }
        response.getItems().stream()
                .filter(item -> "IMPORTED".equals(item.getResult()))
                .map(SubjectImportItemResponse::getSubjectId)
                .filter(id -> id != null)
                .forEach(franchiseAutoLinkService::detectAndLinkForSubject);
        return response;
    }

    private SubjectBatchImportRequest buildSubjectBatchImportRequest(BangumiSeasonImportRequest request,
                                                                     BangumiImportProgressReporter progressReporter) {
        if (progressReporter != null) {
            progressReporter.update(5, "FETCHING", "正在连接 Bangumi，准备获取作品列表");
        }

        List<JsonNode> bangumiSubjects = fetchSeasonSubjects(request, progressReporter);
        if (progressReporter != null) {
            progressReporter.update(38, "MAPPING", "正在整理 Bangumi 字段与作品标签");
        }

        List<SubjectCreateRequest> subjects = bangumiSubjects.stream()
                .filter(subject -> shouldKeepSubject(subject, request))
                .filter(subject -> !isExcludedSubject(subject, request))
                .map(subject -> toSubjectCreateRequest(subject, request, false))
                .toList();

        if (progressReporter != null) {
            progressReporter.update(48, "CHECKING", "已整理 " + subjects.size() + " 条候选作品，准备进入导入校验");
        }

        SubjectBatchImportRequest batchRequest = new SubjectBatchImportRequest();
        batchRequest.setSubjects(subjects);
        return batchRequest;
    }

    private PreparedSourceBatch prepareSourceBatch(BangumiSubjectBatchImportRequest request) {
        List<SubjectCreateRequest> validSubjects = new ArrayList<>();
        List<SubjectImportPreviewItemResponse> errorPreviewItems = new ArrayList<>();
        List<SubjectImportItemResponse> errorImportItems = new ArrayList<>();

        for (BangumiSubjectImportItemRequest item : request.getSubjects()) {
            try {
                validSubjects.add(toSubjectCreateRequestFromSourceItem(item));
            } catch (Exception ex) {
                String displayTitle = buildSourceImportDisplayTitle(item);
                String message = buildSourceImportErrorMessage(ex);
                errorPreviewItems.add(new SubjectImportPreviewItemResponse(
                        displayTitle,
                        "ERROR",
                        message,
                        "BANGUMI",
                        item == null ? null : item.getSourceId()
                ));
                errorImportItems.add(new SubjectImportItemResponse(
                        null,
                        displayTitle,
                        "ERROR",
                        message
                ));
            }
        }

        return new PreparedSourceBatch(
                request.getSubjects().size(),
                validSubjects,
                errorPreviewItems,
                errorImportItems
        );
    }

    private SubjectCreateRequest toSubjectCreateRequestFromSourceItem(BangumiSubjectImportItemRequest item) {
        String sourceId = item.getSourceId().trim();
        JsonNode subject = fetchBangumiSubjectById(sourceId);
        BangumiSeasonImportRequest context = new BangumiSeasonImportRequest();
        context.setYear(item.getReleaseYear());
        context.setQuarter(item.getReleaseQuarter());
        context.setIncludeWeb(true);
        context.setJapaneseOnly(false);
        return toSubjectCreateRequest(subject, context, true);
    }

    private String buildSourceImportDisplayTitle(BangumiSubjectImportItemRequest item) {
        if (item == null || item.getSourceId() == null || item.getSourceId().trim().isEmpty()) {
            return "sourceId 未提供";
        }
        return "sourceId=" + item.getSourceId().trim();
    }

    private String buildSourceImportErrorMessage(Exception ex) {
        if (ex instanceof BangumiImportException) {
            return ex.getMessage();
        }
        String message = ex.getMessage();
        if (message == null || message.isBlank()) {
            return "读取 Bangumi 条目失败";
        }
        return "读取 Bangumi 条目失败：" + message;
    }

    private static final class PreparedSourceBatch {
        private final int totalCount;
        private final int errorCount;
        private final List<SubjectCreateRequest> validSubjects;
        private final List<SubjectImportPreviewItemResponse> errorPreviewItems;
        private final List<SubjectImportItemResponse> errorImportItems;

        private PreparedSourceBatch(int totalCount,
                                    List<SubjectCreateRequest> validSubjects,
                                    List<SubjectImportPreviewItemResponse> errorPreviewItems,
                                    List<SubjectImportItemResponse> errorImportItems) {
            this.totalCount = totalCount;
            this.errorCount = errorPreviewItems.size();
            this.validSubjects = validSubjects;
            this.errorPreviewItems = errorPreviewItems;
            this.errorImportItems = errorImportItems;
        }
    }

    private List<JsonNode> fetchSeasonSubjects(BangumiSeasonImportRequest request) {
        return fetchSeasonSubjects(request, null);
    }

    private List<JsonNode> fetchSeasonSubjects(BangumiSeasonImportRequest request,
                                               BangumiImportProgressReporter progressReporter) {
        int month = toBangumiMonth(request.getQuarter());
        Set<Integer> categories = new LinkedHashSet<>();
        categories.add(ANIME_CATEGORY_TV);
        if (Boolean.TRUE.equals(request.getIncludeWeb())) {
            categories.add(ANIME_CATEGORY_WEB);
        }

        Map<String, JsonNode> subjectById = new LinkedHashMap<>();
        int categoryIndex = 0;
        for (Integer category : categories) {
            categoryIndex++;
            if (progressReporter != null) {
                progressReporter.update(
                        categoryIndex == 1 ? 10 : 24,
                        "FETCHING",
                        "正在获取 Bangumi " + categoryName(category) + " 动画列表"
                );
            }
            fetchCategorySubjects(request.getYear(), month, category, progressReporter)
                    .forEach(subject -> {
                        String id = subject.path("id").asText("");
                        if (!id.isBlank()) {
                            subjectById.putIfAbsent(id, subject);
                        }
                    });
        }
        return new ArrayList<>(subjectById.values());
    }

    private List<JsonNode> fetchCategorySubjects(Integer year, int month, int category) {
        return fetchCategorySubjects(year, month, category, null);
    }

    private List<JsonNode> fetchCategorySubjects(Integer year, int month, int category,
                                                 BangumiImportProgressReporter progressReporter) {
        List<JsonNode> subjects = new ArrayList<>();
        int offset = 0;
        int pageCount = 0;

        while (pageCount < MAX_PAGE_COUNT) {
            if (progressReporter != null) {
                progressReporter.update(
                        Math.min(35, 12 + pageCount * 4),
                        "FETCHING",
                        "正在获取 " + categoryName(category) + " 第 " + (pageCount + 1) + " 页"
                );
            }
            JsonNode page = fetchBangumiSubjectsPage(year, month, category, offset);
            JsonNode data = page.path("data");
            if (!data.isArray() || data.isEmpty()) {
                break;
            }

            data.forEach(subjects::add);

            int total = page.path("total").asInt(subjects.size());
            int limit = page.path("limit").asInt(PAGE_SIZE);
            offset += limit;
            pageCount++;

            if (offset >= total) {
                break;
            }
        }

        return subjects;
    }

    private String categoryName(int category) {
        if (category == ANIME_CATEGORY_WEB) {
            return "WEB";
        }
        return "TV";
    }

    private JsonNode fetchBangumiSubjectsPage(Integer year, int month, int category, int offset) {
        String url = BANGUMI_API_BASE
                + "/subjects?type=" + SUBJECT_TYPE_ANIME
                + "&cat=" + category
                + "&year=" + year
                + "&month=" + month
                + "&sort=rank"
                + "&limit=" + PAGE_SIZE
                + "&offset=" + offset;
        return fetchBangumiJson(url, 20);
    }

    private JsonNode fetchBangumiSubjectById(String sourceId) {
        String normalizedSourceId = sourceId == null ? "" : sourceId.trim();
        if (normalizedSourceId.isBlank()) {
            throw new BangumiImportException("sourceId 不能为空");
        }
        String url = BANGUMI_API_BASE + "/subjects/" + normalizedSourceId;
        return fetchBangumiJson(url, 20);
    }

    private JsonNode fetchBangumiJson(String url, int timeoutSeconds) {
        HttpRequest httpRequest = HttpRequest.newBuilder(URI.create(url))
                .GET()
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .header("Accept", "application/json")
                .header("User-Agent", USER_AGENT)
                .build();

        IOException lastIoException = null;
        for (int attempt = 1; attempt <= MAX_FETCH_ATTEMPTS; attempt++) {
            try {
                HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() >= 200 && response.statusCode() < 300) {
                    return objectMapper.readTree(response.body());
                }

                if (!shouldRetryStatus(response.statusCode()) || attempt == MAX_FETCH_ATTEMPTS) {
                    throw new BangumiImportException(
                            "Bangumi 接口请求失败，HTTP " + response.statusCode() + "：" + abbreviate(response.body())
                    );
                }
            } catch (JsonProcessingException ex) {
                throw new BangumiImportException("Bangumi 接口返回内容不是合法 JSON：" + ex.getOriginalMessage(), ex);
            } catch (IOException ex) {
                lastIoException = ex;
                if (attempt == MAX_FETCH_ATTEMPTS) {
                    break;
                }
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new BangumiImportException("Bangumi 接口请求被中断", ex);
            }

            waitBeforeRetry(attempt);
        }

        throw new BangumiImportException(
                "Bangumi 接口暂时不可用，已重试 " + MAX_FETCH_ATTEMPTS + " 次仍失败：" + describeException(lastIoException),
                lastIoException
        );
    }

    private boolean shouldRetryStatus(int statusCode) {
        return statusCode == 429 || statusCode >= 500;
    }

    private void waitBeforeRetry(int attempt) {
        try {
            Thread.sleep(400L * attempt);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new BangumiImportException("Bangumi 接口请求被中断", ex);
        }
    }

    private String describeException(Exception ex) {
        if (ex == null) {
            return "未知网络错误";
        }

        String message = ex.getMessage();
        if (message == null || message.isBlank()) {
            return ex.getClass().getSimpleName();
        }

        return message;
    }

    private String abbreviate(String value) {
        if (value == null || value.isBlank()) {
            return "响应体为空";
        }

        String compact = value.replaceAll("\\s+", " ").trim();
        return compact.length() <= 160 ? compact : compact.substring(0, 160) + "...";
    }

    private SubjectCreateRequest toSubjectCreateRequest(JsonNode subject,
                                                        BangumiSeasonImportRequest request,
                                                        boolean mapCategoryByBangumiType) {
        SubjectCreateRequest createRequest = new SubjectCreateRequest();
        String bangumiId = subject.path("id").asText("");
        String name = subject.path("name").asText("");
        String nameCn = subject.path("name_cn").asText("");
        String displayTitle = firstText(nameCn, name, "Bangumi Subject " + bangumiId);

        createRequest.setTitleCn(displayTitle);
        createRequest.setDisplayTitle(displayTitle);
        createRequest.setSubtitle(hasText(nameCn) && hasText(name) && !nameCn.equals(name) ? name : null);
        createRequest.setSeasonIndex(resolveSeasonIndex(displayTitle, name));
        createRequest.setPartIndex(resolvePartIndex(displayTitle, name));
        createRequest.setCoverUrl(resolveCoverUrl(subject));
        createRequest.setSummary(truncate(subject.path("summary").asText(null), 2000));
        createRequest.setReleaseYear(request.getYear());
        createRequest.setReleaseQuarter(request.getQuarter());
        createRequest.setCategory(resolveSubjectCategory(subject, mapCategoryByBangumiType, bangumiId));
        createRequest.setStudioName(truncate(resolveStudioName(subject), 255));
        createRequest.setPlatformLink(BANGUMI_SUBJECT_BASE + bangumiId);
        createRequest.setPlatformScore(resolvePlatformScore(subject));
        createRequest.setEpisodeCount(resolveEpisodeCount(subject));
        createRequest.setTagNames(resolveTagNames(subject, request.getYear()));
        createRequest.setSourcePlatform("BANGUMI");
        createRequest.setSourceId(bangumiId);
        createRequest.setStatus("NORMAL");
        return createRequest;
    }

    private String resolveSubjectCategory(JsonNode subject, boolean mapCategoryByBangumiType, String bangumiId) {
        if (!mapCategoryByBangumiType) {
            return "ANIME";
        }
        int bangumiType = subject.path("type").asInt();
        return switch (bangumiType) {
            case SUBJECT_TYPE_ANIME -> "ANIME";
            case SUBJECT_TYPE_BOOK -> "COMIC";
            case SUBJECT_TYPE_NOVEL -> "NOVEL";
            case SUBJECT_TYPE_GAME -> "GAME";
            default -> throw new BangumiImportException(
                    "sourceId=" + bangumiId + " 的 Bangumi 类型不支持导入，type=" + bangumiType
            );
        };
    }

    private boolean isExcludedSubject(JsonNode subject, BangumiSeasonImportRequest request) {
        if (request.getExcludedSourceIds() == null || request.getExcludedSourceIds().isEmpty()) {
            return false;
        }

        String bangumiId = subject.path("id").asText("");
        return request.getExcludedSourceIds().stream()
                .filter(this::hasText)
                .anyMatch(excludedId -> excludedId.trim().equals(bangumiId));
    }

    private boolean shouldKeepSubject(JsonNode subject, BangumiSeasonImportRequest request) {
        if (subject.path("type").asInt() != SUBJECT_TYPE_ANIME) {
            return false;
        }
        if (subject.path("nsfw").asBoolean(false)) {
            return false;
        }

        String platform = subject.path("platform").asText("").toUpperCase(Locale.ROOT);
        boolean tvAnime = "TV".equals(platform);
        boolean webAnime = Boolean.TRUE.equals(request.getIncludeWeb()) && "WEB".equals(platform);
        if (!tvAnime && !webAnime) {
            return false;
        }

        String date = subject.path("date").asText("");
        if (hasText(date) && !date.startsWith(request.getYear() + "-" + String.format("%02d", toBangumiMonth(request.getQuarter())))) {
            return false;
        }

        return !Boolean.TRUE.equals(request.getJapaneseOnly()) || hasBangumiTag(subject, "日本");
    }

    private boolean hasBangumiTag(JsonNode subject, String tagName) {
        JsonNode metaTags = subject.path("meta_tags");
        if (metaTags.isArray()) {
            for (JsonNode tag : metaTags) {
                if (tagName.equals(tag.asText())) {
                    return true;
                }
            }
        }

        JsonNode tags = subject.path("tags");
        if (tags.isArray()) {
            for (JsonNode tag : tags) {
                if (tagName.equals(tag.path("name").asText())) {
                    return true;
                }
            }
        }

        return false;
    }

    private String resolveCoverUrl(JsonNode subject) {
        JsonNode images = subject.path("images");
        return firstText(
                images.path("large").asText(null),
                images.path("common").asText(null),
                subject.path("image").asText(null),
                images.path("medium").asText(null),
                images.path("small").asText(null)
        );
    }

    private BigDecimal resolvePlatformScore(JsonNode subject) {
        double score = subject.path("rating").path("score").asDouble(0);
        if (score <= 0) {
            return null;
        }
        return BigDecimal.valueOf(score);
    }

    private Integer resolveEpisodeCount(JsonNode subject) {
        int totalEpisodes = subject.path("total_episodes").asInt(0);
        if (totalEpisodes > 0) {
            return totalEpisodes;
        }

        int episodes = subject.path("eps").asInt(0);
        if (episodes > 0) {
            return episodes;
        }

        String infoboxEpisodes = findInfoboxValue(subject, "话数", "話数", "集数");
        if (!hasText(infoboxEpisodes)) {
            return null;
        }

        String digits = infoboxEpisodes.replaceAll("[^0-9]", "");
        if (!hasText(digits)) {
            return null;
        }

        try {
            return Integer.parseInt(digits);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String resolveStudioName(JsonNode subject) {
        return findInfoboxValue(
                subject,
                "动画制作",
                "動畫制作",
                "动画制作公司",
                "动画制片厂",
                "製作",
                "制作"
        );
    }

    private String findInfoboxValue(JsonNode subject, String... keys) {
        JsonNode infobox = subject.path("infobox");
        if (!infobox.isArray()) {
            return null;
        }

        Set<String> keySet = Set.of(keys);
        for (JsonNode item : infobox) {
            String key = item.path("key").asText("");
            if (keySet.contains(key)) {
                String value = flattenInfoValue(item.path("value"));
                if (hasText(value)) {
                    return value;
                }
            }
        }
        return null;
    }

    private String flattenInfoValue(JsonNode value) {
        if (value == null || value.isMissingNode() || value.isNull()) {
            return null;
        }
        if (value.isTextual() || value.isNumber() || value.isBoolean()) {
            return value.asText();
        }
        if (value.isArray()) {
            List<String> parts = new ArrayList<>();
            for (JsonNode item : value) {
                String text = flattenInfoValue(item);
                if (hasText(text)) {
                    parts.add(text);
                }
            }
            return String.join(" / ", parts);
        }
        if (value.isObject()) {
            String valueText = firstText(
                    value.path("v").asText(null),
                    value.path("name").asText(null),
                    value.path("text").asText(null)
            );
            if (hasText(valueText)) {
                return valueText;
            }
        }
        return null;
    }

    private List<String> resolveTagNames(JsonNode subject, Integer year) {
        Set<String> tagNames = new LinkedHashSet<>();
        addTagsFromArray(tagNames, subject.path("meta_tags"), year, true);
        addTagsFromArray(tagNames, subject.path("tags"), year, false);
        return tagNames.stream().limit(12).toList();
    }

    private void addTagsFromArray(Set<String> tagNames, JsonNode tags, Integer year, boolean plainArray) {
        if (!tags.isArray()) {
            return;
        }
        for (JsonNode tag : tags) {
            String name = plainArray ? tag.asText("") : tag.path("name").asText("");
            String normalized = normalizeTagName(name, year);
            if (hasText(normalized)) {
                tagNames.add(normalized);
            }
        }
    }

    private String normalizeTagName(String value, Integer year) {
        if (!hasText(value)) {
            return null;
        }

        String tag = value.trim();
        if (tag.length() > 32) {
            return null;
        }

        String lowerTag = tag.toLowerCase(Locale.ROOT);
        if (tag.equals("TV")
                || tag.equals("WEB")
                || tag.equals("OVA")
                || tag.equals("剧场版")
                || tag.equals("日本")
                || lowerTag.equals("bangumi")
                || tag.matches("\\d{4}")
                || tag.matches("\\d{4}年\\d{1,2}月")
                || (year != null && tag.contains(String.valueOf(year)) && tag.contains("月"))) {
            return null;
        }

        return normalizeAllowedTag(tag, lowerTag);
    }

    private String normalizeAllowedTag(String tag, String lowerTag) {
        String normalized = switch (lowerTag) {
            case "漫画改", "漫改" -> "漫画改";
            case "小说改", "轻小说改", "轻改", "ラノベ改" -> "小说改";
            case "游戏改", "galgame改", "gal改" -> "游戏改";
            case "原创", "原創" -> "原创";
            case "奇幻", "魔法" -> "奇幻";
            case "战斗", "动作", "热血" -> "战斗";
            case "冒险" -> "冒险";
            case "科幻", "sf" -> "科幻";
            case "悬疑", "推理" -> "悬疑";
            case "校园", "学园" -> "校园";
            case "青春" -> "青春";
            case "恋爱", "爱情" -> "恋爱";
            case "喜剧", "搞笑" -> "喜剧";
            case "日常" -> "日常";
            case "治愈" -> "治愈";
            case "致郁" -> "致郁";
            case "运动" -> "运动";
            case "音乐" -> "音乐";
            case "偶像" -> "偶像";
            case "机战", "机器人" -> "机战";
            case "异世界" -> "异世界";
            case "转生" -> "转生";
            case "后宫" -> "后宫";
            case "百合" -> "百合";
            case "乙女" -> "乙女";
            case "女性向" -> "女性向";
            case "历史" -> "历史";
            case "战争" -> "战争";
            case "职场" -> "职场";
            case "美食" -> "美食";
            case "家庭" -> "家庭";
            case "社团" -> "社团";
            case "泡面番", "短片" -> "短片";
            case "3d", "3dcg" -> "3D";
            case "子供向", "少儿" -> "少儿向";
            case "少年向" -> "少年向";
            case "少女向" -> "少女向";
            default -> null;
        };

        return isAllowedCommonTag(normalized) ? normalized : null;
    }

    private boolean isAllowedCommonTag(String tag) {
        if (!hasText(tag)) {
            return false;
        }

        return switch (tag) {
            case "漫画改", "小说改", "游戏改", "原创", "奇幻", "战斗", "冒险", "科幻",
                 "悬疑", "校园", "青春", "恋爱", "喜剧", "日常", "治愈", "致郁", "运动",
                 "音乐", "偶像", "机战", "异世界", "转生", "后宫", "百合", "乙女",
                 "女性向", "历史", "战争", "职场", "美食", "家庭", "社团", "短片",
                 "3D", "少儿向", "少年向", "少女向" -> true;
            default -> false;
        };
    }

    private Integer resolveSeasonIndex(String... titles) {
        String joinedTitle = joinTitles(titles);
        if (!hasText(joinedTitle)) {
            return null;
        }

        Integer chineseSeason = findChineseNumberBefore(joinedTitle, "季", "期");
        if (chineseSeason != null && chineseSeason > 1) {
            return chineseSeason;
        }

        Integer season = findArabicSeason(joinedTitle);
        return season != null && season > 1 ? season : null;
    }

    private Integer resolvePartIndex(String... titles) {
        String joinedTitle = joinTitles(titles);
        if (!hasText(joinedTitle)) {
            return null;
        }

        Integer chinesePart = findChineseNumberBefore(joinedTitle, "部分", "部", "クール");
        if (chinesePart != null && chinesePart > 1) {
            return chinesePart;
        }

        Integer part = findArabicPart(joinedTitle);
        return part != null && part > 1 ? part : null;
    }

    private String joinTitles(String... titles) {
        List<String> values = new ArrayList<>();
        for (String title : titles) {
            if (hasText(title)) {
                values.add(title);
            }
        }
        return String.join(" ", values);
    }

    private Integer findChineseNumberBefore(String value, String... suffixes) {
        for (String suffix : suffixes) {
            java.util.regex.Matcher matcher = java.util.regex.Pattern
                    .compile("第\\s*([0-9一二三四五六七八九十]+)\\s*" + java.util.regex.Pattern.quote(suffix))
                    .matcher(value);
            if (matcher.find()) {
                return parseChineseNumber(matcher.group(1));
            }
        }
        return null;
    }

    private Integer findArabicSeason(String value) {
        java.util.regex.Matcher matcher = java.util.regex.Pattern
                .compile("(?i)(?:season|s)\\s*([0-9]+)|([0-9]+)(?:st|nd|rd|th)\\s+season")
                .matcher(value);
        if (!matcher.find()) {
            return null;
        }
        return Integer.parseInt(firstText(matcher.group(1), matcher.group(2)));
    }

    private Integer findArabicPart(String value) {
        java.util.regex.Matcher matcher = java.util.regex.Pattern
                .compile("(?i)(?:part|p)\\s*([0-9]+)")
                .matcher(value);
        if (!matcher.find()) {
            return null;
        }
        return Integer.parseInt(matcher.group(1));
    }

    private Integer parseChineseNumber(String value) {
        if (!hasText(value)) {
            return null;
        }
        String trimmedValue = value.trim();
        if (trimmedValue.matches("[0-9]+")) {
            return Integer.parseInt(trimmedValue);
        }

        return switch (trimmedValue) {
            case "一" -> 1;
            case "二", "两" -> 2;
            case "三" -> 3;
            case "四" -> 4;
            case "五" -> 5;
            case "六" -> 6;
            case "七" -> 7;
            case "八" -> 8;
            case "九" -> 9;
            case "十" -> 10;
            default -> null;
        };
    }

    private int toBangumiMonth(Integer quarter) {
        return switch (quarter) {
            case 1 -> 1;
            case 2 -> 4;
            case 3 -> 7;
            case 4 -> 10;
            default -> throw new IllegalArgumentException("不支持的月份档期：" + quarter);
        };
    }

    private String firstText(String... values) {
        for (String value : values) {
            if (hasText(value)) {
                return value.trim();
            }
        }
        return null;
    }

    private String truncate(String value, int maxLength) {
        if (!hasText(value)) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.length() <= maxLength) {
            return trimmed;
        }
        return trimmed.substring(0, maxLength);
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
