package com.thoughtworks.swagger.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.models.HttpMethod;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;

/**
 * Created by bsneha on 15/06/17.
 */
public class SwaggerSchemaParserTest {
    HashMap<String, HashMap<String, JsonNode>> parsedResponse;
    SwaggerSchemaParser swaggerSchemaParser;
    ObjectMapper mapper = new ObjectMapper();


    @Before
    public void setUp() throws IOException, ParseException {
        swaggerSchemaParser = new SwaggerSchemaParser();
        swaggerSchemaParser.initializeParser("./src/test/resources/sampleJson");
    }

    @Test
    public void test() {
        HashMap<String, JsonNode> parsedRequest = swaggerSchemaParser.parseRequest("/pet/findByStatus");
    }

    @Test
    public void ShouldBeAbleToParseTheResponseForHTTPGetMethodAndReturnTheHaspMapForAllLinkedReferencesWithHTTPStatusOf200() throws IOException {
        parsedResponse = swaggerSchemaParser.parseResponse("/pet/findByStatus", HttpMethod.GET, ResponseType.OK);
        Assert.assertEquals(parsedResponse.size(), 1);
        HashMap<String, JsonNode> parsedResponseDefinations = parsedResponse.get(ResponseType.OK.getCodeValue());
        Assert.assertEquals(parsedResponseDefinations.size(), 3);
        JsonNode ExpectedTag = mapper.readTree("{\n  \"type\" : \"object\",\n  \"properties\" : {\n    \"id\" : {\n      \"type\" : \"integer\",\n      \"format\" : \"int64\"\n    },\n    \"name\" : {\n      \"type\" : \"string\"\n    }\n  },\n  \"xml\" : {\n    \"name\" : \"Tag\"\n  }\n}");
        JsonNode ExpectedCatergory = mapper.readTree("{\n  \"type\" : \"object\",\n  \"properties\" : {\n    \"id\" : {\n      \"type\" : \"integer\",\n      \"format\" : \"int64\"\n    },\n    \"name\" : {\n      \"type\" : \"string\"\n    }\n  },\n  \"xml\" : {\n    \"name\" : \"Category\"\n  }\n}");
        JsonNode ExpectedPet = mapper.readTree("{\n  \"type\" : \"object\",\n  \"required\" : [ \"name\", \"photoUrls\" ],\n  \"properties\" : {\n    \"id\" : {\n      \"type\" : \"integer\",\n      \"format\" : \"int64\"\n    },\n    \"category\" : {\n      \"$ref\" : \"#/definitions/Category\"\n    },\n    \"name\" : {\n      \"type\" : \"string\",\n      \"example\" : \"doggie\"\n    },\n    \"photoUrls\" : {\n      \"type\" : \"array\",\n      \"xml\" : {\n        \"name\" : \"photoUrl\",\n        \"wrapped\" : true\n      },\n      \"items\" : {\n        \"type\" : \"string\"\n      }\n    },\n    \"tags\" : {\n      \"type\" : \"array\",\n      \"xml\" : {\n        \"name\" : \"tag\",\n        \"wrapped\" : true\n      },\n      \"items\" : {\n        \"$ref\" : \"#/definitions/Tag\"\n      }\n    },\n    \"status\" : {\n      \"type\" : \"string\",\n      \"description\" : \"pet status in the store\",\n      \"enum\" : [ \"available\", \"pending\", \"sold\" ]\n    }\n  },\n  \"xml\" : {\n    \"name\" : \"Pet\"\n  }\n}");
        Assert.assertEquals(ExpectedPet, parsedResponseDefinations.get("Root"));
        Assert.assertEquals(ExpectedCatergory, parsedResponseDefinations.get("category"));
        Assert.assertEquals(ExpectedTag, parsedResponseDefinations.get("tags"));
    }

