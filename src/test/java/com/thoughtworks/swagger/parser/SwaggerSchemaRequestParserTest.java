package com.thoughtworks.swagger.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.deploy.util.StringUtils;
import io.swagger.models.HttpMethod;
import io.swagger.models.Swagger;
import io.swagger.parser.SwaggerParser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 * Created by bsneha on 21/07/17.
 */
public class SwaggerSchemaRequestParserTest {
    private SwaggerSchemaRequestParser swaggerSchemaParser;
    private Swagger swagger;
    JsonNode ExpectedTag;
    JsonNode ExpectedCatergory;
    JsonNode ExpectedPet;
    JsonNode User;
    @Before
    public void setUp() throws IOException, ParseException {
        ObjectMapper mapper = new ObjectMapper();
        swagger = new SwaggerParser().read("./src/test/resources/sampleJson");

        swaggerSchemaParser = new SwaggerSchemaRequestParser(swagger);
        User= mapper.readTree("{\"type\":\"object\",\"properties\":{\"id\":{\"type\":\"integer\",\"format\":\"int64\"},\"username\":{\"type\":\"string\"},\"firstName\":{\"type\":\"string\"},\"lastName\":{\"type\":\"string\"},\"email\":{\"type\":\"string\"},\"password\":{\"type\":\"string\"},\"phone\":{\"type\":\"string\"},\"userStatus\":{\"type\":\"integer\",\"format\":\"int32\",\"description\":\"User Status\"}},\"xml\":{\"name\":\"User\"}}");
        ExpectedTag = mapper.readTree("{\n  \"type\" : \"object\",\n  \"properties\" : {\n    \"id\" : {\n      \"type\" : \"integer\",\n      \"format\" : \"int64\"\n    },\n    \"name\" : {\n      \"type\" : \"string\"\n    }\n  },\n  \"xml\" : {\n    \"name\" : \"Tag\"\n  }\n}");
        ExpectedCatergory = mapper.readTree("{\n  \"type\" : \"object\",\n  \"properties\" : {\n    \"id\" : {\n      \"type\" : \"integer\",\n      \"format\" : \"int64\"\n    },\n    \"name\" : {\n      \"type\" : \"string\"\n    }\n  },\n  \"xml\" : {\n    \"name\" : \"Category\"\n  }\n}");
        ExpectedPet = mapper.readTree("{\n  \"type\" : \"object\",\n  \"required\" : [ \"name\", \"photoUrls\" ],\n  \"properties\" : {\n    \"id\" : {\n      \"type\" : \"integer\",\n      \"format\" : \"int64\"\n    },\n    \"category\" : {\n      \"$ref\" : \"#/definitions/Category\"\n    },\n    \"name\" : {\n      \"type\" : \"string\",\n      \"example\" : \"doggie\"\n    },\n    \"photoUrls\" : {\n      \"type\" : \"array\",\n      \"xml\" : {\n        \"name\" : \"photoUrl\",\n        \"wrapped\" : true\n      },\n      \"items\" : {\n        \"type\" : \"string\"\n      }\n    },\n    \"tags\" : {\n      \"type\" : \"array\",\n      \"xml\" : {\n        \"name\" : \"tag\",\n        \"wrapped\" : true\n      },\n      \"items\" : {\n        \"$ref\" : \"#/definitions/Tag\"\n      }\n    },\n    \"status\" : {\n      \"type\" : \"string\",\n      \"description\" : \"pet status in the store\",\n      \"enum\" : [ \"available\", \"pending\", \"sold\" ]\n    }\n  },\n  \"xml\" : {\n    \"name\" : \"Pet\"\n  }\n}");
    }

    @Test
    public void shouldBeAbleToParseRequestForAGivenPathAndHTTPMethod() {
        SwaggerRequestSchema parsedRequest = swaggerSchemaParser.parseRequest("/pet/findByStatus", HttpMethod.GET);
        Assert.assertEquals(parsedRequest.getParameters().size(), 1);
        RequestParameter requestParameter = parsedRequest.getParameters().get(0);
        Assert.assertNotNull(requestParameter.getSwaggerSchema());
        Assert.assertNotNull(requestParameter.getIsRequired());
        Assert.assertNotNull(requestParameter.getName());
        Assert.assertNotNull(requestParameter.getParameterIn());
    }

