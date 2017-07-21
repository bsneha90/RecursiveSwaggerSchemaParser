package com.thoughtworks.swagger.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.models.*;
import io.swagger.models.parameters.*;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;
import io.swagger.models.properties.StringProperty;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by bsneha on 16/07/17.
 */
public class SwaggerSchemaRequestParser {

    private final Swagger swagger;

    public SwaggerSchemaRequestParser(Swagger swagger) {
        this.swagger = swagger;
    }

    public HashMap<String, SwaggerRequestSchema> parseRequestForAllPaths() {
        Map<String, Path> paths = swagger.getPaths();
        HashMap<String, SwaggerRequestSchema> responses = new HashMap<>();
        paths.forEach((path, methods) -> {
            Map<HttpMethod, Operation> operationMap = methods.getOperationMap();
            operationMap.forEach(((httpMethod, operation) -> {
                responses.put(httpMethod.name() + path, parseRequest(path,httpMethod));
            }));
        });
        return responses;
    }

    public SwaggerRequestSchema parseRequest(String path, HttpMethod httpMethod) {
        return getSwaggerDefinitionsForRequest(path, httpMethod);
    }

    private SwaggerRequestSchema getSwaggerDefinitionsForRequest(String key, HttpMethod httpMethod) {
        SwaggerRequestSchema swaggerRequestSchema = new SwaggerRequestSchema();

        SwaggerSchema swaggerSchema = new SwaggerSchema();

        Operation operation = swagger.getPaths().get(key).getOperationMap().get(httpMethod);
        List<Parameter> operationParameters = operation.getParameters();

        operationParameters.forEach(parameter -> {
            RequestParameter swaggerRequestParameter = new RequestParameter();
            String name = parameter.getName();
            Boolean isRequired = parameter.getRequired();
            String parameterIn = parameter.getIn();
            swaggerRequestParameter.setParameterIn(parameterIn);
            swaggerRequestParameter.setIsRequired(isRequired);
            swaggerRequestParameter.setName(name);

            if (parameter instanceof QueryParameter) {
                Property items = ((QueryParameter) parameter).getItems();
                String itemsType= ((QueryParameter) parameter).getType();
                if (items instanceof StringProperty) {
                    HashMap<String, JsonNode> parsedSchema = new HashMap<>();
                    ObjectMapper mapper = new ObjectMapper();
                    ObjectNode jNode = mapper.createObjectNode();
                    List<String> itemsEnumValues = ((StringProperty) items).getEnum();
                    if(itemsEnumValues!=null && itemsEnumValues.size()>0) {
                        jNode.putPOJO("enum", itemsEnumValues );
                    }
                    parsedSchema.put(itemsType,jNode);
                    swaggerSchema.setParsedSchema(parsedSchema);
                }
                swaggerSchema.setType(itemsType);

            } else if (parameter instanceof PathParameter || parameter instanceof QueryParameter
                    || parameter instanceof FormParameter || parameter instanceof HeaderParameter || parameter instanceof CookieParameter) {
                String parameterType = ((AbstractSerializableParameter) parameter).getType();
                {
                    swaggerSchema.setType(parameterType);
                }
            } else if (parameter instanceof RefParameter) {
                swaggerSchema.setType("ref");
                RefParameter qp = ((RefParameter) parameter);
                swaggerSchema.setParsedSchema(new SwaggerParserHelper(swagger).getDefinationsFromSchemaReference(qp.getSimpleRef(), name));
            } else if (parameter instanceof BodyParameter) {
                BodyParameter qp = ((BodyParameter) parameter);
                Model schema = qp.getSchema();
                String simpleRef;
                if (schema instanceof RefModel) {
                    swaggerSchema.setType("ref");
                    simpleRef = ((RefModel) schema).getSimpleRef();
                } else {
                    Property items = ((ArrayModel) schema).getItems();
                    swaggerSchema.setType("array");
                    if (items instanceof RefProperty) {
                        simpleRef = ((RefProperty) items).getSimpleRef();
                    } else {
                        return;
                    }
                }
                if (simpleRef != null) {
                    swaggerSchema.setParsedSchema(new SwaggerParserHelper(swagger).getDefinationsFromSchemaReference(simpleRef, name));
                }
            }
            swaggerRequestParameter.setSwaggerSchema(swaggerSchema);
            swaggerRequestSchema.getParameters().add(swaggerRequestParameter);
        });

        return swaggerRequestSchema;
    }
}
