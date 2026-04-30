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
                buildSummary(
                        context,
                        analysisType,
                        year,
                        quarter,
                        displayCategory,
                        totalRecords,
                        averageScore,
                        highScoreCount
                ),
                buildKeywords(context, analysisType),
                buildSections(context, totalRecords, averageScore, highScoreCount, lowScoreCount, yearOverYearDiff),
                buildRecommendations(context),
                buildEvidence(context, request),
                List.of(
                        "分析结果基于系统内的记录、标签、评分与收藏数据生成，适合用于个人复盘与展示。",
                        "智能分析模块只读数据，不会自动修改评分、标签、记录或收藏状态。"
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

        if ("TASTE_PROFILE".equals(analysisType)) {
            return displayCategory + " 个人口味画像";
        }

        if ("PREFERENCE_CONTRADICTION".equals(analysisType)) {
            return displayCategory + " 偏好矛盾分析";
        }

        if ("INTEREST_TREND".equals(analysisType)) {
            return displayCategory + " 兴趣变化趋势分析";
        }

        if ("FRANCHISE_COMPLETION".equals(analysisType)) {
            return displayCategory + " 系列补完分析";
        }

        if ("COMPREHENSIVE_REPORT".equals(analysisType)) {
            return year + " 年 " + displayCategory + " 一键智能分析报告";
        }

        if ("HOME_INSIGHT".equals(analysisType)) {
            return year + " 年 " + quarterLabel(quarter) + " " + displayCategory + " 首页图表解读";
        }

        if ("QUESTION_ANSWER".equals(analysisType)) {
            return "自然语言问答";
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
            Map<String, Object> context,
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

        if ("TASTE_PROFILE".equals(analysisType)) {
            return scope + " 会把类型分布、评分结构、标签命中和高低分样本合在一起，生成一份克制版个人口味画像。";
        }

        if ("PREFERENCE_CONTRADICTION".equals(analysisType)) {
            return scope + " 会重点寻找“看得多但不一定喜欢”和“看得少但命中很高”的方向，用来解释偏好边界。";
        }

        if ("INTEREST_TREND".equals(analysisType)) {
            Map<String, Object> signals = readObjectMap(context, "interestTrendSignals");
            return scope + " 会按年份整理记录数量、均分、类型重心和标签线索。当前覆盖 "
                    + signals.getOrDefault("coveredYears", 0)
                    + " 个年份，从 " + signals.getOrDefault("earliestYear", "未知年份")
                    + " 到 " + signals.getOrDefault("latestYear", "未知年份")
                    + "，适合解释个人兴趣是扩大、收缩还是转向。";
        }

        if ("FRANCHISE_COMPLETION".equals(analysisType)) {
            Map<String, Long> stats = readLongMap(context, "franchiseCompletionStats");
            return scope + " 会围绕已有正式记录触发的系列进行整理，当前纳入 "
                    + stats.getOrDefault("franchiseCount", 0L)
                    + " 个系列、" + stats.getOrDefault("seriesSubjectCount", 0L)
                    + " 部系列内作品，其中仍有 "
                    + stats.getOrDefault("unrecordedSubjectCount", 0L)
                    + " 部未形成正式记录。";
        }

        if ("COMPREHENSIVE_REPORT".equals(analysisType)) {
            return scope + " 会整合年度统计、长期评分结构、标签偏好、口味画像、偏好矛盾和推荐候选。"
                    + "本次年度样本 " + totalRecords + " 条，并会同时参考长期正式记录。";
        }

        if ("HOME_INSIGHT".equals(analysisType)) {
            return scope + " 是首页统计图表的轻量解读：当前筛选下有 "
                    + totalRecords + " 条正式记录，均分 " + averageScore
                    + " / 50，高分样本 " + highScoreCount + " 条。";
        }

        if ("QUESTION_ANSWER".equals(analysisType)) {
            return scope + " 已根据问题识别出对应范围，命中 "
                    + totalRecords + " 条正式记录，均分 " + averageScore
                    + " / 50，高分样本 " + highScoreCount + " 条。";
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
            case "TASTE_PROFILE" -> "口味画像";
            case "PREFERENCE_CONTRADICTION" -> "偏好矛盾";
            case "INTEREST_TREND" -> "兴趣趋势";
            case "FRANCHISE_COMPLETION" -> "系列补完";
            case "COMPREHENSIVE_REPORT" -> "一键报告";
            case "HOME_INSIGHT" -> "首页解读";
            case "QUESTION_ANSWER" -> "自然问答";
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
            keywords.add("智能分析");
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

        if ("TASTE_PROFILE".equals(readStringValue(context, "analysisType", ""))) {
            return List.of(
                    new LlmAnalysisSectionResponse(
                            "画像概览",
                            buildTasteProfileOverviewContent(
                                    context,
                                    totalRecords,
                                    averageScore,
                                    highScoreCount,
                                    lowScoreCount
                            )
                    ),
                    new LlmAnalysisSectionResponse(
                            "稳定偏好",
                            buildStablePreferenceContent(context)
                    ),
                    new LlmAnalysisSectionResponse(
                            "探索倾向",
                            buildExplorationPreferenceContent(context)
                    ),
                    new LlmAnalysisSectionResponse(
                            "边界与风险",
                            buildPreferenceBoundaryContent(context)
                    )
            );
        }

        if ("PREFERENCE_CONTRADICTION".equals(readStringValue(context, "analysisType", ""))) {
            return List.of(
                    new LlmAnalysisSectionResponse(
                            "矛盾概览",
                            buildContradictionOverviewContent(context, totalRecords, averageScore)
                    ),
                    new LlmAnalysisSectionResponse(
                            "看得多但不稳",
                            buildDenseLowScoreContent(context)
                    ),
                    new LlmAnalysisSectionResponse(
                            "低频高命中",
                            buildSparseHighScoreContent(context)
                    ),
                    new LlmAnalysisSectionResponse(
                            "解释建议",
                            "这类分析适合辅助推荐和自我理解，不适合直接当成绝对结论。样本少的标签要保守看待，样本多但均分偏低的标签则更适合作为风险提醒。"
                    )
            );
        }

        if ("INTEREST_TREND".equals(readStringValue(context, "analysisType", ""))) {
            return List.of(
                    new LlmAnalysisSectionResponse(
                            "趋势概览",
                            buildInterestTrendOverviewContent(context, totalRecords, averageScore)
                    ),
                    new LlmAnalysisSectionResponse(
                            "年度变化",
                            buildInterestTrendYearContent(context)
                    ),
                    new LlmAnalysisSectionResponse(
                            "类型转向",
                            buildInterestTrendCategoryContent(context)
                    ),
                    new LlmAnalysisSectionResponse(
                            "标签迁移",
                            buildInterestTrendTagContent(context)
                    ),
                    new LlmAnalysisSectionResponse(
                            "解释建议",
                            "兴趣趋势更适合看长期方向，不适合把单一年份的偶然高低分直接当成口味转变。样本年份越多，趋势解释越稳定。"
                    )
            );
        }

        if ("FRANCHISE_COMPLETION".equals(readStringValue(context, "analysisType", ""))) {
            return List.of(
                    new LlmAnalysisSectionResponse(
                            "系列补完概览",
                            buildFranchiseCompletionOverviewContent(context)
                    ),
                    new LlmAnalysisSectionResponse(
                            "优先补完系列",
                            buildFranchisePriorityContent(context)
                    ),
                    new LlmAnalysisSectionResponse(
                            "系列状态判断",
                            buildFranchiseStatusContent(context)
                    ),
                    new LlmAnalysisSectionResponse(
                            "使用建议",
                            "系列补完分析只围绕已经有正式记录牵引出的系列展开，不会把作品库里所有未记录作品都塞进系列建议。这样既能保留系列页特色，也能避免库存作品过多时造成噪音。"
                    )
            );
        }

        if ("COMPREHENSIVE_REPORT".equals(readStringValue(context, "analysisType", ""))) {
            return List.of(
                    new LlmAnalysisSectionResponse(
                            "年度总览",
                            buildComprehensiveOverviewContent(
                                    context,
                                    totalRecords,
                                    averageScore,
                                    highScoreCount,
                                    lowScoreCount,
                                    yearOverYearDiff
                            )
                    ),
                    new LlmAnalysisSectionResponse(
                            "评分与类型结构",
                            buildComprehensiveStructureContent(context)
                    ),
                    new LlmAnalysisSectionResponse(
                            "标签与口味画像",
                            buildComprehensiveTasteContent(context)
                    ),
                    new LlmAnalysisSectionResponse(
                            "偏好矛盾",
                            buildComprehensiveContradictionContent(context)
                    ),
                    new LlmAnalysisSectionResponse(
                            "后续行动",
                            buildComprehensiveActionContent(context)
                    )
            );
        }

        if ("HOME_INSIGHT".equals(readStringValue(context, "analysisType", ""))) {
            return List.of(
                    new LlmAnalysisSectionResponse(
                            "当前图表",
                            buildHomeInsightOverviewContent(
                                    context,
                                    totalRecords,
                                    averageScore,
                                    highScoreCount,
                                    lowScoreCount,
                                    yearOverYearDiff
                            )
                    ),
                    new LlmAnalysisSectionResponse(
                            "类型与月份",
                            buildHomeInsightDistributionContent(context)
                    ),
                    new LlmAnalysisSectionResponse(
                            "展示提示",
                            buildHomeInsightActionContent(context)
                    )
            );
        }

        if ("QUESTION_ANSWER".equals(readStringValue(context, "analysisType", ""))) {
            return List.of(
                    new LlmAnalysisSectionResponse(
                            "直接回答",
                            buildQuestionAnswerContent(
                                    context,
                                    totalRecords,
                                    averageScore,
                                    highScoreCount,
                                    lowScoreCount
                            )
                    ),
                    new LlmAnalysisSectionResponse(
                            "相关样本",
                            buildQuestionSampleContent(context)
                    ),
                    new LlmAnalysisSectionResponse(
                            "可追溯依据",
                            buildQuestionEvidenceContent(context)
                    ),
                    new LlmAnalysisSectionResponse(
                            "边界说明",
                            "当前回答基于系统识别出的年份、月份、类型和标签范围生成，所有结论均可回到记录、标签、评分与收藏数据中追溯。"
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
                + "。这两组样本可以帮助系统解释你的评分边界。";
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

    private String buildTasteProfileOverviewContent(
            Map<String, Object> context,
            Integer totalRecords,
            String averageScore,
            Integer highScoreCount,
            Integer lowScoreCount
    ) {
        Map<String, Long> categoryDistribution = readLongMap(context, "categoryDistribution");
        String dominantCategory = topDistributionItem(categoryDistribution);

        if (totalRecords == null || totalRecords == 0) {
            return "当前还没有足够正式记录生成口味画像。系统会保留入口，等记录和标签增加后再形成更稳定的判断。";
        }

        return "当前样本共 " + totalRecords + " 条，个人均分 " + averageScore
                + " / 50，高分样本 " + highScoreCount + " 条，低分样本 " + lowScoreCount
                + " 条。数量最集中的媒介是 " + dominantCategory
                + "。这份画像优先解释你的长期稳定偏好，而不是给某一部作品下结论。";
    }

    private String buildStablePreferenceContent(Map<String, Object> context) {
        Map<String, List<Map<String, Object>>> groups = readTagPreferenceGroups(context);
        String stableTags = formatTagGroup(groups.get("highFrequencyHighScore"));
        String topRecords = formatRecordList(readMapList(context, "topRecords"));

        return "稳定偏好主要看“出现次数较多且均分较高”的标签。目前可用标签为：" + stableTags
                + "。高分样本包括：" + topRecords
                + "。这类方向后续适合作为推荐系统的强偏好依据。";
    }

    private String buildExplorationPreferenceContent(Map<String, Object> context) {
        Map<String, List<Map<String, Object>>> groups = readTagPreferenceGroups(context);
        String explorationTags = formatTagGroup(groups.get("lowFrequencyHighScore"));

        return "探索倾向主要看“出现次数不多但均分较高”的标签。目前可用标签为：" + explorationTags
                + "。它们不一定代表长期主口味，但很适合作为后续推荐里的惊喜方向。";
    }

    private String buildPreferenceBoundaryContent(Map<String, Object> context) {
        Map<String, List<Map<String, Object>>> groups = readTagPreferenceGroups(context);
        String riskTags = formatTagGroup(groups.get("highFrequencyLowScore"));
        String lowRecords = formatRecordList(readMapList(context, "lowScoreRecords"));

        return "偏好边界主要看“出现频率不低但均分偏低”的标签。目前风险标签为：" + riskTags
                + "。低分样本包括：" + lowRecords
                + "。这些方向不是绝对不能看，而是推荐时需要更多理由支撑。";
    }

    private String buildContradictionOverviewContent(
            Map<String, Object> context,
            Integer totalRecords,
            String averageScore
    ) {
        if (totalRecords == null || totalRecords == 0) {
            return "当前没有足够样本生成偏好矛盾分析。这个模块需要同时依赖标签数量、个人评分和高低分样本。";
        }

        return "当前从 " + totalRecords + " 条正式记录中寻找偏好矛盾，整体均分为 "
                + averageScore + " / 50。重点不是判断某个标签好坏，而是区分“经常接触”和“真正容易高分命中”。";
    }

    private String buildDenseLowScoreContent(Map<String, Object> context) {
        Map<String, List<Map<String, Object>>> groups = readTagPreferenceGroups(context);
        String denseLowTags = formatTagGroup(groups.get("highFrequencyLowScore"));

        return "看得多但不稳定的方向为：" + denseLowTags
                + "。如果这里出现明确标签，说明它们可能是高频尝试题材，但未必是稳定高分来源。";
    }

    private String buildSparseHighScoreContent(Map<String, Object> context) {
        Map<String, List<Map<String, Object>>> groups = readTagPreferenceGroups(context);
        String sparseHighTags = formatTagGroup(groups.get("lowFrequencyHighScore"));

        return "低频但高命中的方向为：" + sparseHighTags
                + "。这些标签适合在推荐中少量提高权重，但因为样本较少，仍需要保留不确定性。";
    }

    private String buildComprehensiveOverviewContent(
            Map<String, Object> context,
            Integer totalRecords,
            String averageScore,
            Integer highScoreCount,
            Integer lowScoreCount,
            Integer yearOverYearDiff
    ) {
        String longTermTotal = readStringValue(context, "longTermTotalRecords", "0");
        String longTermAverage = readStringValue(context, "longTermAverageScore", "0.0");

        if (totalRecords == null || totalRecords == 0) {
            return "当前年份没有正式记录，报告会退回参考长期画像样本。长期样本共 "
                    + longTermTotal + " 条，长期均分 " + longTermAverage
                    + " / 50。建议先补充年度记录，再让综合报告承担答辩展示入口。";
        }

        return "本年度正式记录 " + totalRecords + " 条，个人均分 " + averageScore
                + " / 50，高分样本 " + highScoreCount + " 条，低分样本 " + lowScoreCount
                + " 条。相对上一年变化 " + formatDiff(yearOverYearDiff)
                + " 条。长期画像样本共 " + longTermTotal + " 条，长期均分 "
                + longTermAverage + " / 50。";
    }

    private String buildComprehensiveStructureContent(Map<String, Object> context) {
        return "年度类型分布：" + formatMap(readLongMap(context, "categoryDistribution"))
                + "。年度月份档期分布：" + formatMap(readLongMap(context, "quarterDistribution"))
                + "。长期评分分段：" + formatMap(readLongMap(context, "scoreBands"))
                + "。这部分用于回答“这一年看了什么”和“长期评分标准是否集中”。";
    }

    private String buildComprehensiveTasteContent(Map<String, Object> context) {
        Map<String, List<Map<String, Object>>> groups = readTagPreferenceGroups(context);
        String stableTags = formatTagGroup(groups.get("highFrequencyHighScore"));
        String explorationTags = formatTagGroup(groups.get("lowFrequencyHighScore"));
        String topRecords = formatRecordList(readMapList(context, "topRecords"));

        return "稳定高分标签：" + stableTags
                + "。低频高命中标签：" + explorationTags
                + "。年度高分样本：" + topRecords
                + "。综合来看，稳定标签适合做推荐主依据，低频高命中标签适合做探索补充。";
    }

    private String buildComprehensiveContradictionContent(Map<String, Object> context) {
        Map<String, List<Map<String, Object>>> groups = readTagPreferenceGroups(context);
        String denseLowTags = formatTagGroup(groups.get("highFrequencyLowScore"));
        String lowRecords = formatRecordList(readMapList(context, "lowScoreRecords"));

        return "高频但均分偏低的风险方向：" + denseLowTags
                + "。年度低分样本：" + lowRecords
                + "。这部分可以解释“看得多不等于喜欢”，也能为推荐系统提供降权依据。";
    }

    private String buildComprehensiveActionContent(Map<String, Object> context) {
        Map<String, Long> collectionCandidates = readLongMap(context, "collectionCandidates");
        String recommendations = buildRecommendationSummaryContent(context);

        return "未记录候选中，想看 " + collectionCandidates.getOrDefault("WANT", 0L)
                + " 条，搁置 " + collectionCandidates.getOrDefault("PAUSED", 0L)
                + " 条，普通作品库候选 " + context.getOrDefault("libraryCandidateCount", 0)
                + " 条。当前优先推荐：" + recommendations
                + "。后续展示时可以先讲画像，再讲矛盾，最后落到推荐候选。";
    }

    private String buildHomeInsightOverviewContent(
            Map<String, Object> context,
            Integer totalRecords,
            String averageScore,
            Integer highScoreCount,
            Integer lowScoreCount,
            Integer yearOverYearDiff
    ) {
        Integer year = readInteger(context, "year");
        Integer quarter = readInteger(context, "quarter");
        String category = readStringValue(context, "category", "全部类型");

        if (totalRecords == null || totalRecords == 0) {
            return year + " 年 " + quarterLabel(quarter) + " " + category
                    + " 暂时没有正式记录。首页图表会保留筛选状态，适合继续切换年份、月份或类型寻找已有数据。";
        }

        return year + " 年 " + quarterLabel(quarter) + " " + category
                + " 共有 " + totalRecords + " 条正式记录，均分 " + averageScore
                + " / 50，高分 " + highScoreCount + " 条，低分 " + lowScoreCount
                + " 条。相对上一年同档期变化 " + formatDiff(yearOverYearDiff)
                + " 条。";
    }

    private String buildHomeInsightDistributionContent(Map<String, Object> context) {
        String topRecords = formatRecordList(readMapList(context, "topRecords"));
        String topTags = buildTopTagShortText(context);

        return "年度类型分布：" + formatMap(readLongMap(context, "categoryDistribution"))
                + "。当前类型的年度月份分布：" + formatMap(readLongMap(context, "quarterDistribution"))
                + "。当前筛选下评分靠前样本：" + topRecords
                + "。标签线索：" + topTags + "。";
    }

    private String buildHomeInsightActionContent(Map<String, Object> context) {
        Map<String, Long> collectionCandidates = readLongMap(context, "collectionCandidates");

        return "首页展示时可以先用类型总览说明年度重心，再用月份分布解释当前档期是否集中。"
                + "如果本档期数据较少，可以切到一键报告或口味画像。未记录候选中，想看 "
                + collectionCandidates.getOrDefault("WANT", 0L) + " 条，搁置 "
                + collectionCandidates.getOrDefault("PAUSED", 0L) + " 条，可作为下一步补完入口。";
    }

    private String buildQuestionAnswerContent(
            Map<String, Object> context,
            Integer totalRecords,
            String averageScore,
            Integer highScoreCount,
            Integer lowScoreCount
    ) {
        String intent = readStringValue(context, "questionIntent", "OVERVIEW");
        String question = readStringValue(context, "question", "");
        int total = totalRecords == null ? 0 : totalRecords;
        int highCount = highScoreCount == null ? 0 : highScoreCount;
        int lowCount = lowScoreCount == null ? 0 : lowScoreCount;

        if (total == 0 && !"RECOMMENDATION".equals(intent) && !"COLLECTION".equals(intent)) {
            return "这个问题暂时没有命中正式记录。已识别的问题是「" + question
                    + "」，你可以放宽年份、月份或类型条件后再试。";
        }

        return switch (intent) {
            case "HIGH_SCORE" -> "这个问题被识别为高分查询。当前范围共有 " + total
                    + " 条记录，其中 45 分以上 " + highCount
                    + " 条。靠前样本是：" + formatRecordList(readMapList(context, "topRecords")) + "。";
            case "LOW_SCORE" -> "这个问题被识别为低分或避雷查询。当前范围共有 " + total
                    + " 条记录，其中 30 分以下 " + lowCount
                    + " 条。需要注意的样本是：" + formatRecordList(readMapList(context, "lowScoreRecords")) + "。";
            case "TAG_PREFERENCE" -> buildQuestionTagAnswerContent(context, total, averageScore);
            case "RECOMMENDATION", "COLLECTION" -> "这个问题被识别为补完或收藏清单整理。当前可参考候选是："
                    + buildRecommendationSummaryContent(context)
                    + "。建议先看命中历史高分标签、且来源为想看的作品。";
            default -> "这个问题被识别为总体概览。当前范围共有 " + total
                    + " 条正式记录，均分 " + averageScore
                    + " / 50。类型分布为：" + formatMap(readLongMap(context, "categoryDistribution"))
                    + "。月份分布为：" + formatMap(readLongMap(context, "quarterDistribution")) + "。";
        };
    }

    private String buildQuestionTagAnswerContent(
            Map<String, Object> context,
            int totalRecords,
            String averageScore
    ) {
        String matchedTag = readStringValue(context, "matchedTag", "");
        Map<String, List<Map<String, Object>>> groups = readTagPreferenceGroups(context);
        String stableTags = formatTagGroup(groups.get("highFrequencyHighScore"));
        String explorationTags = formatTagGroup(groups.get("lowFrequencyHighScore"));

        if (!matchedTag.isBlank()) {
            return "问题中提到了「" + matchedTag + "」标签。当前范围共有 " + totalRecords
                    + " 条记录，均分 " + averageScore
                    + " / 50。稳定高分标签为：" + stableTags
                    + "。低频但高命中的探索标签为：" + explorationTags + "。";
        }

        return "这个问题被识别为标签偏好查询。当前范围共有 " + totalRecords
                + " 条记录，均分 " + averageScore
                + " / 50。高频标签线索为：" + buildTopTagShortText(context)
                + "；稳定高分标签为：" + stableTags + "。";
    }

    private String buildQuestionSampleContent(Map<String, Object> context) {
        return "高分样本：" + formatRecordList(readMapList(context, "topRecords"))
                + "。低分样本：" + formatRecordList(readMapList(context, "lowScoreRecords"))
                + "。标签线索：" + buildTopTagShortText(context) + "。";
    }

    private String buildQuestionEvidenceContent(Map<String, Object> context) {
        Integer year = readInteger(context, "year");
        Integer quarter = readInteger(context, "quarter");
        String category = readStringValue(context, "category", "全部类型");
        String matchedTag = readStringValue(context, "matchedTag", "");
        String yearText = year == null ? "不限年份" : year + " 年";
        String quarterText = quarter == null ? "不限月份" : quarterLabel(quarter);
        String tagText = matchedTag.isBlank() ? "未命中特定标签" : "命中标签「" + matchedTag + "」";

        return "识别范围：" + yearText + "、" + quarterText + "、" + category
                + "、" + tagText + "。评分段为：" + formatMap(readLongMap(context, "scoreBands"))
                + "。这部分用于展示系统为什么给出上面的回答。";
    }

    private String buildInterestTrendOverviewContent(
            Map<String, Object> context,
            Integer totalRecords,
            String averageScore
    ) {
        Map<String, Object> signals = readObjectMap(context, "interestTrendSignals");
        if (totalRecords == null || totalRecords == 0) {
            return "当前还没有足够正式记录生成兴趣趋势。系统会保留入口，等跨年份数据增加后再判断兴趣扩张、收缩或转向。";
        }

        return "当前长期样本共 " + totalRecords + " 条，整体均分 "
                + averageScore + " / 50，覆盖 " + signals.getOrDefault("coveredYears", 0)
                + " 个年份。记录数量从 " + signals.getOrDefault("earliestCount", 0)
                + " 条变化到 " + signals.getOrDefault("latestCount", 0)
                + " 条，均分变化 " + formatSignedValue(signals.get("averageScoreDiff"))
                + "。记录最多的年份是 " + signals.getOrDefault("strongestYear", "未知")
                + " 年，均分最高的年份是 " + signals.getOrDefault("highestAverageYear", "未知") + " 年。";
    }

    private String buildInterestTrendYearContent(Map<String, Object> context) {
        List<Map<String, Object>> trend = readMapList(context, "yearScoreTrend");
        if (trend.isEmpty()) {
            return "当前没有跨年份趋势样本。";
        }

        return trend.stream()
                .map(item -> item.get("year") + " 年："
                        + item.get("count") + " 条，均分 "
                        + item.get("averageScore") + "，高分 "
                        + item.get("highScoreCount") + " 条")
                .collect(java.util.stream.Collectors.joining("；"));
    }

    private String buildInterestTrendCategoryContent(Map<String, Object> context) {
        List<Map<String, Object>> trend = readMapList(context, "yearlyCategoryTrend");
        if (trend.isEmpty()) {
            return "当前没有足够数据判断类型重心变化。";
        }

        return trend.stream()
                .map(item -> item.get("year") + " 年主类型为 "
                        + item.get("dominantCategory") + "，分布："
                        + formatMap(readNestedLongMap(item, "categoryDistribution")))
                .collect(java.util.stream.Collectors.joining("；"));
    }

    private String buildInterestTrendTagContent(Map<String, Object> context) {
        List<Map<String, Object>> trend = readMapList(context, "yearlyTagTrend");
        if (trend.isEmpty()) {
            return "当前没有足够标签数据判断兴趣迁移。";
        }

        return trend.stream()
                .map(item -> item.get("year") + " 年标签线索："
                        + formatTagList(readNestedMapList(item, "topTags")))
                .collect(java.util.stream.Collectors.joining("；"));
    }

    private String buildFranchiseCompletionOverviewContent(Map<String, Object> context) {
        Map<String, Long> stats = readLongMap(context, "franchiseCompletionStats");
        long franchiseCount = stats.getOrDefault("franchiseCount", 0L);
        long seriesSubjectCount = stats.getOrDefault("seriesSubjectCount", 0L);
        long watchedSubjectCount = stats.getOrDefault("watchedSubjectCount", 0L);
        long unrecordedSubjectCount = stats.getOrDefault("unrecordedSubjectCount", 0L);

        if (franchiseCount == 0) {
            return "当前还没有足够的系列样本。系列补完分析需要至少一个系列中存在正式记录，系统才会把同系列作品纳入补完判断。";
        }

        return "当前纳入 " + franchiseCount + " 个系列，系列内共有 "
                + seriesSubjectCount + " 部作品，其中已正式记录 " + watchedSubjectCount
                + " 部，未形成正式记录 " + unrecordedSubjectCount
                + " 部。这个模块的重点是解释系列推进状态，而不是简单推荐新作品。";
    }

    private String buildFranchisePriorityContent(Map<String, Object> context) {
        List<Map<String, Object>> items = readMapList(context, "franchiseCompletionItems");
        if (items.isEmpty()) {
            return "当前没有可排序的系列补完候选。";
        }

        return items.stream()
                .limit(5)
                .map(item -> item.get("franchiseName") + "（已记录 "
                        + item.get("watchedCount") + "/" + item.get("totalSubjects")
                        + "，待补 " + item.get("unrecordedCount")
                        + "，状态：" + item.get("status") + "）")
                .collect(java.util.stream.Collectors.joining("；"));
    }

    private String buildFranchiseStatusContent(Map<String, Object> context) {
        List<Map<String, Object>> items = readMapList(context, "franchiseCompletionItems");
        if (items.isEmpty()) {
            return "系列样本不足时，建议先在作品库中维护 franchiseName，并至少添加一条同系列正式记录。";
        }

        String nextCandidates = items.stream()
                .limit(3)
                .map(item -> {
                    List<Map<String, Object>> candidates = readNestedMapList(item, "nextCandidates");
                    String candidateText = candidates.isEmpty()
                            ? "暂无待补作品"
                            : candidates.stream()
                                    .limit(3)
                                    .map(candidate -> candidate.get("title") + "（"
                                            + subjectStatusLabel(String.valueOf(candidate.get("status"))) + "）")
                                    .collect(java.util.stream.Collectors.joining("、"));
                    return item.get("franchiseName") + "：" + candidateText;
                })
                .collect(java.util.stream.Collectors.joining("；"));

        return "待补作品会按发行年份、月份档期、季数和 Part 排序，方便观察系列顺序。当前可关注：" + nextCandidates + "。";
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
        evidence.add("分析引擎：ACGN preference analyzer");
        evidence.add("上下文已整理为结构化统计依据");
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

        if ("TASTE_PROFILE".equals(analysisType)) {
            return displayCategory + " 口味画像范围";
        }

        if ("PREFERENCE_CONTRADICTION".equals(analysisType)) {
            return displayCategory + " 偏好矛盾范围";
        }

        if ("INTEREST_TREND".equals(analysisType)) {
            return displayCategory + " 兴趣变化趋势范围";
        }

        if ("FRANCHISE_COMPLETION".equals(analysisType)) {
            return displayCategory + " 系列补完范围";
        }

        if ("COMPREHENSIVE_REPORT".equals(analysisType)) {
            return year + " 年 " + displayCategory + " 综合报告范围";
        }

        if ("HOME_INSIGHT".equals(analysisType)) {
            return year + " 年 " + quarterLabel(quarter) + " " + displayCategory + " 首页图表范围";
        }

        if ("QUESTION_ANSWER".equals(analysisType)) {
            return "自然语言问答范围";
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

    private List<Map<String, Object>> readNestedMapList(Map<String, Object> item, String key) {
        Object value = item.get(key);
        if (!(value instanceof List<?> list)) {
            return List.of();
        }

        return list.stream()
                .filter(Map.class::isInstance)
                .map(nestedItem -> {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> map = (Map<String, Object>) nestedItem;
                    return map;
                })
                .toList();
    }

    private Map<String, Object> readObjectMap(Map<String, Object> context, String key) {
        Object value = context.get(key);
        if (!(value instanceof Map<?, ?> map)) {
            return Map.of();
        }

        java.util.Map<String, Object> result = new java.util.LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            result.put(String.valueOf(entry.getKey()), entry.getValue());
        }
        return result;
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

    private Map<String, Long> readNestedLongMap(Map<String, Object> item, String key) {
        Object value = item.get(key);
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

    private String formatTagList(List<Map<String, Object>> tags) {
        if (tags.isEmpty()) {
            return "暂无稳定标签";
        }

        return tags.stream()
                .limit(4)
                .map(item -> item.get("name") + "×" + item.get("count"))
                .collect(java.util.stream.Collectors.joining("、"));
    }

    private String formatSignedValue(Object value) {
        if (value == null) {
            return "0";
        }

        String text = String.valueOf(value);
        if (text.startsWith("-") || "0".equals(text) || "0.0".equals(text)) {
            return text;
        }
        return "+" + text;
    }

    private String topDistributionItem(Map<String, Long> map) {
        if (map.isEmpty()) {
            return "暂无数据";
        }

        return map.entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .map(entry -> entry.getKey() + "（" + entry.getValue() + " 条）")
                .orElse("暂无数据");
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

    private String buildTopTagShortText(Map<String, Object> context) {
        List<Map<String, Object>> topTags = readMapList(context, "topTags");
        if (topTags.isEmpty()) {
            return "暂无稳定标签";
        }

        return topTags.stream()
                .limit(4)
                .map(item -> item.get("name") + "×" + item.get("count"))
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

    private String subjectStatusLabel(String status) {
        return switch (status) {
            case "WATCHED" -> "已记录";
            case "WANT" -> "想看";
            case "PAUSED" -> "搁置";
            default -> "未记录";
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
