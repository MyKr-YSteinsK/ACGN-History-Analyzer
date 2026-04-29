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
                "作品库准备完成后，再查看 record 导入模板。",
                "先调用 record preview，确认记录能否正确匹配已有作品。",
                "最后调用 record import，正式导入个人记录。"
        );

        List<String> notes = List.of(
                "subject 的业务层唯一性规则为：titleCn + subtitle + seasonIndex + partIndex + category 全都一样，则视为重复作品。",
                "record 导入定位 subject 的规则，已经与 subject 的业务唯一性规则对齐。",
                "当前系统规则是一作品一记录；如果某个 subject 已经有记录，则新的 record 导入会跳过。",
                "franchiseId 和 franchiseName 在 subject 导入中二选一即可；如果都传，当前逻辑优先使用 franchiseId。",
                "月份档期规则：1=一月，2=四月，3=七月，4=十月。",
                "subject 导入时 tagNames 会写入结构化标签系统，后续作品库可按标签筛选。",
                "subject 导入时 coverUrl 可以填写 Bangumi 图片地址，正式导入会尝试缓存成本地 /api/assets/covers 路径。",
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
                        "record 导入模板",
                        "GET",
                        "/api/import/templates/record",
                        "返回 record 导入示例 JSON、字段说明和规则说明。"
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
                "推荐顺序：franchise（如需）→ subject preview → subject import → record preview → record import",
                steps,
                notes,
                endpoints
        );
    }
}
