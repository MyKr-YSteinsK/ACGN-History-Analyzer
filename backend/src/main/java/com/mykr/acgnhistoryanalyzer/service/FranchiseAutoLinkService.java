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
        if (subjectId == null) {
            return;
        }

        Subject recordedSubject = subjectRepository.findById(subjectId).orElse(null);
        if (recordedSubject == null || !hasText(recordedSubject.getTitleCn())) {
            return;
        }

        List<Subject> relatedSubjects = subjectRepository.findAllNormalByNormalizedTitleCn(
                normalizeTextKey(recordedSubject.getTitleCn())
        );
        if (!shouldCreateSeries(recordedSubject, relatedSubjects)) {
            return;
        }

        Franchise franchise = resolveFranchise(recordedSubject, relatedSubjects);
        if (franchise == null) {
            return;
        }

        List<Subject> changedSubjects = relatedSubjects.stream()
                .filter(subject -> subject.getFranchiseId() == null)
                .peek(subject -> subject.setFranchiseId(franchise.getId()))
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

    private Franchise resolveFranchise(Subject recordedSubject, List<Subject> relatedSubjects) {
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

        String franchiseName = chooseFranchiseName(recordedSubject, relatedSubjects);
        Franchise existingNamedFranchise = findExistingFranchiseByName(franchiseName);
        if (existingNamedFranchise != null) {
            return existingNamedFranchise;
        }

        return franchiseRepository.save(new Franchise(
                franchiseName,
                franchiseName,
                "由正式记录自动检测创建，用于关联同名多季、Part 或跨媒介作品。",
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
                .filter(this::hasText)
                .orElse(recordedSubject.getTitleCn())
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
