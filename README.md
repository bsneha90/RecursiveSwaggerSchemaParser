# RecursiveSwaggerSchemaParser
Swagger schema contains nested model definitions which in turn could contain other nested models. This library is created to parse all these the nested responses for a given path and HTTP Method and store it in a HashMap.


## Initialize the parser swagger json contract path:
```
SwaggerSchemaParser swaggerSchemaParser = new SwaggerSchemaParser();
swaggerSchemaParser.initializeParser("http://petstore.swagger.io/v2/swagger.json");
```
## Parse a response:

##### Sample:
``` 
HashMap<String, HashMap<String, String>> parsedResponse= swaggerSchemaParser.parseResponse("/pet/findByStatus", HttpMethod.GET,ResponseType.OK);
```

##### parseResponse(String path, HTTPMethod httpMethod, ResponseType responseType):

- **_path_** 

    The sub path that needs to be parsed.**(Required)**

- **_HTTPMethod:_** 
    
    Can be GET,PUT,POST or DELETE.**(Required)**

- **_ResponseType:_** 

    This specifies the HTTPResponse status we are looking for. Can be one of the following:**(Optional. Default is All)**
               
   - OK("200"),
   - NOT_FOUND("404"),
   - BAD_REQUEST("400"),
   - INTERNAL_SERVER_ERROR("500"),
   - NOT_IMPLEMENTED("502"),
   - BAD_GATEWAY("502"),
   - SERVICE_UNAVAILABLE("503"),
   - All("All") : This will return all responses that are returned by swagger.


## License

![GNU Public License version 3.0](http://www.gnu.org/graphics/gplv3-127x51.png)
RecursiveSwaggerSchemaParser is released under [GNU Public License version 3.0](http://www.gnu.org/licenses/gpl-3.0.txt)
