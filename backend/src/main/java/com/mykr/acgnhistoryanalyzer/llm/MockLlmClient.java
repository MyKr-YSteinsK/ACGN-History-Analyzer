package com.mykr.acgnhistoryanalyzer.llm;

import com.mykr.acgnhistoryanalyzer.response.LlmAnalysisResponse;
import com.mykr.acgnhistoryanalyzer.response.LlmAnalysisScopeResponse;
import com.mykr.acgnhistoryanalyzer.response.LlmAnalysisSectionResponse;
import com.mykr.acgnhistoryanalyzer.response.LlmRecommendationResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class MockLlmClient implements LlmClient {

    private final LlmProperties properties;

    public MockLlmClient(LlmProperties properties) {
        this.properties = properties;
    }

    @Override
    public LlmAnalysisResponse generate(LlmGenerateRequest request) {
        Map<String, Object> context = request.context();
        Integer year = readInteger(context, "year");
        Integer quarter = readInteger(context, "quarter");
        String category = readString(context, "category");
        String analysisType = readStringValue(context, "analysisType", request.analysisType());
        String displayCategory = category == null ? "全部类型" : category;
        Integer totalRecords = readInteger(context, "totalRecords");
        Integer highScoreCount = readInteger(context, "highScoreCount");
        Integer lowScoreCount = readInteger(context, "lowScoreCount");
        Integer yearOverYearDiff = readInteger(context, "yearOverYearDiff");
        String averageScore = readStringValue(context, "averageScore", "0.0");

        return new LlmAnalysisResponse(
                request.analysisType(),
                buildTitle(analysisType, year, quarter, displayCategory),
                buildSummary(analysisType, year, quarter, displayCategory, totalRecords, averageScore, highScoreCount),
                buildKeywords(context, analysisType),
                buildSections(context, totalRecords, averageScore, highScoreCount, lowScoreCount, yearOverYearDiff),
                buildRecommendations(context),
                buildEvidence(context, request),
                List.of(
                        "当前为 Mock 模式：报告基于真实统计上下文生成，但不是外部大模型的自然语言输出。",
                        "AI 模块只读数据，不会自动修改评分、标签、记录或收藏状态。"
                ),
                new LlmAnalysisScopeResponse(year, quarter, category),
                true,
                properties.getModel(),
                LocalDateTime.now()
        );
    }

    private String buildTitle(String analysisType, Integer year, Integer quarter, String displayCategory) {
        if ("TYPE_PREFERENCE".equals(analysisType)) {
            return displayCategory + " 长期类型偏好分析";
        }

        if ("SCORE_STRUCTURE".equals(analysisType)) {
            return displayCategory + " 个人评分结构分析";
        }

        if ("TAG_PREFERENCE".equals(analysisType)) {
            return displayCategory + " 标签偏好分析";
        }

        if ("RECOMMENDATION".equals(analysisType)) {
            return displayCategory + " 智能推荐池";
        }

        if ("COLLECTION_BACKLOG".equals(analysisType)) {
            return displayCategory + " 收藏夹补坑建议";
        }

        if ("QUARTERLY".equals(analysisType)) {
            return year + " 年 " + quarterLabel(quarter) + "档期 " + displayCategory + " 智能总结";
        }

        if (year == null) {
            return displayCategory + " ACGN 智能分析预览";
        }

        return year + " 年 " + displayCategory + " 年度智能总结";
    }

    private String buildSummary(
            String analysisType,
            Integer year,
            Integer quarter,
            String displayCategory,
            Integer totalRecords,
            String averageScore,
            Integer highScoreCount
    ) {
        String scope = buildScopeText(analysisType, year, quarter, displayCategory);

        if (totalRecords == null || totalRecords == 0) {
            return scope + " 暂时没有正式记录。系统已经跑通真实统计上下文，后续有数据后会自动生成更具体的偏好解释。";
        }

        if ("SCORE_STRUCTURE".equals(analysisType)) {
            return scope + " 共纳入 " + totalRecords
                    + " 条正式记录，均分约 " + averageScore
                    + " / 50，高分记录 " + highScoreCount
                    + " 条。这个模块重点解释你的评分分布，而不是评价作品本身。";
        }

        if ("TAG_PREFERENCE".equals(analysisType)) {
            return scope + " 共纳入 " + totalRecords
                    + " 条正式记录，系统会根据标签出现次数、标签均分和高分命中数量，区分“经常看”和“真正容易打高分”的方向。";
        }

        if ("RECOMMENDATION".equals(analysisType)) {
            return scope + " 会从作品库未记录作品和收藏夹中挑选候选，并优先考虑想看、搁置以及命中历史高分标签的作品。";
        }

        if ("COLLECTION_BACKLOG".equals(analysisType)) {
            return scope + " 只聚焦想看和搁置清单，目标不是随机推荐新作品，而是帮你判断哪些收藏更适合优先补完。";
        }

        return scope + " 共整理到 " + totalRecords
                + " 条正式记录，个人均分约 " + averageScore
                + " / 50，其中 45 分以上高分记录 " + highScoreCount
                + " 条。下面的结论来自数据库统计，不来自模型臆测。";
    }

    private List<String> buildKeywords(Map<String, Object> context, String analysisType) {
        List<String> keywords = new java.util.ArrayList<>();
        keywords.add("真实统计");
        keywords.add(switch (analysisType) {
            case "QUARTERLY" -> "月份总结";
            case "TYPE_PREFERENCE" -> "类型偏好";
            case "SCORE_STRUCTURE" -> "评分结构";
            case "TAG_PREFERENCE" -> "标签偏好";
            case "RECOMMENDATION" -> "智能推荐";
            case "COLLECTION_BACKLOG" -> "补坑建议";
            default -> "年度总结";
        });

        for (Map<String, Object> tag : readMapList(context, "topTags")) {
            Object name = tag.get("name");
            if (name != null && keywords.size() < 6) {
                keywords.add(String.valueOf(name));
            }
        }

        if (keywords.size() < 4) {
            keywords.add("结构化依据");
            keywords.add("Mock 生成");
        }

        return keywords;
    }

    private List<LlmAnalysisSectionResponse> buildSections(
            Map<String, Object> context,
            Integer totalRecords,
            String averageScore,
            Integer highScoreCount,
            Integer lowScoreCount,
            Integer yearOverYearDiff
    ) {
        String comparisonLabel = readStringValue(context, "comparisonLabel", "上一年同范围");

        if ("SCORE_STRUCTURE".equals(readStringValue(context, "analysisType", ""))) {
            return List.of(
                    new LlmAnalysisSectionResponse(
                            "评分分布",
                            buildScoreBandContent(context, totalRecords, averageScore, highScoreCount, lowScoreCount)
                    ),
                    new LlmAnalysisSectionResponse(
                            "类型差异",
                            buildCategoryScoreContent(context)
                    ),
                    new LlmAnalysisSectionResponse(
                            "年份变化",
                            buildYearScoreTrendContent(context)
                    ),
                    new LlmAnalysisSectionResponse(
                            "高低分样本",
                            buildHighLowRecordContent(context)
                    )
            );
        }

        if ("TAG_PREFERENCE".equals(readStringValue(context, "analysisType", ""))) {
            return List.of(
                    new LlmAnalysisSectionResponse(
                            "高频标签",
                            buildTopTagContent(context)
                    ),
                    new LlmAnalysisSectionResponse(
                            "偏好分组",
                            buildTagPreferenceGroupContent(context)
                    ),
                    new LlmAnalysisSectionResponse(
                            "高分线索",
                            buildTopRecordContent(context)
                    ),
                    new LlmAnalysisSectionResponse(
                            "使用建议",
                            "标签偏好适合后续推荐系统使用：高频高分标签可以作为强偏好，低频高分标签可以作为探索方向，高频低分标签则应被视为风险提示。"
                    )
            );
        }

        if ("RECOMMENDATION".equals(readStringValue(context, "analysisType", ""))) {
            return List.of(
                    new LlmAnalysisSectionResponse(
                            "推荐池规则",
                            "候选来自作品库未记录作品、想看清单和搁置清单；已经有正式记录的作品会被排除。排序会优先考虑 WANT > PAUSED > 普通作品库，再叠加高分标签匹配和平台评分参考。"
                    ),
                    new LlmAnalysisSectionResponse(
                            "高优先级候选",
                            buildRecommendationSummaryContent(context)
                    ),
                    new LlmAnalysisSectionResponse(
                            "匹配依据",
                            "推荐理由会显示来源、命中的历史高分标签、平台评分参考和可能风险。平台评分只作为辅助信息，不会替代你的个人评分偏好。"
                    ),
                    new LlmAnalysisSectionResponse(
                            "使用建议",
                            "高优先级适合近期补，MEDIUM 适合放入候选，LOW 更适合作为探索或等待更多数据后再判断。"
                    )
            );
        }

        if ("COLLECTION_BACKLOG".equals(readStringValue(context, "analysisType", ""))) {
            return List.of(
                    new LlmAnalysisSectionResponse(
                            "补坑范围",
                            "这个模块只看收藏夹中的想看和搁置作品，并继续排除已经正式记录的作品。它适合用来整理“我到底先补哪个”。"
                    ),
                    new LlmAnalysisSectionResponse(
                            "优先补完",
                            buildRecommendationSummaryContent(context)
                    ),
                    new LlmAnalysisSectionResponse(
                            "搁置恢复判断",
                            "搁置作品会保留风险提示：如果缺少高分标签命中，或当初可能因为节奏不合被搁置，就不建议强行立刻补完。"
                    ),
                    new LlmAnalysisSectionResponse(
                            "使用建议",
                            "想看清单里命中高分标签的作品优先级最高；搁置作品则更适合在状态合适时恢复，而不是被推荐分数硬推着看。"
                    )
            );
        }

        return List.of(
                new LlmAnalysisSectionResponse(
                        "整体趋势",
                        buildTrendContent(
                                totalRecords,
                                averageScore,
                                highScoreCount,
                                lowScoreCount,
                                yearOverYearDiff,
                                comparisonLabel
                        )
                ),
                new LlmAnalysisSectionResponse(
                        "类型与月份结构",
                        "类型分布为：" + formatMap(readLongMap(context, "categoryDistribution"))
                                + "。月份档期分布为：" + formatMap(readLongMap(context, "quarterDistribution"))
                                + "。这些数据适合后续判断偏好是集中在某个媒介，还是在不同媒介之间分散探索。"
                ),
                new LlmAnalysisSectionResponse(
                        "高分线索",
                        buildTopRecordContent(context)
                ),
                new LlmAnalysisSectionResponse(
                        "后续建议",
                        buildCandidateContent(context)
                )
        );
    }

    private String buildTrendContent(
            Integer totalRecords,
            String averageScore,
            Integer highScoreCount,
            Integer lowScoreCount,
            Integer yearOverYearDiff,
            String comparisonLabel
    ) {
        if (totalRecords == null || totalRecords == 0) {
            return "这个范围内还没有正式记录，因此系统只保留分析入口，不做偏好判断。";
        }

        return "本范围内共有 " + totalRecords + " 条正式记录，个人均分 " + averageScore
                + " / 50，高分记录 " + highScoreCount
                + " 条，低分记录 " + lowScoreCount
                + " 条。与" + comparisonLabel + "相比变化 " + formatDiff(yearOverYearDiff)
                + " 条，可以作为活跃度变化的第一层解释。";
    }

    private String buildTopRecordContent(Map<String, Object> context) {
        List<Map<String, Object>> topRecords = readMapList(context, "topRecords");
        List<Map<String, Object>> topTags = readMapList(context, "topTags");

        if (topRecords.isEmpty()) {
            return "当前范围没有可用于排序的个人评分记录，因此暂不生成高分共性判断。";
        }

        String recordsText = topRecords.stream()
                .map(item -> item.get("title") + "（" + item.get("score") + "）")
                .collect(java.util.stream.Collectors.joining("、"));
        String tagsText = topTags.stream()
                .limit(5)
                .map(item -> item.get("name") + "，均分 " + item.get("averageScore"))
                .collect(java.util.stream.Collectors.joining("；"));

        return "评分靠前的作品包括 " + recordsText + "。"
                + (tagsText.isBlank()
                ? "当前标签数据还不够完整，后续可以通过作品标签增强偏好解释。"
                : "高频标签线索为：" + tagsText + "。");
    }

    private String buildCandidateContent(Map<String, Object> context) {
        Map<String, Long> collectionCandidates = readLongMap(context, "collectionCandidates");
        Object libraryCandidateCount = context.getOrDefault("libraryCandidateCount", 0);

        return "推荐池会优先考虑收藏夹：想看 " + collectionCandidates.getOrDefault("WANT", 0L)
                + " 条，搁置 " + collectionCandidates.getOrDefault("PAUSED", 0L)
                + " 条；普通作品库中仍有 " + libraryCandidateCount
                + " 条未记录候选。下一阶段可以用高分标签和类型偏好为这些候选打分。";
    }

    private String buildScoreBandContent(
            Map<String, Object> context,
            Integer totalRecords,
            String averageScore,
            Integer highScoreCount,
            Integer lowScoreCount
    ) {
        return "当前样本共 " + totalRecords + " 条，均分 " + averageScore
                + " / 50，高分记录 " + highScoreCount
                + " 条，低分记录 " + lowScoreCount
                + " 条。评分分段为：" + formatMap(readLongMap(context, "scoreBands"))
                + "。如果高分段集中，说明评分标准偏认可型；如果中段较多，说明你更常给出保留判断。";
    }

    private String buildCategoryScoreContent(Map<String, Object> context) {
        List<Map<String, Object>> categoryStats = readMapList(context, "categoryScoreStats");
        if (categoryStats.isEmpty()) {
            return "当前没有足够的类型评分数据。";
        }

        return categoryStats.stream()
                .map(item -> item.get("category") + "："
                        + item.get("count") + " 条，均分 "
                        + item.get("averageScore") + "，高分 "
                        + item.get("highScoreCount") + " 条")
                .collect(java.util.stream.Collectors.joining("；"));
    }

    private String buildYearScoreTrendContent(Map<String, Object> context) {
        List<Map<String, Object>> trend = readMapList(context, "yearScoreTrend");
        if (trend.isEmpty()) {
            return "当前没有跨年份评分趋势样本。";
        }

        return trend.stream()
                .map(item -> item.get("year") + " 年："
                        + item.get("count") + " 条，均分 "
                        + item.get("averageScore") + "，高分 "
                        + item.get("highScoreCount") + " 条")
                .collect(java.util.stream.Collectors.joining("；"));
    }

    private String buildHighLowRecordContent(Map<String, Object> context) {
        String highRecords = formatRecordList(readMapList(context, "topRecords"));
        String lowRecords = formatRecordList(readMapList(context, "lowScoreRecords"));

        return "高分样本：" + highRecords + "。低分样本：" + lowRecords
                + "。这两组样本后续可以帮助真实模型解释你的评分边界。";
    }

    private String buildTopTagContent(Map<String, Object> context) {
        List<Map<String, Object>> topTags = readMapList(context, "topTags");
        if (topTags.isEmpty()) {
            return "当前作品标签还不够完整，暂时无法稳定分析标签偏好。";
        }

        return topTags.stream()
                .map(item -> item.get("name") + "：出现 "
                        + item.get("count") + " 次，均分 "
                        + item.get("averageScore") + "，高分命中 "
                        + item.get("highScoreCount") + " 次")
                .collect(java.util.stream.Collectors.joining("；"));
    }

    private String buildTagPreferenceGroupContent(Map<String, Object> context) {
        Map<String, List<Map<String, Object>>> groups = readTagPreferenceGroups(context);
        return "高频高分：" + formatTagGroup(groups.get("highFrequencyHighScore"))
                + "。高频低分：" + formatTagGroup(groups.get("highFrequencyLowScore"))
                + "。低频高分：" + formatTagGroup(groups.get("lowFrequencyHighScore"))
                + "。低频低分：" + formatTagGroup(groups.get("lowFrequencyLowScore")) + "。";
    }

    private String buildRecommendationSummaryContent(Map<String, Object> context) {
        List<LlmRecommendationResponse> recommendations = buildRecommendations(context);
        if (recommendations.isEmpty()) {
            return "当前没有可推荐候选。可能是作品库未记录作品较少，或收藏夹作品都已经完成正式记录。";
        }

        return recommendations.stream()
                .limit(5)
                .map(item -> item.title() + "（" + sourceLabel(item.source())
                        + "，" + priorityLabel(item.priority()) + "，匹配分 "
                        + item.matchScore() + "）")
                .collect(java.util.stream.Collectors.joining("；"));
    }

    private List<String> buildEvidence(Map<String, Object> context, LlmGenerateRequest request) {
        List<String> evidence = new java.util.ArrayList<>(readStringList(context, "evidence"));
        evidence.add("当前模型提供者：" + properties.getProvider());
        evidence.add("Prompt 已生成，长度约 " + request.prompt().length() + " 字");
        return evidence;
    }

    private String buildScopeText(
            String analysisType,
            Integer year,
            Integer quarter,
            String displayCategory
    ) {
        if ("TYPE_PREFERENCE".equals(analysisType)) {
            return displayCategory + " 长期类型偏好范围";
        }

        if ("SCORE_STRUCTURE".equals(analysisType)) {
            return displayCategory + " 评分结构范围";
        }

        if ("TAG_PREFERENCE".equals(analysisType)) {
            return displayCategory + " 标签偏好范围";
        }

        if ("RECOMMENDATION".equals(analysisType)) {
            return displayCategory + " 推荐池范围";
        }

        if ("COLLECTION_BACKLOG".equals(analysisType)) {
            return displayCategory + " 收藏夹补坑范围";
        }

        if ("QUARTERLY".equals(analysisType)) {
            return year + " 年 " + quarterLabel(quarter) + "档期 " + displayCategory;
        }

        return year + " 年 " + displayCategory;
    }

    private String quarterLabel(Integer quarter) {
        if (quarter == null) {
            return "未知月份";
        }

        return switch (quarter) {
            case 1 -> "一月";
            case 2 -> "四月";
            case 3 -> "七月";
            case 4 -> "十月";
            default -> "未知月份";
        };
    }

    private Integer readInteger(Map<String, Object> context, String key) {
        Object value = context.get(key);
        if (value instanceof Integer integer) {
            return integer;
        }
        if (value instanceof Long longValue) {
            return longValue.intValue();
        }
        return null;
    }

    private String readString(Map<String, Object> context, String key) {
        Object value = context.get(key);
        if (value instanceof String string && !string.isBlank()) {
            return string;
        }
        return null;
    }

    private String readStringValue(Map<String, Object> context, String key, String fallback) {
        Object value = context.get(key);
        if (value == null) {
            return fallback;
        }
        return String.valueOf(value);
    }

    private List<Map<String, Object>> readMapList(Map<String, Object> context, String key) {
        Object value = context.get(key);
        if (!(value instanceof List<?> list)) {
            return List.of();
        }

        return list.stream()
                .filter(Map.class::isInstance)
                .map(item -> {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> map = (Map<String, Object>) item;
                    return map;
                })
                .toList();
    }

    private List<String> readStringList(Map<String, Object> context, String key) {
        Object value = context.get(key);
        if (!(value instanceof List<?> list)) {
            return List.of();
        }

        return list.stream()
                .map(String::valueOf)
                .toList();
    }

    private Map<String, List<Map<String, Object>>> readTagPreferenceGroups(
            Map<String, Object> context
    ) {
        Object value = context.get("tagPreferenceGroups");
        if (!(value instanceof Map<?, ?> map)) {
            return Map.of();
        }

        java.util.Map<String, List<Map<String, Object>>> result = new java.util.LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            Object rawValue = entry.getValue();
            if (rawValue instanceof List<?> list) {
                List<Map<String, Object>> items = list.stream()
                        .filter(Map.class::isInstance)
                        .map(item -> {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> typedItem = (Map<String, Object>) item;
                            return typedItem;
                        })
                        .toList();
                result.put(String.valueOf(entry.getKey()), items);
            }
        }

        return result;
    }

    private List<LlmRecommendationResponse> buildRecommendations(Map<String, Object> context) {
        return readMapList(context, "recommendations").stream()
                .map(item -> new LlmRecommendationResponse(
                        readLongValue(item, "subjectId"),
                        readItemString(item, "title"),
                        readItemString(item, "coverUrl"),
                        readItemString(item, "category"),
                        readItemStringList(item, "tags"),
                        readItemString(item, "franchiseName"),
                        readItemString(item, "source"),
                        readItemString(item, "reason"),
                        readItemString(item, "risk"),
                        readItemString(item, "evidence"),
                        readItemString(item, "priority"),
                        readItemInteger(item, "matchScore"),
                        readBigDecimalValue(item, "platformScore")
                ))
                .toList();
    }

    private Map<String, Long> readLongMap(Map<String, Object> context, String key) {
        Object value = context.get(key);
        if (!(value instanceof Map<?, ?> map)) {
            return Map.of();
        }

        java.util.Map<String, Long> result = new java.util.LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            Object rawValue = entry.getValue();
            if (rawValue instanceof Number number) {
                result.put(String.valueOf(entry.getKey()), number.longValue());
            }
        }
        return result;
    }

    private String formatMap(Map<String, Long> map) {
        if (map.isEmpty()) {
            return "暂无数据";
        }

        return map.entrySet().stream()
                .map(entry -> entry.getKey() + " " + entry.getValue() + " 条")
                .collect(java.util.stream.Collectors.joining("、"));
    }

    private String formatDiff(Integer diff) {
        if (diff == null) {
            return "0";
        }
        return diff > 0 ? "+" + diff : String.valueOf(diff);
    }

    private String formatRecordList(List<Map<String, Object>> records) {
        if (records == null || records.isEmpty()) {
            return "暂无";
        }

        return records.stream()
                .map(item -> item.get("title") + "（" + item.get("score") + "）")
                .collect(java.util.stream.Collectors.joining("、"));
    }

    private String formatTagGroup(List<Map<String, Object>> tags) {
        if (tags == null || tags.isEmpty()) {
            return "暂无稳定样本";
        }

        return tags.stream()
                .map(item -> item.get("name") + "（" + item.get("count")
                        + " 次，均分 " + item.get("averageScore") + "）")
                .collect(java.util.stream.Collectors.joining("、"));
    }

    private String sourceLabel(String source) {
        return switch (source) {
            case "WANT" -> "想看";
            case "PAUSED" -> "搁置";
            default -> "作品库";
        };
    }

    private String priorityLabel(String priority) {
        return switch (priority) {
            case "HIGH" -> "高优先级";
            case "MEDIUM" -> "中优先级";
            default -> "低优先级";
        };
    }

    private Long readLongValue(Map<String, Object> item, String key) {
        Object value = item.get(key);
        if (value instanceof Number number) {
            return number.longValue();
        }
        return null;
    }

    private Integer readItemInteger(Map<String, Object> item, String key) {
        Object value = item.get(key);
        if (value instanceof Number number) {
            return number.intValue();
        }
        return null;
    }

    private String readItemString(Map<String, Object> item, String key) {
        Object value = item.get(key);
        return value == null ? null : String.valueOf(value);
    }

    private List<String> readItemStringList(Map<String, Object> item, String key) {
        Object value = item.get(key);
        if (!(value instanceof List<?> list)) {
            return List.of();
        }

        return list.stream()
                .map(String::valueOf)
                .toList();
    }

    private BigDecimal readBigDecimalValue(Map<String, Object> item, String key) {
        Object value = item.get(key);
        if (value instanceof BigDecimal bigDecimal) {
            return bigDecimal;
        }
        if (value instanceof Number number) {
            return BigDecimal.valueOf(number.doubleValue());
        }
        return null;
    }
}
