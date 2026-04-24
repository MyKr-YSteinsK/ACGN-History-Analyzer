package com.mykr.acgnhistoryanalyzer.response;

public class ImportEndpointDocResponse {

    private String name;
    private String method;
    private String path;
    private String description;

    public ImportEndpointDocResponse(String name, String method, String path, String description) {
        this.name = name;
        this.method = method;
        this.path = path;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public String getDescription() {
        return description;
    }
}