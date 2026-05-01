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

    public String buildInterestTrendPrompt(String category) {
        String scope = category == null || category.isBlank()
                ? "个人 ACGN 兴趣变化趋势分析"
                : category + " 兴趣变化趋势分析";

        return buildBasePrompt(scope);
    }

    public String buildFranchiseCompletionPrompt(String category) {
        String scope = category == null || category.isBlank()
                ? "系列补完分析"
                : category + " 系列补完分析";

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

    public String buildQuestionAnswerPrompt(String question) {
        return """
                你是一个个人 ACGN 历史数据分析助手。
                你只需要输出自然语言回答，不要输出 JSON，不要输出 markdown，不要输出代码块。
                请只根据后端提供的结构化上下文回答用户问题，不要编造作品、标签或评分。
                用户评分满分 50，平台评分满分 10。
                如果上下文中没有足够信息，请直接说明“不足以判断”，并给出基于现有数据的谨慎解释。
                回答要像真实 AI 助手，语气自然、简洁、具体。
                问题：%s
                """.formatted(question);
    }

    private String buildBasePrompt(String scope) {
        return """
                你是一个个人 ACGN 历史数据分析助手。
                请只根据后端提供的结构化上下文生成中文分析，不要编造作品、标签或评分。
                用户评分满分 50，平台评分满分 10。
                月份档期规则：1=一月，2=四月，3=七月，4=十月。
                当前分析范围：%s。
                输出应克制、具体、有判断，并且必须带引用依据。
                你必须只输出一个合法 JSON 对象，不要输出 markdown，不要输出代码块，不要在 JSON 前后添加解释文字。
                JSON 字段必须符合：
                {
                  "analysisType": "后端传入的 analysisType",
                  "title": "报告标题",
                  "summary": "120字以内摘要",
                  "keywords": ["2到6个关键词"],
                  "sections": [{"title": "段落标题", "content": "段落内容"}],
                  "recommendations": [{
                    "subjectId": null,
                    "title": "候选作品名",
                    "coverUrl": null,
                    "category": "ANIME",
                    "tags": ["标签"],
                    "franchiseName": null,
                    "source": "LIBRARY",
                    "reason": "推荐理由",
                    "risk": "风险或边界",
                    "evidence": "引用依据",
                    "priority": "LOW",
                    "matchScore": 60,
                    "platformScore": null
                  }],
                  "evidence": ["引用后端上下文中的统计、作品或标签依据"],
                  "warnings": ["数据不足或解释边界"],
                  "scope": {"year": null, "quarter": null, "category": null},
                  "mockMode": false,
                  "modelName": "deepseek-v4-flash"
                }
                如果上下文没有 recommendations，recommendations 输出空数组。
                """.formatted(scope);
    }
}
