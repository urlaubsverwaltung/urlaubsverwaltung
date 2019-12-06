package org.synyx.urlaubsverwaltung.util;

import java.math.BigDecimal;


/**
 * Contains helper methods for handling {@link BigDecimal}s.
 */
public final class CalcUtil {

    private CalcUtil() {

        // Hide constructor for util classes
    }

    public static boolean isZero(BigDecimal number) {

        /*
         * NOTE: {@link BigDecimal#signum()} returns:
         * -1 if the number is negative,
         * 0 if the number is zero,
         * 1 if the number is positive
         */
        return number.signum() == 0;
    }


    public static boolean isNegative(BigDecimal number) {

        return number.signum() == -1;
    }


    public static boolean isPositive(BigDecimal number) {

        return number.signum() == 1;
    }
}
