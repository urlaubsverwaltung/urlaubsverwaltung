package org.synyx.urlaubsverwaltung.companyclosure;

import org.synyx.urlaubsverwaltung.period.DayLength;

import java.time.LocalDate;
import java.time.MonthDay;
import java.util.List;

/**
 * A yearly company closure day with all its versions.
 *
 * @param monthDay day of the closure
 * @param name     name of the closure, e.g. "Heiligabend"
 * @param versions sorted by {@code validFrom}, {@code null} first
 */
public record CompanyClosureEntry(MonthDay monthDay, String name, List<CompanyClosureVersion> versions) {

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
