package com.mykr.acgnhistoryanalyzer.repository;

import com.mykr.acgnhistoryanalyzer.common.enums.RecordStatus;
import com.mykr.acgnhistoryanalyzer.entity.UserSubjectRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserSubjectRecordRepository extends JpaRepository<UserSubjectRecord, Long> {

    List<UserSubjectRecord> findByRecordStatus(RecordStatus recordStatus);
}