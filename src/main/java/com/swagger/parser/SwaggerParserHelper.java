package com.swagger.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.models.Model;
import io.swagger.models.Swagger;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;
import io.swagger.util.Json;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by bsneha on 15/07/17.
 */
public class SwaggerParserHelper {
    private final Swagger swagger;
    ObjectMapper jsonMapper = new ObjectMapper();

    public SwaggerParserHelper(Swagger swagger) {
        this.swagger = swagger;
    }

    private HashMap<String, JsonNode> schemaDefinationsForReferences;

    public HashMap<String, JsonNode> getDefinationsFromSchemaReference(String schemaReference, String schemaName) {
        if (schemaDefinationsForReferences == null) {
            schemaDefinationsForReferences = new HashMap<>();
        }

        Model model = swagger.getDefinitions().get(schemaReference);
        if(model==null){
            System.out.println(schemaReference);
        }
        if (schemaDefinationsForReferences.containsKey(schemaName)) {
            return null;
        }
        try {
            schemaDefinationsForReferences.put(schemaName, jsonMapper.readTree(Json.pretty(model)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        getSchemaFromModel(model);
        return schemaDefinationsForReferences;
    }

    private HashMap<String, Property> getAllSchemaReferencesFromModel(Model model) {
        HashMap<String, Property> references = new HashMap<>();
        Map<String, Property> modelProperties = model.getProperties();
        if (modelProperties != null) {
            for (Map.Entry<String, Property> map : modelProperties.entrySet()) {
                if ("ref".equals(map.getValue().getType())
                        || "array".equals(map.getValue().getType())) {
                    references.put(map.getKey(), map.getValue());
                }
            }
        }

        return references;
    }


    private void getSchemaFromModel(Model model) {
        HashMap<String, Property> references = getAllSchemaReferencesFromModel(model);

        references.forEach((modelKey, property) -> {
            String simpleRef = null;
            Boolean isArray = false;
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
                getDefinationsFromSchemaReference(simpleRef, modelKey);
            }

        });
    }

    private Model getSchemaModelForRef(String schemaReference) {
        Model model = swagger.getDefinitions().get(schemaReference);
        return model;
    }

    HashMap<String, JsonNode> schemaStructureForReferences;

    private HashMap<String, JsonNode> getSchemaStructureWithAllReferences(String scehamReference, String schemaName) {
        if (schemaStructureForReferences == null) {
            schemaStructureForReferences = new HashMap<>();
        }
        Model model = getSchemaModelForRef(scehamReference);


        return null;
    }




}
