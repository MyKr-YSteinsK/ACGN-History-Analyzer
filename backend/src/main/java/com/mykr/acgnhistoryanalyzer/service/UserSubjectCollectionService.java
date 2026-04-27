package com.mykr.acgnhistoryanalyzer.service;

import com.mykr.acgnhistoryanalyzer.common.enums.CollectionType;
import com.mykr.acgnhistoryanalyzer.entity.Subject;
import com.mykr.acgnhistoryanalyzer.entity.UserSubjectCollection;
import com.mykr.acgnhistoryanalyzer.repository.SubjectRepository;
import com.mykr.acgnhistoryanalyzer.repository.UserSubjectCollectionRepository;
import com.mykr.acgnhistoryanalyzer.repository.UserSubjectRecordRepository;
import com.mykr.acgnhistoryanalyzer.request.UserSubjectCollectionCreateRequest;
import com.mykr.acgnhistoryanalyzer.request.UserSubjectCollectionUpdateRequest;
import com.mykr.acgnhistoryanalyzer.response.PageResponse;
import com.mykr.acgnhistoryanalyzer.response.UserSubjectCollectionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserSubjectCollectionService {

    private final UserSubjectCollectionRepository collectionRepository;
    private final UserSubjectRecordRepository recordRepository;
    private final SubjectRepository subjectRepository;

    public UserSubjectCollectionService(UserSubjectCollectionRepository collectionRepository,
                                        UserSubjectRecordRepository recordRepository,
                                        SubjectRepository subjectRepository) {
        this.collectionRepository = collectionRepository;
        this.recordRepository = recordRepository;
        this.subjectRepository = subjectRepository;
    }

    public boolean subjectExists(Long subjectId) {
        return subjectRepository.existsById(subjectId);
    }

    public boolean recordExistsBySubjectId(Long subjectId) {
        return recordRepository.existsBySubjectId(subjectId);
    }

    @Transactional
    public UserSubjectCollectionResponse createOrMoveCollection(UserSubjectCollectionCreateRequest request) {
        Subject subject = subjectRepository.findById(request.getSubjectId()).orElse(null);
        if (subject == null) {
            return null;
        }

        UserSubjectCollection collection = collectionRepository.findBySubjectId(subject.getId())
                .orElseGet(() -> new UserSubjectCollection(
                        subject.getId(),
                        subject.getDisplayTitle(),
                        request.getCollectionType(),
                        request.getNote()
                ));

        collection.setSubjectId(subject.getId());
        collection.setSubjectTitle(subject.getDisplayTitle());
        collection.setCollectionType(request.getCollectionType());
        collection.setNote(request.getNote());

        UserSubjectCollection saved = collectionRepository.save(collection);
        return toResponse(saved);
    }

    public PageResponse<UserSubjectCollectionResponse> getCollectionPage(CollectionType type,
                                                                        String category,
                                                                        String keyword,
                                                                        Integer page,
                                                                        Integer size) {
        int effectivePage = normalizePage(page);
        int effectiveSize = normalizeSize(size);
        String normalizedCategory = normalizeBlankToNull(category);
        String normalizedKeyword = normalizeBlankToNull(keyword);

        Pageable pageable = PageRequest.of(
                effectivePage,
                effectiveSize,
                Sort.by("createdAt").descending().and(Sort.by("id").descending())
        );

        Page<UserSubjectCollection> collectionPage = collectionRepository.findCollectionPageWithSubjectFilters(
                type,
                normalizedCategory,
                normalizedKeyword,
                pageable
        );

        return new PageResponse<>(
                collectionPage.getContent().stream().map(this::toResponse).toList(),
                collectionPage.getNumber(),
                collectionPage.getSize(),
                collectionPage.getTotalElements(),
                collectionPage.getTotalPages(),
                collectionPage.isFirst(),
                collectionPage.isLast()
        );
    }

    public UserSubjectCollectionResponse getCollectionById(Long id) {
        return collectionRepository.findById(id).map(this::toResponse).orElse(null);
    }

    public UserSubjectCollectionResponse getCollectionBySubjectId(Long subjectId) {
        return collectionRepository.findBySubjectId(subjectId).map(this::toResponse).orElse(null);
    }

    @Transactional
    public UserSubjectCollectionResponse updateCollection(Long id, UserSubjectCollectionUpdateRequest request) {
        UserSubjectCollection collection = collectionRepository.findById(id).orElse(null);
        if (collection == null) {
            return null;
        }

        collection.setCollectionType(request.getCollectionType());
        collection.setNote(request.getNote());

        UserSubjectCollection saved = collectionRepository.save(collection);
        return toResponse(saved);
    }

    @Transactional
    public boolean deleteCollection(Long id) {
        if (!collectionRepository.existsById(id)) {
            return false;
        }
        collectionRepository.deleteById(id);
        return true;
    }

    @Transactional
    public void removeCollectionBySubjectId(Long subjectId) {
        if (subjectId != null && collectionRepository.existsBySubjectId(subjectId)) {
            collectionRepository.deleteBySubjectId(subjectId);
        }
    }

    private UserSubjectCollectionResponse toResponse(UserSubjectCollection collection) {
        String subjectTitle = collection.getSubjectTitle();
        String subjectCategory = null;
        Integer subjectReleaseYear = null;
        Integer subjectReleaseQuarter = null;
        String subjectCoverUrl = null;

        if (collection.getSubjectId() != null) {
            Subject subject = subjectRepository.findById(collection.getSubjectId()).orElse(null);
            if (subject != null) {
                subjectTitle = subject.getDisplayTitle();
                subjectCategory = subject.getCategory();
                subjectReleaseYear = subject.getReleaseYear();
                subjectReleaseQuarter = subject.getReleaseQuarter();
                subjectCoverUrl = subject.getCoverUrl();
            }
        }

        return new UserSubjectCollectionResponse(
                collection.getId(),
                collection.getSubjectId(),
                subjectTitle,
                subjectCategory,
                subjectReleaseYear,
                subjectReleaseQuarter,
                subjectCoverUrl,
                collection.getCollectionType().name(),
                collection.getCollectionType().getLabel(),
                collection.getNote(),
                collection.getCreatedAt(),
                collection.getUpdatedAt()
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

    private String normalizeBlankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
