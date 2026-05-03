package com.mykr.acgnhistoryanalyzer.repository;

import com.mykr.acgnhistoryanalyzer.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Collection;
import java.util.List;

public interface SubjectRepository
        extends JpaRepository<Subject, Long>, JpaSpecificationExecutor<Subject> {

    List<Subject> findByFranchiseId(Long franchiseId);

    List<Subject> findByFranchiseIdIn(Collection<Long> franchiseIds);

    @Query("""
        select s
        from Subject s
        where lower(trim(coalesce(s.titleCn, ''))) = :titleCnKey
          and s.status = 'NORMAL'
        order by s.titleCn asc,
                 s.category asc,
                 s.seasonIndex asc,
                 s.partIndex asc,
                 s.releaseYear asc,
                 s.releaseQuarter asc,
                 s.id asc
        """)
    List<Subject> findAllNormalByNormalizedTitleCn(@Param("titleCnKey") String titleCnKey);

    boolean existsByDisplayTitleAndReleaseYearAndReleaseQuarterAndCategory(
            String displayTitle,
            Integer releaseYear,
            Integer releaseQuarter,
            String category
    );

    List<Subject> findAllByDisplayTitleAndReleaseYearAndReleaseQuarterAndCategory(
            String displayTitle,
            Integer releaseYear,
            Integer releaseQuarter,
            String category
    );

    @Query("""
        select (count(s) > 0)
        from Subject s
        where lower(trim(coalesce(s.sourceId, ''))) = :sourceIdKey
        """)
    boolean existsBySourceIdentity(@Param("sourceIdKey") String sourceIdKey);

    @Query("""
        select (count(s) > 0)
        from Subject s
        where s.id <> :id
          and lower(trim(coalesce(s.sourceId, ''))) = :sourceIdKey
        """)
    boolean existsBySourceIdentityAndIdNot(@Param("sourceIdKey") String sourceIdKey,
                                           @Param("id") Long id);

    @Query("""
        select s
        from Subject s
        where lower(trim(coalesce(s.sourceId, ''))) = :sourceIdKey
        order by s.id asc
        """)
    List<Subject> findAllBySourceIdentity(@Param("sourceIdKey") String sourceIdKey);

    @Query("""
        select s
        from Subject s
        where s.status = 'NORMAL'
          and (
                lower(trim(coalesce(s.titleCn, ''))) like concat('%', :keywordKey, '%')
                or lower(trim(coalesce(s.displayTitle, ''))) like concat('%', :keywordKey, '%')
          )
        order by s.id asc
        """)
    List<Subject> searchAllNormalByTitleKeyword(@Param("keywordKey") String keywordKey);
}
