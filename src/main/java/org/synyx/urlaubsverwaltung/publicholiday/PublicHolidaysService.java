package org.synyx.urlaubsverwaltung.publicholiday;

import org.synyx.urlaubsverwaltung.workingtime.FederalState;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeSettings;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PublicHolidaysService {

    /**
     * Returns the public holiday information for a date and the federal state.
     * If there is no public holiday at the given date the return value is an empty optional, otherwise
     * the public holiday will be returned.
     *
     * @param date         to get public holiday for
     * @param federalState the federal state to consider holiday settings for
     * @return the public holiday if there is one at the given date, otherwise empty optional
     */
    Optional<PublicHoliday> getPublicHoliday(LocalDate date, FederalState federalState);

    /**
     * Returns the public holiday information for a date and the federal state.
     * If there is no public holiday at the given date the return value is an empty optional, otherwise
     * the public holiday will be returned.
     *
     * Hint: we added this method so that the working time settings will not be requested from the database for each call of a day e.g.
     * We will remove this method when the "betriebsferien" will be ready so that we do not need to call for Christmas and new years eve.
     *
     * @param date         to get public holiday for
     * @param federalState the federal state to consider holiday settings for
     * @return the public holiday if there is one at the given date, otherwise empty optional
     */
    @Deprecated(since = "4.48.0", forRemoval = true)
    Optional<PublicHoliday> getPublicHoliday(LocalDate date, FederalState federalState, WorkingTimeSettings workingTimeSettings);

    /**
     * Returns a list of public holiday information for the given date range (inclusive from and to) and the federal state.
     * If there is no public holiday at the given date range the return value is an empty list, otherwise
     * the public holidays will be returned.
     *
     * @param from         to get public holiday from
     * @param to           to get public holiday to
     * @param federalState the federal state to consider holiday settings for
     * @return a list of public holiday if there are any for the given date range, otherwise empty list
     */
    List<PublicHoliday> getPublicHolidays(LocalDate from, LocalDate to, FederalState federalState);

    /**
     * Returns a list of public holiday information for the given date range (inclusive from and to) and the federal state.
     * If there is no public holiday at the given date range the return value is an empty list, otherwise
     * the public holidays will be returned.
     * <p>
     * Hint: we added this method so that the working time settings will not be requested from the database for each call of a day e.g.
     * We will remove this method when the "betriebsferien" will be ready so that we do not need to call for Christmas and new years eve.
     *
     * @param from                to get public holiday from
     * @param to                  to get public holiday to
     * @param federalState        the federal state to consider holiday settings for
     * @param workingTimeSettings needs the working time settings for Christmas and new years eve
     * @return a list of public holiday if there are any for the given date range, otherwise empty list
     */
    @Deprecated(since = "4.48.0", forRemoval = true)
    List<PublicHoliday> getPublicHolidays(LocalDate from, LocalDate to, FederalState federalState, WorkingTimeSettings workingTimeSettings);
}
