package com.bookbridge.routes;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;

import java.util.Map;

public class GoogleBooksRoute extends RouteBuilder {
    @Override
    public void configure() throws Exception {

        // This route starts from a direct endpoint that can be triggered by a ProducerTemplate or unit test.
        from("direct:fetchBookData")
                // Set HTTP method header to GET
                .setHeader(Exchange.HTTP_METHOD, constant("GET"))
                // Dynamically build the URL using the header values for query parameters.
                // The toD (dynamic to) component replaces placeholders with header values.
                .toD("https://www.googleapis.com/books/v1/volumes?q=${header.q}&fields=${header.fields}&key=${header.key}&bridgeEndpoint=true")
                // Log the raw JSON response for debugging purposes
                .log("Raw Response: ${body}")
                // Unmarshal the JSON response using camel-jackson into a Map
                .unmarshal().json(JsonLibrary.Jackson, Map.class)
                // Process the Map to extract or manipulate the fields as needed.
                .process(exchange -> {
                    Map<String, Object> responseBody = exchange.getIn().getBody(Map.class);
                    // For example, extract totalItems and log it
                    Object totalItems = responseBody.get("totalItems");
                    exchange.getIn().setHeader("totalItems", totalItems);
                    // You can also traverse nested objects if needed.
                    // For instance, extract volumeInfo from the first item:
                    if (responseBody.containsKey("items")) {
                        Object firstItem = ((java.util.List) responseBody.get("items")).get(0);
                        if (firstItem instanceof Map) {
                            Map<String, Object> itemMap = (Map<String, Object>) firstItem;
                            Object volumeInfo = itemMap.get("volumeInfo");
                            exchange.getIn().setHeader("volumeInfo", volumeInfo);
                        }
                    }
                })
                // Log the processed data
                .log("Processed Data: totalItems = ${header.totalItems}, volumeInfo = ${header.volumeInfo}")
                .setBody(simple("${header.volumeInfo}"));

    }
}
