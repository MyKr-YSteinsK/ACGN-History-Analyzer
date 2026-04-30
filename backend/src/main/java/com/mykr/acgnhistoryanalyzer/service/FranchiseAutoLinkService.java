package com.mykr.acgnhistoryanalyzer.service;

import com.mykr.acgnhistoryanalyzer.entity.Franchise;
import com.mykr.acgnhistoryanalyzer.entity.Subject;
import com.mykr.acgnhistoryanalyzer.repository.FranchiseRepository;
import com.mykr.acgnhistoryanalyzer.repository.SubjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
public class FranchiseAutoLinkService {

    private final SubjectRepository subjectRepository;
    private final FranchiseRepository franchiseRepository;

    public FranchiseAutoLinkService(SubjectRepository subjectRepository,
                                    FranchiseRepository franchiseRepository) {
        this.subjectRepository = subjectRepository;
        this.franchiseRepository = franchiseRepository;
    }

    @Transactional
    public void detectAndLinkForRecordedSubject(Long subjectId) {
        detectAndLinkForSubject(subjectId);
    }

    @Transactional
    public void detectAndLinkForSubject(Long subjectId) {
        if (subjectId == null) {
            return;
        }

        Subject subject = subjectRepository.findById(subjectId).orElse(null);
        if (subject == null || !hasText(subject.getTitleCn())) {
            return;
        }

        String seriesKey = normalizeSeriesKey(subject);
        if (!hasText(seriesKey)) {
            return;
        }

        List<Subject> relatedSubjects = subjectRepository.findAll().stream()
                .filter(item -> "NORMAL".equals(item.getStatus()))
                .filter(item -> seriesKey.equals(normalizeSeriesKey(item)))
                .toList();

        Franchise existingFranchise = findExistingFranchiseBySeriesKey(seriesKey);
        if (existingFranchise == null && !shouldCreateSeries(subject, relatedSubjects)) {
            return;
        }

        Franchise franchise = resolveFranchise(subject, relatedSubjects, seriesKey, existingFranchise);
        if (franchise == null) {
            return;
        }

        List<Subject> changedSubjects = relatedSubjects.stream()
                .filter(item -> item.getFranchiseId() == null)
                .peek(item -> item.setFranchiseId(franchise.getId()))
                .toList();

        if (!changedSubjects.isEmpty()) {
            subjectRepository.saveAll(changedSubjects);
        }
    }

    private boolean shouldCreateSeries(Subject recordedSubject, List<Subject> relatedSubjects) {
        if (relatedSubjects.size() >= 2) {
            return true;
        }

        return isMultiSeasonOrPart(recordedSubject);
    }

    private boolean isMultiSeasonOrPart(Subject subject) {
        return (subject.getSeasonIndex() != null && subject.getSeasonIndex() > 1)
                || (subject.getPartIndex() != null && subject.getPartIndex() > 1);
    }

    private Franchise resolveFranchise(Subject recordedSubject, List<Subject> relatedSubjects,
                                       String seriesKey, Franchise existingFranchise) {
        Franchise existingLinkedFranchise = relatedSubjects.stream()
                .map(Subject::getFranchiseId)
                .filter(id -> id != null)
                .distinct()
                .sorted()
                .map(id -> franchiseRepository.findById(id).orElse(null))
                .filter(franchise -> franchise != null)
                .findFirst()
                .orElse(null);

        if (existingLinkedFranchise != null) {
            return existingLinkedFranchise;
        }

        if (existingFranchise != null) {
            return existingFranchise;
        }

        String franchiseName = chooseFranchiseName(recordedSubject, relatedSubjects);
        Franchise existingNamedFranchise = findExistingFranchiseByName(franchiseName);
        if (existingNamedFranchise != null) {
            return existingNamedFranchise;
        }

        return franchiseRepository.save(new Franchise(
                franchiseName,
                franchiseName,
                "由 Bangumi 导入或正式记录自动检测创建，用于关联同名多季、Part、衍生作或跨媒介作品。",
                "NORMAL"
        ));
    }

    private String chooseFranchiseName(Subject recordedSubject, List<Subject> relatedSubjects) {
        return relatedSubjects.stream()
                .min(Comparator
                        .comparing((Subject subject) -> subject.getSeasonIndex() == null ? Integer.MAX_VALUE : subject.getSeasonIndex())
                        .thenComparing(subject -> subject.getPartIndex() == null ? Integer.MAX_VALUE : subject.getPartIndex())
                        .thenComparing(Subject::getId))
                .map(Subject::getTitleCn)
                .map(this::stripSeriesMarkers)
                .filter(this::hasText)
                .orElse(recordedSubject.getTitleCn())
                .trim();
    }

    private Franchise findExistingFranchiseBySeriesKey(String seriesKey) {
        return franchiseRepository.findAll().stream()
                .filter(franchise -> seriesKey.equals(normalizeSeriesKey(franchise.getNameCn()))
                        || seriesKey.equals(normalizeSeriesKey(franchise.getName())))
                .findFirst()
                .orElse(null);
    }

    private String normalizeSeriesKey(Subject subject) {
        return normalizeSeriesKey(subject == null ? null : subject.getTitleCn());
    }

    private String normalizeSeriesKey(String value) {
        if (!hasText(value)) {
            return "";
        }

        String stripped = stripSeriesMarkers(value);
        return stripped
                .toLowerCase()
                .replaceAll("[\\s　·・:：,，.。!！?？'\"“”‘’\\-—_~～（）()\\[\\]【】《》<>]+", "");
    }

    private String stripSeriesMarkers(String value) {
        if (!hasText(value)) {
            return "";
        }

        return value.trim()
                .replaceAll("(?i)\\b(?:season|s)\\s*\\d+\\b", "")
                .replaceAll("(?i)\\b\\d+(?:st|nd|rd|th)\\s+season\\b", "")
                .replaceAll("(?i)\\b(?:part|p)\\s*\\d+\\b", "")
                .replaceAll("第\\s*[0-9一二三四五六七八九十两]+\\s*(季|期|部|部分|クール|章)", "")
                .replaceAll("(第二季|第三季|第四季|第五季|第六季|二期|三期|四期)", "")
                .replaceAll("(前篇|后篇|後篇|篇)$", "")
                .trim();
    }

    private Franchise findExistingFranchiseByName(String franchiseName) {
        if (!hasText(franchiseName)) {
            return null;
        }

        List<Franchise> matchedByCn = franchiseRepository.findAllByNormalizedNameCn(franchiseName.trim());
        if (!matchedByCn.isEmpty()) {
            return matchedByCn.get(0);
        }

        List<Franchise> matchedByName = franchiseRepository.findAllByNormalizedName(franchiseName.trim());
        if (!matchedByName.isEmpty()) {
            return matchedByName.get(0);
        }

        return null;
    }

    private String normalizeTextKey(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
