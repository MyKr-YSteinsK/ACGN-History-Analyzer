package com.mykr.acgnhistoryanalyzer.service;

import com.mykr.acgnhistoryanalyzer.common.enums.RecordStatus;
import com.mykr.acgnhistoryanalyzer.entity.UserSubjectRecord;
import com.mykr.acgnhistoryanalyzer.repository.UserSubjectRecordRepository;
import com.mykr.acgnhistoryanalyzer.request.UserSubjectRecordCreateRequest;
import com.mykr.acgnhistoryanalyzer.response.UserSubjectRecordResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserSubjectRecordService {

    private final UserSubjectRecordRepository userSubjectRecordRepository;

    public UserSubjectRecordService(UserSubjectRecordRepository userSubjectRecordRepository) {
        this.userSubjectRecordRepository = userSubjectRecordRepository;
    }

    public UserSubjectRecordResponse createRecord(UserSubjectRecordCreateRequest request) {
        UserSubjectRecord record = new UserSubjectRecord(
                request.getSubjectTitle(),
                request.getRecordStatus(),
                request.getScoreValue(),
                request.getRecordYear(),
                request.getRecordQuarter(),
                request.getNote()
        );

        UserSubjectRecord savedRecord = userSubjectRecordRepository.save(record);
        return toResponse(savedRecord);
    }

    public List<UserSubjectRecordResponse> getRecords(RecordStatus status) {
        List<UserSubjectRecord> records;

        if (status == null) {
            records = userSubjectRecordRepository.findAll();
        } else {
            records = userSubjectRecordRepository.findByRecordStatus(status);
        }

        return records.stream()
                .map(this::toResponse)
                .toList();
    }

    public UserSubjectRecordResponse getRecordById(Long id) {
        UserSubjectRecord record = userSubjectRecordRepository.findById(id).orElse(null);

        if (record == null) {
            return null;
        }

        return toResponse(record);
    }

    public UserSubjectRecordResponse updateRecord(Long id, UserSubjectRecordCreateRequest request) {
        UserSubjectRecord existingRecord = userSubjectRecordRepository.findById(id).orElse(null);

        if (existingRecord == null) {
            return null;
        }

        existingRecord.setSubjectTitle(request.getSubjectTitle());
        existingRecord.setRecordStatus(request.getRecordStatus());
        existingRecord.setScoreValue(request.getScoreValue());
        existingRecord.setRecordYear(request.getRecordYear());
        existingRecord.setRecordQuarter(request.getRecordQuarter());
        existingRecord.setNote(request.getNote());

        UserSubjectRecord updatedRecord = userSubjectRecordRepository.save(existingRecord);
        return toResponse(updatedRecord);
    }

    private UserSubjectRecordResponse toResponse(UserSubjectRecord record) {
        return new UserSubjectRecordResponse(
                record.getId(),
                record.getSubjectTitle(),
                record.getRecordStatus().name(),
                record.getRecordStatus().getLabel(),
                record.getScoreValue(),
                record.getRecordYear(),
                record.getRecordQuarter(),
                record.getNote()
        );
    }
}