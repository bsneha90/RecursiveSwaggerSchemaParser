package com.swagger.parser;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.models.HttpMethod;
import io.swagger.models.Swagger;
import io.swagger.parser.SwaggerParser;

import java.io.IOException;
import java.util.HashMap;

/**
 * Created by bsneha on 15/06/17.
 */
public class SwaggerSchemaParser {
    private static final String SCHEMA_KEY = "schema";
    private Swagger swagger;
    private SwaggerParser swaggerParser;
    private HashMap<String, JsonNode> parsedSchema;
    private HashMap<String, HashMap<String, JsonNode>> parsedSchemaPerResponseType;
    ObjectMapper mapper;
    SwaggerParserHelper swaggerParserHelper;
    String swaggerJsonUrl;

    public void initializeParser(String swaggerJsonUrl) {
        parsedSchemaPerResponseType = new HashMap<>();
        mapper = new ObjectMapper();
        this.swaggerJsonUrl = swaggerJsonUrl;
        swaggerParser = new SwaggerParser();
        swagger = swaggerParser.read(swaggerJsonUrl);
        swaggerParserHelper= new SwaggerParserHelper(swagger);
    }

    public HashMap<String, SwaggerResponseSchema> parseResponseForAllPaths() {
       return new SwaggerSchemaResponseParser(swagger).parseResponseForAllPaths();
    }

    public HashMap<String, SwaggerRequestSchema> parseRequestForAllPaths() {
        return new SwaggerSchemaRequestParser(swagger).parseRequestForAllPaths();
    }


    public SwaggerRequestSchema parseRequest(String path, HttpMethod httpMethod) {
        return new SwaggerSchemaRequestParser(swagger).parseRequest(path,httpMethod);
    }

    public SwaggerResponseSchema parseResponse(String path, HttpMethod httpMethod) throws IOException {
        return new SwaggerSchemaResponseParser(swagger).parseReponseForGivenPathHTTPMethodAndAllResponseType(path, httpMethod);
    }

    public SwaggerResponseSchema parseResponse(String path, HttpMethod httpMethod, ResponseType responseType) throws IOException {
       return new SwaggerSchemaResponseParser(swagger).parseReponseForGivenPathHTTPMethodAndResponseType(path, httpMethod,responseType);
    }

}
