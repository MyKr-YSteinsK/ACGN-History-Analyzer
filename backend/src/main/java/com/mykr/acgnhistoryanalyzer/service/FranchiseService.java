package com.mykr.acgnhistoryanalyzer.service;

import com.mykr.acgnhistoryanalyzer.common.enums.CollectionType;
import com.mykr.acgnhistoryanalyzer.common.enums.LibraryStatus;
import com.mykr.acgnhistoryanalyzer.entity.Franchise;
import com.mykr.acgnhistoryanalyzer.entity.Subject;
import com.mykr.acgnhistoryanalyzer.repository.FranchiseRepository;
import com.mykr.acgnhistoryanalyzer.repository.SubjectRepository;
import com.mykr.acgnhistoryanalyzer.repository.UserSubjectCollectionRepository;
import com.mykr.acgnhistoryanalyzer.repository.UserSubjectRecordRepository;
import com.mykr.acgnhistoryanalyzer.request.FranchiseCreateRequest;
import com.mykr.acgnhistoryanalyzer.response.FranchiseDetailResponse;
import com.mykr.acgnhistoryanalyzer.response.FranchiseResponse;
import com.mykr.acgnhistoryanalyzer.response.SubjectResponse;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FranchiseService {

    private final FranchiseRepository franchiseRepository;
    private final SubjectRepository subjectRepository;
    private final UserSubjectRecordRepository userSubjectRecordRepository;
    private final UserSubjectCollectionRepository userSubjectCollectionRepository;
    private final TagService tagService;

    public FranchiseService(FranchiseRepository franchiseRepository,
                            SubjectRepository subjectRepository,
                            UserSubjectRecordRepository userSubjectRecordRepository,
                            UserSubjectCollectionRepository userSubjectCollectionRepository,
                            TagService tagService) {
        this.franchiseRepository = franchiseRepository;
        this.subjectRepository = subjectRepository;
        this.userSubjectRecordRepository = userSubjectRecordRepository;
        this.userSubjectCollectionRepository = userSubjectCollectionRepository;
        this.tagService = tagService;
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

    @Transactional
    public void deleteFranchise(Long id) {
        List<Subject> subjects = subjectRepository.findByFranchiseId(id);
        subjects.forEach(subject -> subject.setFranchiseId(null));
        subjectRepository.saveAll(subjects);
        franchiseRepository.deleteById(id);
    }

    @Transactional
    public boolean removeSubjectFromFranchise(Long franchiseId, Long subjectId) {
        Subject subject = subjectRepository.findById(subjectId).orElse(null);

        if (subject == null || !franchiseId.equals(subject.getFranchiseId())) {
            return false;
        }

        subject.setFranchiseId(null);
        subjectRepository.save(subject);
        return true;
    }

    public boolean franchiseNameExistsForCreate(FranchiseCreateRequest request) {
        String businessName = extractBusinessName(request.getNameCn(), request.getName());
        boolean useChineseName = hasText(request.getNameCn());

        if (useChineseName) {
            return franchiseRepository.existsByNormalizedNameCn(businessName);
        }
        return franchiseRepository.existsByNormalizedName(businessName);
    }

    public boolean franchiseNameExistsForUpdate(Long id, FranchiseCreateRequest request) {
        String businessName = extractBusinessName(request.getNameCn(), request.getName());
        boolean useChineseName = hasText(request.getNameCn());

        if (useChineseName) {
            return franchiseRepository.existsByNormalizedNameCnAndIdNot(businessName, id);
        }
        return franchiseRepository.existsByNormalizedNameAndIdNot(businessName, id);
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

    private LibraryStatus resolveLibraryStatus(Long subjectId) {
        if (userSubjectRecordRepository.existsBySubjectId(subjectId)) {
            return LibraryStatus.WATCHED;
        }

        return userSubjectCollectionRepository.findBySubjectId(subjectId)
                .map(collection -> collection.getCollectionType() == CollectionType.WANT
                        ? LibraryStatus.WANT
                        : LibraryStatus.PAUSED)
                .orElse(LibraryStatus.NONE);
    }

    private SubjectResponse toSubjectResponse(Subject subject) {
        LibraryStatus libraryStatus = resolveLibraryStatus(subject.getId());
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
                subject.getPlatformScore(),
                subject.getEpisodeCount(),
                tagService.toResponses(subject.getStructuredTags()),
                subject.getSourcePlatform(),
                subject.getSourceId(),
                subject.getStatus(),
                libraryStatus.name(),
                libraryStatus.getLabel(),
                subject.getCreatedAt(),
                subject.getUpdatedAt()
        );
    }
}
