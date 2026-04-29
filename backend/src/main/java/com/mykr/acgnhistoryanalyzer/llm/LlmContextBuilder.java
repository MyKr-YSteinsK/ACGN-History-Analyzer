package com.mykr.acgnhistoryanalyzer.llm;

import com.mykr.acgnhistoryanalyzer.common.enums.CollectionType;
import com.mykr.acgnhistoryanalyzer.entity.Franchise;
import com.mykr.acgnhistoryanalyzer.entity.Subject;
import com.mykr.acgnhistoryanalyzer.entity.Tag;
import com.mykr.acgnhistoryanalyzer.entity.UserSubjectCollection;
import com.mykr.acgnhistoryanalyzer.repository.FranchiseRepository;
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
    private final FranchiseRepository franchiseRepository;

    public LlmContextBuilder(UserSubjectRecordService userSubjectRecordService,
                             UserSubjectRecordRepository userSubjectRecordRepository,
                             UserSubjectCollectionRepository userSubjectCollectionRepository,
                             SubjectRepository subjectRepository,
                             FranchiseRepository franchiseRepository) {
        this.userSubjectRecordService = userSubjectRecordService;
        this.userSubjectRecordRepository = userSubjectRecordRepository;
        this.userSubjectCollectionRepository = userSubjectCollectionRepository;
        this.subjectRepository = subjectRepository;
        this.franchiseRepository = franchiseRepository;
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

    public Map<String, Object> buildScoreStructureContext(String category) {
        List<UserSubjectRecordResponse> records = filterByCategory(
                userSubjectRecordService.getRecords(null, null, null, null),
                category
        );

        Map<String, Object> context = new LinkedHashMap<>();
        context.put("analysisType", "SCORE_STRUCTURE");
        context.put("category", category);
        context.put("comparisonLabel", "全部评分记录");
        context.put("totalRecords", records.size());
        context.put("previousYearTotalRecords", records.size());
        context.put("yearOverYearDiff", records.size());
        context.put("averageScore", averageScore(records));
        context.put("highScoreCount", countScoreAtLeast(records, 45));
        context.put("lowScoreCount", countScoreBelow(records, 30));
        context.put("categoryDistribution", buildCategoryDistribution(records));
        context.put("quarterDistribution", buildQuarterDistribution(records));
        context.put("scoreBands", buildScoreBands(records));
        context.put("categoryScoreStats", buildCategoryScoreStats(records));
        context.put("yearScoreTrend", buildYearScoreTrend(records));
        context.put("topRecords", buildRecordHighlights(records, true));
        context.put("lowScoreRecords", buildRecordHighlights(records, false));
        context.put("topTags", buildTagStats(records));
        context.put("collectionCandidates", buildCollectionCandidateStats(category));
        context.put("libraryCandidateCount", buildLibraryCandidateCount(category));
        context.put("evidence", buildEvidence(context));
        return context;
    }

    public Map<String, Object> buildTagPreferenceContext(String category) {
        List<UserSubjectRecordResponse> records = filterByCategory(
                userSubjectRecordService.getRecords(null, null, null, null),
                category
        );

        Map<String, Object> context = new LinkedHashMap<>();
        context.put("analysisType", "TAG_PREFERENCE");
        context.put("category", category);
        context.put("comparisonLabel", "全部带标签记录");
        context.put("totalRecords", records.size());
        context.put("previousYearTotalRecords", records.size());
        context.put("yearOverYearDiff", records.size());
        context.put("averageScore", averageScore(records));
        context.put("highScoreCount", countScoreAtLeast(records, 45));
        context.put("lowScoreCount", countScoreBelow(records, 30));
        context.put("categoryDistribution", buildCategoryDistribution(records));
        context.put("quarterDistribution", buildQuarterDistribution(records));
        context.put("topRecords", buildRecordHighlights(records, true));
        context.put("lowScoreRecords", buildRecordHighlights(records, false));
        context.put("topTags", buildTagStats(records));
        context.put("tagPreferenceGroups", buildTagPreferenceGroups(records));
        context.put("collectionCandidates", buildCollectionCandidateStats(category));
        context.put("libraryCandidateCount", buildLibraryCandidateCount(category));
        context.put("evidence", buildEvidence(context));
        return context;
    }

    public Map<String, Object> buildRecommendationContext(String category) {
        List<UserSubjectRecordResponse> allRecords =
                userSubjectRecordService.getRecords(null, null, null, null);
        List<UserSubjectRecordResponse> scopedRecords = filterByCategory(allRecords, category);
        List<Map<String, Object>> recommendations = buildRecommendationItems(category, false);

        Map<String, Object> context = new LinkedHashMap<>();
        context.put("analysisType", "RECOMMENDATION");
        context.put("category", category);
        context.put("comparisonLabel", "历史高分偏好");
        context.put("totalRecords", scopedRecords.size());
        context.put("previousYearTotalRecords", allRecords.size());
        context.put("yearOverYearDiff", recommendations.size());
        context.put("averageScore", averageScore(scopedRecords));
        context.put("highScoreCount", countScoreAtLeast(scopedRecords, 45));
        context.put("lowScoreCount", countScoreBelow(scopedRecords, 30));
        context.put("categoryDistribution", buildCategoryDistribution(scopedRecords));
        context.put("quarterDistribution", buildQuarterDistribution(scopedRecords));
        context.put("topRecords", buildRecordHighlights(scopedRecords, true));
        context.put("lowScoreRecords", buildRecordHighlights(scopedRecords, false));
        context.put("topTags", buildTagStats(scopedRecords));
        context.put("collectionCandidates", buildCollectionCandidateStats(category));
        context.put("libraryCandidateCount", buildLibraryCandidateCount(category));
        context.put("recommendations", recommendations);
        context.put("evidence", buildEvidence(context));
        return context;
    }

    public Map<String, Object> buildCollectionBacklogContext(String category) {
        List<UserSubjectRecordResponse> allRecords =
                userSubjectRecordService.getRecords(null, null, null, null);
        List<UserSubjectRecordResponse> scopedRecords = filterByCategory(allRecords, category);
        List<Map<String, Object>> recommendations = buildRecommendationItems(category, true);

        Map<String, Object> context = new LinkedHashMap<>();
        context.put("analysisType", "COLLECTION_BACKLOG");
        context.put("category", category);
        context.put("comparisonLabel", "收藏夹待补作品");
        context.put("totalRecords", scopedRecords.size());
        context.put("previousYearTotalRecords", allRecords.size());
        context.put("yearOverYearDiff", recommendations.size());
        context.put("averageScore", averageScore(scopedRecords));
        context.put("highScoreCount", countScoreAtLeast(scopedRecords, 45));
        context.put("lowScoreCount", countScoreBelow(scopedRecords, 30));
        context.put("categoryDistribution", buildCategoryDistribution(scopedRecords));
        context.put("quarterDistribution", buildQuarterDistribution(scopedRecords));
        context.put("topRecords", buildRecordHighlights(scopedRecords, true));
        context.put("lowScoreRecords", buildRecordHighlights(scopedRecords, false));
        context.put("topTags", buildTagStats(scopedRecords));
        context.put("collectionCandidates", buildCollectionCandidateStats(category));
        context.put("libraryCandidateCount", buildLibraryCandidateCount(category));
        context.put("recommendations", recommendations);
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

    private Map<String, Long> buildScoreBands(List<UserSubjectRecordResponse> records) {
        Map<String, Long> bands = new LinkedHashMap<>();
        bands.put("47-50", 0L);
        bands.put("40-46", 0L);
        bands.put("30-39", 0L);
        bands.put("0-29", 0L);
        bands.put("未评分", 0L);

        for (UserSubjectRecordResponse record : records) {
            Integer score = record.getScoreValue();
            if (score == null) {
                bands.computeIfPresent("未评分", (key, value) -> value + 1);
            } else if (score >= 47) {
                bands.computeIfPresent("47-50", (key, value) -> value + 1);
            } else if (score >= 40) {
                bands.computeIfPresent("40-46", (key, value) -> value + 1);
            } else if (score >= 30) {
                bands.computeIfPresent("30-39", (key, value) -> value + 1);
            } else {
                bands.computeIfPresent("0-29", (key, value) -> value + 1);
            }
        }

        return bands;
    }

    private List<Map<String, Object>> buildCategoryScoreStats(List<UserSubjectRecordResponse> records) {
        return CATEGORY_ORDER.stream()
                .map(category -> {
                    List<UserSubjectRecordResponse> categoryRecords = filterByCategory(records, category);
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("category", category);
                    item.put("count", categoryRecords.size());
                    item.put("averageScore", averageScore(categoryRecords));
                    item.put("highScoreCount", countScoreAtLeast(categoryRecords, 45));
                    item.put("lowScoreCount", countScoreBelow(categoryRecords, 30));
                    return item;
                })
                .toList();
    }

    private List<Map<String, Object>> buildYearScoreTrend(List<UserSubjectRecordResponse> records) {
        return records.stream()
                .filter(record -> record.getRecordYear() != null)
                .collect(Collectors.groupingBy(
                        UserSubjectRecordResponse::getRecordYear,
                        LinkedHashMap::new,
                        Collectors.toList()
                ))
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("year", entry.getKey());
                    item.put("count", entry.getValue().size());
                    item.put("averageScore", averageScore(entry.getValue()));
                    item.put("highScoreCount", countScoreAtLeast(entry.getValue(), 45));
                    return item;
                })
                .toList();
    }

    private Map<String, List<Map<String, Object>>> buildTagPreferenceGroups(
            List<UserSubjectRecordResponse> records
    ) {
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

        int maxCount = bucketMap.values().stream()
                .mapToInt(TagScoreBucket::count)
                .max()
                .orElse(0);
        int highFrequencyThreshold = Math.max(2, (int) Math.ceil(maxCount * 0.5));

        Map<String, List<Map<String, Object>>> groups = new LinkedHashMap<>();
        groups.put("highFrequencyHighScore", filterTagBuckets(bucketMap, highFrequencyThreshold, true, true));
        groups.put("highFrequencyLowScore", filterTagBuckets(bucketMap, highFrequencyThreshold, true, false));
        groups.put("lowFrequencyHighScore", filterTagBuckets(bucketMap, highFrequencyThreshold, false, true));
        groups.put("lowFrequencyLowScore", filterTagBuckets(bucketMap, highFrequencyThreshold, false, false));
        return groups;
    }

    private List<Map<String, Object>> filterTagBuckets(
            Map<String, TagScoreBucket> bucketMap,
            int highFrequencyThreshold,
            boolean highFrequency,
            boolean highScore
    ) {
        return bucketMap.values().stream()
                .filter(bucket -> highFrequency
                        ? bucket.count() >= highFrequencyThreshold
                        : bucket.count() < highFrequencyThreshold)
                .filter(bucket -> highScore
                        ? bucket.averageScore().compareTo(BigDecimal.valueOf(42)) >= 0
                        : bucket.averageScore().compareTo(BigDecimal.valueOf(35)) < 0)
                .sorted(Comparator
                        .comparing(TagScoreBucket::averageScore, highScore
                                ? Comparator.reverseOrder()
                                : Comparator.naturalOrder())
                        .thenComparing(TagScoreBucket::count, Comparator.reverseOrder())
                        .thenComparing(TagScoreBucket::name))
                .limit(5)
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

    private List<Map<String, Object>> buildRecommendationItems(String category, boolean collectionOnly) {
        List<UserSubjectRecordResponse> allRecords =
                userSubjectRecordService.getRecords(null, null, null, null);
        List<UserSubjectRecordResponse> scopedRecords = filterByCategory(allRecords, category);
        Map<String, Long> highScoreTagWeights = buildHighScoreTagWeights(scopedRecords);
        Set<Long> recordedSubjectIds = allRecords.stream()
                .map(UserSubjectRecordResponse::getSubjectId)
                .collect(Collectors.toSet());
        Map<Long, UserSubjectCollection> collectionMap = userSubjectCollectionRepository.findAll()
                .stream()
                .collect(Collectors.toMap(
                        UserSubjectCollection::getSubjectId,
                        collection -> collection,
                        (first, second) -> first
                ));

        return subjectRepository.findAll()
                .stream()
                .filter(subject -> "NORMAL".equalsIgnoreCase(subject.getStatus()))
                .filter(subject -> category == null || category.isBlank()
                        || category.equalsIgnoreCase(subject.getCategory()))
                .filter(subject -> !recordedSubjectIds.contains(subject.getId()))
                .filter(subject -> !collectionOnly || collectionMap.containsKey(subject.getId()))
                .map(subject -> buildRecommendationItem(subject, collectionMap.get(subject.getId()), highScoreTagWeights))
                .sorted(Comparator
                        .comparing((Map<String, Object> item) -> (Integer) item.get("matchScore")).reversed()
                        .thenComparing(item -> String.valueOf(item.get("title"))))
                .limit(12)
                .toList();
    }

    private Map<String, Long> buildHighScoreTagWeights(List<UserSubjectRecordResponse> records) {
        return records.stream()
                .filter(record -> record.getScoreValue() != null && record.getScoreValue() >= 42)
                .filter(record -> record.getTagList() != null)
                .flatMap(record -> record.getTagList().stream())
                .collect(Collectors.groupingBy(
                        TagResponse::getName,
                        Collectors.counting()
                ));
    }

    private Map<String, Object> buildRecommendationItem(
            Subject subject,
            UserSubjectCollection collection,
            Map<String, Long> highScoreTagWeights
    ) {
        List<String> tags = subject.getStructuredTags()
                .stream()
                .map(Tag::getName)
                .toList();
        List<String> matchedTags = tags.stream()
                .filter(highScoreTagWeights::containsKey)
                .limit(5)
                .toList();
        String source = resolveRecommendationSource(collection);
        int sourceScore = switch (source) {
            case "WANT" -> 80;
            case "PAUSED" -> 58;
            default -> 30;
        };
        int tagScore = matchedTags.stream()
                .mapToInt(tag -> highScoreTagWeights.getOrDefault(tag, 0L).intValue())
                .sum() * 8;
        int platformScore = subject.getPlatformScore() == null
                ? 0
                : subject.getPlatformScore().multiply(BigDecimal.valueOf(3)).intValue();
        int matchScore = sourceScore + tagScore + platformScore;

        Map<String, Object> item = new LinkedHashMap<>();
        item.put("subjectId", subject.getId());
        item.put("title", subject.getDisplayTitle());
        item.put("coverUrl", subject.getCoverUrl());
        item.put("category", subject.getCategory());
        item.put("tags", tags.stream().limit(6).toList());
        item.put("franchiseName", resolveFranchiseName(subject.getFranchiseId()));
        item.put("source", source);
        item.put("reason", buildRecommendationReason(source, matchedTags, subject));
        item.put("risk", buildRecommendationRisk(source, matchedTags, subject));
        item.put("evidence", buildRecommendationEvidence(matchedTags, subject));
        item.put("priority", resolvePriority(matchScore, source));
        item.put("matchScore", matchScore);
        item.put("platformScore", subject.getPlatformScore());
        return item;
    }

    private String resolveRecommendationSource(UserSubjectCollection collection) {
        if (collection == null) {
            return "LIBRARY";
        }

        return collection.getCollectionType() == CollectionType.WANT ? "WANT" : "PAUSED";
    }

    private String buildRecommendationReason(String source, List<String> matchedTags, Subject subject) {
        String sourceText = switch (source) {
            case "WANT" -> "来自想看清单，优先级天然高于普通作品库候选";
            case "PAUSED" -> "来自搁置清单，适合判断是否值得恢复";
            default -> "来自作品库未记录候选";
        };

        if (!matchedTags.isEmpty()) {
            return sourceText + "，并且匹配你历史高分标签：" + String.join("、", matchedTags);
        }

        if (subject.getPlatformScore() != null) {
            return sourceText + "，平台评分 " + subject.getPlatformScore() + " 可作为辅助参考";
        }

        return sourceText + "，当前主要依靠收藏状态和作品基础信息推荐";
    }

    private String buildRecommendationRisk(String source, List<String> matchedTags, Subject subject) {
        if ("PAUSED".equals(source)) {
            return "搁置作品可能和当初的观看节奏或兴趣状态有关，建议降低即时补完压力";
        }

        if (matchedTags.isEmpty()) {
            return "缺少历史高分标签匹配，推荐依据偏弱，适合作为探索候选";
        }

        if (subject.getPlatformScore() == null) {
            return "缺少平台评分，只能更多依赖个人标签偏好";
        }

        return "风险较低，但仍需结合当前心情和系列前置作品判断";
    }

    private String buildRecommendationEvidence(List<String> matchedTags, Subject subject) {
        String tagEvidence = matchedTags.isEmpty()
                ? "暂无高分标签命中"
                : "命中高分标签 " + String.join("、", matchedTags);
        String scoreEvidence = subject.getPlatformScore() == null
                ? "平台评分未知"
                : "平台评分 " + subject.getPlatformScore();

        return tagEvidence + "；" + scoreEvidence;
    }

    private String resolvePriority(int matchScore, String source) {
        if (matchScore >= 95 || "WANT".equals(source)) {
            return "HIGH";
        }

        if (matchScore >= 65 || "PAUSED".equals(source)) {
            return "MEDIUM";
        }

        return "LOW";
    }

    private String resolveFranchiseName(Long franchiseId) {
        if (franchiseId == null) {
            return null;
        }

        return franchiseRepository.findById(franchiseId)
                .map(this::displayFranchiseName)
                .orElse(null);
    }

    private String displayFranchiseName(Franchise franchise) {
        if (franchise.getNameCn() != null && !franchise.getNameCn().isBlank()) {
            return franchise.getNameCn();
        }

        return franchise.getName();
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
        } else if ("SCORE_STRUCTURE".equals(analysisType)) {
            evidence.add(scope + " 评分结构样本 " + context.get("totalRecords") + " 条");
        } else if ("TAG_PREFERENCE".equals(analysisType)) {
            evidence.add(scope + " 标签偏好样本 " + context.get("totalRecords") + " 条");
        } else if ("RECOMMENDATION".equals(analysisType)) {
            evidence.add(scope + " 推荐候选 " + ((List<?>) context.get("recommendations")).size() + " 条");
        } else if ("COLLECTION_BACKLOG".equals(analysisType)) {
            evidence.add(scope + " 收藏夹补坑候选 " + ((List<?>) context.get("recommendations")).size() + " 条");
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

        @SuppressWarnings("unchecked")
        Map<String, Long> scoreBands = (Map<String, Long>) context.get("scoreBands");
        if (scoreBands != null && !scoreBands.isEmpty()) {
            evidence.add("评分分段：" + scoreBands.entrySet().stream()
                    .map(entry -> entry.getKey() + " " + entry.getValue() + " 条")
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
