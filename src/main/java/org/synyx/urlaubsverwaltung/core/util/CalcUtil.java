/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.synyx.urlaubsverwaltung.core.util;

import java.math.BigDecimal;


/**
 * Contains helper methods for handling {@link java.math.BigDecimal}s.
 *
 * @author  Aljona Murygina
 */
public final class CalcUtil {

    private CalcUtil() {

        // Hide constructor for util classes
    }

    public static boolean isZero(BigDecimal number) {

        /**
         * NOTE: {@link java.math.BigDecimal#signum()} returns:
         * -1 if the number is negative,
         * 0 if the number is zero,
         * 1 if the number is positive
         */
        return number.signum() == 0;
    }


    public static boolean isNegative(BigDecimal number) {

        return number.signum() == -1;
    }
}
