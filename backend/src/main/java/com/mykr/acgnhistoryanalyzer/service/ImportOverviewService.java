package com.mykr.acgnhistoryanalyzer.service;

import com.mykr.acgnhistoryanalyzer.response.ImportEndpointDocResponse;
import com.mykr.acgnhistoryanalyzer.response.ImportOverviewResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ImportOverviewService {

    public ImportOverviewResponse getImportOverview() {
        List<String> steps = List.of(
                "先查看 subject 导入模板，确认作品字段结构。",
                "先调用 subject preview，确认哪些作品能导入、哪些会跳过。",
                "再调用 subject import，正式导入作品库。",
                "作品库准备完成后，可以导入 collection 想看/搁置。",
                "最后查看 record 导入模板，预览并正式导入个人记录。"
        );

        List<String> notes = List.of(
                "subject 的业务层唯一性规则为：titleCn + subtitle + seasonIndex + partIndex + category 全都一样，则视为重复作品。",
                "record 导入定位 subject 的规则，已经与 subject 的业务唯一性规则对齐。",
                "当前系统规则是一作品一记录；如果某个 subject 已经有记录，则新的 record 导入会跳过。",
                "collection 导入只管理 WANT / PAUSED；如果作品已有正式记录，则收藏导入会跳过。",
                "franchiseId 和 franchiseName 在 subject 导入中二选一即可；如果都传，当前逻辑优先使用 franchiseId。",
                "franchiseName 匹配不到已有系列时，subject 仍会导入，只是不关联系列；franchiseId 明确不存在时才会跳过。",
                "record 正式导入成功后，会触发轻量系列检测，同名多季、Part 或跨媒介作品会自动关联系列。",
                "月份档期规则：1=一月，2=四月，3=七月，4=十月。",
                "subject 导入时 tagNames 会写入结构化标签系统，后续作品库可按标签筛选。",
                "subject 导入时 coverUrl 可以填写 Bangumi 图片地址，正式导入会尝试缓存成本地 /api/assets/covers 路径。",
                "Bangumi 新番导入会调用官方 /v0/subjects 接口，按年份与一月/四月/七月/十月档期拉取 TV 动画，可选择是否包含 WEB 动画。",
                "preview 接口只检查、不写库；正式 import 接口才会真正写数据库。"
        );

        List<ImportEndpointDocResponse> endpoints = List.of(
                new ImportEndpointDocResponse(
                        "subject 导入模板",
                        "GET",
                        "/api/import/templates/subject",
                        "返回 subject 导入示例 JSON、字段说明和规则说明。"
                ),
                new ImportEndpointDocResponse(
                        "subject 导入预览",
                        "POST",
                        "/api/subjects/import/preview",
                        "预检查 subject 导入结果，不写库。"
                ),
                new ImportEndpointDocResponse(
                        "subject 正式导入",
                        "POST",
                        "/api/subjects/import",
                        "正式批量导入作品到 subject 表。"
                ),
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
                "推荐顺序：franchise（如需）→ subject → collection（如需）→ record",
                steps,
                notes,
                endpoints
        );
    }
}
