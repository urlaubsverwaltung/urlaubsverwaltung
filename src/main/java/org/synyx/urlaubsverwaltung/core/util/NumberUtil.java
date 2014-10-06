/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.synyx.urlaubsverwaltung.core.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;


/**
 * @author  Aljona Murygina
 */
public class NumberUtil {

    /**
     * This method parses the BigDecimal to the desired number format.
     *
     * @param  numberToFormat
     * @param  locale
     *
     * @return  number with desired format
     */
    public static String formatNumber(BigDecimal numberToFormat, Locale locale) {

        NumberFormat n = NumberFormat.getNumberInstance(locale);
        n.setMaximumFractionDigits(1);

        return n.format(numberToFormat.setScale(1, RoundingMode.HALF_UP).doubleValue());
    }


    /**
     * This method parses a BigDecimal from String by regarding the local specific decimal separator.
     *
     * @param  numberToParse
     * @param  locale
     *
     * @return
     */
    public static BigDecimal parseNumber(String numberToParse, Locale locale) {

        DecimalFormatSymbols dfs = new DecimalFormatSymbols(locale);
        char sep = dfs.getDecimalSeparator();

        numberToParse = numberToParse.replace(sep, '.');

        return new BigDecimal(numberToParse);
    }
}
