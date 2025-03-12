package com.bookbridge.config;

public class GoogleBooksConfig {

   //maxResults
    public static final int MAX_RESULTS = 10;

    //API URL
    public static final String GOOGLE_BOOKS_API_URL = "https://www.googleapis.com/books/v1/volumes";

    //API key
    public static final String API_KEY = System.getenv("GOOGLE_BOOKS_API_KEY");

    //output directory
    public static final String OUTPUT_DIRECTORY = "data/"; // Define where to save the extracted data

    public GoogleBooksConfig() {
        throw new UnsupportedOperationException("Utility class - cannot be instantiated.");
    }

}
