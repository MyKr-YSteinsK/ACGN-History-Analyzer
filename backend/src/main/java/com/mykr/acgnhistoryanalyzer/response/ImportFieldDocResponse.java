package com.mykr.acgnhistoryanalyzer.response;

public class ImportFieldDocResponse {

    private String fieldName;
    private boolean required;
    private String description;
    private String example;
    private String notes;

    public ImportFieldDocResponse(String fieldName,
                                  boolean required,
                                  String description,
                                  String example,
                                  String notes) {
        this.fieldName = fieldName;
        this.required = required;
        this.description = description;
        this.example = example;
        this.notes = notes;
    }

    public String getFieldName() {
        return fieldName;
    }

    public boolean isRequired() {
        return required;
    }

    public String getDescription() {
        return description;
    }

    public String getExample() {
        return example;
    }

    public String getNotes() {
        return notes;
    }
}