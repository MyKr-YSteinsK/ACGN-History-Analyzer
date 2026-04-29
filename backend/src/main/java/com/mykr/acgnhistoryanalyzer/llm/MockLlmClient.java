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
        String displayCategory = category == null ? "全部类型" : category;

        return new LlmAnalysisResponse(
                request.analysisType(),
                buildTitle(year, displayCategory),
                "这是 Mock 模式生成的结构化智能分析。它暂时不调用真实大模型，但会保留第三阶段正式 AI 功能需要的展示形态：摘要、关键词、分段结论、引用依据和风险提示。",
                List.of("Mock 演示", "结构化报告", "可追溯依据", "后续可替换真实模型"),
                List.of(
                        new LlmAnalysisSectionResponse(
                                "整体观察",
                                "当前版本先把 AI 分析的骨架跑通：后端负责组织分析范围和提示词，LLM Client 负责生成报告，前端只消费统一结构。这样以后接入真实模型时，不需要推翻页面。"
                        ),
                        new LlmAnalysisSectionResponse(
                                "偏好解释",
                                "正式版本会把作品记录、个人评分、标签、系列和收藏夹候选整理成 AnalysisContext。Mock 阶段先用稳定文本占位，重点验证接口和页面是否顺手。"
                        ),
                        new LlmAnalysisSectionResponse(
                                "后续建议",
                                "下一步会把年度总结改成基于真实数据库统计：记录总数、类型分布、月份分布、评分前三、高频标签和同比变化都会进入引用依据。"
                        )
                ),
                List.of(
                        "分析类型：" + request.analysisType(),
                        "分析年份：" + (year == null ? "未指定" : year),
                        "分析类型范围：" + displayCategory,
                        "当前模型提供者：" + properties.getProvider(),
                        "Prompt 已生成，长度约 " + request.prompt().length() + " 字"
                ),
                List.of(
                        "当前为 Mock 模式，文本不代表真实模型判断。",
                        "本轮不会调用外部 AI 服务，也不会写入或修改数据库。"
                ),
                new LlmAnalysisScopeResponse(year, quarter, category),
                true,
                properties.getModel(),
                LocalDateTime.now()
        );
    }

    private String buildTitle(Integer year, String displayCategory) {
        if (year == null) {
            return displayCategory + " ACGN 智能分析预览";
        }

        return year + " 年 " + displayCategory + " 智能分析预览";
    }

    private Integer readInteger(Map<String, Object> context, String key) {
        Object value = context.get(key);
        if (value instanceof Integer integer) {
            return integer;
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
}
