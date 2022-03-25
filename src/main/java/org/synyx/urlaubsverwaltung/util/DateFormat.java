package org.synyx.urlaubsverwaltung.util;

public final class DateFormat {

    /**
     * The main date format used to display dates to the user.
     */
    public static final String DD_MM_YYYY = "dd.MM.yyyy";

    /**
     * Should be used as fallback pattern. The user can then enter dates with two year digits for instance.
     */
    public static final String D_M_YY = "d.M.yy";

    /**
     * Should be used as fallback pattern. The user can then enter dates with one day and/or month digit for instance.
     */
    public static final String D_M_YYYY = "d.M.yyyy";

    private DateFormat() {

        // Hide constructor for util classes
    }
}
