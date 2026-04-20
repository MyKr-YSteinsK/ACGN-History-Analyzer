package com.mykr.acgnhistoryanalyzer.repository;

import com.mykr.acgnhistoryanalyzer.common.enums.RecordStatus;
import com.mykr.acgnhistoryanalyzer.entity.UserSubjectRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface UserSubjectRecordRepository
        extends JpaRepository<UserSubjectRecord, Long>, JpaSpecificationExecutor<UserSubjectRecord> {

    boolean existsBySubjectId(Long subjectId);
}