package com.mykr.acgnhistoryanalyzer.common.enums;

public enum LibraryStatus {
    WATCHED("看过"),
    WANT("想看"),
    PAUSED("搁置"),
    NONE("未记录");

    private final String label;

    LibraryStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
