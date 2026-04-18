package com.mykr.acgnhistoryanalyzer.service;

import com.mykr.acgnhistoryanalyzer.entity.UserSubjectRecord;
import com.mykr.acgnhistoryanalyzer.repository.UserSubjectRecordRepository;
import com.mykr.acgnhistoryanalyzer.request.UserSubjectRecordCreateRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserSubjectRecordService {

    private final UserSubjectRecordRepository userSubjectRecordRepository;

    public UserSubjectRecordService(UserSubjectRecordRepository userSubjectRecordRepository) {
        this.userSubjectRecordRepository = userSubjectRecordRepository;
    }

    public UserSubjectRecord createRecord(UserSubjectRecordCreateRequest request) {
        UserSubjectRecord record = new UserSubjectRecord(
                request.getSubjectTitle(),
                request.getRecordStatus(),
                request.getScoreValue(),
                request.getRecordYear(),
                request.getRecordQuarter(),
                request.getNote()
        );

        return userSubjectRecordRepository.save(record);
    }

    public List<UserSubjectRecord> getAllRecords() {
        return userSubjectRecordRepository.findAll();
    }
}