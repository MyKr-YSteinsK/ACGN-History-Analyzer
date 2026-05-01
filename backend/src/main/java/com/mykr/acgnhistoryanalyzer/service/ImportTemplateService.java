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
        subjectItem.put("sourceId", "400602");
        subjectItem.put("releaseYear", 2020);
        subjectItem.put("releaseQuarter", 4);

        List<Map<String, Object>> subjects = new ArrayList<>();
        subjects.add(subjectItem);

        Map<String, Object> templateBody = new LinkedHashMap<>();
        templateBody.put("subjects", subjects);

        List<String> rules = List.of(
                "该模板用于“sourceId 定位导入”：每行必须提供 sourceId、releaseYear、releaseQuarter。",
                "后端会按 sourceId 从 Bangumi 拉取作品详情并自动补齐标题、标签、评分、集数、封面等字段。",
                "releaseYear 与 releaseQuarter 以你填写值为准，用于手动修正 Bangumi 自动判定不理想的档期。",
                "subject 唯一性仅由 sourceId 判断（默认 Bangumi subject id）。",
                "同一 sourceId 已存在时，重复导入只会同步三项：tagNames、platformScore、episodeCount；其余字段保持库中原值。",
                "preview 和正式 import 使用同一套判定规则。",
                "月份档期按 1/4/7/10 月填写：1=一月，2=四月，3=七月，4=十月。"
        );

        List<ImportFieldDocResponse> fields = List.of(
                new ImportFieldDocResponse("sourceId", true, "Bangumi subject id。", "400602", "不能为空；后端会据此去 Bangumi 拉完整作品信息。"),
                new ImportFieldDocResponse("releaseYear", true, "你希望写入的发行年份。", "2020", "必填；用于修正自动判定不准确的情况。"),
                new ImportFieldDocResponse("releaseQuarter", true, "你希望写入的月份档期，只能是 1~4；1=一月，2=四月，3=七月，4=十月。", "4", "必填；用于修正自动判定不准确的情况。")
        );

        return new ImportTemplateResponse(
                "SUBJECT_IMPORT",
                "/api/import/bangumi/subject/import",
                "先 Bangumi 导入作品，再 collection，再 record",
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
