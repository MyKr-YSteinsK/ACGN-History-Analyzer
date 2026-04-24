package com.mykr.acgnhistoryanalyzer.response;

import java.util.List;

public class ImportOverviewResponse {

    private String moduleName;
    private String recommendedOrder;
    private List<String> steps;
    private List<String> notes;
    private List<ImportEndpointDocResponse> endpoints;

    public ImportOverviewResponse(String moduleName,
                                  String recommendedOrder,
                                  List<String> steps,
                                  List<String> notes,
                                  List<ImportEndpointDocResponse> endpoints) {
        this.moduleName = moduleName;
        this.recommendedOrder = recommendedOrder;
        this.steps = steps;
        this.notes = notes;
        this.endpoints = endpoints;
    }

    public String getModuleName() {
        return moduleName;
    }

    public String getRecommendedOrder() {
        return recommendedOrder;
    }

    public List<String> getSteps() {
        return steps;
    }

    public List<String> getNotes() {
        return notes;
    }

    public List<ImportEndpointDocResponse> getEndpoints() {
        return endpoints;
    }
}