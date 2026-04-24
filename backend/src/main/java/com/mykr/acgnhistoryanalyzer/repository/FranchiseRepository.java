package com.mykr.acgnhistoryanalyzer.repository;

import com.mykr.acgnhistoryanalyzer.entity.Franchise;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FranchiseRepository extends JpaRepository<Franchise, Long> {

    boolean existsByNameCnIgnoreCase(String nameCn);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameCnIgnoreCaseAndIdNot(String nameCn, Long id);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}