package com.mykr.acgnhistoryanalyzer.response;

/**
 * Two JSON blobs ready to paste into Import center (SUBJECT then RECORD), plus usage hint.
 */
public class BackupExportResponse {

    /** {@code {"records":[...]}} — NON_NULL fields only. */
    private String recordJsonText;

    /**
     * {@code {"subjects":[...]}} or {@code null} when there are no Bangumi subjects to re-import
     * (empty list is invalid for Bangumi batch validation).
     */
    private String subjectJsonText;

    private String usageHint;

    public BackupExportResponse(String recordJsonText, String subjectJsonText, String usageHint) {
        this.recordJsonText = recordJsonText;
        this.subjectJsonText = subjectJsonText;
        this.usageHint = usageHint;
    }

    public String getRecordJsonText() {
        return recordJsonText;
    }

    public void setRecordJsonText(String recordJsonText) {
        this.recordJsonText = recordJsonText;
    }

    public String getSubjectJsonText() {
        return subjectJsonText;
    }

    public void setSubjectJsonText(String subjectJsonText) {
        this.subjectJsonText = subjectJsonText;
    }

    public String getUsageHint() {
        return usageHint;
    }

    public void setUsageHint(String usageHint) {
        this.usageHint = usageHint;
    }
}
