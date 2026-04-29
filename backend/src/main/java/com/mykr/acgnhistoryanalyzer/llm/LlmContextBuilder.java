package com.mykr.acgnhistoryanalyzer.llm;

import com.mykr.acgnhistoryanalyzer.common.enums.CollectionType;
import com.mykr.acgnhistoryanalyzer.entity.UserSubjectCollection;
import com.mykr.acgnhistoryanalyzer.repository.SubjectRepository;
import com.mykr.acgnhistoryanalyzer.repository.UserSubjectCollectionRepository;
import com.mykr.acgnhistoryanalyzer.repository.UserSubjectRecordRepository;
import com.mykr.acgnhistoryanalyzer.response.TagResponse;
import com.mykr.acgnhistoryanalyzer.response.UserSubjectRecordResponse;
import com.mykr.acgnhistoryanalyzer.service.UserSubjectRecordService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class LlmContextBuilder {

    private static final List<String> CATEGORY_ORDER = List.of("ANIME", "COMIC", "GAME", "NOVEL");

    private static final Map<Integer, String> QUARTER_LABELS = Map.of(
            1, "一月",
            2, "四月",
            3, "七月",
            4, "十月"
    );

    private final UserSubjectRecordService userSubjectRecordService;
    private final UserSubjectRecordRepository userSubjectRecordRepository;
    private final UserSubjectCollectionRepository userSubjectCollectionRepository;
    private final SubjectRepository subjectRepository;

    public LlmContextBuilder(UserSubjectRecordService userSubjectRecordService,
                             UserSubjectRecordRepository userSubjectRecordRepository,
                             UserSubjectCollectionRepository userSubjectCollectionRepository,
                             SubjectRepository subjectRepository) {
        this.userSubjectRecordService = userSubjectRecordService;
        this.userSubjectRecordRepository = userSubjectRecordRepository;
        this.userSubjectCollectionRepository = userSubjectCollectionRepository;
        this.subjectRepository = subjectRepository;
    }

    public Map<String, Object> buildYearlyContext(Integer year, String category) {
        List<UserSubjectRecordResponse> yearRecords = filterByCategory(
                userSubjectRecordService.getRecords(year, null, null, null),
                category
        );
        List<UserSubjectRecordResponse> previousYearRecords = filterByCategory(
                userSubjectRecordService.getRecords(year - 1, null, null, null),
                category
        );

        Map<String, Object> context = new LinkedHashMap<>();
        context.put("analysisType", "YEARLY");
        context.put("year", year);
        context.put("category", category);
        context.put("comparisonLabel", "上一年同范围");
        context.put("totalRecords", yearRecords.size());
        context.put("previousYearTotalRecords", previousYearRecords.size());
        context.put("yearOverYearDiff", yearRecords.size() - previousYearRecords.size());
        context.put("averageScore", averageScore(yearRecords));
        context.put("highScoreCount", countScoreAtLeast(yearRecords, 45));
        context.put("lowScoreCount", countScoreBelow(yearRecords, 30));
        context.put("categoryDistribution", buildCategoryDistribution(yearRecords));
        context.put("quarterDistribution", buildQuarterDistribution(yearRecords));
        context.put("topRecords", buildRecordHighlights(yearRecords, true));
        context.put("lowScoreRecords", buildRecordHighlights(yearRecords, false));
        context.put("topTags", buildTagStats(yearRecords));
        context.put("collectionCandidates", buildCollectionCandidateStats(category));
        context.put("libraryCandidateCount", buildLibraryCandidateCount(category));
        context.put("evidence", buildEvidence(context));
        return context;
    }

    public Map<String, Object> buildQuarterlyContext(Integer year, Integer quarter, String category) {
        List<UserSubjectRecordResponse> quarterRecords = filterByCategory(
                userSubjectRecordService.getRecords(year, quarter, null, null),
                category
        );
        QuarterPosition previousQuarter = previousQuarter(year, quarter);
        List<UserSubjectRecordResponse> previousQuarterRecords = filterByCategory(
                userSubjectRecordService.getRecords(
                        previousQuarter.year(),
                        previousQuarter.quarter(),
                        null,
                        null
                ),
                category
        );

        Map<String, Object> context = new LinkedHashMap<>();
        context.put("analysisType", "QUARTERLY");
        context.put("year", year);
        context.put("quarter", quarter);
        context.put("quarterLabel", QUARTER_LABELS.getOrDefault(quarter, "未知月份"));
        context.put("category", category);
        context.put("comparisonLabel", "上一档期");
        context.put("totalRecords", quarterRecords.size());
        context.put("previousYearTotalRecords", previousQuarterRecords.size());
        context.put("yearOverYearDiff", quarterRecords.size() - previousQuarterRecords.size());
        context.put("averageScore", averageScore(quarterRecords));
        context.put("highScoreCount", countScoreAtLeast(quarterRecords, 45));
        context.put("lowScoreCount", countScoreBelow(quarterRecords, 30));
        context.put("categoryDistribution", buildCategoryDistribution(quarterRecords));
        context.put("quarterDistribution", buildQuarterDistribution(quarterRecords));
        context.put("topRecords", buildRecordHighlights(quarterRecords, true));
        context.put("lowScoreRecords", buildRecordHighlights(quarterRecords, false));
        context.put("topTags", buildTagStats(quarterRecords));
        context.put("collectionCandidates", buildCollectionCandidateStats(category));
        context.put("libraryCandidateCount", buildLibraryCandidateCount(category));
        context.put("evidence", buildEvidence(context));
        return context;
    }

    public Map<String, Object> buildTypePreferenceContext(String category) {
        List<UserSubjectRecordResponse> allRecords =
                userSubjectRecordService.getRecords(null, null, null, null);
        List<UserSubjectRecordResponse> categoryRecords = filterByCategory(allRecords, category);

        Map<String, Object> context = new LinkedHashMap<>();
        context.put("analysisType", "TYPE_PREFERENCE");
        context.put("category", category);
        context.put("comparisonLabel", "全部记录中的该类型");
        context.put("totalRecords", categoryRecords.size());
        context.put("previousYearTotalRecords", allRecords.size());
        context.put("yearOverYearDiff", categoryRecords.size());
        context.put("averageScore", averageScore(categoryRecords));
        context.put("highScoreCount", countScoreAtLeast(categoryRecords, 45));
        context.put("lowScoreCount", countScoreBelow(categoryRecords, 30));
        context.put("categoryDistribution", buildCategoryDistribution(allRecords));
        context.put("quarterDistribution", buildQuarterDistribution(categoryRecords));
        context.put("topRecords", buildRecordHighlights(categoryRecords, true));
        context.put("lowScoreRecords", buildRecordHighlights(categoryRecords, false));
        context.put("topTags", buildTagStats(categoryRecords));
        context.put("collectionCandidates", buildCollectionCandidateStats(category));
        context.put("libraryCandidateCount", buildLibraryCandidateCount(category));
        context.put("evidence", buildEvidence(context));
        return context;
    }

    private List<UserSubjectRecordResponse> filterByCategory(
            List<UserSubjectRecordResponse> records,
            String category
    ) {
        if (category == null || category.isBlank()) {
            return records;
        }

        return records.stream()
                .filter(record -> category.equalsIgnoreCase(record.getSubjectCategory()))
                .toList();
    }

    private BigDecimal averageScore(List<UserSubjectRecordResponse> records) {
        List<Integer> scores = records.stream()
                .map(UserSubjectRecordResponse::getScoreValue)
                .filter(Objects::nonNull)
                .toList();

        if (scores.isEmpty()) {
            return BigDecimal.ZERO;
        }

        double average = scores.stream().mapToInt(Integer::intValue).average().orElse(0);
        return BigDecimal.valueOf(average).setScale(1, RoundingMode.HALF_UP);
    }

    private long countScoreAtLeast(List<UserSubjectRecordResponse> records, int score) {
        return records.stream()
                .map(UserSubjectRecordResponse::getScoreValue)
                .filter(Objects::nonNull)
                .filter(value -> value >= score)
                .count();
    }

    private long countScoreBelow(List<UserSubjectRecordResponse> records, int score) {
        return records.stream()
                .map(UserSubjectRecordResponse::getScoreValue)
                .filter(Objects::nonNull)
                .filter(value -> value < score)
                .count();
    }

    private Map<String, Long> buildCategoryDistribution(List<UserSubjectRecordResponse> records) {
        Map<String, Long> grouped = records.stream()
                .filter(record -> record.getSubjectCategory() != null)
                .collect(Collectors.groupingBy(
                        UserSubjectRecordResponse::getSubjectCategory,
                        Collectors.counting()
                ));

        Map<String, Long> distribution = new LinkedHashMap<>();
        for (String category : CATEGORY_ORDER) {
            distribution.put(category, grouped.getOrDefault(category, 0L));
        }
        return distribution;
    }

    private Map<String, Long> buildQuarterDistribution(List<UserSubjectRecordResponse> records) {
        Map<Integer, Long> grouped = records.stream()
                .filter(record -> record.getRecordQuarter() != null)
                .collect(Collectors.groupingBy(
                        UserSubjectRecordResponse::getRecordQuarter,
                        Collectors.counting()
                ));

        Map<String, Long> distribution = new LinkedHashMap<>();
        for (int quarter = 1; quarter <= 4; quarter++) {
            distribution.put(QUARTER_LABELS.get(quarter), grouped.getOrDefault(quarter, 0L));
        }
        return distribution;
    }

    private List<Map<String, Object>> buildRecordHighlights(
            List<UserSubjectRecordResponse> records,
            boolean highScore
    ) {
        Comparator<UserSubjectRecordResponse> comparator = Comparator.comparing(
                UserSubjectRecordResponse::getScoreValue,
                Comparator.nullsLast(Integer::compareTo)
        );

        if (highScore) {
            comparator = comparator.reversed();
        }

        return records.stream()
                .filter(record -> record.getScoreValue() != null)
                .sorted(comparator.thenComparing(UserSubjectRecordResponse::getSubjectTitle))
                .limit(3)
                .map(this::toRecordHighlight)
                .toList();
    }

    private Map<String, Object> toRecordHighlight(UserSubjectRecordResponse record) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("title", record.getSubjectTitle());
        item.put("score", record.getScoreValue());
        item.put("category", record.getSubjectCategory());
        item.put("tags", record.getTagList() == null
                ? List.of()
                : record.getTagList().stream().map(TagResponse::getName).limit(5).toList());
        return item;
    }

    private List<Map<String, Object>> buildTagStats(List<UserSubjectRecordResponse> records) {
        Map<String, TagScoreBucket> bucketMap = new HashMap<>();

        for (UserSubjectRecordResponse record : records) {
            if (record.getTagList() == null || record.getTagList().isEmpty()) {
                continue;
            }

            for (TagResponse tag : record.getTagList()) {
                TagScoreBucket bucket = bucketMap.computeIfAbsent(tag.getName(), TagScoreBucket::new);
                bucket.add(record.getScoreValue());
            }
        }

        return bucketMap.values().stream()
                .sorted(Comparator
                        .comparing(TagScoreBucket::count).reversed()
                        .thenComparing(TagScoreBucket::averageScore, Comparator.reverseOrder())
                        .thenComparing(TagScoreBucket::name))
                .limit(8)
                .map(TagScoreBucket::toMap)
                .toList();
    }

    private Map<String, Long> buildCollectionCandidateStats(String category) {
        List<UserSubjectCollection> collections = userSubjectCollectionRepository.findAll().stream()
                .filter(collection -> matchesCollectionCategory(collection, category))
                .filter(collection -> !userSubjectRecordRepository.existsBySubjectId(collection.getSubjectId()))
                .toList();

        Map<String, Long> stats = new LinkedHashMap<>();
        stats.put("WANT", collections.stream()
                .filter(collection -> collection.getCollectionType() == CollectionType.WANT)
                .count());
        stats.put("PAUSED", collections.stream()
                .filter(collection -> collection.getCollectionType() == CollectionType.PAUSED)
                .count());
        return stats;
    }

    private boolean matchesCollectionCategory(UserSubjectCollection collection, String category) {
        if (category == null || category.isBlank()) {
            return true;
        }

        return subjectRepository.findById(collection.getSubjectId())
                .map(subject -> category.equalsIgnoreCase(subject.getCategory()))
                .orElse(false);
    }

    private long buildLibraryCandidateCount(String category) {
        Set<Long> recordedSubjectIds = userSubjectRecordRepository.findAll().stream()
                .map(record -> record.getSubjectId())
                .collect(Collectors.toSet());

        return subjectRepository.findAll().stream()
                .filter(subject -> "NORMAL".equalsIgnoreCase(subject.getStatus()))
                .filter(subject -> category == null || category.isBlank()
                        || category.equalsIgnoreCase(subject.getCategory()))
                .filter(subject -> !recordedSubjectIds.contains(subject.getId()))
                .count();
    }

    private List<String> buildEvidence(Map<String, Object> context) {
        List<String> evidence = new ArrayList<>();
        String analysisType = (String) context.get("analysisType");
        Integer year = (Integer) context.get("year");
        Integer quarter = (Integer) context.get("quarter");
        String category = (String) context.get("category");
        String scope = category == null ? "全部类型" : category;

        if ("TYPE_PREFERENCE".equals(analysisType)) {
            evidence.add(scope + " 长期正式记录 " + context.get("totalRecords") + " 条");
        } else if ("QUARTERLY".equals(analysisType)) {
            evidence.add(year + " 年 " + QUARTER_LABELS.getOrDefault(quarter, "未知月份")
                    + "档期 " + scope + " 正式记录 " + context.get("totalRecords") + " 条");
        } else {
            evidence.add(year + " 年 " + scope + " 正式记录 " + context.get("totalRecords") + " 条");
        }

        evidence.add("个人评分均值 " + context.get("averageScore") + " / 50，高分记录 "
                + context.get("highScoreCount") + " 条，低分记录 " + context.get("lowScoreCount") + " 条");
        evidence.add("相对" + context.get("comparisonLabel") + "变化 "
                + formatDiff((Integer) context.get("yearOverYearDiff")) + " 条");

        @SuppressWarnings("unchecked")
        Map<String, Long> collectionCandidates = (Map<String, Long>) context.get("collectionCandidates");
        evidence.add("未记录推荐候选：想看 " + collectionCandidates.get("WANT")
                + " 条，搁置 " + collectionCandidates.get("PAUSED")
                + " 条，作品库未记录 " + context.get("libraryCandidateCount") + " 条");

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> topTags = (List<Map<String, Object>>) context.get("topTags");
        if (!topTags.isEmpty()) {
            evidence.add("高频标签：" + topTags.stream()
                    .limit(5)
                    .map(tag -> tag.get("name") + "×" + tag.get("count"))
                    .collect(Collectors.joining("、")));
        }

        return evidence;
    }

    private String formatDiff(Integer diff) {
        if (diff == null) {
            return "0";
        }
        return diff > 0 ? "+" + diff : String.valueOf(diff);
    }

    private QuarterPosition previousQuarter(Integer year, Integer quarter) {
        if (quarter == null || quarter <= 1) {
            return new QuarterPosition(year - 1, 4);
        }

        return new QuarterPosition(year, quarter - 1);
    }

    private record QuarterPosition(Integer year, Integer quarter) {
    }

    private record TagScoreBucket(String name, List<Integer> scores) {

        TagScoreBucket(String name) {
            this(name, new ArrayList<>());
        }

        void add(Integer score) {
            if (score != null) {
                scores.add(score);
            }
        }

        int count() {
            return scores.size();
        }

        BigDecimal averageScore() {
            if (scores.isEmpty()) {
                return BigDecimal.ZERO;
            }

            double average = scores.stream().mapToInt(Integer::intValue).average().orElse(0);
            return BigDecimal.valueOf(average).setScale(1, RoundingMode.HALF_UP);
        }

        long highScoreCount() {
            return scores.stream().filter(score -> score >= 45).count();
        }

        Map<String, Object> toMap() {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("name", name);
            item.put("count", count());
            item.put("averageScore", averageScore());
            item.put("highScoreCount", highScoreCount());
            return item;
        }
    }
}
