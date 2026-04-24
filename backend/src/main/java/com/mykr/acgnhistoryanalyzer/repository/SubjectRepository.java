package com.mykr.acgnhistoryanalyzer.repository;

import com.mykr.acgnhistoryanalyzer.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface SubjectRepository
        extends JpaRepository<Subject, Long>, JpaSpecificationExecutor<Subject> {

    List<Subject> findByFranchiseId(Long franchiseId);

    boolean existsByDisplayTitleAndReleaseYearAndReleaseQuarterAndCategory(
            String displayTitle,
            Integer releaseYear,
            Integer releaseQuarter,
            String category
    );

    Optional<Subject> findByDisplayTitleAndReleaseYearAndReleaseQuarterAndCategory(
            String displayTitle,
            Integer releaseYear,
            Integer releaseQuarter,
            String category
    );
}