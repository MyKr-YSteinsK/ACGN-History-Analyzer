package com.mykr.acgnhistoryanalyzer.service;

import com.mykr.acgnhistoryanalyzer.common.enums.HomeLibrarySortType;
import com.mykr.acgnhistoryanalyzer.common.enums.SubjectPageSortType;
import com.mykr.acgnhistoryanalyzer.entity.Franchise;
import com.mykr.acgnhistoryanalyzer.entity.Subject;
import com.mykr.acgnhistoryanalyzer.repository.FranchiseRepository;
import com.mykr.acgnhistoryanalyzer.repository.SubjectRepository;
import com.mykr.acgnhistoryanalyzer.repository.UserSubjectRecordRepository;
import com.mykr.acgnhistoryanalyzer.request.SubjectCreateRequest;
import com.mykr.acgnhistoryanalyzer.request.SubjectBatchImportRequest;
import com.mykr.acgnhistoryanalyzer.response.*;
import com.mykr.acgnhistoryanalyzer.specification.SubjectSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.ArrayList;

@Service
public class SubjectService {

    private final SubjectRepository subjectRepository;
    private final UserSubjectRecordRepository userSubjectRecordRepository;
    private final UserSubjectRecordService userSubjectRecordService;
    private final FranchiseRepository franchiseRepository;

    public SubjectService(SubjectRepository subjectRepository,
                          UserSubjectRecordRepository userSubjectRecordRepository,
                          UserSubjectRecordService userSubjectRecordService, FranchiseRepository franchiseRepository) {
        this.subjectRepository = subjectRepository;
        this.userSubjectRecordRepository = userSubjectRecordRepository;
        this.userSubjectRecordService = userSubjectRecordService;
        this.franchiseRepository = franchiseRepository;
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
                request.getReleaseQuarter(),
                request.getCategory(),
                request.getStudioName(),
                request.getPlatformLink(),
                request.getStatus()
        );

