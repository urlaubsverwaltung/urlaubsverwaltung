package org.synyx.urlaubsverwaltung.application.service;

import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.person.Person;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


/**
 * This service provides access to the {@link Application} entities. Except for saving, the access is read-only.
 * Business interactions are found in
 * {@link org.synyx.urlaubsverwaltung.application.service.ApplicationInteractionService}.
 */
public interface ApplicationService {

    /**
     * Gets an {@link Application} by its primary key.
     *
     * @param id to get the {@link Application} by.
     * @return optional {@link Application} for the given id
     */
    Optional<Application> getApplicationById(Integer id);


    /**
     * Saves a new {@link Application}.
     *
     * @param application to be saved
     * @return the saved {@link Application}
     */
    Application save(Application application);

    /**
     * Gets all {@link Application}s with vacation time between startDate x and endDate y for the given person.
     *
     * @param startDate {@link LocalDate}
     * @param endDate   {@link LocalDate}
     * @param person    {@link Person}
     * @return all {@link Application}s of the given person with vacation time between startDate x and endDate y
     */
    List<Application> getApplicationsForACertainPeriodAndPerson(LocalDate startDate, LocalDate endDate,
                                                                Person person);


    /**
     * Gets all {@link Application}s with vacation time between startDate x and endDate y for the given state.
     *
     * @param startDate {@link LocalDate}
     * @param endDate   {@link LocalDate}
     * @param status    {@link ApplicationStatus}
     * @return all {@link Application}s with the given state and vacation time between startDate x and endDate y
     */
    List<Application> getApplicationsForACertainPeriodAndState(LocalDate startDate, LocalDate endDate,
                                                               ApplicationStatus status);


    /**
     * Gets all {@link Application}s with vacation time between startDate x and endDate y for the given person and
     * state.
     *
     * @param startDate {@link LocalDate}
     * @param endDate   {@link LocalDate}
     * @param person    {@link Person}
     * @param status    {@link ApplicationStatus}
     * @return all {@link Application}s of the given person with vacation time between startDate x and endDate y and
     * with a certain state
     */
    List<Application> getApplicationsForACertainPeriodAndPersonAndState(LocalDate startDate, LocalDate endDate,
                                                                        Person person, ApplicationStatus status);


    /**
     * Get all {@link Application} with specific states
     *
     * @return all {@link Application}
     */
    List<Application> getForStates(List<ApplicationStatus> statuses);

    /**
     * Get all {@link Application} with specific states since
     *
     * @return all {@link Application}
     */
    List<Application> getForStatesSince(List<ApplicationStatus> statuses, LocalDate since);

    /**
     * Get all {@link Application} with specific states and persons
     *
     * @return all {@link Application}
     */
    List<Application> getForStatesAndPerson(List<ApplicationStatus> statuses, List<Person> persons);

    /**
     * Get all {@link Application} with specific states and persons
     *
     * @return all {@link Application}
     */
    List<Application> getForStatesAndPersonSince(List<ApplicationStatus> statuses, List<Person> persons, LocalDate since);


    /**
     * Get the total hours of overtime reduction for a certain person.
     *
     * @param person to get the total hours of overtime reduction for
     * @return the total overtime reduction of a person, never {@code null}
     */
    Duration getTotalOvertimeReductionOfPerson(Person person);


    /**
     * Get a list of all active holiday replacements of the given person and that are active at the given date
     * <p>
     * A active holiday replacement is a replacement that will end after the given date
     *
     * @param holidayReplacement of the application
     * @param date               that will indicate when a holiday replacement is active or not
     * @return List of applications where the given person is the active holiday replacement
     */
    List<Application> getForHolidayReplacement(Person holidayReplacement, LocalDate date);
}
