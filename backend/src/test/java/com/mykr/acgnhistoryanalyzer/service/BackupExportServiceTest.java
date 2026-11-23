package com.mykr.acgnhistoryanalyzer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mykr.acgnhistoryanalyzer.entity.Subject;
import com.mykr.acgnhistoryanalyzer.entity.UserSubjectRecord;
import com.mykr.acgnhistoryanalyzer.repository.SubjectRepository;
import com.mykr.acgnhistoryanalyzer.repository.UserSubjectRecordRepository;
import com.mykr.acgnhistoryanalyzer.response.BackupExportResponse;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class BackupExportServiceTest {

    private final UserSubjectRecordRepository records = mock(UserSubjectRecordRepository.class);
    private final SubjectRepository subjects = mock(SubjectRepository.class);
    private final ObjectMapper json = new ObjectMapper();
    private final BackupExportService exporter = new BackupExportService(records, subjects, json);

    @Test
    void exportsBangumiSourceIdAndRetainsLocalSubjectIdForOtherRecords() throws Exception {
        Subject bangumi = new Subject();
        ReflectionTestUtils.setField(bangumi, "id", 11L);
        bangumi.setSourcePlatform(" bangumi ");
        bangumi.setSourceId(" 12345 ");
        bangumi.setReleaseYear(2014);
        bangumi.setReleaseQuarter(4);

        Subject local = new Subject();
        ReflectionTestUtils.setField(local, "id", 12L);
        local.setSourcePlatform("LOCAL");
        local.setSourceId("local-id");

        UserSubjectRecord bangumiRecord = record(11L, 46, "demo");
        UserSubjectRecord localRecord = record(12L, 39, null);
        when(records.findAll(Sort.by("id").ascending())).thenReturn(List.of(bangumiRecord, localRecord));
        when(subjects.findAllById(List.of(11L, 12L))).thenReturn(List.of(bangumi, local));

        BackupExportResponse export = exporter.buildBangumiRoundtripExport();

        assertEquals(json.readTree("""
                {"records":[
                  {"recordYear":2020,"recordQuarter":1,"scoreValue":46,"note":"demo","sourceId":"12345"},
                  {"recordYear":2020,"recordQuarter":1,"scoreValue":39,"subjectId":12}
                ]}
                """), json.readTree(export.getRecordJsonText()));
        assertEquals(json.readTree("""
                {"subjects":[{"sourceId":"12345","releaseYear":2014,"releaseQuarter":4}]}
                """), json.readTree(export.getSubjectJsonText()));
    }

    @Test
    void omitsSubjectJsonWhenThereIsNothingToExport() throws Exception {
        when(records.findAll(Sort.by("id").ascending())).thenReturn(List.of());

        BackupExportResponse export = exporter.buildBangumiRoundtripExport();

        assertEquals(json.readTree("""
                {"records":[]}
                """), json.readTree(export.getRecordJsonText()));
        assertNull(export.getSubjectJsonText());
        verifyNoInteractions(subjects);
    }

    private static UserSubjectRecord record(long subjectId, int score, String note) {
        UserSubjectRecord record = new UserSubjectRecord();
        record.setSubjectId(subjectId);
        record.setRecordYear(2020);
        record.setRecordQuarter(1);
        record.setScoreValue(score);
        record.setNote(note);
        return record;
    }
}
