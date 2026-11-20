package org.synyx.urlaubsverwaltung.companyclosure;

import org.synyx.urlaubsverwaltung.period.DayLength;

import java.time.LocalDate;
import java.util.Locale;
import java.util.function.Function;

/**
 * @param date         date of the closure
 * @param dayLength    closed portion of the day, never {@link DayLength#ZERO}
 * @param nameByLocale resolves the name of the closure for a locale, e.g. "Heiligabend" for german
 */
public record CompanyClosure(LocalDate date, DayLength dayLength, Function<Locale, String> nameByLocale) {

    /**
     * @param locale language of the reader
     * @return name of the closure in the given locale
     */
    public String name(Locale locale) {
        return nameByLocale.apply(locale);
    }
}
