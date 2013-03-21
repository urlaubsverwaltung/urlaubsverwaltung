package org.synyx.urlaubsverwaltung.application.service;

import java.math.BigDecimal;
import java.util.List;
import org.joda.time.DateMidnight;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.person.Person;

/**
 * This service provides access to the applications for leave (domain object {@link Application}), i.e. saves or allows it, etc.
 * 
 * @author Aljona Murygina - murygina@synyx.de
 */
public interface ApplicationService {

    /**
     * get Id of latest Application with given Person and ApplicationStatus
     * 
     * @param person
     * @param status
     * @return int id (primary key of Application)
     */
    int getIdOfLatestApplication(Person person, ApplicationStatus status);

    /**
     * use this to get an application by its id
     *
     * @param  id
     *
     * @return
     */
    Application getApplicationById(Integer id);

    /**
     * use this to save a new application
     *
     * @param  application  the application to be saved
     */
    void save(Application application);

    /**
     * a new application's state is set to waiting and days of the vacation time is calculated and set
     * 
     * @param application
     * @param person - person for that the application for leave is
     * @param applier - the person that applied this application
     */
    Application apply(Application application, Person person, Person applier);

    /**
     * use this to set a application to allowed (only boss)
     *
     * @param  application  the application to be edited
     */
    void allow(Application application, Person boss);

    /**
     * use this to set a application to rejected (only boss)
     *
     * @param  application  the application to be edited
     */
    void reject(Application application, Person boss);

    /**
     * application's state is set to cancelled if user cancels vacation
     *
     * @param  application
     */
    void cancel(Application application);

    /**
     * signs an application with the private key of the signing user (applicant)
     *
     * @param  application
     * @param  user
     */
    void signApplicationByUser(Application application, Person user);

    /**
     * signs an application with the private key of the signing boss
     *
     * @param  application
     * @param  boss
     */
    void signApplicationByBoss(Application application, Person boss);
    
    
     /**
     * use this to get all applications by person and year (all applications no matter which status they have)
     *
     * @param  person
     * @param  year
     *
     * @return  return a list of applications of the given person and year
     */
    List<Application> getAllApplicationsByPersonAndYear(Person person, int year);

    /**
     * use this to get all applications by a certain state (like waiting) and year
     *
     * @param  state
     * @param  year
     *
     * @return  returns all applications in a list
     */
    List<Application> getApplicationsByStateAndYear(ApplicationStatus state, int year);

    /**
     * use this to get all cancelled applications that have have been allowed sometime (i.e. formerlyAllowed = true) by a certain year
     *
     * @param  year
     *
     * @return  returns all applications in a list
     */
    List<Application> getCancelledApplicationsByYearFormerlyAllowed(int year);

    /**
     * use this to get all allowed applications with vacation time between startDate x and endDate y
     * 
     * @param startDate
     * @param endDate
     * 
     * @return 
     */
    List<Application> getAllowedApplicationsForACertainPeriod(DateMidnight startDate, DateMidnight endDate);

    /**
     * use this to get all applications with vacation time between startDate x and endDate y
     * 
     * @param startDate
     * @param endDate
     * 
     * @return 
     */
    List<Application> getApplicationsForACertainPeriod(DateMidnight startDate, DateMidnight endDate);
}
