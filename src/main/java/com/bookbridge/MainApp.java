package com.bookbridge;

import com.bookbridge.config.GoogleBooksConfig;
import com.bookbridge.config.GoogleBooksFields;
import com.bookbridge.routes.BookBridgeRoute;
import com.bookbridge.routes.BooksDataRoute;
import com.bookbridge.routes.GoogleBooksRoute;
import com.bookbridge.routes.RestApiRoute;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.impl.DefaultCamelContext;

import java.util.HashMap;
import java.util.Map;

public class MainApp {
    public static void main(String[] args) throws Exception {

        // Create Camel context
        CamelContext context = new DefaultCamelContext();

        try {
            // Add our custom route
            context.addRoutes(new BooksDataRoute());

            // Start the context
            context.start();

            // Hardcoded book search query (to be replaced later with REST API input)
            String bookTitle = "The Human Condition".toLowerCase();
            String authorFirstName = "Hannah".toLowerCase();
            String authorLastName = "Arendt".toLowerCase();

            // Build search query dynamically
            String searchQuery = "intitle:" + bookTitle.replace(" ", "+") + "+inauthor:" +
                    authorFirstName + "+" + authorLastName;

            // Prepare headers dynamically
            Map<String, Object> headers = new HashMap<>();
            headers.put("q", searchQuery);
            headers.put("fields", GoogleBooksFields.FIELDS);
            headers.put("key", GoogleBooksConfig.API_KEY);
            headers.put("maxResults", GoogleBooksConfig.MAX_RESULTS);

            // Trigger the route
            ProducerTemplate template = context.createProducerTemplate();
            template.sendBodyAndHeaders("direct:fetchBookData", null, headers);

            // Allow time for processing (optional)
            Thread.sleep(5000);

        } catch (Exception e) {
            System.err.println("Error in Camel execution: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                // Ensure Camel context stops properly
                if (context != null) {
                    context.stop();
                }
            } catch (Exception stopException) {
                System.err.println("Error while stopping Camel context: " + stopException.getMessage());
            }
        }
    }
}
