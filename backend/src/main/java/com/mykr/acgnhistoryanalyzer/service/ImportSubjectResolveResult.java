package com.mykr.acgnhistoryanalyzer.service;

import com.mykr.acgnhistoryanalyzer.entity.Subject;
import com.mykr.acgnhistoryanalyzer.response.SubjectMatchCandidateResponse;

import java.util.List;

/**
 * 记录/收藏批量导入时，按 subjectId、sourceId、标题关键词解析目标作品的统一结果。
 */
public final class ImportSubjectResolveResult {

    private final Subject subject;
    private final String errorMessage;
    private final List<SubjectMatchCandidateResponse> candidates;

    public ImportSubjectResolveResult(Subject subject, String errorMessage) {
        this(subject, errorMessage, List.of());
    }

    public ImportSubjectResolveResult(Subject subject,
                                      String errorMessage,
                                      List<SubjectMatchCandidateResponse> candidates) {
        this.subject = subject;
        this.errorMessage = errorMessage;
        this.candidates = candidates == null ? List.of() : candidates;
    }

    public Subject getSubject() {
        return subject;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public List<SubjectMatchCandidateResponse> getCandidates() {
        return candidates;
    }

    public boolean hasAmbiguousCandidates() {
        return !candidates.isEmpty();
    }

    public boolean hasError() {
        return errorMessage != null;
    }
}
