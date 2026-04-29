package com.mykr.acgnhistoryanalyzer.llm;

import com.mykr.acgnhistoryanalyzer.response.LlmAnalysisResponse;
import com.mykr.acgnhistoryanalyzer.response.LlmAnalysisScopeResponse;
import com.mykr.acgnhistoryanalyzer.response.LlmAnalysisSectionResponse;

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
}
