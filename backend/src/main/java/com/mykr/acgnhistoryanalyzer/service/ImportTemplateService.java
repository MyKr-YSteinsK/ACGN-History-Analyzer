package com.mykr.acgnhistoryanalyzer.service;

import com.mykr.acgnhistoryanalyzer.response.ImportFieldDocResponse;
import com.mykr.acgnhistoryanalyzer.response.ImportTemplateResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ImportTemplateService {

    public ImportTemplateResponse getSubjectImportTemplate() {
        Map<String, Object> subjectItem = new LinkedHashMap<>();
        subjectItem.put("titleCn", "进击的巨人");
        subjectItem.put("subtitle", "The Final Season Part 1");
        subjectItem.put("displayTitle", "进击的巨人 The Final Season Part 1");
        subjectItem.put("franchiseId", null);
        subjectItem.put("franchiseName", "进击的巨人");
        subjectItem.put("seasonIndex", 4);
        subjectItem.put("partIndex", 1);
        subjectItem.put("coverUrl", "https://example.com/aot-p1.jpg");
        subjectItem.put("summary", "样例：巨人最终季前半部分");
        subjectItem.put("releaseYear", 2020);
        subjectItem.put("releaseQuarter", 4);
        subjectItem.put("category", "ANIME");
        subjectItem.put("studioName", "MAPPA");
        subjectItem.put("platformLink", "https://bgm.tv/subject/aot-final-part1");
        subjectItem.put("platformScore", 8.9);
        subjectItem.put("episodeCount", 16);
        subjectItem.put("tagNames", List.of("战斗", "奇幻", "漫画改"));
        subjectItem.put("sourcePlatform", "BANGUMI");
        subjectItem.put("sourceId", "aot-final-part1");
        subjectItem.put("status", "NORMAL");

        List<Map<String, Object>> subjects = new ArrayList<>();
        subjects.add(subjectItem);

        Map<String, Object> templateBody = new LinkedHashMap<>();
        templateBody.put("subjects", subjects);

        List<String> rules = List.of(
                "建议导入顺序：先准备 franchise，再导入 subject，最后再导入 record。",
                "subject 的业务层唯一性规则为：titleCn + subtitle + seasonIndex + partIndex + category 全都一样，则视为重复作品。",
                "franchiseId 和 franchiseName 二选一即可；如果都传，当前逻辑优先按 franchiseId 处理。",
                "如果传 franchiseName，则后端会尝试匹配已有系列；匹配不到时会跳过。",
                "preview 和正式 import 使用同一套判定规则。",
                "作品标签使用 tagNames 数组，建议 3~7 个，最多 12 个；这些标签会进入结构化标签系统。",
                "coverUrl 可以填写 Bangumi 等远程图片地址；正式导入时后端会尽量下载到本地 uploads/covers，并把作品封面地址改成本地访问路径。"
        );

        List<ImportFieldDocResponse> fields = List.of(
                new ImportFieldDocResponse("titleCn", true, "作品主标题，参与作品唯一性判断。", "进击的巨人", "不能为空。"),
                new ImportFieldDocResponse("subtitle", false, "作品副标题，参与作品唯一性判断。", "The Final Season Part 1", "可以为空。"),
                new ImportFieldDocResponse("displayTitle", true, "前端展示标题，不参与当前作品唯一性判断。", "进击的巨人 The Final Season Part 1", "建议写完整可读名称。"),
                new ImportFieldDocResponse("franchiseId", false, "已有系列的数据库 id。", "1", "如果提供则优先使用。"),
                new ImportFieldDocResponse("franchiseName", false, "已有系列名称，用于自动匹配 franchise。", "进击的巨人", "当 franchiseId 为空时可使用。"),
                new ImportFieldDocResponse("seasonIndex", false, "第几季，参与作品唯一性判断。", "4", "可以为空。"),
                new ImportFieldDocResponse("partIndex", false, "第几部分，参与作品唯一性判断。", "1", "可以为空。"),
                new ImportFieldDocResponse("coverUrl", false, "封面地址。", "https://example.com/aot-p1.jpg", "推荐填 Bangumi 图片 URL，后端会尝试缓存到本地。"),
                new ImportFieldDocResponse("summary", false, "作品简介。", "样例：巨人最终季前半部分", "仅展示用途。"),
                new ImportFieldDocResponse("releaseYear", true, "作品发行年份。", "2020", "用于展示和检索。"),
                new ImportFieldDocResponse("releaseQuarter", true, "作品发行季度，只能是 1~4。", "4", "用于展示和检索。"),
                new ImportFieldDocResponse("category", true, "作品类型，参与作品唯一性判断。", "ANIME", "当前值为 ANIME / COMIC / NOVEL / GAME。"),
                new ImportFieldDocResponse("studioName", false, "制作公司、出版社或相关机构名。", "MAPPA", "当前字段名沿用 studioName。"),
                new ImportFieldDocResponse("platformLink", false, "外部平台链接。", "https://bgm.tv/subject/xxx", "可为空。"),
                new ImportFieldDocResponse("platformScore", false, "外部平台评分，例如 Bangumi 评分。", "8.9", "范围 0~10，可以为空。"),
                new ImportFieldDocResponse("episodeCount", false, "集数、话数或章节数。", "16", "可以为空。"),
                new ImportFieldDocResponse("tagNames", false, "结构化作品标签数组，适合后续筛选和 AI 分析。", "[\"战斗\", \"奇幻\", \"漫画改\"]", "推荐 3~7 个，最多 12 个。"),
                new ImportFieldDocResponse("sourcePlatform", false, "数据来源平台。", "BANGUMI", "预留给后续平台导入和 AI 分析溯源。"),
                new ImportFieldDocResponse("sourceId", false, "来源平台上的作品 ID。", "123456", "可以为空。"),
                new ImportFieldDocResponse("status", true, "作品状态。", "NORMAL", "通常使用 NORMAL 或 HIDDEN。")
        );

        return new ImportTemplateResponse(
                "SUBJECT_IMPORT",
                "/api/subjects/import",
                "先 franchise，后 subject，再 record",
                rules,
                templateBody,
                fields
        );
    }

    public ImportTemplateResponse getRecordImportTemplate() {
        Map<String, Object> recordItem = new LinkedHashMap<>();
        recordItem.put("subjectTitleCn", "进击的巨人");
        recordItem.put("subjectSubtitle", "The Final Season Part 1");
        recordItem.put("subjectSeasonIndex", 4);
        recordItem.put("subjectPartIndex", 1);
        recordItem.put("subjectCategory", "ANIME");
        recordItem.put("scoreValue", 48);
        recordItem.put("recordYear", 2024);
        recordItem.put("recordQuarter", 1);
        recordItem.put("note", "样例：最终季前半部分补完");

        List<Map<String, Object>> records = new ArrayList<>();
        records.add(recordItem);

        Map<String, Object> templateBody = new LinkedHashMap<>();
        templateBody.put("records", records);

        List<String> rules = List.of(
                "record 导入前，必须先保证对应 subject 已经存在。",
                "record 导入定位 subject 的规则，已经与 subject 的业务唯一性规则对齐：titleCn + subtitle + seasonIndex + partIndex + category。",
                "当前 record 只表示正式完成记录，不再承载想看/搁置；想看/搁置由 collection 模块管理。",
                "当前系统规则是一作品一正式记录；如果某个 subject 已经有正式记录，则新的 record 导入会跳过。",
                "preview 和正式 import 使用同一套 subject 匹配逻辑。",
                "如果匹配到多个 subject，则会跳过，并提示先清理重复作品。"
        );

        List<ImportFieldDocResponse> fields = List.of(
                new ImportFieldDocResponse("subjectTitleCn", true, "目标作品主标题，用于定位 subject。", "进击的巨人", "参与作品定位。"),
                new ImportFieldDocResponse("subjectSubtitle", false, "目标作品副标题，用于定位 subject。", "The Final Season Part 1", "参与作品定位。"),
                new ImportFieldDocResponse("subjectSeasonIndex", false, "目标作品第几季，用于定位 subject。", "4", "参与作品定位。"),
                new ImportFieldDocResponse("subjectPartIndex", false, "目标作品第几部分，用于定位 subject。", "1", "参与作品定位。"),
                new ImportFieldDocResponse("subjectCategory", true, "目标作品类型，用于定位 subject。", "ANIME", "参与作品定位。"),
                new ImportFieldDocResponse("scoreValue", false, "个人评分，范围 0~50。", "48", "可以为空。"),
                new ImportFieldDocResponse("recordYear", true, "记录年份。", "2024", "必填。"),
                new ImportFieldDocResponse("recordQuarter", true, "记录季度，只能是1~4。", "1", "必填。"),
                new ImportFieldDocResponse("note", false, "备注。", "样例：最终季前半部分补完", "可以为空。")
        );

        return new ImportTemplateResponse(
                "RECORD_IMPORT",
                "/api/records/import",
                "先 subject，后 record",
                rules,
                templateBody,
                fields
        );
    }
}
