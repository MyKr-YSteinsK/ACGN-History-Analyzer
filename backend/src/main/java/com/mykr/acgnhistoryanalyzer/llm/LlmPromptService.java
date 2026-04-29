package com.mykr.acgnhistoryanalyzer.llm;

import org.springframework.stereotype.Service;

@Service
public class LlmPromptService {

    public String buildYearlySummaryPrompt(Integer year, String category) {
        String scope = category == null || category.isBlank()
                ? year + " 年全部 ACGN 类型"
                : year + " 年 " + category;

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
