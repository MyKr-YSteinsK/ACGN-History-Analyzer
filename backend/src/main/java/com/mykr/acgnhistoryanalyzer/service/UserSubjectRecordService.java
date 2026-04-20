package com.mykr.acgnhistoryanalyzer.service;

import com.mykr.acgnhistoryanalyzer.common.enums.RecordStatus;
import com.mykr.acgnhistoryanalyzer.entity.Subject;
import com.mykr.acgnhistoryanalyzer.entity.UserSubjectRecord;
import com.mykr.acgnhistoryanalyzer.repository.SubjectRepository;
import com.mykr.acgnhistoryanalyzer.repository.UserSubjectRecordRepository;
import com.mykr.acgnhistoryanalyzer.request.UserSubjectRecordCreateRequest;
import com.mykr.acgnhistoryanalyzer.response.RecordScoreBandStatsResponse;
import com.mykr.acgnhistoryanalyzer.response.UserSubjectRecordResponse;
import com.mykr.acgnhistoryanalyzer.specification.UserSubjectRecordSpecifications;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserSubjectRecordService {

    private final UserSubjectRecordRepository userSubjectRecordRepository;
    private final SubjectRepository subjectRepository;

    public UserSubjectRecordService(UserSubjectRecordRepository userSubjectRecordRepository,
                                    SubjectRepository subjectRepository) {
        this.userSubjectRecordRepository = userSubjectRecordRepository;
        this.subjectRepository = subjectRepository;
    }

    public boolean subjectExists(Long subjectId) {
        return subjectRepository.existsById(subjectId);
    }

    public UserSubjectRecordResponse createRecord(UserSubjectRecordCreateRequest request) {
        Subject subject = subjectRepository.findById(request.getSubjectId()).orElse(null);

        if (subject == null) {
            return null;
        }

        UserSubjectRecord record = new UserSubjectRecord(
                subject.getId(),
                subject.getDisplayTitle(),
                request.getRecordStatus(),
                request.getScoreValue(),
                request.getRecordYear(),
                request.getRecordQuarter(),
                request.getNote()
        );

        UserSubjectRecord savedRecord = userSubjectRecordRepository.save(record);
        return toResponse(savedRecord);
    }

    public List<UserSubjectRecordResponse> getRecords(Integer year, String quarter,
                                                      RecordStatus status, Integer minScore, Integer maxScore) {

        Specification<UserSubjectRecord> specification =
                buildRecordSpecification(year, quarter, status, minScore, maxScore);

        List<UserSubjectRecord> records = userSubjectRecordRepository.findAll(specification);

        return records.stream()
                .map(this::toResponse)
                .toList();
    }

    public RecordScoreBandStatsResponse getScoreBandStats(Integer year, String quarter, RecordStatus status) {
        Specification<UserSubjectRecord> specification =
                buildRecordSpecification(year, quarter, status, null, null);

        List<UserSubjectRecord> records = userSubjectRecordRepository.findAll(specification);

        int excellentCount = 0;
        int normalCount = 0;
        int badCount = 0;
        int unratedCount = 0;

        for (UserSubjectRecord record : records) {
            Integer score = record.getScoreValue();

            if (score == null) {
                unratedCount++;
            } else if (score >= 42 && score <= 50) {
                excellentCount++;
            } else if (score >= 35 && score <= 41) {
                normalCount++;
            } else if (score >= 20 && score <= 34) {
                badCount++;
            }
        }

        return new RecordScoreBandStatsResponse(
                records.size(),
                excellentCount,
                normalCount,
                badCount,
                unratedCount
        );
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

        Subject subject = subjectRepository.findById(request.getSubjectId()).orElse(null);

        if (subject == null) {
            return null;
        }

        existingRecord.setSubjectId(subject.getId());
        existingRecord.setSubjectTitle(subject.getDisplayTitle());
        existingRecord.setRecordStatus(request.getRecordStatus());
        existingRecord.setScoreValue(request.getScoreValue());
        existingRecord.setRecordYear(request.getRecordYear());
        existingRecord.setRecordQuarter(request.getRecordQuarter());
        existingRecord.setNote(request.getNote());

        UserSubjectRecord updatedRecord = userSubjectRecordRepository.save(existingRecord);
        return toResponse(updatedRecord);
    }

    public boolean deleteRecord(Long id) {
        if (!userSubjectRecordRepository.existsById(id)) {
            return false;
        }

        userSubjectRecordRepository.deleteById(id);
        return true;
    }

    private Specification<UserSubjectRecord> buildRecordSpecification(Integer year, String quarter,
                                                                      RecordStatus status,
                                                                      Integer minScore, Integer maxScore) {
        return Specification
                .where(UserSubjectRecordSpecifications.hasRecordYear(year))
                .and(UserSubjectRecordSpecifications.hasRecordQuarter(quarter))
                .and(UserSubjectRecordSpecifications.hasRecordStatus(status))
                .and(UserSubjectRecordSpecifications.hasMinScore(minScore))
                .and(UserSubjectRecordSpecifications.hasMaxScore(maxScore));
    }

    private UserSubjectRecordResponse toResponse(UserSubjectRecord record) {
        String subjectTitle = record.getSubjectTitle();
        String subjectCategory = null;
        Integer subjectReleaseYear = null;
        Integer subjectReleaseMonth = null;
        String subjectCoverUrl = null;

        if (record.getSubjectId() != null) {
            Subject subject = subjectRepository.findById(record.getSubjectId()).orElse(null);
            if (subject != null) {
                subjectTitle = subject.getDisplayTitle();
                subjectCategory = subject.getCategory();
                subjectReleaseYear = subject.getReleaseYear();
                subjectReleaseMonth = subject.getReleaseMonth();
                subjectCoverUrl = subject.getCoverUrl();
            }
        }

        return new UserSubjectRecordResponse(
                record.getId(),
                record.getSubjectId(),
                subjectTitle,
                subjectCategory,
                subjectReleaseYear,
                subjectReleaseMonth,
                subjectCoverUrl,
                record.getRecordStatus().name(),
                record.getRecordStatus().getLabel(),
                record.getScoreValue(),
                record.getRecordYear(),
                record.getRecordQuarter(),
                record.getNote()
        );
    }
}