    @Test
    public void shouldBeAbleToParseRequestForAllPaths() {
        HashMap<String, SwaggerRequestSchema> parsedRequest = swaggerSchemaParser.parseRequestForAllPaths();
        Set<String> parsedPaths = parsedRequest.keySet();
        String paths = "GET/store/order/{orderId},DELETE/store/order/{orderId},DELETE/user/{username},POST/user/createWithArray,PUT/user/{username},POST/user/createWithList,PUT/pet,GET/store/inventory,GET/user/{username},POST/pet/{petId},POST/pet/{petId}/uploadImage,POST/store/order,POST/pet,POST/user,GET/pet/{petId},GET/pet/findByStatus,GET/user/logout,DELETE/pet/{petId},GET/pet/findByTags,GET/user/login";
        Assert.assertNotNull(parsedRequest);
        Assert.assertEquals(parsedRequest.size(), 20);
        Assert.assertEquals(paths, StringUtils.join(parsedPaths, ","));
    }

    @Test
    public void ShouldBeAbleToParseRequestWithParamtersEmpty() {
        SwaggerRequestSchema parsedRequest = swaggerSchemaParser.parseRequest("/store/inventory", HttpMethod.GET);
        Assert.assertEquals(parsedRequest.getParameters().size(), 0);
    }

    @Test
    public void ShouldBeAbleToParseRequestWithParamtersInQueryString() {
        SwaggerRequestSchema parsedRequest = swaggerSchemaParser.parseRequest("/user/login", HttpMethod.GET);
        ArrayList<RequestParameter> requestParameters = parsedRequest.getParameters();
        Assert.assertEquals(requestParameters.size(), 2);
        Assert.assertEquals(requestParameters.get(0).getParameterIn(), "query");
        Assert.assertEquals(requestParameters.get(0).getName(), "username");
        Assert.assertEquals(requestParameters.get(0).getIsRequired(), true);
        Assert.assertNotNull(requestParameters.get(0).getSwaggerSchema());
        Assert.assertEquals(requestParameters.get(0).getSwaggerSchema().getType(), "string");

        Assert.assertEquals(requestParameters.get(1).getParameterIn(), "query");
        Assert.assertEquals(requestParameters.get(1).getName(), "password");
        Assert.assertEquals(requestParameters.get(1).getIsRequired(), true);
        Assert.assertNotNull(requestParameters.get(1).getSwaggerSchema());
        Assert.assertEquals(requestParameters.get(1).getSwaggerSchema().getType(), "string");

    }

    @Test
    public void ShouldBeAbleToParseRequestWithParamtersInFormData() {
        SwaggerRequestSchema parsedRequest = swaggerSchemaParser.parseRequest("/pet/{petId}/uploadImage", HttpMethod.POST);
        ArrayList<RequestParameter> requestParameters = parsedRequest.getParameters();
        Assert.assertEquals(requestParameters.size(), 3);
        Assert.assertEquals(requestParameters.get(1).getParameterIn(), "formData");
        Assert.assertEquals(requestParameters.get(1).getName(), "additionalMetadata");
        Assert.assertEquals(requestParameters.get(1).getIsRequired(), false);
        Assert.assertNotNull(requestParameters.get(1).getSwaggerSchema());
        Assert.assertEquals(requestParameters.get(1).getSwaggerSchema().getType(), "string");

        Assert.assertEquals(requestParameters.get(2).getParameterIn(), "formData");
        Assert.assertEquals(requestParameters.get(2).getName(), "file");
        Assert.assertEquals(requestParameters.get(2).getIsRequired(), false);
        Assert.assertNotNull(requestParameters.get(2).getSwaggerSchema());
        Assert.assertEquals(requestParameters.get(2).getSwaggerSchema().getType(), "file");

    }

    @Test
    public void ShouldBeAbleToParseRequestWithParamtersInPath() {
        SwaggerRequestSchema parsedRequest = swaggerSchemaParser.parseRequest("/store/order/{orderId}", HttpMethod.GET);
        ArrayList<RequestParameter> requestParameters = parsedRequest.getParameters();
        Assert.assertEquals(requestParameters.size(), 1);
        Assert.assertEquals(requestParameters.get(0).getParameterIn(), "path");
        Assert.assertEquals(requestParameters.get(0).getName(), "orderId");
        Assert.assertEquals(requestParameters.get(0).getIsRequired(), true);
        Assert.assertNotNull(requestParameters.get(0).getSwaggerSchema());
        Assert.assertEquals(requestParameters.get(0).getSwaggerSchema().getType(), "integer");
    }

    @Test
    public void ShouldBeAbleToParseRequestWithParamterInHeader() {
        SwaggerRequestSchema parsedRequest = swaggerSchemaParser.parseRequest("/pet/{petId}", HttpMethod.DELETE);
        ArrayList<RequestParameter> requestParameters = parsedRequest.getParameters();
        Assert.assertEquals(requestParameters.size(), 2);
        Assert.assertEquals(requestParameters.get(0).getParameterIn(), "header");
        Assert.assertEquals(requestParameters.get(0).getName(), "api_key");
        Assert.assertEquals(requestParameters.get(0).getIsRequired(), false);
        Assert.assertNotNull(requestParameters.get(0).getSwaggerSchema());
        Assert.assertEquals(requestParameters.get(0).getSwaggerSchema().getType(), "string");
    }

