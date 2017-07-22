package com.swagger.parser;

import com.fasterxml.jackson.databind.JsonNode;

public class SwaggerReferenceModel{

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    private String modelName;

    public JsonNode getReferenceData() {
        return referenceData;
    }

    public void setReferenceData(JsonNode referenceData) {
        this.referenceData = referenceData;
    }

    private JsonNode referenceData;
}
