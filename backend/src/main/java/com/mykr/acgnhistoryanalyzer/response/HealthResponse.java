package com.mykr.acgnhistoryanalyzer.response;

public class HealthResponse {

    private String appName;
    private String status;
    private String message;

    public HealthResponse(String appName, String status, String message) {
        this.appName = appName;
        this.status = status;
        this.message = message;
    }

    public String getAppName() {
        return appName;
    }

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}