    @Test
    public void ShouldBeAbleToParseResponseForPathWithParametersAndHTTPGetMethodAndReturnTheHaspMapForAllLinkedReferencesWithHTTPStatusOf200() throws IOException {
        parsedResponse = swaggerSchemaParser.parseResponse("/pet/{petId}", HttpMethod.GET, ResponseType.OK);
        HashMap<String, JsonNode> parsedResponseDefinations = parsedResponse.get(ResponseType.OK.getCodeValue());
        Assert.assertEquals(parsedResponseDefinations.size(), 3);
        Assert.assertEquals(parsedResponse.size(), 1);
        JsonNode ExpectedTag = mapper.readTree("{\n  \"type\" : \"object\",\n  \"properties\" : {\n    \"id\" : {\n      \"type\" : \"integer\",\n      \"format\" : \"int64\"\n    },\n    \"name\" : {\n      \"type\" : \"string\"\n    }\n  },\n  \"xml\" : {\n    \"name\" : \"Tag\"\n  }\n}");
        JsonNode ExpectedCatergory = mapper.readTree("{\n  \"type\" : \"object\",\n  \"properties\" : {\n    \"id\" : {\n      \"type\" : \"integer\",\n      \"format\" : \"int64\"\n    },\n    \"name\" : {\n      \"type\" : \"string\"\n    }\n  },\n  \"xml\" : {\n    \"name\" : \"Category\"\n  }\n}");
        JsonNode ExpectedPet = mapper.readTree("{\n  \"type\" : \"object\",\n  \"required\" : [ \"name\", \"photoUrls\" ],\n  \"properties\" : {\n    \"id\" : {\n      \"type\" : \"integer\",\n      \"format\" : \"int64\"\n    },\n    \"category\" : {\n      \"$ref\" : \"#/definitions/Category\"\n    },\n    \"name\" : {\n      \"type\" : \"string\",\n      \"example\" : \"doggie\"\n    },\n    \"photoUrls\" : {\n      \"type\" : \"array\",\n      \"xml\" : {\n        \"name\" : \"photoUrl\",\n        \"wrapped\" : true\n      },\n      \"items\" : {\n        \"type\" : \"string\"\n      }\n    },\n    \"tags\" : {\n      \"type\" : \"array\",\n      \"xml\" : {\n        \"name\" : \"tag\",\n        \"wrapped\" : true\n      },\n      \"items\" : {\n        \"$ref\" : \"#/definitions/Tag\"\n      }\n    },\n    \"status\" : {\n      \"type\" : \"string\",\n      \"description\" : \"pet status in the store\",\n      \"enum\" : [ \"available\", \"pending\", \"sold\" ]\n    }\n  },\n  \"xml\" : {\n    \"name\" : \"Pet\"\n  }\n}");
        Assert.assertEquals(ExpectedTag, parsedResponseDefinations.get("tags"));
        Assert.assertEquals(ExpectedCatergory, parsedResponseDefinations.get("category"));
        Assert.assertEquals(ExpectedPet, parsedResponseDefinations.get("Root"));
    }

    @Test
    public void ShouldBeAllToParserTheResponseForAllHttpMethods() throws IOException {
        parsedResponse = swaggerSchemaParser.parseResponse("/user/{username}", HttpMethod.PUT);
        Assert.assertEquals(parsedResponse.size(), 2);

        HashMap<String, JsonNode> parsedResponseDefinationsForStatus400 = parsedResponse.get(ResponseType.BAD_REQUEST.getCodeValue());
        HashMap<String, JsonNode> parsedResponseDefinationsForStatus404 = parsedResponse.get(ResponseType.NOT_FOUND.getCodeValue());
        Assert.assertEquals(parsedResponseDefinationsForStatus400.get("description"),  mapper.createObjectNode().put("description","Invalid user supplied"));
        Assert.assertEquals(parsedResponseDefinationsForStatus404.get("description"),  mapper.createObjectNode().put("description","User not found"));
    }
}
