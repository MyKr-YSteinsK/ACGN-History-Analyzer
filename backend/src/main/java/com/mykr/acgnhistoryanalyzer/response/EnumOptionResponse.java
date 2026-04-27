package com.mykr.acgnhistoryanalyzer.response;

public class EnumOptionResponse {

    private String value;
    private String label;

    public EnumOptionResponse(String value, String label) {
        this.value = value;
        this.label = label;
    }

    public String getValue() {
        return value;
    }

    public String getLabel() {
        return label;
    }
}
