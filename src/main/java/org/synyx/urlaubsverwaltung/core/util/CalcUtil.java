/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.synyx.urlaubsverwaltung.core.util;

import java.math.BigDecimal;


/**
 * @author  Aljona Murygina
 *
 *          <p>This class contains auxiliary functions for handling BigDecimals.</p>
 */
public class CalcUtil {

    private CalcUtil() {

        // Hide constructor for util classes
    }

    /*
     * function signum() of BigDecimal checks if a BigDecimal is negative, zero or positive.
     *
     * if negative: -1 if zero: 0 if positive: 1
     */

    public static boolean isZero(BigDecimal number) {

        return number.signum() == 0;
    }


    public static boolean isNegative(BigDecimal number) {

        return number.signum() == -1;
    }


    public static boolean isGreaterThanZero(BigDecimal number) {

        return number.signum() == 1;
    }


    public static boolean isEqualOrGreaterThanZero(BigDecimal number) {

        return ((number.signum() == 0) || (number.signum() == 1));
    }
}
