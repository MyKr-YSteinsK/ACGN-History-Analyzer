package com.mykr.acgnhistoryanalyzer.repository;

import com.mykr.acgnhistoryanalyzer.common.enums.CollectionType;
import com.mykr.acgnhistoryanalyzer.entity.UserSubjectCollection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserSubjectCollectionRepository
        extends JpaRepository<UserSubjectCollection, Long>, JpaSpecificationExecutor<UserSubjectCollection> {

    boolean existsBySubjectId(Long subjectId);

    Optional<UserSubjectCollection> findBySubjectId(Long subjectId);

    void deleteBySubjectId(Long subjectId);

    @Query(
            value = """
                select c
                from UserSubjectCollection c
                join Subject s on c.subjectId = s.id
                where (:type is null or c.collectionType = :type)
                  and (:category is null or lower(s.category) = lower(:category))
                  and (:keyword is null or lower(s.displayTitle) like lower(concat('%', :keyword, '%')))
                """,
            countQuery = """
                select count(c)
                from UserSubjectCollection c
                join Subject s on c.subjectId = s.id
                where (:type is null or c.collectionType = :type)
                  and (:category is null or lower(s.category) = lower(:category))
                  and (:keyword is null or lower(s.displayTitle) like lower(concat('%', :keyword, '%')))
                """
    )
    Page<UserSubjectCollection> findCollectionPageWithSubjectFilters(
            @Param("type") CollectionType type,
            @Param("category") String category,
            @Param("keyword") String keyword,
            Pageable pageable
    );
}
