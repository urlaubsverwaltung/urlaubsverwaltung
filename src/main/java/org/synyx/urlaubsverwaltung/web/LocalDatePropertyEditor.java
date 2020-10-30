package org.synyx.urlaubsverwaltung.web;

import org.springframework.util.StringUtils;
import org.synyx.urlaubsverwaltung.util.DateFormat;

import java.beans.PropertyEditorSupport;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;


/**
 * Converts a {@link String} to {@link LocalDate} and vice versa.
 */
public class LocalDatePropertyEditor extends PropertyEditorSupport {

    private final DateTimeFormatter formatter;

    public LocalDatePropertyEditor() {

        this.formatter = DateTimeFormatter.ofPattern(DateFormat.DD_MM_YYYY);
    }

    // Date to String
    @Override
    public String getAsText() {

        if (this.getValue() == null) {
            return "";
        }

        return formatter.format((TemporalAccessor) this.getValue());
    }


    // String to Date
    @Override
    public void setAsText(String text) {

        if (!StringUtils.hasText(text)) {
            this.setValue(null);
        } else {
            LocalDate date;
            try {
                date = LocalDate.parse(text, formatter);
            } catch (DateTimeParseException exception) {
                throw new IllegalArgumentException(exception.getMessage());
            }

            this.setValue(date);
        }
    }
}
