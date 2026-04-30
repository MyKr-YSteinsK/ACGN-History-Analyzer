package com.mykr.acgnhistoryanalyzer.service;

import com.mykr.acgnhistoryanalyzer.response.ImportEndpointDocResponse;
import com.mykr.acgnhistoryanalyzer.response.ImportOverviewResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ImportOverviewService {

    public ImportOverviewResponse getImportOverview() {
        List<String> steps = List.of(
                "先选择年份和一月/四月/七月/十月档期，使用 Bangumi 自动导入作品库。",
                "Bangumi 预览会检查重复、字段和候选作品，确认前可以排除不想写入的条目。",
                "作品库准备完成后，可以导入 collection 想看/搁置。",
                "最后查看 record 导入模板，预览并正式导入个人记录。"
        );

        List<String> notes = List.of(
                "作品库不再开放手动 JSON/CSV 导入，避免脏数据污染作品本体。",
                "subject 的业务层唯一性规则为：titleCn + subtitle + seasonIndex + partIndex + category 全都一样，则视为重复作品。",
                "Bangumi 导入会额外使用 sourcePlatform + sourceId 去重，同一批次内也会去重。",
                "record 导入定位 subject 的规则，已经与 subject 的业务唯一性规则对齐。",
                "当前系统规则是一作品一记录；如果某个 subject 已经有记录，则新的 record 导入会跳过。",
                "collection 导入只管理 WANT / PAUSED；如果作品已有正式记录，则收藏导入会跳过。",
                "Bangumi 导入和 record 正式导入成功后，会触发轻量系列检测，同名多季、Part 或跨媒介作品会自动关联系列。",
                "月份档期规则：1=一月，2=四月，3=七月，4=十月。",
                "Bangumi 标签会经过常见标签白名单过滤，只保留适合筛选和 AI 分析的结构化标签。",
                "Bangumi 导入时会尝试缓存封面到本地 /api/assets/covers 路径。",
                "Bangumi 新番导入会调用官方 /v0/subjects 接口，按年份与一月/四月/七月/十月档期拉取 TV 动画，可选择是否包含 WEB 动画。",
                "preview 接口只检查、不写库；正式 import 接口才会真正写数据库。"
        );

        List<ImportEndpointDocResponse> endpoints = List.of(
                new ImportEndpointDocResponse(
                        "Bangumi 新番预览",
                        "POST",
                        "/api/import/bangumi/season/preview",
                        "按年份与月份档期从 Bangumi 拉取动画作品，转换为 subject 导入预览，不写库。"
                ),
                new ImportEndpointDocResponse(
                        "Bangumi 新番导入",
                        "POST",
                        "/api/import/bangumi/season/import",
                        "按年份与月份档期从 Bangumi 拉取动画作品，复用 subject 导入规则正式写入作品库。"
                ),
                new ImportEndpointDocResponse(
                        "Bangumi 导入任务启动",
                        "POST",
                        "/api/import/bangumi/season/import-jobs",
                        "启动异步 Bangumi 导入任务，返回 jobId，前端用它查询导入进度。"
                ),
                new ImportEndpointDocResponse(
                        "Bangumi 导入任务进度",
                        "GET",
                        "/api/import/bangumi/season/import-jobs/{jobId}",
                        "查询 Bangumi 导入任务的阶段、百分比、当前消息和最终写入结果。"
                ),
                new ImportEndpointDocResponse(
                        "record 导入模板",
                        "GET",
                        "/api/import/templates/record",
                        "返回 record 导入示例 JSON、字段说明和规则说明。"
                ),
                new ImportEndpointDocResponse(
                        "collection 导入模板",
                        "GET",
                        "/api/import/templates/collection",
                        "返回 collection 导入示例 JSON、字段说明和规则说明。"
                ),
                new ImportEndpointDocResponse(
                        "collection 导入预览",
                        "POST",
                        "/api/collections/import/preview",
                        "预检查 collection 导入结果，不写库。"
                ),
                new ImportEndpointDocResponse(
                        "collection 正式导入",
                        "POST",
                        "/api/collections/import",
                        "正式批量导入想看/搁置收藏到 user_subject_collection 表。"
                ),
                new ImportEndpointDocResponse(
                        "record 导入预览",
                        "POST",
                        "/api/records/import/preview",
                        "预检查 record 导入结果，不写库。"
                ),
                new ImportEndpointDocResponse(
                        "record 正式导入",
                        "POST",
                        "/api/records/import",
                        "正式批量导入个人记录到 user_subject_record 表。"
                )
        );

        return new ImportOverviewResponse(
                "IMPORT_OVERVIEW",
                "推荐顺序：Bangumi 作品库 → collection（如需）→ record",
                steps,
                notes,
                endpoints
        );
    }
}
