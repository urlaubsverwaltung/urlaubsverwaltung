package org.synyx.urlaubsverwaltung.publicholiday;

import org.synyx.urlaubsverwaltung.workingtime.FederalState;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public interface PublicHolidaysService {

    /**
     * Check if the given {@link LocalDate} is a public holiday in the given {@link FederalState} or not.
     *
     * @param date         the date to check
     * @param federalState the federal state to consider holiday settings for
     * @return {@code true} when the date is a public holiday, {@code false} otherwise.
     */
    boolean isPublicHoliday(LocalDate date, FederalState federalState);

    /**
     * Returns the public holiday information for a date and the federal state.
     *
     * <p>This variant accepts a {@link Supplier} for {@link PublicHolidaysSettings}, allowing callers to provide
     * cached settings and avoid repeated database access when processing multiple dates in a loop.</p>
     *
     * @param date                   the date to check for a public holiday
     * @param federalState           the federal state to consider holiday settings for
     * @param publicHolidaysSettings supplier that provides the public holiday settings (use a cached supplier for performance)
     * @return the public holiday if there is one at the given date, otherwise empty optional
     */
    Optional<PublicHoliday> getPublicHoliday(LocalDate date, FederalState federalState, Supplier<PublicHolidaysSettings> publicHolidaysSettings);

    /**
     * Returns the public holiday information for a date and the federal state.
     *
     * <p>This is a convenience variant that loads {@link PublicHolidaysSettings} internally.
     * For bulk operations (e.g., iterating over many dates), prefer the overload accepting
     * a {@link Supplier}<{@link PublicHolidaysSettings}> to avoid repeated settings lookup.</p>
     *
     * @param date         the date to check for a public holiday
     * @param federalState the federal state to consider holiday settings for
     * @return the public holiday if there is one at the given date, otherwise empty optional
     */
    Optional<PublicHoliday> getPublicHoliday(LocalDate date, FederalState federalState);

    /**
     * Returns a list of public holiday information for the given date range (inclusive from and to) and the federal state.
     * If there is no public holiday at the given date range the return value is an empty list, otherwise
     * the public holidays will be returned.
     * <p>
     * Hint: it is possible that there are two public holidays at the same day e.g. if there is a fixed public holiday
     * and a movable that will be at the same day in a special year.
     *
     * @param from         to get public holiday from
     * @param to           to get public holiday to
     * @param federalState the federal state to consider holiday settings for
     * @return a list of public holiday if there are any for the given date range, otherwise empty list
     */
    List<PublicHoliday> getPublicHolidays(LocalDate from, LocalDate to, FederalState federalState);
}
