package com.thoughtworks.swagger.parser;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
    private HashMap<String, JsonNode> parsedSchema;
    private HashMap<String, HashMap<String, JsonNode>> parsedSchemaPerResponseType = new HashMap<>();
    ObjectMapper mapper = new ObjectMapper();
    String swaggerJsonUrl;

    public void initializeParser(String swaggerJsonUrl) {
        this.swaggerJsonUrl = swaggerJsonUrl;
        swaggerParser = new SwaggerParser();
        swagger = swaggerParser.read(swaggerJsonUrl);
    }

    public HashMap<String, JsonNode> parseRequest(String path) {
        return getSwaggerDefinitionsForRequest(path, swaggerJsonUrl);
    }

    public HashMap<String, HashMap<String, JsonNode>> parseResponse(String path, HttpMethod httpMethod) throws IOException {
        return parseResponse(path, httpMethod, ResponseType.All);
    }

    public HashMap<String, HashMap<String, JsonNode>> parseResponse(String path, HttpMethod httpMethod, ResponseType responseType) throws IOException {
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

    private HashMap<String, JsonNode> getParsedSchemaForResponse(Response response) {
        String mainDefinationName = null;
        try {
            mainDefinationName = getMainDefinitionNameInResponse(response);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (mainDefinationName == null) {
            HashMap<String, JsonNode> description = new HashMap<String, JsonNode>();
            ObjectNode descriptionNode = mapper.createObjectNode();
            descriptionNode.put("description", response.getDescription());
            description.put("description", descriptionNode);
            return description;
        } else {
            parsedSchema = new HashMap<>();
            return getDefinationSchema(mainDefinationName,"Root");
        }
    }
    private HashMap<String, JsonNode> getDefinationSchema(String referenceSchema, String key) {

        Model model = swagger.getDefinitions().get(referenceSchema);
        if (parsedSchema.containsKey(referenceSchema)) {
            return null;
        }
        try {
            parsedSchema.put(key, mapper.readTree(Json.pretty(model)));
        } catch (IOException e) {
            e.printStackTrace();
        }

        HashMap<String,Property> referenceSet = new HashMap<>();
        Map<String, Property> modelProperties = model.getProperties();
        if (modelProperties != null) {
            for (Map.Entry<String, Property> map : modelProperties.entrySet()) {
                if ("ref".equals(map.getValue().getType())
                        || "array".equals(map.getValue().getType())) {
                    referenceSet.put(map.getKey(),map.getValue());
                }
            }
        }
        referenceSet.forEach((modelKey,property )-> {
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
                getDefinationSchema(simpleRef,modelKey);
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

    private HashMap<String, JsonNode> getSwaggerDefinitionsForRequest(String key, String swaggerJsonUrl) {
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
            return getDefinationSchema(referenceSchema,"");
        } else {
            System.out.println("Get Method");
            getParam.put("", Json.pretty(operationParameters));
            return null;
        }
    }



}
