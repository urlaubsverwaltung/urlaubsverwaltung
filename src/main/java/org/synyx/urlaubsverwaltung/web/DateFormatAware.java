package org.synyx.urlaubsverwaltung.web;

import org.slf4j.Logger;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.datetime.DateFormatter;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;
import java.util.Optional;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.util.ObjectUtils.isEmpty;
import static org.synyx.urlaubsverwaltung.util.DateAndTimeFormat.DD_MM_YYYY;
import static org.synyx.urlaubsverwaltung.util.DateAndTimeFormat.D_M_YY;
import static org.synyx.urlaubsverwaltung.util.DateAndTimeFormat.D_M_YYYY;
import static org.synyx.urlaubsverwaltung.util.DateAndTimeFormat.ISO_DATE;

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

    private static final Logger LOG = getLogger(lookup().lookupClass());

    /**
     * @param dateString valid date string in random date format
     * @param locale     {@linkplain Locale} to parse the date
     * @return the {@linkplain LocalDate} of the given dateString or an empty {@linkplain Optional} when the string cannot be parsed.
     */
    public Optional<LocalDate> parse(String dateString, Locale locale) {

        if (isEmpty(dateString)) {
            return Optional.empty();
        }

        final DateFormatter dateFormatter = new DateFormatter();
        dateFormatter.setIso(DateTimeFormat.ISO.DATE);
        dateFormatter.setPattern(ISO_DATE);
        dateFormatter.setFallbackPatterns(D_M_YY, DD_MM_YYYY, D_M_YYYY);

        Optional<Date> d = Optional.empty();

        try {
            final Date parse = dateFormatter.parse(dateString, locale);
            d = Optional.of(parse);
        } catch (ParseException e) {
            LOG.debug("could not parse dateString={} locale={}", dateString, locale);
        }

        return d.map(date -> date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
    }

    /**
     * @param localDate the {@link LocalDate} to format.
     * @return the formatted date with the user specified locale (e.g. <code>"yyyy-MM-dd"</code>, <code>"dd.MM.yyyy"</code>)
     */
    public String format(LocalDate localDate) {
        return localDate.format(DateTimeFormatter.ofPattern(DD_MM_YYYY));
    }

    /**
     * @param localDate the {@link LocalDate} to format.
     * @return the formatted date in {@link DateTimeFormatter#ISO_DATE} format.
     */
    public String formatISO(LocalDate localDate) {
        return localDate.format(DateTimeFormatter.ISO_DATE);
    }
}
