package com.swagger.parser;

/**
 * Created by bsneha on 19/06/17.
 */
public enum ResponseType {

    OK("200"),
    NOT_FOUND("404"),
    BAD_REQUEST("400"),
    INTERNAL_SERVER_ERROR("500"),
    NOT_IMPLEMENTED("502"),
    BAD_GATEWAY("502"),
    SERVICE_UNAVAILABLE("503"),
    All("All");

    private String codeValue;

    ResponseType(String codeValue) {
        this.codeValue = codeValue;
    }

    public String getCodeValue() {
        return codeValue;
    }

}
