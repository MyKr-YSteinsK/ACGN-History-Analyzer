package com.mykr.acgnhistoryanalyzer.common.enums;

public enum CollectionType {
    WANT("想看"),
    PAUSED("搁置");

    private final String label;

    CollectionType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
