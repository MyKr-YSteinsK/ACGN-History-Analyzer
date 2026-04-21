package com.mykr.acgnhistoryanalyzer.service;

import com.mykr.acgnhistoryanalyzer.common.enums.RecordStatus;
import com.mykr.acgnhistoryanalyzer.response.HomeQuarterDashboardResponse;
import com.mykr.acgnhistoryanalyzer.response.RecordQuarterOverviewResponse;
import com.mykr.acgnhistoryanalyzer.response.SubjectResponse;
import com.mykr.acgnhistoryanalyzer.response.UserSubjectRecordResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HomeService {

    private final UserSubjectRecordService userSubjectRecordService;
    private final SubjectService subjectService;

    public HomeService(UserSubjectRecordService userSubjectRecordService,
                       SubjectService subjectService) {
        this.userSubjectRecordService = userSubjectRecordService;
        this.subjectService = subjectService;
    }

    public HomeQuarterDashboardResponse getQuarterDashboard(Integer year, Integer quarter,
                                                            String category, String status) {
        String effectiveCategory = normalizeCategory(category);
        RecordStatus effectiveRecordStatus = normalizeRecordStatus(status);

        List<UserSubjectRecordResponse> allQuarterRecords =
                userSubjectRecordService.getRecords(year, quarter, effectiveRecordStatus, null, null);

        List<UserSubjectRecordResponse> recordList = allQuarterRecords.stream()
                .filter(record -> effectiveCategory.equals(record.getSubjectCategory()))
                .toList();

        List<UserSubjectRecordResponse> highScoreRecordList = recordList.stream()
                .filter(record -> record.getScoreValue() != null && record.getScoreValue() >= 45)
                .sorted((a, b) -> b.getScoreValue().compareTo(a.getScoreValue()))
                .toList();

        RecordQuarterOverviewResponse quarterOverview =
                buildQuarterOverview(year, quarter, recordList);

        List<SubjectResponse> subjectLibraryList =
                subjectService.getSubjects(year, quarter, effectiveCategory, null, "NORMAL");

        return new HomeQuarterDashboardResponse(
                year,
                quarter,
                effectiveCategory,
                effectiveRecordStatus == null ? null : effectiveRecordStatus.name(),
                quarterOverview,
                recordList,
                highScoreRecordList,
                subjectLibraryList
        );
    }

    private String normalizeCategory(String category) {
        if (category == null || category.isBlank()) {
            return "ANIME";
        }
        return category.toUpperCase();
    }

    private RecordStatus normalizeRecordStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        return RecordStatus.valueOf(status.toUpperCase());
    }

    private RecordQuarterOverviewResponse buildQuarterOverview(Integer year, Integer quarter,
                                                               List<UserSubjectRecordResponse> recordList) {
        int totalCount = recordList.size();
        int highScoreCount = 0;
        int watchedCount = 0;
        int onHoldCount = 0;
        int wantToWatchCount = 0;
        int excellentCount = 0;
        int normalCount = 0;
        int badCount = 0;
        int unratedCount = 0;

        for (UserSubjectRecordResponse record : recordList) {
            if (record.getRecordStatus() != null) {
                switch (record.getRecordStatus()) {
                    case "WATCHED" -> watchedCount++;
                    case "ON_HOLD" -> onHoldCount++;
                    case "WANT_TO_WATCH" -> wantToWatchCount++;
                }
            }

            Integer score = record.getScoreValue();

            if (score == null) {
                unratedCount++;
            } else {
                if (score >= 45) {
                    highScoreCount++;
                }

                if (score >= 42 && score <= 50) {
                    excellentCount++;
                } else if (score >= 35 && score <= 41) {
                    normalCount++;
                } else if (score >= 20 && score <= 34) {
                    badCount++;
                }
            }
        }

        return new RecordQuarterOverviewResponse(
                year,
                quarter,
                totalCount,
                highScoreCount,
                watchedCount,
                onHoldCount,
                wantToWatchCount,
                excellentCount,
                normalCount,
                badCount,
                unratedCount
        );
    }
}