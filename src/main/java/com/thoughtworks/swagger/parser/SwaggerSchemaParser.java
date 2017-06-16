package com.thoughtworks.swagger.parser;


import io.swagger.models.*;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;
import io.swagger.parser.SwaggerParser;
import io.swagger.util.Json;

import java.io.IOException;
import java.text.ParseException;
import java.util.*;

/**
 * Created by bsneha on 15/06/17.
 */
public class SwaggerSchemaParser {
    private Swagger swagger;
    private SwaggerParser swaggerParser;
    private HashMap<String, String> parsedSchema = new HashMap<>();
    String swaggerJsonUrl;
    public SwaggerSchemaParser(String swaggerJsonUrl)
            throws IOException, ParseException {
        this.swaggerJsonUrl=swaggerJsonUrl;
    }

    public HashMap<String, String> parseRequest(String path) {
        return getSwaggerDefinitionsForRequest(path,swaggerJsonUrl);
    }

    public HashMap<String, String> parseResponse(String path) throws IOException {
        String referenceSchema = getSwaggerDefinitionsForResponse(path,swaggerJsonUrl);
        return getReference(referenceSchema);
    }

    private HashMap<String, String> getReference(String referenceSchema) {

        Model model = swagger.getDefinitions().get(referenceSchema);
        if (parsedSchema.containsKey(referenceSchema)) {
            return null;
        }
        parsedSchema.put(referenceSchema, Json.pretty(model));

        Set<Property> referenceSet = new HashSet<>();
        if (model.getProperties() != null) {
            for (Map.Entry<String, Property> map : model.getProperties().entrySet()) {
                if ("ref".equals(map.getValue().getType())
                        || "array".equals(map.getValue().getType())) {
                    referenceSet.add(map.getValue());
                }
            }
        }
        referenceSet.forEach(property -> {
            String simpleRef = null;
            if (property instanceof RefProperty) {
                simpleRef = ((RefProperty) property).getSimpleRef();
            } else {
                Property items = ((ArrayProperty) property).getItems();
                if (items instanceof RefProperty) {
                    simpleRef = ((RefProperty) items).getSimpleRef();
                } else {
                    return;
                }
            }
            if (simpleRef != null) {
                getReference(simpleRef);
            }

        });

        return parsedSchema;
    }

    private String getSwaggerDefinitionsForResponse(String key,String swaggerJsonUrl) throws IOException {
        swaggerParser = new SwaggerParser();
        swagger = swaggerParser.read(swaggerJsonUrl);
        Operation operation = swagger.getPaths().get(key).getOperations().get(0);
        Property schema = operation.getResponses().get("200").getSchema();
        String referenceSchema = ((RefProperty) schema).getSimpleRef();
        return referenceSchema;
    }

    private HashMap<String, String> getSwaggerDefinitionsForRequest(String key, String swaggerJsonUrl) {
        HashMap<String,String> getParam = new HashMap<>();

        swaggerParser = new SwaggerParser();
        swagger = swaggerParser.read(swaggerJsonUrl);
        HttpMethod getOperationType = swagger.getPaths().get(key)
                .getOperationMap().entrySet().iterator().next().getKey();
        Operation operation = swagger.getPaths().get(key).getOperations().get(0);
        List<Parameter> operationParameters = operation.getParameters();
        String referenceSchema = null;
        if (getOperationType.toString().equals("POST")) {
            System.out.println("Post Method");
            Parameter parameter = operationParameters.get(0);
            Model schema = ((BodyParameter) parameter).getSchema();
            referenceSchema = ((RefModel) schema).getSimpleRef();
            return getReference(referenceSchema);
        } else {
            System.out.println("Get Method");
            getParam.put("parameter",Json.pretty(operationParameters));
            return getParam;
        }
    }

}
