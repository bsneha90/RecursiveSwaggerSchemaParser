package com.swagger.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.models.HttpMethod;
import io.swagger.models.Swagger;
import io.swagger.parser.SwaggerParser;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Set;

/**
 * Created by bsneha on 20/07/17.
 */
public class SwaggerSchemaResponseParserTest {
    private SwaggerSchemaResponseParser swaggerSchemaParser;
    private Swagger swagger;
    JsonNode ExpectedTag;
    JsonNode ExpectedCatergory;
    JsonNode ExpectedPet;

    @Before
    public void setUp() throws IOException, ParseException {
        ObjectMapper mapper = new ObjectMapper();

        swagger = new SwaggerParser().read("./src/test/resources/sampleJson");

        swaggerSchemaParser = new SwaggerSchemaResponseParser(swagger);
        ExpectedTag = mapper.readTree("{\n  \"type\" : \"object\",\n  \"properties\" : {\n    \"id\" : {\n      \"type\" : \"integer\",\n      \"format\" : \"int64\"\n    },\n    \"name\" : {\n      \"type\" : \"string\"\n    }\n  },\n  \"xml\" : {\n    \"name\" : \"Tag\"\n  }\n}");
        ExpectedCatergory = mapper.readTree("{\n  \"type\" : \"object\",\n  \"properties\" : {\n    \"id\" : {\n      \"type\" : \"integer\",\n      \"format\" : \"int64\"\n    },\n    \"name\" : {\n      \"type\" : \"string\"\n    }\n  },\n  \"xml\" : {\n    \"name\" : \"Category\"\n  }\n}");
        ExpectedPet = mapper.readTree("{\n  \"type\" : \"object\",\n  \"required\" : [ \"name\", \"photoUrls\" ],\n  \"properties\" : {\n    \"id\" : {\n      \"type\" : \"integer\",\n      \"format\" : \"int64\"\n    },\n    \"category\" : {\n      \"$ref\" : \"#/definitions/Category\"\n    },\n    \"name\" : {\n      \"type\" : \"string\",\n      \"example\" : \"doggie\"\n    },\n    \"photoUrls\" : {\n      \"type\" : \"array\",\n      \"xml\" : {\n        \"name\" : \"photoUrl\",\n        \"wrapped\" : true\n      },\n      \"items\" : {\n        \"type\" : \"string\"\n      }\n    },\n    \"tags\" : {\n      \"type\" : \"array\",\n      \"xml\" : {\n        \"name\" : \"tag\",\n        \"wrapped\" : true\n      },\n      \"items\" : {\n        \"$ref\" : \"#/definitions/Tag\"\n      }\n    },\n    \"status\" : {\n      \"type\" : \"string\",\n      \"description\" : \"pet status in the store\",\n      \"enum\" : [ \"available\", \"pending\", \"sold\" ]\n    }\n  },\n  \"xml\" : {\n    \"name\" : \"Pet\"\n  }\n}");
    }

    @Test
    public void shouldBeAbleToParseResponseForGivenHTTPMethodAndAllResponseTypes() throws IOException {
        HashMap<String, SwaggerSchema> parsedResponse = swaggerSchemaParser.parseReponseForGivenPathHTTPMethodAndAllResponseType("/pet/findByStatus", HttpMethod.GET).getSwaggerStructurePerReponseType();
        Assert.assertEquals(parsedResponse.size(), 2);
        Assert.assertNotNull(parsedResponse.get(ResponseType.OK.getCodeValue()));
        Assert.assertNotNull(parsedResponse.get(ResponseType.BAD_REQUEST.getCodeValue()));
    }

    @Test
    public void shouldBeAbleToParseResponseForGivenHTTPMethodAndResponseTypes() throws IOException {
        HashMap<String, SwaggerSchema> parsedResponse = swaggerSchemaParser.parseReponseForGivenPathHTTPMethodAndResponseType("/pet/findByStatus", HttpMethod.GET, ResponseType.OK).getSwaggerStructurePerReponseType();
        Assert.assertEquals(parsedResponse.size(), 1);
        Assert.assertNotNull(parsedResponse.get(ResponseType.OK.getCodeValue()));
    }

    @Test
    public void ShouldBeAbleToParseAllPathsWithAllHTTPMethods() {
        HashMap<String, SwaggerResponseSchema> parsedResponse = swaggerSchemaParser.parseResponseForAllPaths();
        Set<String> parsedPaths = parsedResponse.keySet();
        String paths = "GET/store/order/{orderId},DELETE/store/order/{orderId},DELETE/user/{username},POST/user/createWithArray,PUT/user/{username},POST/user/createWithList,PUT/pet,GET/store/inventory,GET/user/{username},POST/pet/{petId},POST/pet/{petId}/uploadImage,POST/store/order,POST/pet,POST/user,GET/pet/{petId},GET/pet/findByStatus,GET/user/logout,DELETE/pet/{petId},GET/pet/findByTags,GET/user/login";
        Assert.assertEquals(parsedResponse.size(), 20);
        Assert.assertEquals(paths, StringUtils.join(parsedPaths, ","));
    }

