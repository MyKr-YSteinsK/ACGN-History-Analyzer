package com.mykr.acgnhistoryanalyzer.response;

public class FranchiseSimpleResponse {

    private Long id;
    private String name;
    private String nameCn;

    public FranchiseSimpleResponse(Long id, String name, String nameCn) {
        this.id = id;
        this.name = name;
        this.nameCn = nameCn;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getNameCn() {
        return nameCn;
    }
}