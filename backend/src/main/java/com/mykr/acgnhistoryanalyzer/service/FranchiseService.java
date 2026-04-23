package com.mykr.acgnhistoryanalyzer.service;

import com.mykr.acgnhistoryanalyzer.entity.Franchise;
import com.mykr.acgnhistoryanalyzer.entity.Subject;
import com.mykr.acgnhistoryanalyzer.repository.FranchiseRepository;
import com.mykr.acgnhistoryanalyzer.repository.SubjectRepository;
import com.mykr.acgnhistoryanalyzer.request.FranchiseCreateRequest;
import com.mykr.acgnhistoryanalyzer.response.FranchiseDetailResponse;
import com.mykr.acgnhistoryanalyzer.response.FranchiseResponse;
import com.mykr.acgnhistoryanalyzer.response.SubjectResponse;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FranchiseService {

    private final FranchiseRepository franchiseRepository;
    private final SubjectRepository subjectRepository;

    public FranchiseService(FranchiseRepository franchiseRepository,
                            SubjectRepository subjectRepository) {
        this.franchiseRepository = franchiseRepository;
        this.subjectRepository = subjectRepository;
    }

    public FranchiseResponse createFranchise(FranchiseCreateRequest request) {
        Franchise franchise = new Franchise(
                request.getName(),
                request.getNameCn(),
                request.getDescription(),
                request.getStatus()
        );

        Franchise savedFranchise = franchiseRepository.save(franchise);
        return toResponse(savedFranchise);
    }

    public List<FranchiseResponse> getFranchises() {
        return franchiseRepository.findAll(Sort.by("id").ascending())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public FranchiseDetailResponse getFranchiseDetailById(Long id) {
        Franchise franchise = franchiseRepository.findById(id).orElse(null);

        if (franchise == null) {
            return null;
        }

        List<SubjectResponse> subjects = subjectRepository.findByFranchiseId(id)
                .stream()
                .map(this::toSubjectResponse)
                .toList();

        return new FranchiseDetailResponse(
                franchise.getId(),
                franchise.getName(),
                franchise.getNameCn(),
                franchise.getDescription(),
                franchise.getStatus(),
                franchise.getCreatedAt(),
                franchise.getUpdatedAt(),
                subjects
        );
    }

    public FranchiseResponse updateFranchise(Long id, FranchiseCreateRequest request) {
        Franchise franchise = franchiseRepository.findById(id).orElse(null);

        if (franchise == null) {
            return null;
        }

        franchise.setName(request.getName());
        franchise.setNameCn(request.getNameCn());
        franchise.setDescription(request.getDescription());
        franchise.setStatus(request.getStatus());

        Franchise updatedFranchise = franchiseRepository.save(franchise);
        return toResponse(updatedFranchise);
    }

    public boolean franchiseExists(Long id) {
        return franchiseRepository.existsById(id);
    }

    public boolean isFranchiseReferenced(Long id) {
        return !subjectRepository.findByFranchiseId(id).isEmpty();
    }

    public void deleteFranchise(Long id) {
        franchiseRepository.deleteById(id);
    }

    private FranchiseResponse toResponse(Franchise franchise) {
        return new FranchiseResponse(
                franchise.getId(),
                franchise.getName(),
                franchise.getNameCn(),
                franchise.getDescription(),
                franchise.getStatus(),
                franchise.getCreatedAt(),
                franchise.getUpdatedAt()
        );
    }

    private SubjectResponse toSubjectResponse(Subject subject) {
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
                subject.getReleaseQuarter(),
                subject.getCategory(),
                subject.getStudioName(),
                subject.getPlatformLink(),
                subject.getStatus()
        );
    }
}