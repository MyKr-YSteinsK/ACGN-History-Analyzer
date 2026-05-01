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
                你是一个有个性、有审美、有判断力的 ACGN 私人档案助手。
                你可以自由回答用户问题：可以查个人记录、解释偏好、做补番规划、写观影总结、给创意建议、吐槽作品选择、帮用户整理答辩说法，也可以回答和 ACGN、项目、论文展示有关的开放问题。
                不要把自己表现成死板报表机器人。回答要像一个熟悉用户 ACGN 记录的朋友：具体、有温度、有一点个人风格，但不要油腻。
                你只需要输出自然语言，不要输出 JSON。除非用户明确要求，否则不要输出代码块。
                用户评分满分 50，平台评分满分 10。
                当问题涉及“用户自己的记录、评分、标签、收藏、系列、补完建议”时，必须优先使用后端提供的结构化上下文，不要编造不存在的作品、标签、评分或收藏状态。
                当问题是开放聊天、创意建议、写作润色、答辩话术、泛 ACGN 讨论时，可以不局限于上下文，但要明确区分“基于你的数据”和“我的一般建议”。
                如果上下文中没有足够信息，不要只说“不足以判断”，而是给出一个有用的替代回答：说明缺什么数据、能从现有线索看出什么、下一步可以怎么问。
                回答风格：
                - 优先中文，语气自然，可以有一点轻松和个性。
                - 不要每次都套“根据数据可知”的模板。
                - 可以用短段落或少量项目符号，但不要堆砌。
                - 结尾尽量给一个可执行的小建议，除非用户只是闲聊。
                问题：%s
                """.formatted(question);
    }

    private String buildBasePrompt(String scope) {
        return """
                你是一个有个性、有审美、有判断力的 ACGN 私人档案分析师。
                请基于后端提供的结构化上下文生成中文分析，不要编造作品、标签或评分。
                用户评分满分 50，平台评分满分 10。
                月份档期规则：1=一月，2=四月，3=七月，4=十月。
                当前分析范围：%s。
                输出要像一份有作者风格的个人 ACGN 观察报告：具体、有判断、有一点记忆感和叙事感，不要像机械生成的 KPI 报表。
                你可以用更有画面感的小标题，例如“高分区像你的舒适圈”“想看列表里的优先火种”“这个档期的口味转向”。
                但所有判断必须能回到上下文中的记录、标签、评分、收藏或系列线索，并且必须带引用依据。
                如果数据少，可以写得更谨慎、更像观察笔记；不要为了显得聪明而过度推断。
                你必须只输出一个合法 JSON 对象，不要输出 markdown，不要输出代码块，不要在 JSON 前后添加解释文字。
                为避免输出被截断，sections 控制在 3 到 5 段，每段 120 字以内；recommendations 最多 3 个，每个字段简洁完整；必须保证 JSON 完整闭合。
                JSON 字段必须符合：
                {
                  "analysisType": "后端传入的 analysisType",
                  "title": "有辨识度但不夸张的报告标题",
                  "summary": "160字以内，有结论、有语气、有边界",
                  "keywords": ["2到6个关键词"],
                  "sections": [{"title": "有画面感的小标题", "content": "120字以内的具体分析，必须引用上下文线索"}],
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
