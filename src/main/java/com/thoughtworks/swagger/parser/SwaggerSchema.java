package com.thoughtworks.swagger.parser;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.HashMap;

/**
 * Created by bsneha on 17/07/17.
 */
public class SwaggerSchema {
    private String type;

    public Boolean getRequired() {
        return isRequired;
    }

    public void setRequired(Boolean required) {
        isRequired = required;
    }

    private Boolean isRequired;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public HashMap<String, JsonNode> getParsedSchema() {
        return parsedSchema;
    }

    public void setParsedSchema(HashMap<String, JsonNode> parsedSchema) {
        this.parsedSchema = parsedSchema;
    }

    private HashMap<String, JsonNode> parsedSchema;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAdditionalPropertiesType() {
        return additionalPropertiesType;
    }

    public void setAdditionalPropertiesType(String additionalPropertiesType) {
        this.additionalPropertiesType = additionalPropertiesType;
    }



    private String additionalPropertiesType;

    private String description;

}

