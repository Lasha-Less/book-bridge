package com.bookbridge.config;

public class GoogleBooksFields {

    public static final String KIND = "kind";
    public static final String TOTAL_ITEMS = "totalItems";

    public static final String TITLE = "title";
    public static final String AUTHORS = "authors";
    public static final String PUBLISHER = "publisher";
    public static final String PUBLISHED_DATE = "publishedDate";
    public static final String DESCRIPTION = "description";
    public static final String CATEGORIES = "categories";
    public static final String LANGUAGE = "language";
    public static final String INFO_LINK = "infoLink";
    public static final String CANONICAL_VOLUME_LINK = "canonicalVolumeLink";

    // Image Links
    public static final String THUMBNAIL = "thumbnail";
    public static final String SMALL_THUMBNAIL = "smallThumbnail";
    public static final String IMAGE_LINKS = "imageLinks(" + THUMBNAIL + "," + SMALL_THUMBNAIL + ")";

    // Access Info
    public static final String EPUB = "epub";
    public static final String PDF = "pdf";
    public static final String ACCESS_INFO = "accessInfo(" + EPUB + "," + PDF + ")";

    // Volume Info
    public static final String VOLUME_INFO = "volumeInfo(" + TITLE + "," + AUTHORS + "," + PUBLISHER + "," +
            PUBLISHED_DATE + "," + DESCRIPTION + "," + CATEGORIES + "," + IMAGE_LINKS + "," + LANGUAGE + "," +
            INFO_LINK + "," + CANONICAL_VOLUME_LINK + ")";

    public static final String ITEMS = "items(" + VOLUME_INFO + ACCESS_INFO + ")";

    // Final Fields Query Parameter
    public static final String FIELDS = KIND + "," + TOTAL_ITEMS + "," + ITEMS;

    private GoogleBooksFields() {
        throw new UnsupportedOperationException("Utility class - cannot be instantiated.");
    }

}
