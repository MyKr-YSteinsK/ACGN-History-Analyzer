package com.mykr.acgnhistoryanalyzer.common.enums;

public enum RecordStatus {
    WANT_TO_WATCH("想看"),
    WATCHED("看过"),
    ON_HOLD("搁置");

    private final String label;

    RecordStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
