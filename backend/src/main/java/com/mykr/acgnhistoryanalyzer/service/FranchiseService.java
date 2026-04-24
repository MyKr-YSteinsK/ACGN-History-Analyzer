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
                request.getName().trim(),
                normalizeNullable(request.getNameCn()),
                normalizeNullable(request.getDescription()),
                request.getStatus().trim()
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

        franchise.setName(request.getName().trim());
        franchise.setNameCn(normalizeNullable(request.getNameCn()));
        franchise.setDescription(normalizeNullable(request.getDescription()));
        franchise.setStatus(request.getStatus().trim());

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

    public boolean franchiseNameExistsForCreate(FranchiseCreateRequest request) {
        String businessName = extractBusinessName(request.getNameCn(), request.getName());
        boolean useChineseName = hasText(request.getNameCn());

        if (useChineseName) {
            return franchiseRepository.existsByNameCnIgnoreCase(businessName);
        }
        return franchiseRepository.existsByNameIgnoreCase(businessName);
    }

    public boolean franchiseNameExistsForUpdate(Long id, FranchiseCreateRequest request) {
        String businessName = extractBusinessName(request.getNameCn(), request.getName());
        boolean useChineseName = hasText(request.getNameCn());

        if (useChineseName) {
            return franchiseRepository.existsByNameCnIgnoreCaseAndIdNot(businessName, id);
        }
        return franchiseRepository.existsByNameIgnoreCaseAndIdNot(businessName, id);
    }

    private String extractBusinessName(String nameCn, String name) {
        if (hasText(nameCn)) {
            return nameCn.trim();
        }
        return name == null ? null : name.trim();
    }

    private String normalizeNullable(String value) {
        if (!hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
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