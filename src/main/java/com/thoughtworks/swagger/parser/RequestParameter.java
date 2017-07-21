package com.thoughtworks.swagger.parser;

public class RequestParameter{
    public String getParameterIn() {
        return parameterIn;
    }

    public void setParameterIn(String parameterIn) {
        this.parameterIn = parameterIn;
    }

    public Boolean getIsRequired() {
        return isRequired;
    }

    public void setIsRequired(Boolean isRequired) {
        this.isRequired = isRequired;
    }

    public SwaggerSchema getSwaggerSchema() {
        return swaggerSchema;
    }

    public void setSwaggerSchema(SwaggerSchema swaggerSchema) {
        this.swaggerSchema = swaggerSchema;
    }

    private String parameterIn;
    private Boolean isRequired;
    private SwaggerSchema swaggerSchema;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private String name;

}
