package com.mykr.acgnhistoryanalyzer.llm;

import org.springframework.stereotype.Service;

@Service
public class LlmPromptService {

    public String buildYearlySummaryPrompt(Integer year, String category) {
        String scope = category == null || category.isBlank()
                ? year + " 年全部 ACGN 类型"
                : year + " 年 " + category;

        return buildBasePrompt(scope);
    }

    public String buildQuarterlySummaryPrompt(Integer year, Integer quarter, String category) {
        String monthLabel = switch (quarter) {
            case 1 -> "一月";
            case 2 -> "四月";
            case 3 -> "七月";
            case 4 -> "十月";
            default -> "未知月份";
        };
        String scope = category == null || category.isBlank()
                ? year + " 年 " + monthLabel + "档期全部 ACGN 类型"
                : year + " 年 " + monthLabel + "档期 " + category;

        return buildBasePrompt(scope);
    }

    public String buildTypePreferencePrompt(String category) {
        return buildBasePrompt(category + " 长期类型偏好");
    }

    public String buildScoreStructurePrompt(String category) {
        String scope = category == null || category.isBlank()
                ? "全部 ACGN 个人评分结构"
                : category + " 个人评分结构";

        return buildBasePrompt(scope);
    }

    public String buildTagPreferencePrompt(String category) {
        String scope = category == null || category.isBlank()
                ? "全部 ACGN 标签偏好"
                : category + " 标签偏好";

        return buildBasePrompt(scope);
    }

    public String buildRecommendationPrompt(String category) {
        String scope = category == null || category.isBlank()
                ? "作品库与收藏夹智能推荐"
                : category + " 作品库与收藏夹智能推荐";

        return buildBasePrompt(scope);
    }

    public String buildCollectionBacklogPrompt(String category) {
        String scope = category == null || category.isBlank()
                ? "收藏夹补坑建议"
                : category + " 收藏夹补坑建议";

        return buildBasePrompt(scope);
    }

    public String buildTasteProfilePrompt(String category) {
        String scope = category == null || category.isBlank()
                ? "个人 ACGN 口味画像"
                : category + " 个人口味画像";

        return buildBasePrompt(scope);
    }

    public String buildPreferenceContradictionPrompt(String category) {
        String scope = category == null || category.isBlank()
                ? "个人 ACGN 偏好矛盾分析"
                : category + " 偏好矛盾分析";

        return buildBasePrompt(scope);
    }

    public String buildComprehensiveReportPrompt(Integer year, String category) {
        String scope = category == null || category.isBlank()
                ? year + " 年个人 ACGN 一键智能分析报告"
                : year + " 年 " + category + " 一键智能分析报告";

        return buildBasePrompt(scope);
    }

    public String buildHomeInsightPrompt(Integer year, Integer quarter, String category) {
        String monthLabel = switch (quarter) {
            case 1 -> "一月";
            case 2 -> "四月";
            case 3 -> "七月";
            case 4 -> "十月";
            default -> "未知月份";
        };
        String scope = category == null || category.isBlank()
                ? year + " 年 " + monthLabel + " 首页统计图表解读"
                : year + " 年 " + monthLabel + " " + category + " 首页统计图表解读";

        return buildBasePrompt(scope);
    }

    private String buildBasePrompt(String scope) {
        return """
                你是一个个人 ACGN 历史数据分析助手。
                请只根据后端提供的结构化上下文生成中文分析，不要编造作品、标签或评分。
                用户评分满分 50，平台评分满分 10。
                月份档期规则：1=一月，2=四月，3=七月，4=十月。
                当前分析范围：%s。
                输出应克制、具体、有判断，并且必须带引用依据。
                """.formatted(scope);
    }
}
