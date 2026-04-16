package com.mykr.acgnhistoryanalyzer.repository;

import com.mykr.acgnhistoryanalyzer.entity.UserSubjectRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserSubjectRecordRepository extends JpaRepository<UserSubjectRecord, Long> {
}