package com.swagger.parser;

import io.swagger.models.*;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.MapProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by bsneha on 16/07/17.
 */
public class SwaggerSchemaResponseParser {

    private final Swagger swagger;
    private static final String SCHEMA_KEY = "schema";

    public SwaggerSchemaResponseParser(Swagger swagger) {
        this.swagger = swagger;
    }

    public HashMap<String, SwaggerResponseSchema> parseResponseForAllPaths() {
        Map<String, Path> paths = swagger.getPaths();
        HashMap<String, SwaggerResponseSchema> responses = new HashMap<>();
        paths.forEach((path, methods) -> {
            Map<HttpMethod, Operation> operationMap = methods.getOperationMap();
            operationMap.forEach(((httpMethod, operation) -> {
                SwaggerResponseSchema response = parseReponseForGivenPathHTTPMethodAndAllResponseType(path, httpMethod);
                responses.put(httpMethod.name() + path, response);
            }));
        });
        return responses;
    }

    public SwaggerResponseSchema parseReponseForGivenPathHTTPMethodAndResponseType(String path, HttpMethod httpMethod, ResponseType responseType) {
        HashMap<String, SwaggerSchema> swaggerStructurePerReponseType = new HashMap<>();
        Path swaggerPath = swagger.getPaths().get(path);
        SwaggerResponseSchema swaggerResponseSchema = new SwaggerResponseSchema();
        if(swaggerPath==null){
            swaggerResponseSchema.setErrorMessage(Constants.INCORRECT_PATH);
            return swaggerResponseSchema;
        }
        Operation httpOperation = swaggerPath.getOperationMap().get(httpMethod);
        if(httpOperation ==null){
            swaggerResponseSchema.setErrorMessage(Constants.INCORRECT_HTTP_MTHHOD);
            return swaggerResponseSchema;
        }
        Map<String, Response> responses =  httpOperation.getResponses();
        if (ResponseType.All == responseType) {
            responses.forEach((responseCode, response) -> {
                SwaggerSchema swaggerStructureForReponse = null;
                try {
                    swaggerStructureForReponse = getSchemaStructureFromResponse(responses.get(responseCode));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                swaggerStructurePerReponseType.put(responseCode, swaggerStructureForReponse);
            });
        } else {
            String responseCode = responseType.getCodeValue();
            SwaggerSchema swaggerStructureForReponse = null;
            try {
                swaggerStructureForReponse = getSchemaStructureFromResponse(responses.get(responseCode));
            } catch (IOException e) {
                e.printStackTrace();
            }
            swaggerStructurePerReponseType.put(responseCode, swaggerStructureForReponse);
        }

        swaggerResponseSchema.setSwaggerStructurePerReponseType(swaggerStructurePerReponseType);
        return swaggerResponseSchema;
    }

    public SwaggerResponseSchema parseReponseForGivenPathHTTPMethodAndAllResponseType(String path, HttpMethod httpMethod) {
        return parseReponseForGivenPathHTTPMethodAndResponseType(path, httpMethod, ResponseType.All);
    }

    private SwaggerSchema getSchemaStructureFromResponse(Response response) throws IOException {
        SwaggerSchema swaggerSchema = new SwaggerSchema();
        swaggerSchema.setDescription(response.getDescription());
        Property responseProperty = response.getSchema();

        if (responseProperty != null) {
            swaggerSchema.setType(responseProperty.getType());
            if (swaggerSchema.getType() == "object" && responseProperty instanceof MapProperty) {
                Property additionalProperties = ((MapProperty) responseProperty).getAdditionalProperties();
                swaggerSchema.setAdditionalPropertiesType(additionalProperties.getType());
            } else {
                String mainScehmaReference = getMainDefinitionNameInResponse(response);
                if (!mainScehmaReference.isEmpty())
                    swaggerSchema.setParsedSchema(new SwaggerParserHelper(swagger).getDefinationsFromSchemaReference(mainScehmaReference, "root"));
            }
        }
        return swaggerSchema;
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
}