    @Test
    public void ShouldBeAbleToParseRequestWithParamtersInBody() {
        SwaggerRequestSchema parsedRequest = swaggerSchemaParser.parseRequest("/pet", HttpMethod.POST);
        ArrayList<RequestParameter> requestParameters = parsedRequest.getParameters();
        Assert.assertEquals(requestParameters.size(), 1);
        Assert.assertEquals(requestParameters.get(0).getParameterIn(), "body");
        Assert.assertEquals(requestParameters.get(0).getName(), "body");
        Assert.assertEquals(requestParameters.get(0).getIsRequired(), true);
        SwaggerSchema swaggerSchema = requestParameters.get(0).getSwaggerSchema();
        Assert.assertNotNull(swaggerSchema);
        Assert.assertEquals(swaggerSchema.getType(), "ref");
        HashMap<String, JsonNode> parsedSchema = swaggerSchema.getParsedSchema();
        Assert.assertEquals(parsedSchema.size(), 3);
        Assert.assertEquals(ExpectedPet, parsedSchema.get("root"));
        Assert.assertEquals(ExpectedCatergory, parsedSchema.get("category"));
        Assert.assertEquals(ExpectedTag, parsedSchema.get("tags"));
    }

    @Test
    public void ShouldBeAbleToParseRequestWithParamtersInBodyWithSchemaTypeArray() {
        SwaggerRequestSchema parsedRequest = swaggerSchemaParser.parseRequest("/user/createWithList", HttpMethod.POST);
        ArrayList<RequestParameter> requestParameters = parsedRequest.getParameters();
        Assert.assertEquals(requestParameters.size(), 1);
        Assert.assertEquals(requestParameters.get(0).getParameterIn(), "body");
        Assert.assertEquals(requestParameters.get(0).getName(), "body");
        Assert.assertEquals(requestParameters.get(0).getIsRequired(), true);
        SwaggerSchema swaggerSchema = requestParameters.get(0).getSwaggerSchema();
        Assert.assertNotNull(swaggerSchema);
        Assert.assertEquals(swaggerSchema.getType(), "array");
        Assert.assertEquals(swaggerSchema.getItemsType(), "ref");
        HashMap<String, JsonNode> parsedSchema = swaggerSchema.getParsedSchema();
        Assert.assertEquals(parsedSchema.size(), 1);
        Assert.assertEquals(User, parsedSchema.get("root"));
    }

    @Test
    public void ShouldBeAbleToParseRequestWithParamtersInQueryAndTypeArray() {
        SwaggerRequestSchema parsedRequest = swaggerSchemaParser.parseRequest("/pet/findByTags", HttpMethod.GET);
        ArrayList<RequestParameter> requestParameters = parsedRequest.getParameters();
        Assert.assertEquals(requestParameters.size(), 1);
        Assert.assertEquals(requestParameters.get(0).getParameterIn(), "query");
        Assert.assertEquals(requestParameters.get(0).getName(), "tags");
        Assert.assertEquals(requestParameters.get(0).getIsRequired(), true);
        SwaggerSchema swaggerSchema = requestParameters.get(0).getSwaggerSchema();
        Assert.assertNotNull(swaggerSchema);
        Assert.assertEquals(swaggerSchema.getType(), "array");
        Assert.assertEquals(swaggerSchema.getItemsType(), "string");
    }

    @Test
    public void ShouldBeAbleToParseRequestWithParamtersInQueryAndTypeArrayWithEnum() throws IOException {
        SwaggerRequestSchema parsedRequest = swaggerSchemaParser.parseRequest("/pet/findByStatus", HttpMethod.GET);
        ArrayList<RequestParameter> requestParameters = parsedRequest.getParameters();
        Assert.assertEquals(requestParameters.size(), 1);
        Assert.assertEquals(requestParameters.get(0).getParameterIn(), "query");
        Assert.assertEquals(requestParameters.get(0).getName(), "status");
        Assert.assertEquals(requestParameters.get(0).getIsRequired(), true);
        SwaggerSchema swaggerSchema = requestParameters.get(0).getSwaggerSchema();
        Assert.assertNotNull(swaggerSchema);
        Assert.assertEquals(swaggerSchema.getType(), "array");
        Assert.assertEquals(swaggerSchema.getItemsType(), "string");
        Assert.assertEquals(swaggerSchema.getEnumValues(), "available,pending,sold");

    }


    @Test
    public void shouldReturnAMeaningfulMessageWhenPathNotFound() {

    }
}
