package com.mykr.acgnhistoryanalyzer.repository;

import com.mykr.acgnhistoryanalyzer.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {

    Optional<Tag> findByNormalizedName(String normalizedName);

    List<Tag> findByStatusOrderByTagTypeAscNameAsc(String status);

    List<Tag> findByNameContainingAndStatusOrderByTagTypeAscNameAsc(String keyword, String status);
}
