package com.mykr.acgnhistoryanalyzer.repository;

import com.mykr.acgnhistoryanalyzer.entity.UserSubjectRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserSubjectRecordRepository
        extends JpaRepository<UserSubjectRecord, Long>, JpaSpecificationExecutor<UserSubjectRecord> {

    boolean existsBySubjectId(Long subjectId);

    boolean existsBySubjectIdAndIdNot(Long subjectId, Long id);

    List<UserSubjectRecord> findBySubjectId(Long subjectId);

    List<UserSubjectRecord> findAllBySubjectIdIn(List<Long> subjectIds);

    @Query(
            value = """
                select r
                from UserSubjectRecord r
                join Subject s on r.subjectId = s.id
                where (:year is null or r.recordYear = :year)
                  and (:quarter is null or r.recordQuarter = :quarter)
                  and (:minScore is null or r.scoreValue >= :minScore)
                  and (:maxScore is null or r.scoreValue <= :maxScore)
                  and (:category is null or lower(s.category) = lower(:category))
                  and (:keyword is null or lower(s.displayTitle) like lower(concat('%', :keyword, '%')))
                """,
            countQuery = """
                select count(r)
                from UserSubjectRecord r
                join Subject s on r.subjectId = s.id
                where (:year is null or r.recordYear = :year)
                  and (:quarter is null or r.recordQuarter = :quarter)
                  and (:minScore is null or r.scoreValue >= :minScore)
                  and (:maxScore is null or r.scoreValue <= :maxScore)
                  and (:category is null or lower(s.category) = lower(:category))
                  and (:keyword is null or lower(s.displayTitle) like lower(concat('%', :keyword, '%')))
                """
    )
    Page<UserSubjectRecord> findRecordPageWithSubjectFilters(
            @Param("year") Integer year,
            @Param("quarter") Integer quarter,
            @Param("minScore") Integer minScore,
            @Param("maxScore") Integer maxScore,
            @Param("category") String category,
            @Param("keyword") String keyword,
            Pageable pageable
    );
}
