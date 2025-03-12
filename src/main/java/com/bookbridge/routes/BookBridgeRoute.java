package com.bookbridge.routes;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.model.rest.RestBindingMode;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BookBridgeRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        String apiKey = System.getenv("GOOGLE_BOOKS_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("API key is missing! Set GOOGLE_BOOKS_API_KEY in your environment");
        }

        // Configure REST DSL
        restConfiguration()
                .component("undertow")
                .host("localhost")
                .port(8080)
                .bindingMode(RestBindingMode.auto);

        // Expose GET /fetch-books
        rest("/fetch-books")
                .get()
                .to("direct:fetchBooks");

        // Route to fetch books from Google Books API
        from("direct:fetchBooks")
                .routeId("google-books-fetch-route")
                .process(exchange -> {
                    // ✅ Construct the API request URL dynamically
                    String baseUrl = "https://www.googleapis.com/books/v1/volumes";
                    String searchQuery = URLEncoder.encode("quentin meillassoux after finitude", StandardCharsets.UTF_8);
                    String fields = String.join(",",
                            "kind",
                            "totalItems",
                            "items(volumeInfo(title,authors,publisher,publishedDate,description,categories,imageLinks(thumbnail,smallThumbnail),language,infoLink,canonicalVolumeLink),accessInfo(epub,pdf))"
                    );

                    String requestUrl = baseUrl + "?q=" + searchQuery + "&fields=" + fields + "&key=" + apiKey;

                    // ✅ Set the correct URL in the header before sending
                    exchange.getIn().setHeader("CamelHttpUri", requestUrl);

                    // ✅ Log the full request details
                    System.out.println("📡 FINAL REQUEST BEING SENT:");
                    System.out.println("URL: " + requestUrl);
                    System.out.println("Headers: " + exchange.getIn().getHeaders());

                    // ✅ Set required HTTP headers
                    exchange.getIn().setHeader("CamelHttpMethod", "GET");
                    exchange.getIn().setHeader("Accept", "application/json");
                    exchange.getIn().setHeader("Content-Type", "application/json");
                    exchange.getIn().setHeader("User-Agent", "Apache-Camel-Client");
                })
                .toD("${header.CamelHttpUri}") // ✅ Ensure this now has the correct URL
                .unmarshal().json(JsonLibrary.Jackson)
                .to("direct:processBooks");

        // Route to process API response and extract book details
        from("direct:processBooks")
                .routeId("process-books-route")
                .process(this::processBooks)
                .log("Processed Books: ${body}");
    }

    private void processBooks(Exchange exchange) {
        Object body = exchange.getIn().getBody();

        if (body instanceof Map) {
            @SuppressWarnings("unchecked") // Suppressing unchecked cast warnings
            Map<String, Object> response = (Map<String, Object>) body;

            // Ensure "items" key exists and is a List before casting
            Object itemsObject = response.get("items");

            if (itemsObject instanceof List<?>) {
                List<Map<String, Object>> books = ((List<?>) itemsObject).stream()
                        .filter(item -> item instanceof Map) // Ensure each item is a Map
                        .map(item -> (Map<String, Object>) item)
                        .collect(Collectors.toList());

                List<Map<String, Object>> extractedBooks = books.stream().map(book -> {
                    Map<String, Object> volumeInfo = (Map<String, Object>) book.get("volumeInfo");
                    return Map.of(
                            "title", volumeInfo.get("title"),
                            "authors", volumeInfo.get("authors"),
                            "publishedDate", volumeInfo.get("publishedDate")
                    );
                }).collect(Collectors.toList());

                exchange.getIn().setBody(extractedBooks);
            } else {
                exchange.getIn().setBody(List.of()); // Empty list if "items" is missing
            }
        } else {
            exchange.getIn().setBody(Map.of("error", "Invalid API response")); // Handle unexpected response
        }
    }
}