        Subject savedSubject = subjectRepository.save(subject);
        return toResponse(savedSubject);
    }

    public List<SubjectResponse> getSubjects(Integer year, Integer quarter,
                                             String category, String keyword, String status) {
        return getSubjects(year, quarter, category, keyword, status, null);
    }

    public List<SubjectResponse> getSubjects(Integer year, Integer quarter,
                                             String category, String keyword,
                                             String status, String sortType) {
        Specification<Subject> specification =
                buildSubjectSpecification(year, quarter, category, keyword, status);

        HomeLibrarySortType effectiveSortType = normalizeLibrarySort(sortType);
        Sort sort = buildHomeSubjectSort(effectiveSortType);

        List<Subject> subjects = subjectRepository.findAll(specification, sort);

        return subjects.stream()
                .map(this::toResponse)
                .toList();
    }

    public PageResponse<SubjectResponse> getSubjectPage(Integer year, Integer quarter,
                                                        String category, String keyword,
                                                        String status, Integer page,
                                                        Integer size, String sortType) {
        int effectivePage = normalizePage(page);
        int effectiveSize = normalizeSize(size);
        SubjectPageSortType effectiveSortType = normalizeSubjectPageSort(sortType);

        Specification<Subject> specification =
                buildSubjectSpecification(year, quarter, category, keyword, status);

        Pageable pageable = PageRequest.of(
                effectivePage,
                effectiveSize,
                buildSubjectPageSort(effectiveSortType)
        );

        Page<Subject> subjectPage = subjectRepository.findAll(specification, pageable);

        List<SubjectResponse> content = subjectPage.getContent().stream()
                .map(this::toResponse)
                .toList();

        return new PageResponse<>(
                content,
                subjectPage.getNumber(),
                subjectPage.getSize(),
                subjectPage.getTotalElements(),
                subjectPage.getTotalPages(),
                subjectPage.isFirst(),
                subjectPage.isLast()
        );
    }

    public SubjectDetailResponse getSubjectDetailById(Long id) {
        Subject subject = subjectRepository.findById(id).orElse(null);

        if (subject == null) {
            return null;
        }

        List<UserSubjectRecordResponse> records = userSubjectRecordService.getRecordsBySubjectId(id);

        FranchiseSimpleResponse franchise = null;
        if (subject.getFranchiseId() != null) {
            Franchise franchiseEntity = franchiseRepository.findById(subject.getFranchiseId()).orElse(null);
            if (franchiseEntity != null) {
                franchise = new FranchiseSimpleResponse(
                        franchiseEntity.getId(),
                        franchiseEntity.getName(),
                        franchiseEntity.getNameCn()
                );
            }
        }

        return new SubjectDetailResponse(
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
                subject.getStatus(),
                !records.isEmpty(),
                records.size(),
                records,
                franchise
        );
    }

    public SubjectResponse getSubjectById(Long id) {
        Subject subject = subjectRepository.findById(id).orElse(null);

        if (subject == null) {
            return null;
        }

        return toResponse(subject);
    }

    public SubjectResponse updateSubject(Long id, SubjectCreateRequest request) {
        Subject existingSubject = subjectRepository.findById(id).orElse(null);

        if (existingSubject == null) {
            return null;
        }

        existingSubject.setTitleCn(request.getTitleCn());
        existingSubject.setSubtitle(request.getSubtitle());
        existingSubject.setDisplayTitle(request.getDisplayTitle());
        existingSubject.setSeasonIndex(request.getSeasonIndex());
        existingSubject.setPartIndex(request.getPartIndex());
        existingSubject.setFranchiseId(request.getFranchiseId());
        existingSubject.setCoverUrl(request.getCoverUrl());
        existingSubject.setSummary(request.getSummary());
        existingSubject.setReleaseYear(request.getReleaseYear());
        existingSubject.setReleaseQuarter(request.getReleaseQuarter());
        existingSubject.setCategory(request.getCategory());
        existingSubject.setStudioName(request.getStudioName());
        existingSubject.setPlatformLink(request.getPlatformLink());
        existingSubject.setStatus(request.getStatus());

        Subject updatedSubject = subjectRepository.save(existingSubject);
        return toResponse(updatedSubject);
    }

    public boolean subjectExists(Long id) {
        return subjectRepository.existsById(id);
    }

    public boolean isSubjectReferenced(Long id) {
        return userSubjectRecordRepository.existsBySubjectId(id);
    }

    public void deleteSubject(Long id) {
        subjectRepository.deleteById(id);
    }

    public SubjectImportResponse importSubjects(SubjectBatchImportRequest request) {
        List<SubjectImportItemResponse> items = new ArrayList<>();

        int importedCount = 0;
        int skippedCount = 0;

        for (SubjectCreateRequest item : request.getSubjects()) {
            if (item.getFranchiseId() != null && !franchiseRepository.existsById(item.getFranchiseId())) {
                skippedCount++;
                items.add(new SubjectImportItemResponse(
                        null,
                        item.getDisplayTitle(),
                        "SKIPPED",
                        "系列不存在，已跳过"
                ));
                continue;
            }

            boolean exists = subjectRepository.existsByDisplayTitleAndReleaseYearAndReleaseQuarterAndCategory(
                    item.getDisplayTitle(),
                    item.getReleaseYear(),
                    item.getReleaseQuarter(),
                    item.getCategory()
            );

            if (exists) {
                skippedCount++;
                items.add(new SubjectImportItemResponse(
                        null,
                        item.getDisplayTitle(),
                        "SKIPPED",
                        "作品已存在，已跳过"
                ));
                continue;
            }

            Subject subject = new Subject(
                    item.getTitleCn(),
                    item.getSubtitle(),
                    item.getDisplayTitle(),
                    item.getSeasonIndex(),
                    item.getPartIndex(),
                    item.getFranchiseId(),
                    item.getCoverUrl(),
                    item.getSummary(),
                    item.getReleaseYear(),
                    item.getReleaseQuarter(),
                    item.getCategory(),
                    item.getStudioName(),
                    item.getPlatformLink(),
                    item.getStatus()
            );

            Subject savedSubject = subjectRepository.save(subject);
            importedCount++;

            items.add(new SubjectImportItemResponse(
                    savedSubject.getId(),
                    savedSubject.getDisplayTitle(),
                    "IMPORTED",
                    "导入成功"
            ));
        }

        return new SubjectImportResponse(
                request.getSubjects().size(),
                importedCount,
                skippedCount,
                items
        );
    }

    public SubjectImportPreviewResponse previewImportSubjects(SubjectBatchImportRequest request) {
        List<SubjectImportPreviewItemResponse> items = new ArrayList<>();

        int canImportCount = 0;
        int skippedCount = 0;

        for (SubjectCreateRequest item : request.getSubjects()) {
            if (item.getFranchiseId() != null && !franchiseRepository.existsById(item.getFranchiseId())) {
                skippedCount++;
                items.add(new SubjectImportPreviewItemResponse(
                        item.getDisplayTitle(),
                        "SKIPPED",
                        "系列不存在，正式导入时会跳过"
                ));
                continue;
            }

            boolean exists = subjectRepository.existsByDisplayTitleAndReleaseYearAndReleaseQuarterAndCategory(
                    item.getDisplayTitle(),
                    item.getReleaseYear(),
                    item.getReleaseQuarter(),
                    item.getCategory()
            );

            if (exists) {
                skippedCount++;
                items.add(new SubjectImportPreviewItemResponse(
                        item.getDisplayTitle(),
                        "SKIPPED",
                        "作品已存在，正式导入时会跳过"
                ));
                continue;
            }

            canImportCount++;
            items.add(new SubjectImportPreviewItemResponse(
                    item.getDisplayTitle(),
                    "CAN_IMPORT",
                    "可以导入"
            ));
        }

        return new SubjectImportPreviewResponse(
                request.getSubjects().size(),
                canImportCount,
                skippedCount,
                items
        );
    }

    private int normalizePage(Integer page) {
        if (page == null || page < 0) {
            return 0;
        }
        return page;
    }

    private int normalizeSize(Integer size) {
        if (size == null) {
            return 12;
        }
        if (size < 1) {
            return 1;
        }
        return Math.min(size, 50);
    }

    private HomeLibrarySortType normalizeLibrarySort(String sortType) {
        if (sortType == null || sortType.isBlank()) {
            return HomeLibrarySortType.TITLE_ASC;
        }
        return HomeLibrarySortType.valueOf(sortType.toUpperCase(Locale.ROOT));
    }

    private SubjectPageSortType normalizeSubjectPageSort(String sortType) {
        if (sortType == null || sortType.isBlank()) {
            return SubjectPageSortType.RELEASE_NEWEST;
        }
        return SubjectPageSortType.valueOf(sortType.toUpperCase(Locale.ROOT));
    }

    private Sort buildHomeSubjectSort(HomeLibrarySortType sortType) {
        return switch (sortType) {
            case TITLE_ASC -> Sort.by("displayTitle").ascending();
            case TITLE_DESC -> Sort.by("displayTitle").descending();
            case RELEASE_NEWEST -> Sort.by("releaseYear").descending()
                    .and(Sort.by("releaseQuarter").descending())
                    .and(Sort.by("displayTitle").ascending());
            case RELEASE_OLDEST -> Sort.by("releaseYear").ascending()
                    .and(Sort.by("releaseQuarter").ascending())
                    .and(Sort.by("displayTitle").ascending());
        };
    }

    private Sort buildSubjectPageSort(SubjectPageSortType sortType) {
        return switch (sortType) {
            case TITLE_ASC -> Sort.by("displayTitle").ascending()
                    .and(Sort.by("id").ascending());
            case TITLE_DESC -> Sort.by("displayTitle").descending()
                    .and(Sort.by("id").descending());
            case RELEASE_NEWEST -> Sort.by("releaseYear").descending()
                    .and(Sort.by("releaseQuarter").descending())
                    .and(Sort.by("displayTitle").ascending());
            case RELEASE_OLDEST -> Sort.by("releaseYear").ascending()
                    .and(Sort.by("releaseQuarter").ascending())
                    .and(Sort.by("displayTitle").ascending());
        };
    }

    private Specification<Subject> buildSubjectSpecification(Integer year, Integer quarter,
                                                             String category, String keyword, String status) {
        return Specification.allOf(
                SubjectSpecifications.hasReleaseYear(year),
                SubjectSpecifications.hasReleaseQuarter(quarter),
                SubjectSpecifications.hasCategory(category),
                SubjectSpecifications.titleContainsKeyword(keyword),
                SubjectSpecifications.hasStatus(status)
        );
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
                subject.getReleaseQuarter(),
                subject.getCategory(),
                subject.getStudioName(),
                subject.getPlatformLink(),
                subject.getStatus()
        );
    }
}