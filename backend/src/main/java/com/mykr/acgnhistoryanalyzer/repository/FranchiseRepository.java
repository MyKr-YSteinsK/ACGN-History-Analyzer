package com.mykr.acgnhistoryanalyzer.repository;

import com.mykr.acgnhistoryanalyzer.entity.Franchise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FranchiseRepository extends JpaRepository<Franchise, Long> {

    @Query("""
            select count(f) > 0
            from Franchise f
            where f.nameCn is not null
              and lower(trim(f.nameCn)) = lower(trim(:businessName))
            """)
    boolean existsByNormalizedNameCn(@Param("businessName") String businessName);

    @Query("""
            select count(f) > 0
            from Franchise f
            where lower(trim(f.name)) = lower(trim(:businessName))
            """)
    boolean existsByNormalizedName(@Param("businessName") String businessName);

    @Query("""
            select count(f) > 0
            from Franchise f
            where f.id <> :id
              and f.nameCn is not null
              and lower(trim(f.nameCn)) = lower(trim(:businessName))
            """)
    boolean existsByNormalizedNameCnAndIdNot(@Param("businessName") String businessName,
                                             @Param("id") Long id);

    @Query("""
            select count(f) > 0
            from Franchise f
            where f.id <> :id
              and lower(trim(f.name)) = lower(trim(:businessName))
            """)
    boolean existsByNormalizedNameAndIdNot(@Param("businessName") String businessName,
                                           @Param("id") Long id);

    @Query("""
        select f
        from Franchise f
        where f.nameCn is not null
          and lower(trim(f.nameCn)) = lower(trim(:businessName))
        order by f.id asc
        """)
    List<Franchise> findAllByNormalizedNameCn(@Param("businessName") String businessName);

    @Query("""
        select f
        from Franchise f
        where lower(trim(f.name)) = lower(trim(:businessName))
        order by f.id asc
        """)
    List<Franchise> findAllByNormalizedName(@Param("businessName") String businessName);
}