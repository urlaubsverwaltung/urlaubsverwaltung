package org.synyx.urlaubsverwaltung.web;

import org.springframework.stereotype.Component;
import org.synyx.urlaubsverwaltung.util.DateFormat;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;

import static org.springframework.util.StringUtils.isEmpty;

/**
 * Handles date {@link String}s and {@link LocalDate}s with the user specific date format.
 *
 * <p>With enabled JavaScript the client POSTs a date string in ISO format (<code>"yyyy-MM-dd"</code>).
 * Without JavaScript the date string has the user specific format like <code>"dd.MM.yyyy"</code>.</p>
 *
 * @author Benjamin Seber - seber@synyx.de
 */
@Component
public class DateFormatAware {

    /**
     *
     * @param dateString valid date string in random date format
     * @return the {@link LocalDate} of the given dateString or an empty {@link Optional} when the string cannot be parsed.
     */
    public Optional<LocalDate> parse(String dateString) {

        if (isEmpty(dateString)) {
            return Optional.empty();
        }

        return parseIso(dateString).or(() -> parseUserFormat(dateString));
    }

    /**
     *
     * @param localDate the {@link LocalDate} to format.
     * @return the formatted date with the user specified locale (e.g. <code>"yyyy-MM-dd"</code>, <code>"dd.MM.yyyy"</code>)
     */
    public String format(LocalDate localDate) {

        return localDate.format(getUserSpecifiedDateFormat());
    }

    /**
     *
     * @param localDate the {@link LocalDate} to format.
     * @return the formatted date in {@link DateTimeFormatter#ISO_DATE} format.
     */
    public String formatISO(LocalDate localDate) {

        return localDate.format(DateTimeFormatter.ISO_DATE);
    }

    private static Optional<LocalDate> parseIso(String dateIsoString) {
        return parseDateString(dateIsoString, DateTimeFormatter.ISO_DATE);
    }

    private static Optional<LocalDate> parseUserFormat(String dateString) {
        return parseDateString(dateString, getUserSpecifiedDateFormat());
    }

    private static Optional<LocalDate> parseDateString(String dateString, DateTimeFormatter formatter) {
        try {
            return Optional.of(LocalDate.parse(dateString, formatter));
        } catch(DateTimeParseException e) {
            return Optional.empty();
        }
    }

    private static DateTimeFormatter getUserSpecifiedDateFormat() {
        return DateTimeFormatter.ofPattern(DateFormat.DD_MM_YYYY);
    }
}
