package org.synyx.urlaubsverwaltung.companyclosure;

import org.jspecify.annotations.Nullable;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;

import java.time.LocalDate;
import java.time.MonthDay;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
     * @param modifier  person who changes the closure
     * @throws IllegalArgumentException if there is no closure for the month day or validFrom lies before the current year
     */
    void changeCompanyClosure(MonthDay monthDay, DayLength dayLength, LocalDate validFrom, Person modifier);

    /**
     * Changes versions of an existing company closure day, all at once, so their dates may be moved past each other.
     * Versions starting in the current year or later can be changed, also the ones that have started already: this
     * changes closure days in the past.
     *
     * @param monthDay              day of the closure
     * @param changesByOldValidFrom the changed versions by the date they started from so far
     * @param modifier              person who changes the versions
     * @throws IllegalArgumentException if there is no closure for the month day, a version to change does not
     *                                  exist or starts before the current year, a changed version would start before
     *                                  the current year or two versions would start at the same date
     */
    void changeCompanyClosureVersions(MonthDay monthDay, Map<LocalDate, CompanyClosureVersion> changesByOldValidFrom, Person modifier);

    /**
     * Changes versions, adds a version and renames an existing company closure day in one transaction, so either all
     * changes are saved or none of them.
     *
     * @param monthDay              day of the closure
     * @param changesByOldValidFrom the changed versions by the date they started from so far, empty to change none, see
     *                              {@link #changeCompanyClosureVersions(MonthDay, Map, Person)}
     * @param newVersion            the version to add, {@code null} to add none, see
     *                              {@link #changeCompanyClosure(MonthDay, DayLength, LocalDate, Person)}
     * @param names                 the new names, {@code null} to keep the names, see
     *                              {@link #renameCompanyClosure(MonthDay, Map, Person)}
     * @param modifier              person who changes the closure
     * @throws IllegalArgumentException in the cases of the methods named above
     */
    void updateCompanyClosure(MonthDay monthDay, Map<LocalDate, CompanyClosureVersion> changesByOldValidFrom,
                              @Nullable CompanyClosureVersion newVersion, @Nullable Map<Locale, String> names,
                              Person modifier);

    /**
     * @return all company closure days sorted by month day, from 01.01. to 31.12. The versions of each day are
     * sorted by {@code validFrom} ascending: the initial version without {@code validFrom} first, the latest
     * version last. See {@link CompanyClosureEntry#versions()}.
     */
    List<CompanyClosureEntry> getCompanyClosureEntries();

    /**
     * Creates a new yearly company closure day.
     *
     * @param monthDay  day of the closure, not 29.02. since it does not exist every year
     * @param names     name of the closure per language, at least one of them not blank. Blank names are not stored,
     *                  these languages show a generic name.
     * @param dayLength closed portion of the day
     * @param validFrom first date the closure applies to, not before the start of the current year
     * @param creator   person who creates the closure
     * @throws IllegalArgumentException if there is a closure for the month day already, the month day is 29.02.,
     *                                  validFrom lies before the current year or all names are blank
     */
    void createCompanyClosure(MonthDay monthDay, Map<Locale, String> names, DayLength dayLength, LocalDate validFrom, Person creator);

    /**
     * Renames a company closure day. Names are not versioned, the new names apply to all years. They replace all
     * names of the closure, also the translations of a closure provided by the application.
     *
     * @param names name of the closure per language, at least one of them not blank. Blank names are not stored,
     *              these languages show a generic name.
     * @param modifier person who renames the closure
     * @throws IllegalArgumentException if there is no closure for the month day or all names are blank
     */
    void renameCompanyClosure(MonthDay monthDay, Map<Locale, String> names, Person modifier);
}
