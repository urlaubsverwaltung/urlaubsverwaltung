package org.synyx.urlaubsverwaltung.util;

public final class DateAndTimeFormat {

    public static final String ISO_DATE = "yyyy-MM-dd";

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

    /**
     * The main time format used to display times to the user.
     */
    public static final String HH_MM = "HH:mm";

    /**
     * Should be used as fallback pattern. The user can then enter times with seconds.
     */
    public static final String HH_MM_SS = "HH:mm:ss";

    private DateAndTimeFormat() {
        // Hide constructor for util classes
    }
}
