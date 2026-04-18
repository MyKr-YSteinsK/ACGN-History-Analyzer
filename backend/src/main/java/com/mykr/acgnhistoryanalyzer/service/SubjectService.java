package com.mykr.acgnhistoryanalyzer.service;

import com.mykr.acgnhistoryanalyzer.entity.Subject;
import com.mykr.acgnhistoryanalyzer.repository.SubjectRepository;
import com.mykr.acgnhistoryanalyzer.request.SubjectCreateRequest;
import com.mykr.acgnhistoryanalyzer.response.SubjectResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SubjectService {

    private final SubjectRepository subjectRepository;

    public SubjectService(SubjectRepository subjectRepository) {
        this.subjectRepository = subjectRepository;
    }

    public SubjectResponse createSubject(SubjectCreateRequest request) {
        Subject subject = new Subject(
                request.getTitleCn(),
                request.getSubtitle(),
                request.getDisplayTitle(),
                request.getSeasonIndex(),
                request.getPartIndex(),
                request.getFranchiseId(),
                request.getCoverUrl(),
                request.getSummary(),
                request.getReleaseYear(),
                request.getReleaseMonth(),
                request.getCategory(),
                request.getStudioName(),
                request.getPlatformLink(),
                request.getStatus()
        );

        Subject savedSubject = subjectRepository.save(subject);
        return toResponse(savedSubject);
    }

    public List<SubjectResponse> getSubjects(String category) {
        List<Subject> subjects;

        if (category == null || category.isBlank()) {
            subjects = subjectRepository.findAll();
        } else {
            subjects = subjectRepository.findByCategory(category);
        }

        return subjects.stream()
                .map(this::toResponse)
                .toList();
    }

    private SubjectResponse toResponse(Subject subject) {
        return new SubjectResponse(
                subject.getId(),
                subject.getTitleCn(),
                subject.getSubtitle(),
                subject.getDisplayTitle(),
                subject.getSeasonIndex(),
                subject.getPartIndex(),
                subject.getFranchiseId(),
                subject.getCoverUrl(),
                subject.getSummary(),
                subject.getReleaseYear(),
                subject.getReleaseMonth(),
                subject.getCategory(),
                subject.getStudioName(),
                subject.getPlatformLink(),
                subject.getStatus()
        );
    }
}