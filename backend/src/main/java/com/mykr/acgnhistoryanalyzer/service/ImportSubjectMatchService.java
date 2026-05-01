package com.mykr.acgnhistoryanalyzer.service;

import com.mykr.acgnhistoryanalyzer.entity.Subject;
import com.mykr.acgnhistoryanalyzer.repository.SubjectRepository;
import com.mykr.acgnhistoryanalyzer.response.SubjectMatchCandidateResponse;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class ImportSubjectMatchService {

    private final SubjectRepository subjectRepository;

    public ImportSubjectMatchService(SubjectRepository subjectRepository) {
        this.subjectRepository = subjectRepository;
    }

    /**
     * 优先级：subjectId 大于 sourceId 大于 subjectTitle 关键词（可配合季/Part 过滤）。
     */
    public ImportSubjectResolveResult resolve(
            Long subjectId,
            String sourceId,
            String subjectTitle,
            Integer subjectSeasonIndex,
            Integer subjectPartIndex
    ) {
        List<Subject> matched = findCandidates(
                subjectId,
                sourceId,
                subjectTitle,
                subjectSeasonIndex,
                subjectPartIndex
        );
        if (matched.isEmpty()) {
            return new ImportSubjectResolveResult(null, "作品不存在，请先导入作品库");
        }
        if (matched.size() > 1) {
            return new ImportSubjectResolveResult(null, null, toCandidateResponses(matched));
        }
        return new ImportSubjectResolveResult(matched.get(0), null);
    }

    public List<Subject> findCandidates(
            Long subjectId,
            String sourceId,
            String subjectTitle,
            Integer subjectSeasonIndex,
            Integer subjectPartIndex
    ) {
        if (subjectId != null) {
            Subject direct = subjectRepository.findById(subjectId).orElse(null);
            return direct == null ? List.of() : List.of(direct);
        }

        if (hasText(sourceId)) {
            return subjectRepository.findAllBySourceIdentity(normalizeTextKey(sourceId));
        }

        if (!hasText(subjectTitle)) {
            return List.of();
        }

        return subjectRepository.searchAllNormalByTitleKeyword(normalizeTextKey(subjectTitle)).stream()
                .filter(subject -> subjectSeasonIndex == null
                        || equalsNumber(subject.getSeasonIndex(), subjectSeasonIndex))
                .filter(subject -> subjectPartIndex == null
                        || equalsNumber(subject.getPartIndex(), subjectPartIndex))
                .toList();
    }

    public List<SubjectMatchCandidateResponse> toCandidateResponses(List<Subject> matchedSubjects) {
        return matchedSubjects.stream()
                .sorted(Comparator.comparing(Subject::getId))
                .map(subject -> new SubjectMatchCandidateResponse(
                        subject.getId(),
                        subject.getDisplayTitle(),
                        subject.getSeasonIndex(),
                        subject.getPartIndex(),
                        subject.getReleaseYear(),
                        subject.getReleaseQuarter(),
                        subject.getCategory()
                ))
                .toList();
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private static String normalizeTextKey(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "";
        }
        return value.trim().toLowerCase();
    }

    private static boolean equalsNumber(Number left, Number right) {
        if (left == null && right == null) {
            return true;
        }
        if (left == null || right == null) {
            return false;
        }
        return left.longValue() == right.longValue();
    }
}
