package com.thoughtworks.swagger.parser;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.deploy.util.StringUtils;
import io.swagger.models.*;
import io.swagger.models.parameters.*;
import io.swagger.models.properties.*;

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
                responses.put(httpMethod.name() + path, parseRequest(path, httpMethod));
            }));
        });
        return responses;
    }

    public SwaggerRequestSchema parseRequest(String path, HttpMethod httpMethod) {
        return getSwaggerDefinitionsForRequest(path, httpMethod);
    }

    private SwaggerRequestSchema getSwaggerDefinitionsForRequest(String key, HttpMethod httpMethod) {
        SwaggerRequestSchema swaggerRequestSchema = new SwaggerRequestSchema();

        Path path = swagger.getPaths().get(key);
        if (path == null) {
            swaggerRequestSchema.setErrorMessage(Constants.INCORRECT_PATH);
            return swaggerRequestSchema;
        }
        Operation operation = path.getOperationMap().get(httpMethod);
        if(operation == null){
            swaggerRequestSchema.setErrorMessage(Constants.INCORRECT_HTTP_MTHHOD);
            return swaggerRequestSchema;
        }
        List<Parameter> operationParameters = operation.getParameters();

        operationParameters.forEach(parameter -> {
            SwaggerSchema swaggerSchema = new SwaggerSchema();
            RequestParameter swaggerRequestParameter = new RequestParameter();
            Property items = null;
            String name = parameter.getName();
            Boolean isRequired = parameter.getRequired();
            String parameterIn = parameter.getIn();
            swaggerRequestParameter.setParameterIn(parameterIn);
            swaggerRequestParameter.setIsRequired(isRequired);
            swaggerRequestParameter.setName(name);
            if (parameter instanceof AbstractSerializableParameter) {
                swaggerRequestParameter.setEnumValues(((AbstractSerializableParameter) parameter).getEnumValue());
                items = ((AbstractSerializableParameter) parameter).getItems();
                if (items != null)
                    swaggerSchema.setItemsType(items.getType());
            }
            if (parameter instanceof QueryParameter) {

                String parameterType = ((QueryParameter) parameter).getType();
                String enumDefination = "";
                String emunType = "";
                ObjectMapper mapper = new ObjectMapper();
                if (items instanceof StringProperty) {
                    List<String> enumValues = ((StringProperty) items).getEnum();
                    if (enumValues != null) {
                        enumDefination = StringUtils.join(enumValues, ",");
                        emunType = "string";
                    }
                } else if (items instanceof IntegerProperty) {
                    List<Integer> enumValues = ((IntegerProperty) items).getEnum();
                    if (enumValues != null) {
                        enumDefination = StringUtils.join(enumValues, ",");
                        emunType = "integer";
                    }

                } else if (items instanceof LongProperty) {
                    List<Long> enumValues = ((LongProperty) items).getEnum();
                    if (enumValues != null) {
                        enumDefination = StringUtils.join(enumValues, ",");
                        emunType = "long";
                    }

                } else if (items instanceof FloatProperty) {
                    List<Float> enumValues = ((FloatProperty) items).getEnum();
                    if (enumValues != null) {
                        enumDefination = StringUtils.join(enumValues, ",");
                        emunType = "float";
                    }

                } else if (items instanceof DoubleProperty) {
                    List<Double> enumValues = ((DoubleProperty) items).getEnum();
                    if (enumValues != null) {
                        enumDefination = StringUtils.join(enumValues, ",");
                        emunType = "double";
                    }

                }
                swaggerSchema.setEnumValues(enumDefination);

                swaggerSchema.setType(parameterType);

            } else if (parameter instanceof PathParameter || parameter instanceof QueryParameter
                    || parameter instanceof FormParameter || parameter instanceof HeaderParameter || parameter instanceof CookieParameter) {
                String parameterType = ((AbstractSerializableParameter) parameter).getType();
                {
                    swaggerSchema.setType(parameterType);
                }
            } else if (parameter instanceof RefParameter) {
                swaggerSchema.setType("ref");
                RefParameter qp = ((RefParameter) parameter);
                swaggerSchema.setParsedSchema(new SwaggerParserHelper(swagger).getDefinationsFromSchemaReference(qp.getSimpleRef(), "root"));
            } else if (parameter instanceof BodyParameter) {
                BodyParameter qp = ((BodyParameter) parameter);
                Model schema = qp.getSchema();
                String simpleRef = "";
                if (schema instanceof RefModel) {
                    swaggerSchema.setType("ref");
                    simpleRef = ((RefModel) schema).getSimpleRef();
                } else {
                    Property refItems = ((ArrayModel) schema).getItems();
                    swaggerSchema.setType("array");
                    if (refItems != null) {
                        swaggerSchema.setItemsType(refItems.getType());
                        if (refItems instanceof RefProperty) {
                            simpleRef = ((RefProperty) refItems).getSimpleRef();
                        }
                    }
                }
                if (simpleRef != null && !simpleRef.isEmpty()) {
                    swaggerSchema.setParsedSchema(new SwaggerParserHelper(swagger).getDefinationsFromSchemaReference(simpleRef, "root"));
                }
            }
            swaggerRequestParameter.setSwaggerSchema(swaggerSchema);
            swaggerRequestSchema.getParameters().add(swaggerRequestParameter);
        });

        return swaggerRequestSchema;
    }
}
