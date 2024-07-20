package org.synyx.urlaubsverwaltung.web;

import org.slf4j.Logger;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.datetime.DateFormatter;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Date;
import java.util.Locale;
import java.util.Optional;
import java.util.function.UnaryOperator;

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

    private final MessageSource messageSource;
    private final Clock clock;

    DateFormatAware(MessageSource messageSource, Clock clock) {
        this.messageSource = messageSource;
        this.clock = clock;
    }

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

    /**
     * This method maps a {@linkplain LocalDate} to a word or the user specific format.
     *
     * <p>
     * Examples given {@code 2024-07-19} is today:
     * <ul>
     *     <li>{@code 2024-07-19} -> "heute"</li>
     *     <li>{@code 2024-07-20} -> "morgen"</li>
     *     <li>{@code 2024-07-21} -> "Sonntag, 21. Juli 2024"</li>
     * </ul>
     *
     * <p>
     * Note that words are lower case. Use {@linkplain #formatWord(LocalDate, FormatStyle, UnaryOperator)}
     * if you need capitalized words for instance.
     *
     * @param localDate date to translate to a string
     * @param dateStyle the formatter style to obtain, not null
     * @return the translated local date
     */
    public String formatWord(LocalDate localDate, FormatStyle dateStyle) {
        return formatWord(localDate, dateStyle, String::toLowerCase);
    }

    /**
     * This method maps a {@linkplain LocalDate} to a word or the user specific format.
     *
     * <p>
     * Examples given {@code 2024-07-19} is today:
     * <ul>
     *     <li>{@code 2024-07-19} -> "heute"</li>
     *     <li>{@code 2024-07-20} -> "morgen"</li>
     *     <li>{@code 2024-07-21, FormatStyle.LONG} -> "Sonntag 21. Juli 2024"</li>
     *     <li>{@code 2024-07-21, FormatStyle.MEDIUM} -> "21.07.2024"</li>
     * </ul>
     *
     * @param date date to translate to a string
     * @param dateStyle the date formatter style to obtain, not null
     * @param wordTransformer transform the word to lower case for instance.
     * @return the translated local date (e.g. "Heute", "Morgen" or "Montag, 22. Juli 2024")
     */
    public String formatWord(LocalDate date, FormatStyle dateStyle, UnaryOperator<String> wordTransformer) {
        final LocalDate today = LocalDate.now(clock);
        if (today.isEqual(date)) {
            return wordTransformer.apply(msg("date.word.today"));
        }
        if (today.plusDays(1).isEqual(date)) {
            return wordTransformer.apply(msg("date.word.tomorrow"));
        }
        return format(date, dateStyle);
    }

    private String format(LocalDate localDate, FormatStyle dateStyle) {
        return localDate.format(DateTimeFormatter.ofLocalizedDate(dateStyle).withLocale(locale()));
    }

    private String msg(String key) {
        return messageSource.getMessage(key, new Object[]{}, locale());
    }

    /**
     *
     * @return the user specific locale (request scoped)
     */
    private static Locale locale() {
        return LocaleContextHolder.getLocale();
    }
}

