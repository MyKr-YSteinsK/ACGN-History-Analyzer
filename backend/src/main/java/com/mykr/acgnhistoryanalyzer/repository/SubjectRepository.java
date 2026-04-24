package com.mykr.acgnhistoryanalyzer.repository;

import com.mykr.acgnhistoryanalyzer.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface SubjectRepository
        extends JpaRepository<Subject, Long>, JpaSpecificationExecutor<Subject> {

    List<Subject> findByFranchiseId(Long franchiseId);

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
        where lower(trim(coalesce(s.titleCn, ''))) = :titleCnKey
          and lower(trim(coalesce(s.subtitle, ''))) = :subtitleKey
          and coalesce(s.seasonIndex, -1) = :seasonIndexKey
          and coalesce(s.partIndex, -1) = :partIndexKey
          and lower(trim(coalesce(s.category, ''))) = :categoryKey
        """)
    boolean existsByBusinessUnique(@Param("titleCnKey") String titleCnKey,
                                   @Param("subtitleKey") String subtitleKey,
                                   @Param("seasonIndexKey") Integer seasonIndexKey,
                                   @Param("partIndexKey") Integer partIndexKey,
                                   @Param("categoryKey") String categoryKey);

    @Query("""
        select (count(s) > 0)
        from Subject s
        where s.id <> :id
          and lower(trim(coalesce(s.titleCn, ''))) = :titleCnKey
          and lower(trim(coalesce(s.subtitle, ''))) = :subtitleKey
          and coalesce(s.seasonIndex, -1) = :seasonIndexKey
          and coalesce(s.partIndex, -1) = :partIndexKey
          and lower(trim(coalesce(s.category, ''))) = :categoryKey
        """)
    boolean existsByBusinessUniqueAndIdNot(@Param("titleCnKey") String titleCnKey,
                                           @Param("subtitleKey") String subtitleKey,
                                           @Param("seasonIndexKey") Integer seasonIndexKey,
                                           @Param("partIndexKey") Integer partIndexKey,
                                           @Param("categoryKey") String categoryKey,
                                           @Param("id") Long id);

    @Query("""
        select s
        from Subject s
        where lower(trim(coalesce(s.titleCn, ''))) = :titleCnKey
          and lower(trim(coalesce(s.subtitle, ''))) = :subtitleKey
          and coalesce(s.seasonIndex, -1) = :seasonIndexKey
          and coalesce(s.partIndex, -1) = :partIndexKey
          and lower(trim(coalesce(s.category, ''))) = :categoryKey
        order by s.id asc
        """)
    List<Subject> findAllByBusinessUnique(@Param("titleCnKey") String titleCnKey,
                                          @Param("subtitleKey") String subtitleKey,
                                          @Param("seasonIndexKey") Integer seasonIndexKey,
                                          @Param("partIndexKey") Integer partIndexKey,
                                          @Param("categoryKey") String categoryKey);
}