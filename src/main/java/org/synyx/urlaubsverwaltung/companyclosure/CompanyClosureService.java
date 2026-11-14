package org.synyx.urlaubsverwaltung.companyclosure;

import org.synyx.urlaubsverwaltung.period.DayLength;

import java.time.LocalDate;
import java.time.MonthDay;
import java.util.List;

public interface CompanyClosureService {

    /**
     * @param from start of the range, inclusive
     * @param to   end of the range, inclusive
     * @return one entry per date in the range with a closure that is not {@link DayLength#ZERO}, sorted by date
     * @throws IllegalArgumentException if the range is too long, see {@code DateLimits#requireCalculableDuration}
     */
    List<CompanyClosure> getCompanyClosures(LocalDate from, LocalDate to);

    /**
     * Adds a version to an existing company closure day.
     *
     * @param monthDay  day of the closure
     * @param dayLength closed portion of the day
     * @param validFrom first date the new value applies to, not before the start of the current year
     * @throws IllegalArgumentException if there is no closure for the month day or validFrom lies before the current year
     */
    void changeCompanyClosure(MonthDay monthDay, DayLength dayLength, LocalDate validFrom);
}
