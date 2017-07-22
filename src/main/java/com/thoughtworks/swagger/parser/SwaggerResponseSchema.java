package com.thoughtworks.swagger.parser;

import java.util.HashMap;

/**
 * Created by bsneha on 22/07/17.
 */
public class SwaggerResponseSchema {
   private HashMap<String, SwaggerSchema> swaggerStructurePerReponseType;

    public HashMap<String, SwaggerSchema> getSwaggerStructurePerReponseType() {
        return swaggerStructurePerReponseType;
    }

    public void setSwaggerStructurePerReponseType(HashMap<String, SwaggerSchema> swaggerStructurePerReponseType) {
        this.swaggerStructurePerReponseType = swaggerStructurePerReponseType;
    }

    public String getErrorMessage() {
        return ErrorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        ErrorMessage = errorMessage;
    }

    private String ErrorMessage;

}
