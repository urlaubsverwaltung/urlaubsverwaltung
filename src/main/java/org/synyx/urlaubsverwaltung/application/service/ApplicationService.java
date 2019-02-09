package org.synyx.urlaubsverwaltung.application.service;

import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.person.Person;

import java.math.BigDecimal;
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
     * Gets all {@link Application}s with the given state.
     *
     * @param state of the {@link Application}s for leave to be fetched
     * @return all {@link Application}s for leave with the given state
     */
    List<Application> getApplicationsForACertainState(ApplicationStatus state);


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
     * Get all {@link Application}.
     *
     * @return  all {@link Application}
     */
    List<Application> getForStatesAndPerson(List<ApplicationStatus> statuses, Person person);


    /**
     * Get the total hours of overtime reduction for a certain person.
     *
     * @param person to get the total hours of overtime reduction for
     * @return the total overtime reduction of a person, never {@code null}
     */
    BigDecimal getTotalOvertimeReductionOfPerson(Person person);
}
