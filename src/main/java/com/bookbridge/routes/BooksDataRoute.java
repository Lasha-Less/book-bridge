package com.bookbridge.routes;

import com.bookbridge.config.GoogleBooksConfig;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BooksDataRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {

//        from("direct:fetchBookData")
//                .toD(GoogleBooksConfig.GOOGLE_BOOKS_API_URL +
//                        "?q=${header.q}&maxResults=${header.maxResults}&fields=${header.fields}&key=${header.key}")
//                .log("Received data: ${body}")
//                .convertBodyTo(String.class)  // Ensure response is in string format before writing
//                .to("direct:processBookData"); // Send data for processing
//
//        from("direct:processBookData")
//                .log("Processing book data for storage")
//                .setHeader("CamelFileName", method(this, "generateFileNameFromQuery")) // Generate dynamic filename
//                .to("file:" + GoogleBooksConfig.OUTPUT_DIRECTORY) // Write to file with generated filename
//                .log("Book data successfully written to file: ${header.CamelFileName}");

        // 🔹 Global Error Handling for All Routes
        onException(Exception.class)
                .log(LoggingLevel.ERROR, "Error occurred: ${exception.message}")
                .handled(true); // Prevents the exception from stopping the route

        // 🔹 Fetch Data from Google Books API with Retry Logic
        from("direct:fetchBookData")
                .routeId("FetchBookDataRoute")
                .errorHandler(defaultErrorHandler()
                        .maximumRedeliveries(3) // Retry 3 times before failing
                        .redeliveryDelay(2000)  // Wait 2 seconds before retrying
                        .retryAttemptedLogLevel(LoggingLevel.WARN)
                )
                .toD(GoogleBooksConfig.GOOGLE_BOOKS_API_URL
                        + "?q=${header.q}&maxResults=${header.maxResults}&fields=${header.fields}&key=${header.key}")
                .log("Received data: ${body}")
                .convertBodyTo(String.class)
                .to("direct:processBookData");

        // 🔹 Process & Store Book Data in File with Error Handling
        from("direct:processBookData")
                .routeId("ProcessBookDataRoute")
                .log("Processing book data for storage")
                .setHeader("CamelFileName", method(this, "generateFileNameFromQuery"))
                .doTry()
                .to("file:" + GoogleBooksConfig.OUTPUT_DIRECTORY) // Attempt to write file
                .log("Book data successfully written to file: ${header.CamelFileName}")
                .doCatch(Exception.class)
                .log(LoggingLevel.ERROR, "File write failed: ${exception.message}") // Log file write errors
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) {
                        Exception exception = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
                        exchange.getIn().setBody("File storage failed: " + exception.getMessage());
                    }
                })
                .end();
    }



    private String sanitizeFileName(String query) {
        if (query == null || query.isEmpty()) {
            return "books";
        }
        return query.replaceAll("[^a-zA-Z0-9-_]", "_"); // Keep alphanumeric, replace others with "_"
    }



    public String generateFileNameFromQuery(@org.apache.camel.Header("q") String searchQuery) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        String timestamp = LocalDateTime.now().format(formatter);

        // Remove "intitle:" and "inauthor:" from searchQuery
        String cleanedQuery = searchQuery
                .replaceAll("intitle:", "")  // Remove "intitle:"
                .replaceAll("inauthor:", "") // Remove "inauthor:"
                .trim(); // Remove leading/trailing spaces

        // Sanitize the filename to remove special characters
        String sanitizedQuery = sanitizeFileName(cleanedQuery);

        return sanitizedQuery + "_" + timestamp + ".json";
    }



    public String generateFileName() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        return "books_" + LocalDateTime.now().format(formatter) + ".json";
    }
}
