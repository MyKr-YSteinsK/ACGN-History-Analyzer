package com.mykr.acgnhistoryanalyzer.service;

import com.mykr.acgnhistoryanalyzer.response.TopMasterpieceResponse;
import com.mykr.acgnhistoryanalyzer.response.TopQuarterGroupResponse;
import com.mykr.acgnhistoryanalyzer.response.TopYearHighScoreResponse;
import com.mykr.acgnhistoryanalyzer.response.UserSubjectRecordResponse;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class TopService {

    private static final int YEAR_TOP_THRESHOLD = 45;
    private static final int MASTERPIECE_THRESHOLD = 48;

    private final UserSubjectRecordService userSubjectRecordService;

    public TopService(UserSubjectRecordService userSubjectRecordService) {
        this.userSubjectRecordService = userSubjectRecordService;
    }

    public TopYearHighScoreResponse getYearHighScore(Integer year) {
        Integer effectiveYear = year != null ? year : userSubjectRecordService.getLatestRecordYear();

        if (effectiveYear == null) {
            return new TopYearHighScoreResponse(null, YEAR_TOP_THRESHOLD, 0, List.of(
                    new TopQuarterGroupResponse(1, 0, List.of()),
                    new TopQuarterGroupResponse(2, 0, List.of()),
                    new TopQuarterGroupResponse(3, 0, List.of()),
                    new TopQuarterGroupResponse(4, 0, List.of())
            ));
        }

        TopQuarterGroupResponse q1 = buildQuarterGroup(effectiveYear, 1);
        TopQuarterGroupResponse q2 = buildQuarterGroup(effectiveYear, 2);
        TopQuarterGroupResponse q3 = buildQuarterGroup(effectiveYear, 3);
        TopQuarterGroupResponse q4 = buildQuarterGroup(effectiveYear, 4);

        int totalCount = q1.getCount() + q2.getCount() + q3.getCount() + q4.getCount();

        return new TopYearHighScoreResponse(
                effectiveYear,
                YEAR_TOP_THRESHOLD,
                totalCount,
                List.of(q1, q2, q3, q4)
        );
    }

    public TopMasterpieceResponse getMasterpieces() {
        List<UserSubjectRecordResponse> records = userSubjectRecordService
                .getRecords(null, null, null, MASTERPIECE_THRESHOLD, 50)
                .stream()
                .sorted(
                        Comparator.comparing(UserSubjectRecordResponse::getScoreValue,
                                        Comparator.nullsLast(Integer::compareTo))
                                .reversed()
                                .thenComparing(UserSubjectRecordResponse::getRecordYear,
                                        Comparator.nullsLast(Integer::compareTo))
                                .thenComparing(UserSubjectRecordResponse::getRecordQuarter,
                                        Comparator.nullsLast(Integer::compareTo))
                                .reversed()
                                .thenComparing(record ->
                                        record.getSubjectTitle() == null ? "" : record.getSubjectTitle()
                                )
                )
                .toList();

        return new TopMasterpieceResponse(
                MASTERPIECE_THRESHOLD,
                records.size(),
                records
        );
    }

    private TopQuarterGroupResponse buildQuarterGroup(Integer year, Integer quarter) {
        List<UserSubjectRecordResponse> records = userSubjectRecordService
                .getRecords(year, quarter, null, YEAR_TOP_THRESHOLD, 50)
                .stream()
                .sorted(
                        Comparator.comparing(UserSubjectRecordResponse::getScoreValue,
                                        Comparator.nullsLast(Integer::compareTo))
                                .reversed()
                                .thenComparing(record ->
                                        record.getSubjectTitle() == null ? "" : record.getSubjectTitle()
                                )
                )
                .toList();

        return new TopQuarterGroupResponse(
                quarter,
                records.size(),
                records
        );
    }
}