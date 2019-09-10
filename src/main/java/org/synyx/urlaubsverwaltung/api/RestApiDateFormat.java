package org.synyx.urlaubsverwaltung.api;

public final class RestApiDateFormat {

    public static final String DATE_PATTERN = "yyyy-MM-dd";
    public static final String EXAMPLE_YEAR = "2019";
    public static final String EXAMPLE_FIRST_DAY_OF_YEAR = EXAMPLE_YEAR + "-01-01";
    public static final String EXAMPLE_LAST_DAY_OF_MONTH = EXAMPLE_YEAR + "-01-31";
    public static final String EXAMPLE_LAST_DAY_OF_YEAR = EXAMPLE_YEAR + "-12-31";

    private RestApiDateFormat() {

        // Hide constructor for util classes
    }
}
