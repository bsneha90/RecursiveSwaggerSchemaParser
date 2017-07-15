package com.thoughtworks.swagger.parser;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.models.*;
import io.swagger.models.parameters.*;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.MapProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;
import io.swagger.parser.SwaggerParser;
import io.swagger.util.Json;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by bsneha on 15/06/17.
 */
public class SwaggerSchemaParser {
    private static final String SCHEMA_KEY = "schema";
    private Swagger swagger;
    private SwaggerParser swaggerParser;
    private HashMap<String, JsonNode> parsedSchema;
    private HashMap<String, HashMap<String, JsonNode>> parsedSchemaPerResponseType = new HashMap<>();
    ObjectMapper mapper = new ObjectMapper();
    SwaggerParserHelper swaggerParserHelper;
    String swaggerJsonUrl;

    public void initializeParser(String swaggerJsonUrl) {
        this.swaggerJsonUrl = swaggerJsonUrl;
        swaggerParser = new SwaggerParser();
        swagger = swaggerParser.read(swaggerJsonUrl);
        swaggerParserHelper= new SwaggerParserHelper(swagger);
    }

    public HashMap<String, HashMap<String, HashMap<String, JsonNode>>> parseResponseForAllPaths() {
        Map<String, Path> paths = swagger.getPaths();
        HashMap<String, HashMap<String, HashMap<String, JsonNode>>> responses = new HashMap<>();
        paths.forEach((path, methods) -> {
            Map<HttpMethod, Operation> operationMap = methods.getOperationMap();
            operationMap.forEach(((httpMethod, operation) -> {
                try {

                    HashMap<String, HashMap<String, JsonNode>> response = parseResponse(path, httpMethod);
                    responses.put(httpMethod.name() + path, response);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }));
        });
        return responses;
    }


    public HashMap<String, JsonNode> parseRequest(String path, HttpMethod httpMethod) {
        return getSwaggerDefinitionsForRequest(path, httpMethod);
    }

    public HashMap<String, HashMap<String, JsonNode>> parseResponse(String path, HttpMethod httpMethod) throws IOException {
        return parseResponse(path, httpMethod, ResponseType.All);
    }

    public HashMap<String, HashMap<String, JsonNode>> parseResponse(String path, HttpMethod httpMethod, ResponseType responseType) throws IOException {
        if (responseType == ResponseType.All) {
            Map<String, Response> swaggerResponsesForGivenPathAndHTTPMethod = getSwaggerResponsesForGivenPathAndHTTPMethod(path, httpMethod);
            swaggerResponsesForGivenPathAndHTTPMethod.forEach((responseStatusCode, response) -> {
                parsedSchemaPerResponseType.put(responseStatusCode, getParsedSchemaForResponse(response));
            });
        } else {
            Response response = getPathResponseForHTTPMethodAndResponseType(path, httpMethod, responseType);
            parsedSchemaPerResponseType.put(responseType.getCodeValue(), getParsedSchemaForResponse(response));
        }
        return parsedSchemaPerResponseType;
    }

    private HashMap<String, JsonNode> getParsedSchemaForResponse(Response response) {
        String mainScehmaReference = null;
        try {
            mainScehmaReference = getMainDefinitionNameInResponse(response);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (mainScehmaReference.isEmpty()) {
            Property responeSchema = response.getSchema();
            HashMap<String, JsonNode> schemaStructure = new HashMap<String, JsonNode>();
            if (responeSchema != null) {
                String type = responeSchema.getType();
                if (type == "object") {
                    Property additionalProperties = ((MapProperty) responeSchema).getAdditionalProperties();
                    schemaStructure.put("object", mapper.createObjectNode().put("additionalProperties", additionalProperties.getType()));
                } else
                    schemaStructure.put(SCHEMA_KEY, mapper.createObjectNode().put(type, ""));
            }
            if (schemaStructure.size() == 0) {
                ObjectNode descriptionNode = mapper.createObjectNode();
                descriptionNode.put("description", response.getDescription());
                schemaStructure.put("description", descriptionNode);
            }
            return schemaStructure;
        } else {
           return swaggerParserHelper.getDefinationsFromSchemaReference(mainScehmaReference,SCHEMA_KEY);
        }
    }

    private HashMap<String, JsonNode> getDefinationSchema(String referenceSchema, String key) {

        Model model = swagger.getDefinitions().get(referenceSchema);
        if (parsedSchema.containsKey(key)) {
            return null;
        }
        try {
            parsedSchema.put(key, mapper.readTree(Json.pretty(model)));
        } catch (IOException e) {
            e.printStackTrace();
        }

        getSchemaFromModel(model);

        return parsedSchema;
    }

    private void getSchemaFromModel(Model model) {
        HashMap<String, Property> referenceSet = getReferenceMapFromModel(model);

        referenceSet.forEach((modelKey, property) -> {
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
                getDefinationSchema(simpleRef, modelKey);
            }

        });
    }

    private HashMap<String, Property> getReferenceMapFromModel(Model model) {
        HashMap<String, Property> referenceSet = new HashMap<>();
        Map<String, Property> modelProperties = model.getProperties();
        if (modelProperties != null) {
            for (Map.Entry<String, Property> map : modelProperties.entrySet()) {
                if ("ref".equals(map.getValue().getType())
                        || "array".equals(map.getValue().getType())) {
                    referenceSet.put(map.getKey(), map.getValue());
                }
            }
        }

        return referenceSet;
    }

    private String getMainDefinitionNameInResponse(Response response) throws IOException {
        Property responseProperty = response.getSchema();
        return getDefinationNameForProperty(responseProperty);
    }

    private String getDefinationNameForProperty(Property responseProperty) {
        if (responseProperty == null) {
            return "";
        }

        String definationName = "";
        if (responseProperty instanceof RefProperty) {
            definationName = ((RefProperty) responseProperty).getSimpleRef();
        } else if (responseProperty instanceof ArrayProperty) {
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

    private HashMap<String, JsonNode> getSwaggerDefinitionsForRequest(String key, HttpMethod httpMethod) {
        parsedSchema = new HashMap<>();
        HashMap<String, String> getParam = new HashMap<>();
        Operation operation = swagger.getPaths().get(key).getOperationMap().get(httpMethod);
        List<Parameter> operationParameters = operation.getParameters();

        operationParameters.forEach(parameter -> {
            String name = parameter.getName();
            Boolean isRequired = parameter.getRequired();
            String parameterIn = parameter.getIn();
            if (parameter instanceof PathParameter || parameter instanceof QueryParameter
                    || parameter instanceof FormParameter || parameter instanceof HeaderParameter || parameter instanceof CookieParameter) {
                String parameterType = ((AbstractSerializableParameter) parameter).getType();
                ObjectNode jsonNodes = mapper.createObjectNode();
                jsonNodes.put("type", parameterType);
                jsonNodes.put("in", parameterIn);
                jsonNodes.put("isRequired", isRequired);
                parsedSchema.put(name, jsonNodes);
            } else if (parameter instanceof RefParameter) {
                RefParameter qp = ((RefParameter) parameter);
                getDefinationSchema(qp.getSimpleRef(), name);
            } else if (parameter instanceof BodyParameter) {
                BodyParameter qp = ((BodyParameter) parameter);
                Model schema = qp.getSchema();
                String simpleRef;
                if (schema instanceof RefModel) {
                    simpleRef = ((RefModel) schema).getSimpleRef();
                } else {
                    Property items = ((ArrayModel) schema).getItems();
                    if (items instanceof RefProperty) {
                        simpleRef = ((RefProperty) items).getSimpleRef();
                    } else {
                        return;
                    }
                }
                if (simpleRef != null) {
                    getDefinationSchema(simpleRef, name);
                }
            }
        });

        return parsedSchema;
    }


}
