package com.mykr.acgnhistoryanalyzer.specification;

import com.mykr.acgnhistoryanalyzer.common.enums.RecordStatus;
import com.mykr.acgnhistoryanalyzer.entity.UserSubjectRecord;
import org.springframework.data.jpa.domain.Specification;

public class UserSubjectRecordSpecifications {

    public static Specification<UserSubjectRecord> hasRecordYear(Integer year) {
        return (root, query, criteriaBuilder) -> {
            if (year == null) {
                return null;
            }
            return criteriaBuilder.equal(root.get("recordYear"), year);
        };
    }

    public static Specification<UserSubjectRecord> hasRecordQuarter(String quarter) {
        return (root, query, criteriaBuilder) -> {
            if (quarter == null || quarter.isBlank()) {
                return null;
            }
            return criteriaBuilder.equal(root.get("recordQuarter"), quarter);
        };
    }

    public static Specification<UserSubjectRecord> hasRecordStatus(RecordStatus status) {
        return (root, query, criteriaBuilder) -> {
            if (status == null) {
                return null;
            }
            return criteriaBuilder.equal(root.get("recordStatus"), status);
        };
    }

    public static Specification<UserSubjectRecord> hasMinScore(Integer minScore) {
        return (root, query, criteriaBuilder) -> {
            if (minScore == null) {
                return null;
            }
            return criteriaBuilder.greaterThanOrEqualTo(root.get("scoreValue"), minScore);
        };
    }

    public static Specification<UserSubjectRecord> hasMaxScore(Integer maxScore) {
        return (root, query, criteriaBuilder) -> {
            if (maxScore == null) {
                return null;
            }
            return criteriaBuilder.lessThanOrEqualTo(root.get("scoreValue"), maxScore);
        };
    }
}