package org.synyx.urlaubsverwaltung.publicholiday;

import org.synyx.urlaubsverwaltung.workingtime.FederalState;

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
}
