package com.mykr.acgnhistoryanalyzer.repository;

import com.mykr.acgnhistoryanalyzer.entity.UserSubjectRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface UserSubjectRecordRepository
        extends JpaRepository<UserSubjectRecord, Long>, JpaSpecificationExecutor<UserSubjectRecord> {

    boolean existsBySubjectId(Long subjectId);

    List<UserSubjectRecord> findBySubjectId(Long subjectId);
}