
package org.synyx.urlaubsverwaltung.web;

import org.springframework.util.StringUtils;

import java.beans.PropertyEditorSupport;

import java.math.BigDecimal;
import java.math.RoundingMode;

import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;

import java.util.Locale;


/**
 * Converts a string to decimal number and vice versa.
 *
 * @author  Aljona Murygina
 */
public class DecimalNumberPropertyEditor extends PropertyEditorSupport {

    private final Locale locale;

    public DecimalNumberPropertyEditor(Locale locale) {

        this.locale = locale;
    }

    @Override
    public String getAsText() {

        if (this.getValue() == null) {
            return "";
        }

        try {
            BigDecimal number = (BigDecimal) this.getValue();
            NumberFormat numberFormat = NumberFormat.getNumberInstance(locale);
            numberFormat.setMaximumFractionDigits(2);

            return numberFormat.format(number.setScale(2, RoundingMode.HALF_UP).doubleValue());
        } catch (ClassCastException ex) {
            throw new IllegalArgumentException("The provided value is of invalid type", ex);
        }
    }


    @Override
    public void setAsText(String text) {

        if (!StringUtils.hasText(text)) {
            this.setValue(null);
        } else {
            DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols(locale);
            char sep = decimalFormatSymbols.getDecimalSeparator();

            String normalizedNumberToParse = text.replace(sep, '.');

            this.setValue(new BigDecimal(normalizedNumberToParse).setScale(2, RoundingMode.HALF_UP));
        }
    }
}
