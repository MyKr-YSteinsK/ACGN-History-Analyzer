package com.mykr.acgnhistoryanalyzer.response;

import java.util.List;
import java.util.Map;

public class ImportTemplateResponse {

    private String importType;
    private String endpoint;
    private String recommendedOrder;
    private List<String> rules;
    private Map<String, Object> templateBody;
    private List<ImportFieldDocResponse> fields;

    public ImportTemplateResponse(String importType,
                                  String endpoint,
                                  String recommendedOrder,
                                  List<String> rules,
                                  Map<String, Object> templateBody,
                                  List<ImportFieldDocResponse> fields) {
        this.importType = importType;
        this.endpoint = endpoint;
        this.recommendedOrder = recommendedOrder;
        this.rules = rules;
        this.templateBody = templateBody;
        this.fields = fields;
    }

    public String getImportType() {
        return importType;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public String getRecommendedOrder() {
        return recommendedOrder;
    }

    public List<String> getRules() {
        return rules;
    }

    public Map<String, Object> getTemplateBody() {
        return templateBody;
    }

    public List<ImportFieldDocResponse> getFields() {
        return fields;
    }
}