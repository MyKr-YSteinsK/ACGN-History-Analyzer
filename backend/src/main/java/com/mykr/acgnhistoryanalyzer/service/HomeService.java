package com.mykr.acgnhistoryanalyzer.service;

import com.mykr.acgnhistoryanalyzer.common.enums.HomeRecordSortType;
import com.mykr.acgnhistoryanalyzer.common.enums.RecordStatus;
import com.mykr.acgnhistoryanalyzer.response.HomeQuarterDashboardResponse;
import com.mykr.acgnhistoryanalyzer.response.RecordQuarterOverviewResponse;
import com.mykr.acgnhistoryanalyzer.response.SubjectResponse;
import com.mykr.acgnhistoryanalyzer.response.UserSubjectRecordResponse;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

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
                                                            String category, String status,
                                                            String keyword, String recordSort) {
        String effectiveCategory = normalizeCategory(category);
        RecordStatus effectiveRecordStatus = normalizeRecordStatus(status);
        String effectiveKeyword = normalizeKeyword(keyword);
        HomeRecordSortType effectiveRecordSort = normalizeRecordSort(recordSort);

        List<UserSubjectRecordResponse> allQuarterRecords =
                userSubjectRecordService.getRecords(year, quarter, effectiveRecordStatus, null, null);

        List<UserSubjectRecordResponse> filteredRecordList = allQuarterRecords.stream()
                .filter(record -> effectiveCategory.equals(record.getSubjectCategory()))
                .filter(record -> matchesKeyword(record.getSubjectTitle(), effectiveKeyword))
                .toList();

        List<UserSubjectRecordResponse> recordList = sortRecordList(filteredRecordList, effectiveRecordSort);

        List<UserSubjectRecordResponse> highScoreRecordList = filteredRecordList.stream()
                .filter(record -> record.getScoreValue() != null && record.getScoreValue() >= 45)
                .sorted((a, b) -> b.getScoreValue().compareTo(a.getScoreValue()))
                .toList();

        RecordQuarterOverviewResponse quarterOverview =
                buildQuarterOverview(year, quarter, filteredRecordList);

        List<SubjectResponse> subjectLibraryList =
                subjectService.getSubjects(year, quarter, effectiveCategory, effectiveKeyword, "NORMAL");

        return new HomeQuarterDashboardResponse(
                year,
                quarter,
                effectiveCategory,
                effectiveRecordStatus == null ? null : effectiveRecordStatus.name(),
                effectiveKeyword,
                effectiveRecordSort.name(),
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
        return category.toUpperCase(Locale.ROOT);
    }

    private RecordStatus normalizeRecordStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        return RecordStatus.valueOf(status.toUpperCase(Locale.ROOT));
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        return keyword.trim();
    }

    private HomeRecordSortType normalizeRecordSort(String recordSort) {
        if (recordSort == null || recordSort.isBlank()) {
            return HomeRecordSortType.TITLE_ASC;
        }
        return HomeRecordSortType.valueOf(recordSort.toUpperCase(Locale.ROOT));
    }

    private boolean matchesKeyword(String text, String keyword) {
        if (keyword == null) {
            return true;
        }
        if (text == null) {
            return false;
        }
        return text.toLowerCase(Locale.ROOT).contains(keyword.toLowerCase(Locale.ROOT));
    }

    private List<UserSubjectRecordResponse> sortRecordList(List<UserSubjectRecordResponse> records,
                                                           HomeRecordSortType sortType) {
        Comparator<UserSubjectRecordResponse> comparator = switch (sortType) {
            case TITLE_ASC -> Comparator.comparing(
                    record -> safeLower(record.getSubjectTitle())
            );
            case TITLE_DESC -> Comparator.comparing(
                    (UserSubjectRecordResponse record) -> safeLower(record.getSubjectTitle())
            ).reversed();
            case SCORE_ASC -> Comparator.comparing(
                    UserSubjectRecordResponse::getScoreValue,
                    Comparator.nullsLast(Integer::compareTo)
            );
            case SCORE_DESC -> Comparator.comparing(
                    UserSubjectRecordResponse::getScoreValue,
                    Comparator.nullsLast(Integer::compareTo)
            ).reversed();
        };

        return records.stream()
                .sorted(comparator)
                .toList();
    }

    private String safeLower(String text) {
        return text == null ? "" : text.toLowerCase(Locale.ROOT);
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