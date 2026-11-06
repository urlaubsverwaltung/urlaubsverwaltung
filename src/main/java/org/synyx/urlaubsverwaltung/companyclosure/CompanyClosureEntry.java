package org.synyx.urlaubsverwaltung.companyclosure;

import org.synyx.urlaubsverwaltung.period.DayLength;

import java.time.LocalDate;
import java.time.MonthDay;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

/**
 * A yearly company closure day with all its versions.
 *
 * @param monthDay     day of the closure
 * @param nameByLocale resolves the name of the closure for a locale, e.g. "Heiligabend" for german
 * @param versions     sorted by {@code validFrom}, {@code null} first
 */
public record CompanyClosureEntry(MonthDay monthDay, Function<Locale, String> nameByLocale, List<CompanyClosureVersion> versions) {

    /**
     * @param locale language of the reader
     * @return name of the closure in the given locale
     */
    public String name(Locale locale) {
        return nameByLocale.apply(locale);
    }

    /**
     * @return closed portion of the day of the version valid at the given date (the last one starting at or before it), {@link DayLength#ZERO} without one
     */
    public DayLength dayLengthAt(LocalDate date) {
        DayLength dayLength = DayLength.ZERO;
        for (CompanyClosureVersion version : versions) {
            if (version.validFrom() == null || !version.validFrom().isAfter(date)) {
                dayLength = version.dayLength();
            }
        }
        return dayLength;
    }
}
