package com.thoughtworks.swagger.parser;

import java.util.ArrayList;

/**
 * Created by bsneha on 22/06/17.
 */
public class SwaggerReponse {
    private ArrayList<String> responseSchemas;
    private String responseStatusCode;

    public ArrayList<String>  getResponseSchemas() { return this.responseSchemas; }
    public void setResponseSchemas(ArrayList<String> responseSchemas) { this.responseSchemas = this.responseSchemas; }

    public String getResponseStatusCode() { return this.responseStatusCode; }
    public void setResponseStatusCode(String responseSchema) { this.responseStatusCode = responseStatusCode; }
}
