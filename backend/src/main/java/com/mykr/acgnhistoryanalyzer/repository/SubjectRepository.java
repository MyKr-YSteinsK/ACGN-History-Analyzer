package com.mykr.acgnhistoryanalyzer.repository;

import com.mykr.acgnhistoryanalyzer.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface SubjectRepository
        extends JpaRepository<Subject, Long>, JpaSpecificationExecutor<Subject> {

    List<Subject> findByFranchiseId(Long franchiseId);
}