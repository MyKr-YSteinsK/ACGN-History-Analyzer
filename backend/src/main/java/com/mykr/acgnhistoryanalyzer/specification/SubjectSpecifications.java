package com.mykr.acgnhistoryanalyzer.specification;

import com.mykr.acgnhistoryanalyzer.entity.Subject;
import org.springframework.data.jpa.domain.Specification;

public class SubjectSpecifications {

    public static Specification<Subject> hasReleaseYear(Integer year) {
        return (root, query, criteriaBuilder) -> {
            if (year == null) {
                return null;
            }
            return criteriaBuilder.equal(root.get("releaseYear"), year);
        };
    }

    public static Specification<Subject> hasReleaseQuarter(Integer quarter) {
        return (root, query, criteriaBuilder) -> {
            if (quarter == null) {
                return null;
            }
            return criteriaBuilder.equal(root.get("releaseQuarter"), quarter);
        };
    }

    public static Specification<Subject> hasCategory(String category) {
        return (root, query, criteriaBuilder) -> {
            if (category == null || category.isBlank()) {
                return null;
            }
            return criteriaBuilder.equal(root.get("category"), category);
        };
    }

    public static Specification<Subject> hasStatus(String status) {
        return (root, query, criteriaBuilder) -> {
            if (status == null || status.isBlank()) {
                return null;
            }
            return criteriaBuilder.equal(root.get("status"), status);
        };
    }

    public static Specification<Subject> titleContainsKeyword(String keyword) {
        return (root, query, criteriaBuilder) -> {
            if (keyword == null || keyword.isBlank()) {
                return null;
            }
            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("displayTitle")),
                    "%" + keyword.toLowerCase() + "%"
            );
        };
    }
}