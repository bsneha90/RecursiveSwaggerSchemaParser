package com.thoughtworks.swagger.parser;

import java.util.ArrayList;

/**
 * Created by bsneha on 18/07/17.
 */
public class SwaggerRequestSchema {
    public ArrayList<RequestParameter> getParameters() {
        return parameters;
    }

    private ArrayList<RequestParameter> parameters;

    public SwaggerRequestSchema() {
        this.parameters = new ArrayList<>();
    }

    public String getErrorMessage() {
        return ErrorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        ErrorMessage = errorMessage;
    }

    private String ErrorMessage;
}