    @Test
    public void shouldBeAbleToParseResponseWithSchemaAsSimpleRef() {
        HashMap<String, SwaggerSchema> parsedResponse = swaggerSchemaParser.parseReponseForGivenPathHTTPMethodAndResponseType("/pet/{petId}", HttpMethod.GET, ResponseType.OK).getSwaggerStructurePerReponseType();
        Assert.assertEquals(parsedResponse.size(), 1);
        SwaggerSchema swaggerSchema = parsedResponse.get(ResponseType.OK.getCodeValue());
        Assert.assertNotNull(swaggerSchema);
        HashMap<String, JsonNode> parsedSchema = swaggerSchema.getParsedSchema();
        Assert.assertNotNull(parsedSchema);
        Assert.assertEquals(swaggerSchema.getType(), "ref");
        Assert.assertEquals(swaggerSchema.getDescription(), "successful operation");
        Assert.assertEquals(parsedSchema.size(), 3);
        Assert.assertEquals(parsedSchema.get("root"), ExpectedPet);
        Assert.assertEquals(parsedSchema.get("category"), ExpectedCatergory);
        Assert.assertEquals(parsedSchema.get("tags"), ExpectedTag);

    }

    @Test
    public void shouldBeAbleToParseResponseWithSchemaAsArrayRef() {
        HashMap<String, SwaggerSchema> parsedResponse = swaggerSchemaParser.parseReponseForGivenPathHTTPMethodAndResponseType("/pet/findByStatus", HttpMethod.GET, ResponseType.OK).getSwaggerStructurePerReponseType();
        Assert.assertEquals(parsedResponse.size(), 1);
        SwaggerSchema swaggerSchema = parsedResponse.get(ResponseType.OK.getCodeValue());
        Assert.assertNotNull(swaggerSchema);
        HashMap<String, JsonNode> parsedSchema = swaggerSchema.getParsedSchema();
        Assert.assertNotNull(parsedSchema);
        Assert.assertEquals(swaggerSchema.getType(), "array");
        Assert.assertEquals(swaggerSchema.getDescription(), "successful operation");
        Assert.assertEquals(parsedSchema.size(), 3);
        Assert.assertEquals(parsedSchema.get("root"), ExpectedPet);
        Assert.assertEquals(parsedSchema.get("category"), ExpectedCatergory);
        Assert.assertEquals(parsedSchema.get("tags"), ExpectedTag);

    }

    @Test
    public void shouldBeAbleToParseResponseWithSchemaContainingOnlyDescription() {
        HashMap<String, SwaggerSchema> parsedResponse = swaggerSchemaParser.parseReponseForGivenPathHTTPMethodAndResponseType("/pet/findByStatus", HttpMethod.GET, ResponseType.BAD_REQUEST).getSwaggerStructurePerReponseType();
        Assert.assertEquals(parsedResponse.size(), 1);
        SwaggerSchema swaggerSchema = parsedResponse.get(ResponseType.BAD_REQUEST.getCodeValue());
        Assert.assertNotNull(swaggerSchema);
        HashMap<String, JsonNode> parsedSchema = swaggerSchema.getParsedSchema();
        Assert.assertNull(parsedSchema);
        Assert.assertEquals(swaggerSchema.getDescription(), "Invalid status value");

    }

    @Test
    public void shouldBeAbleToParseResponseWithSchemaAsOnlySimpleType() {
        HashMap<String, SwaggerSchema> parsedResponse = swaggerSchemaParser.parseReponseForGivenPathHTTPMethodAndResponseType("/user/login", HttpMethod.GET, ResponseType.OK).getSwaggerStructurePerReponseType();
        Assert.assertEquals(parsedResponse.size(), 1);
        SwaggerSchema swaggerSchema = parsedResponse.get(ResponseType.OK.getCodeValue());
        Assert.assertNotNull(swaggerSchema);
        HashMap<String, JsonNode> parsedSchema = swaggerSchema.getParsedSchema();
        Assert.assertNull(parsedSchema);
        Assert.assertEquals(swaggerSchema.getType(), "string");

    }

    @Test
    public void shouldBeAbleToParseResponseWithSchemaAsObjectTypeWithAdditionalProperties() {
        HashMap<String, SwaggerSchema> parsedResponse = swaggerSchemaParser.parseReponseForGivenPathHTTPMethodAndResponseType("/store/inventory", HttpMethod.GET, ResponseType.OK).getSwaggerStructurePerReponseType();
        Assert.assertEquals(parsedResponse.size(), 1);
        SwaggerSchema swaggerSchema = parsedResponse.get(ResponseType.OK.getCodeValue());
        Assert.assertNotNull(swaggerSchema);
        HashMap<String, JsonNode> parsedSchema = swaggerSchema.getParsedSchema();
        Assert.assertNull(parsedSchema);
        Assert.assertEquals(swaggerSchema.getType(), "object");
        Assert.assertEquals(swaggerSchema.getAdditionalPropertiesType(), "integer");
    }
    @Test
    public void shouldReturnIncorrectPathMessageWhenPathNotFound() {
       SwaggerResponseSchema swaggerResponseSchema= swaggerSchemaParser.parseReponseForGivenPathHTTPMethodAndAllResponseType("/not/correct",HttpMethod.GET);
        Assert.assertEquals(swaggerResponseSchema.getErrorMessage(),Constants.INCORRECT_PATH);

    }

    @Test
    public void shouldReturnIncorrectHTTPMethodMessageWhenPathIsFoundButHTTPMethodIsIncorrecr() {
        SwaggerResponseSchema parsedRequest = swaggerSchemaParser.parseReponseForGivenPathHTTPMethodAndAllResponseType("/user/createWithList",HttpMethod.GET);
        Assert.assertEquals(parsedRequest.getErrorMessage(),Constants.INCORRECT_HTTP_MTHHOD);

    }
}
