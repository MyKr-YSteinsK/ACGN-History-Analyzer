package com.mykr.acgnhistoryanalyzer.common.error;

public class BusinessError {

    private final int code;
    private final String message;

    public BusinessError(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}