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
import java.util.*;

/**
 * Created by bsneha on 15/06/17.
 */
public class SwaggerSchemaParser {
    private Swagger swagger;
    private SwaggerParser swaggerParser;
    private HashMap<String, String> parsedSchema;
    private HashMap<String, HashMap<String, String>> parsedSchemaPerResponseType = new HashMap<>();

    String swaggerJsonUrl;

    public void initializeParser(String swaggerJsonUrl) {
        this.swaggerJsonUrl = swaggerJsonUrl;
        swaggerParser = new SwaggerParser();
        swagger = swaggerParser.read(swaggerJsonUrl);
    }

    public HashMap<String, String> parseRequest(String path) {
        return getSwaggerDefinitionsForRequest(path, swaggerJsonUrl);
    }

    public HashMap<String, HashMap<String, String>> parseResponse(String path, HttpMethod httpMethod) throws IOException {
        return parseResponse(path, httpMethod, ResponseType.All);
    }

    public HashMap<String, HashMap<String, String>> parseResponse(String path, HttpMethod httpMethod, ResponseType responseType) throws IOException {
        if (responseType == ResponseType.All) {
            Map<String, Response> swaggerResponsesForGivenPathAndHTTPMethod = getSwaggerResponsesForGivenPathAndHTTPMethod(path, httpMethod);
            swaggerResponsesForGivenPathAndHTTPMethod.forEach((responseStatusCode, response) -> {
                    parsedSchemaPerResponseType.put(responseStatusCode,getParsedSchemaForResponse(response));
            });
        } else {
            Response response = getPathResponseForHTTPMethodAndResponseType(path, httpMethod, responseType);
            parsedSchemaPerResponseType.put(responseType.getCodeValue(),getParsedSchemaForResponse(response));
        }
        return parsedSchemaPerResponseType;
    }

    private HashMap<String, String> getParsedSchemaForResponse(Response response) {
        String mainDefinationName = null;
        try {
            mainDefinationName = getMainDefinitionNameInResponse(response);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (mainDefinationName == null) {
            HashMap<String, String> description = new HashMap<String, String>();
            description.put("Description", response.getDescription());
            return description;
        } else {
            parsedSchema = new HashMap<>();
            return getDefinationSchema(mainDefinationName);
        }
    }

    private HashMap<String, String> getDefinationSchema(String referenceSchema) {

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
                getDefinationSchema(simpleRef);
            }

        });

        return parsedSchema;
    }

    private String getMainDefinitionNameInResponse(Response response) throws IOException {
        Property responseProperty = response.getSchema();
        if (responseProperty == null) {
            return null;
        }

        String definationName;
        if (responseProperty instanceof RefProperty) {
            definationName = ((RefProperty) responseProperty).getSimpleRef();
        } else {
            Property items = ((ArrayProperty) responseProperty).getItems();
            definationName = ((RefProperty) items).getSimpleRef();
        }

        return definationName;
    }

    private Response getPathResponseForHTTPMethodAndResponseType(String path, HttpMethod httpMethod, ResponseType responseType) {
        Operation httpOperation = swagger.getPaths().get(path).getOperationMap().get(httpMethod);
        return httpOperation.getResponses().get(responseType.getCodeValue());
    }

    private Map<String, Response> getSwaggerResponsesForGivenPathAndHTTPMethod(String key, HttpMethod httpMethod) {
        Operation operation = swagger.getPaths().get(key).getOperationMap().get(httpMethod);
        return operation.getResponses();
    }

    private HashMap<String, String> getSwaggerDefinitionsForRequest(String key, String swaggerJsonUrl) {
        HashMap<String, String> getParam = new HashMap<>();

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
            return getDefinationSchema(referenceSchema);
        } else {
            System.out.println("Get Method");
            getParam.put("parameter", Json.pretty(operationParameters));
            return getParam;
        }
    }

}
