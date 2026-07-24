package org.synyx.urlaubsverwaltung.blackoutperiod;

import org.synyx.urlaubsverwaltung.application.application.Application;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationType;
import org.synyx.urlaubsverwaltung.person.Person;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * This service provides access to {@link BlackoutPeriod}s and answers whether a given application for leave
 * is blocked by one of them.
 */
public interface BlackoutPeriodService {

    /**
     * @return all existing blackout periods, ordered by start date
     */
    List<BlackoutPeriod> getAllBlackoutPeriods();

    Optional<BlackoutPeriod> getBlackoutPeriodById(Long id);

    BlackoutPeriod create(BlackoutPeriod blackoutPeriod);

    BlackoutPeriod update(BlackoutPeriod blackoutPeriod);

    void delete(Long id);

    /**
     * Finds the blackout period, if any, that blocks a vacation application of the given person, period and
     * vacation type. A blackout period blocks an application if their date ranges overlap, the person is a
     * member of one of the blackout period's departments (or the blackout period is company-wide) and the
     * vacation type is one of the blackout period's restricted types (or the blackout period applies to all
     * vacation types).
     *
     * @param person       the person applying for leave
     * @param startDate    start date of the requested vacation
     * @param endDate      end date of the requested vacation
     * @param vacationType the requested vacation type
     * @return the blocking blackout period, if any
     */
    Optional<BlackoutPeriod> findBlockingBlackoutPeriod(Person person, LocalDate startDate, LocalDate endDate, VacationType<?> vacationType);

    /**
     * Finds all blackout periods that apply to the given person (company-wide or via one of their departments)
     * and overlap with the given date range, independent of vacation type. Intended for visualizing blocked days
     * in calendars, not for validating a concrete application.
     *
     * @param person    the person to find blackout periods for
     * @param startDate start of the period to check, inclusive
     * @param endDate   end of the period to check, inclusive
     * @return the applicable blackout periods, ordered by start date
     */
    List<BlackoutPeriod> findBlackoutPeriodsForPerson(Person person, LocalDate startDate, LocalDate endDate);

    /**
     * Finds already existing, not yet finished applications for leave that would conflict with the given
     * blackout period. Intended to warn office/boss about existing applications when creating or editing a
     * blackout period - it does not modify or cancel anything.
     *
     * @param blackoutPeriod the blackout period to check, does not need to be persisted yet
     * @return the conflicting applications for leave, ordered by start date
     */
    List<Application> findConflictingApplications(BlackoutPeriod blackoutPeriod);
}
