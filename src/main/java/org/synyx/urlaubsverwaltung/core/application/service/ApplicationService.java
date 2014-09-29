package org.synyx.urlaubsverwaltung.core.application.service;

import org.joda.time.DateMidnight;

import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.core.person.Person;

import java.util.List;


/**
 * This service provides read-only access to the {@link Application}s for leave. Interactions occur in
 * {@link org.synyx.urlaubsverwaltung.core.application.service.ApplicationInteractionService}.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public interface ApplicationService {

    /**
     * Returns the Id of the latest {@link Application} for the given {@link Person} and {@link ApplicationStatus}.
     *
     * @param  person {@link Person}
     * @param  status {@link ApplicationStatus}
     *
     * @return  int Id (primary key of {@link Application})
     */
    int getIdOfLatestApplication(Person person, ApplicationStatus status);


    /**
     * Gets an {@link Application} by its primary key.
     *
     * @param  id  Integer
     *
     * @return  {@link Application} for the given id
     */
    Application getApplicationById(Integer id);


    /**
     * Saves a new {@link Application}.
     *
     * @param  application {@link Application} the application to be saved
     */
    void save(Application application);


    /**
     * Gets all {@link Application}s by {@link Person} and year (all {@link Application}s no matter which
     * {@link ApplicationStatus} they have).
     *
     * @param  person {@link Person}
     * @param  year  int
     *
     * @return  {@link List} of {@link Application}s of the given {@link Person} and year
     */
    List<Application> getAllApplicationsByPersonAndYear(Person person, int year);


    /**
     * Gets all {@link Application}s by {@link Person}, year and {@link ApplicationStatus}.
     *
     * @param  person
     * @param  year
     * @param  state
     *
     * @return  {@link List} of {@link Application}s
     */
    List<Application> getAllApplicationsByPersonAndYearAndState(Person person, int year, ApplicationStatus state);


    /**
     * Gets all {@link Application}s by a certain {@link ApplicationStatus} and year.
     *
     * @param  state {@link ApplicationStatus}
     * @param  year  int
     *
     * @return  all {@link Application}s by a certain {@link ApplicationStatus} and year
     */
    List<Application> getApplicationsByStateAndYear(ApplicationStatus state, int year);


    /**
     * Gets all cancelled {@link Application}s that have have been allowed sometime (i.e. formerlyAllowed = true) by a
     * certain year
     *
     * @param  year  int
     *
     * @return  all cancelled {@link Application}s that have have been allowed sometime by a certain year
     */
    List<Application> getCancelledApplicationsByYearFormerlyAllowed(int year);


    /**
     * Gets all allowed {@link Application}s with vacation time between startDate x and endDate y.
     *
     * @param  startDate {@link DateMidnight}
     * @param  endDate {@link DateMidnight}
     *
     * @return  all allowed {@link Application}s with vacation time between startDate x and endDate y
     */
    List<Application> getAllowedApplicationsForACertainPeriod(DateMidnight startDate, DateMidnight endDate);


    /**
     * Gets all {@link Application}s with vacation time between startDate x and endDate y.
     *
     * @param  startDate {@link DateMidnight}
     * @param  endDate {@link DateMidnight}
     *
     * @return  all {@link Application}s with vacation time between startDate x and endDate y
     */
    List<Application> getApplicationsForACertainPeriod(DateMidnight startDate, DateMidnight endDate);


    /**
     * Gets all application for the given month and person.
     *
     * @param  person
     * @param  month
     * @param  year
     *
     * @return  all person's applications that lie in the given month
     */
    List<Application> getAllAllowedApplicationsOfAPersonForAMonth(Person person, int month, int year);
}
