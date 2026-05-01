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
        subjectItem.put("coverUrl", "https://lain.bgm.tv/pic/cover/l/example.jpg");
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
                "推荐顺序：导入中心 Bangumi 新番写入作品库，再导入 collections / records；系列由 Bangumi 与正式记录后的自动检测维护。",
                "subject 唯一性仅由 sourceId 判断（默认 Bangumi subject id）。",
                "同一 sourceId 已存在时，重复导入只会同步三项：tagNames、platformScore、episodeCount；其余字段保持库中原值。",
                "franchiseId 和 franchiseName 二选一即可；如果都传，当前逻辑优先按 franchiseId 处理。",
                "如果传 franchiseName，则后端会尝试匹配已有系列；匹配不到时不会跳过，会按无系列作品导入。",
                "preview 和正式 import 使用同一套判定规则。",
                "作品标签使用 tagNames 数组，建议 3~7 个，最多 12 个；这些标签会进入结构化标签系统。",
                "如果使用 CSV，tagNames 建议写成：战斗|奇幻|漫画改。",
                "月份档期按 1/4/7/10 月填写：1=一月，2=四月，3=七月，4=十月。",
                "coverUrl 可以填写 Bangumi 等远程图片地址；正式导入时后端会尽量下载到本地 uploads/covers，并把作品封面地址改成本地访问路径。"
        );

        List<ImportFieldDocResponse> fields = List.of(
                new ImportFieldDocResponse("titleCn", true, "作品主标题。", "进击的巨人", "不能为空。"),
                new ImportFieldDocResponse("subtitle", false, "作品副标题。", "The Final Season Part 1", "可以为空。"),
                new ImportFieldDocResponse("displayTitle", true, "前端展示标题。", "进击的巨人 The Final Season Part 1", "建议写完整可读名称。"),
                new ImportFieldDocResponse("franchiseId", false, "已有系列的数据库 id。", "1", "如果提供则优先使用。"),
                new ImportFieldDocResponse("franchiseName", false, "已有系列名称，用于自动匹配 franchise。", "进击的巨人", "当 franchiseId 为空时可使用；匹配不到时按无系列作品导入。"),
                new ImportFieldDocResponse("seasonIndex", false, "第几季。", "4", "可以为空。"),
                new ImportFieldDocResponse("partIndex", false, "第几部分。", "1", "可以为空。"),
                new ImportFieldDocResponse("coverUrl", false, "封面地址。", "https://lain.bgm.tv/pic/cover/l/example.jpg", "推荐填 Bangumi 图片 URL，后端会尝试缓存到本地。"),
                new ImportFieldDocResponse("summary", false, "作品简介。", "样例：巨人最终季前半部分", "仅展示用途。"),
                new ImportFieldDocResponse("releaseYear", true, "作品发行年份。", "2020", "用于展示和检索。"),
                new ImportFieldDocResponse("releaseQuarter", true, "作品发行月份档期，只能是 1~4；1=一月，2=四月，3=七月，4=十月。", "4", "用于展示和检索。"),
                new ImportFieldDocResponse("category", true, "作品类型。", "ANIME", "当前值为 ANIME / COMIC / NOVEL / GAME。"),
                new ImportFieldDocResponse("studioName", false, "制作公司、出版社或相关机构名。", "MAPPA", "当前字段名沿用 studioName。"),
                new ImportFieldDocResponse("platformLink", false, "外部平台链接。", "https://bgm.tv/subject/xxx", "可为空。"),
                new ImportFieldDocResponse("platformScore", false, "外部平台评分，例如 Bangumi 评分。", "8.9", "范围 0~10，可以为空。"),
                new ImportFieldDocResponse("episodeCount", false, "集数、话数或章节数。", "16", "可以为空。"),
                new ImportFieldDocResponse("tagNames", false, "结构化作品标签数组，适合后续筛选和 AI 分析。", "[\"战斗\", \"奇幻\", \"漫画改\"]", "JSON 使用数组；CSV 可写 战斗|奇幻|漫画改。推荐 3~7 个，最多 12 个。"),
                new ImportFieldDocResponse("sourcePlatform", false, "数据来源平台。", "BANGUMI", "通常保持 BANGUMI。"),
                new ImportFieldDocResponse("sourceId", true, "来源平台上的作品 ID（唯一键）。", "123456", "不能为空。"),
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
        recordItem.put("subjectTitle", "进击的巨人");
        recordItem.put("subjectSeasonIndex", 4);
        recordItem.put("subjectPartIndex", 1);
        recordItem.put("sourceId", null);
        recordItem.put("subjectId", null);
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
                "record 导入支持 subjectId、sourceId、subjectTitle 三种定位方式，优先级依次是 subjectId > sourceId > 标题匹配。",
                "标题匹配使用 titleCn/displayTitle 关键词检索，可配合 subjectSeasonIndex 与 subjectPartIndex 缩小范围。",
                "如果匹配到多个候选，预览会返回候选列表，需要在前端先选择一个后再正式导入。",
                "当前 record 只表示正式完成记录，不再承载想看/搁置；想看/搁置由 collection 模块管理。",
                "当前系统规则是一作品一正式记录；如果某个 subject 已经有正式记录，则新的 record 导入会跳过。",
                "preview 和正式 import 使用同一套 subject 匹配逻辑。",
                "记录月份档期按 1/4/7/10 月填写：1=一月，2=四月，3=七月，4=十月。"
        );

        List<ImportFieldDocResponse> fields = List.of(
                new ImportFieldDocResponse("subjectTitle", false, "目标作品标题关键词（支持模糊匹配 titleCn/displayTitle）。", "进击的巨人", "subjectId/sourceId 为空时建议填写。"),
                new ImportFieldDocResponse("subjectSeasonIndex", false, "季序号（用于辅助缩小匹配范围）。", "4", "可为空。"),
                new ImportFieldDocResponse("subjectPartIndex", false, "Part 序号（用于辅助缩小匹配范围）。", "1", "可为空。"),
                new ImportFieldDocResponse("sourceId", false, "目标作品来源 ID（Bangumi subject id）。", "123456", "可选；比标题匹配更精确。"),
                new ImportFieldDocResponse("subjectId", false, "目标作品数据库 ID。", "1", "可选；优先级最高。"),
                new ImportFieldDocResponse("scoreValue", false, "个人评分，范围 0~50。", "48", "可以为空。"),
                new ImportFieldDocResponse("recordYear", true, "记录年份。", "2024", "必填。"),
                new ImportFieldDocResponse("recordQuarter", true, "记录月份档期，只能是 1~4；1=一月，2=四月，3=七月，4=十月。", "1", "必填。"),
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

    public ImportTemplateResponse getCollectionImportTemplate() {
        Map<String, Object> collectionItem = new LinkedHashMap<>();
        collectionItem.put("subjectTitle", "进击的巨人");
        collectionItem.put("subjectSeasonIndex", 4);
        collectionItem.put("subjectPartIndex", 1);
        collectionItem.put("sourceId", null);
        collectionItem.put("subjectId", null);
        collectionItem.put("collectionType", "WANT");
        collectionItem.put("note", "样例：之后补完最终季前半部分");

        List<Map<String, Object>> collections = new ArrayList<>();
        collections.add(collectionItem);

        Map<String, Object> templateBody = new LinkedHashMap<>();
        templateBody.put("collections", collections);

        List<String> rules = List.of(
                "collection 导入前，必须先保证对应 subject 已经存在。",
                "collection 导入支持 subjectId、sourceId、subjectTitle 三种定位方式，优先级依次是 subjectId > sourceId > 标题匹配。",
                "标题匹配使用 titleCn/displayTitle 关键词检索，可配合 subjectSeasonIndex 与 subjectPartIndex 缩小范围。",
                "如果匹配到多个候选，预览会返回候选列表，需要在前端先选择一个后再正式导入。",
                "collectionType 只能是 WANT 或 PAUSED。",
                "如果作品已有正式记录，则收藏导入会跳过，因为正式记录优先于想看/搁置。",
                "如果作品已经在收藏夹中，正式导入会更新清单类型和备注。",
                "preview 和正式 import 使用同一套 subject 匹配逻辑。"
        );

        List<ImportFieldDocResponse> fields = List.of(
                new ImportFieldDocResponse("subjectTitle", false, "目标作品标题关键词（支持模糊匹配 titleCn/displayTitle）。", "进击的巨人", "subjectId/sourceId 为空时建议填写。"),
                new ImportFieldDocResponse("subjectSeasonIndex", false, "季序号（用于辅助缩小匹配范围）。", "4", "可为空。"),
                new ImportFieldDocResponse("subjectPartIndex", false, "Part 序号（用于辅助缩小匹配范围）。", "1", "可为空。"),
                new ImportFieldDocResponse("sourceId", false, "目标作品来源 ID（Bangumi subject id）。", "123456", "可选；比标题匹配更精确。"),
                new ImportFieldDocResponse("subjectId", false, "目标作品数据库 ID。", "1", "可选；优先级最高。"),
                new ImportFieldDocResponse("collectionType", true, "收藏类型。", "WANT", "只能是 WANT 或 PAUSED。"),
                new ImportFieldDocResponse("note", false, "收藏备注。", "样例：之后补完最终季前半部分", "可以为空。")
        );

        return new ImportTemplateResponse(
                "COLLECTION_IMPORT",
                "/api/collections/import",
                "先 subject，后 collection；正式 record 会覆盖 collection 状态",
                rules,
                templateBody,
                fields
        );
    }
}
