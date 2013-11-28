package org.synyx.urlaubsverwaltung.application.service;

import org.joda.time.DateMidnight;

import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.application.domain.Comment;
import org.synyx.urlaubsverwaltung.person.Person;

import java.util.List;


/**
 * This service provides access to the {@link Application}s for leave, i.e. save, allow, reject or cancel.
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
     * Sets {@link Application}'s state to waiting and calculates the number of vacation days.
     *
     * @param  application {@link Application}
     * @param  person {@link Person} person for that the application for leave is
     * @param  applier {@link Person} the person that applied this application
     */
    Application apply(Application application, Person person, Person applier);


    /**
     * Sets an {@link Application}'s state to allowed (only by boss).
     *
     * @param  application {@link Application}
     * @param  boss {@link Person}
     * @param  comment {@link Comment}
     */
    void allow(Application application, Person boss, Comment comment);


    /**
     * Sets an {@link Application}'s state to rejected (only by boss).
     *
     * @param  application {@link Application}
     */
    void reject(Application application, Person boss);


    /**
     * Sets an {@link Application}'s state to cancelled.
     *
     * @param  application {@link Application}
     */
    void cancel(Application application);


    /**
     * Signs an {@link Application} with the private key of the signing user (applicant).
     *
     * @param  application {@link Application}
     * @param  user {@link Person}
     */
    void signApplicationByUser(Application application, Person user);


    /**
     * Signs an {@link Application} with the private key of the signing boss.
     *
     * @param  application {@link Application}
     * @param  boss {@link Person}
     */
    void signApplicationByBoss(Application application, Person boss);


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
