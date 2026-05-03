package com.mykr.acgnhistoryanalyzer.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mykr.acgnhistoryanalyzer.entity.Subject;
import com.mykr.acgnhistoryanalyzer.entity.UserSubjectRecord;
import com.mykr.acgnhistoryanalyzer.repository.SubjectRepository;
import com.mykr.acgnhistoryanalyzer.repository.UserSubjectRecordRepository;
import com.mykr.acgnhistoryanalyzer.response.BackupExportResponse;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class BackupExportService {

    private static final String BANGUMI = "BANGUMI";

    private final UserSubjectRecordRepository userSubjectRecordRepository;
    private final SubjectRepository subjectRepository;
    private final ObjectMapper objectMapper;

    public BackupExportService(
            UserSubjectRecordRepository userSubjectRecordRepository,
            SubjectRepository subjectRepository,
            ObjectMapper objectMapper
    ) {
        this.userSubjectRecordRepository = userSubjectRecordRepository;
        this.subjectRepository = subjectRepository;
        this.objectMapper = objectMapper.copy().setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    public BackupExportResponse buildBangumiRoundtripExport() {
        List<UserSubjectRecord> records = userSubjectRecordRepository.findAll(Sort.by("id").ascending());
        List<Long> subjectIds = records.stream()
                .map(UserSubjectRecord::getSubjectId)
                .distinct()
                .toList();

        Map<Long, Subject> subjectById = subjectIds.isEmpty()
                ? Map.of()
                : subjectRepository.findAllById(subjectIds).stream()
                .collect(Collectors.toMap(Subject::getId, s -> s));

        List<Map<String, Object>> recordRows = new ArrayList<>();
        List<Map<String, Object>> subjectRows = new ArrayList<>();
        Set<Long> bangumiSubjectIdsExported = new LinkedHashSet<>();

        for (UserSubjectRecord record : records) {
            Subject subject = subjectById.get(record.getSubjectId());
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("recordYear", record.getRecordYear());
            row.put("recordQuarter", record.getRecordQuarter());
            row.put("scoreValue", record.getScoreValue());
            row.put("note", record.getNote());

            if (subject != null && isBangumi(subject)) {
                row.put("sourceId", subject.getSourceId() == null ? null : subject.getSourceId().trim());
            } else {
                row.put("subjectId", record.getSubjectId());
            }
            recordRows.add(row);

            if (subject != null && isBangumi(subject) && bangumiSubjectIdsExported.add(subject.getId())) {
                Map<String, Object> sj = new LinkedHashMap<>();
                sj.put("sourceId", subject.getSourceId().trim());
                sj.put("releaseYear", subject.getReleaseYear());
                sj.put("releaseQuarter", subject.getReleaseQuarter());
                subjectRows.add(sj);
            }
        }

        try {
            String recordJson = objectMapper.writeValueAsString(Map.of("records", recordRows));
            String subjectJson = subjectRows.isEmpty()
                    ? null
                    : objectMapper.writeValueAsString(Map.of("subjects", subjectRows));

            String hint = subjectJson == null
                    ? "仅复制「记录」JSON 到导入中心 → 个人记录 → JSON。若含 Bangumi sourceId 请先确保库中已有对应作品。"
                    : "① 导入中心 → 作品库 → JSON，粘贴「作品」JSON 并预览写入；② 再选个人记录 → JSON，粘贴「记录」JSON。";

            return new BackupExportResponse(recordJson, subjectJson, hint);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Bangumi round-trip export serialization failed", e);
        }
    }

    private static boolean isBangumi(Subject subject) {
        return subject.getSourcePlatform() != null
                && BANGUMI.equalsIgnoreCase(subject.getSourcePlatform().trim())
                && subject.getSourceId() != null
                && !subject.getSourceId().isBlank();
    }
}
