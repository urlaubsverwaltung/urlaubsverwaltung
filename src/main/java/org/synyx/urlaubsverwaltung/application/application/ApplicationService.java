package org.synyx.urlaubsverwaltung.application.application;

import org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory;
import org.synyx.urlaubsverwaltung.person.Person;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * This service provides access to the {@link Application} entities. Except for saving, the access is read-only.
 * Business interactions are found in
 * {@link ApplicationInteractionService}.
 */
public interface ApplicationService {

    /**
     * Gets an {@link Application} by its primary key.
     *
     * @param id to get the {@link Application} by.
     * @return optional {@link Application} for the given id
     */
    Optional<Application> getApplicationById(Long id);

    List<Application> findApplicationsByIds(Iterable<Long> applicationIds);

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
    List<Application> getApplicationsForACertainPeriodAndPerson(LocalDate startDate, LocalDate endDate, Person person);

    /**
     * Gets all {@link Application}s with vacation time between startDate x and endDate y for the given person.
     *
     * @param startDate
     * @param endDate
     * @param persons
     * @return all {@link Application}s of the given persons with vacation time between startDate and endDate
     */
    List<Application> getApplicationsForACertainPeriodAndStatus(LocalDate startDate, LocalDate endDate, List<Person> persons, List<ApplicationStatus> statuses);

    /**
     * Returns all {@link Application}s where their start or end date is overlapping with the given period between startDate and endDate
     * and filters by the person, status and vacation category
     *
     * @param startDate        {@link LocalDate}
     * @param endDate          {@link LocalDate}
     * @param person           {@link Person}
     * @param statuses         {@link ApplicationStatus} that should be filtered for
     * @param vacationCategory {@link VacationCategory} that should be filtered for
     * @return filters {@link Application}s by status of the given person with vacation category between startDate x and endDate y
     */
    List<Application> getApplicationsForACertainPeriodAndPersonAndVacationCategory(LocalDate startDate, LocalDate endDate, Person person, List<ApplicationStatus> statuses, VacationCategory vacationCategory);


    List<Application> getApplicationsForACertainPeriodAndState(LocalDate startDate, LocalDate endDate, ApplicationStatus status);

    /**
     * Gets all {@link Application}s where the applicant for an upcoming application should be notified
     *
     * @param from     defines the start as {@link LocalDate} of a period that should be considered
     * @param to       defines the end as {@link LocalDate} of a period that should be considered
     * @param statuses {@link ApplicationStatus} that should be filtered for
     * @return all {@link Application}s where the given params match and
     */
    List<Application> getApplicationsWhereApplicantShouldBeNotifiedAboutUpcomingApplication(LocalDate from, LocalDate to, List<ApplicationStatus> statuses);

    /**
     * Gets all {@link Application}s where the replacement should be notified.
     *
     * @param from     defines the start as {@link LocalDate} of a period that should be considered
     * @param to       defines the end as {@link LocalDate} of a period that should be considered
     * @param statuses {@link ApplicationStatus} that should be filtered for
     * @return all {@link Application}s where the given params match and
     */
    List<Application> getApplicationsWhereHolidayReplacementShouldBeNotified(LocalDate from, LocalDate to, List<ApplicationStatus> statuses);

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
     * Get all {@link Application}s with specific states and persons for the given date range
     *
     * @param statuses {@link ApplicationStatus} to filter
     * @param persons  {@link Person}s to consider
     * @param start    start date (inclusive)
     * @param end      end date (inclusive)
     * @return list of all matching {@link Application}s
     */
    List<Application> getForStatesAndPerson(List<ApplicationStatus> statuses, List<Person> persons, LocalDate start, LocalDate end);

    /**
     * Get all {@link Application}s with specific states for the given date range
     *
     * @param statuses {@link ApplicationStatus} to filter
     * @param start    start date (inclusive)
     * @param end      end date (inclusive)
     * @return list of all matching {@link Application}s
     */
    List<Application> getForStates(List<ApplicationStatus> statuses, LocalDate start, LocalDate end);

    /**
     * Get the total hours of overtime reduction for a certain person.
     *
     * @param person to get the total hours of overtime reduction for
     * @return the total overtime reduction of a person, never {@code null}
     */
    Duration getTotalOvertimeReductionOfPerson(Person person);

    Map<Person, Duration> getTotalOvertimeReductionOfPersonUntil(Collection<Person> persons, LocalDate until);

    Duration getTotalOvertimeReductionOfPersonUntil(Person person, LocalDate before);

    /**
     * Get a list of all active replacements of the given person and that are active at the given date
     * <p>
     * A active replacement is a replacement that will end after the given date
     *
     * @param holidayReplacement of the application
     * @param date               that will indicate when a replacement is active or not
     * @return List of applications where the given person is the active replacement
     */
    List<Application> getForHolidayReplacement(Person holidayReplacement, LocalDate date);

    /**
     * Deletes all {@link Application} in the database of applicant with person id.
     *
     * @param person the person whose applications should be deleted
     */
    List<Application> deleteApplicationsByPerson(Person person);

    /**
     * Deletes all interaction with {@link Application} with person id as boss / privileged person or canceller
     *
     * @param person the person whose interactions should be deleted
     */
    void deleteInteractionWithApplications(Person person);
}
