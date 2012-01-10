package org.synyx.urlaubsverwaltung.service;

import org.joda.time.DateMidnight;

import org.synyx.urlaubsverwaltung.domain.Application;
import org.synyx.urlaubsverwaltung.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.domain.Comment;
import org.synyx.urlaubsverwaltung.domain.Person;

import java.util.List;


/**
 * use this service to access to the application-data (who, how many days, ...)
 *
 * @author  Johannes Reuter
 * @author  Aljona Murygina
 */
public interface ApplicationService {

    /**
     * use this to get an application by its id
     *
     * @param  id
     *
     * @return
     */
    Application getApplicationById(Integer id);


    /**
     * use this to save an edited application
     *
     * @param  application  the application to be saved
     */
    void save(Application application);


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
     * @param  reasonToReject  the reason of the rejection
     */
    void reject(Application application, Person boss, Comment reasonToReject);


    /**
     * application's state is set to cancelled if user cancels vacation
     *
     * @param  application
     */
    void cancel(Application application);


    /**
     * sick days are added to application's attribute sickDays and the number of sick days is credited to person's leave
     * account, because sick days are not counted among to holidays
     *
     * @param  application
     * @param  sickDays
     */
    void addSickDaysOnHolidaysAccount(Application application, double sickDays);


    /**
     * use this to get all applications of a certain person
     *
     * @param  person  the person you want to get the applications of
     *
     * @return  returns all applications of a person as a list of Application-objects
     */
    List<Application> getApplicationsByPerson(Person person);


    /**
     * use this to get all applications by person and year
     *
     * @param  person
     * @param  year
     *
     * @return  return a list of applications of the given person and year
     */
    List<Application> getApplicationsByPersonAndYear(Person person, int year);


    /**
     * use this to get all applications of a certain state (like waiting)
     *
     * @param  state
     *
     * @return  returns all applications of a state as a list of application-objects
     */
    List<Application> getApplicationsByState(ApplicationStatus state);


    /**
     * use this to get all applications with vacation time between startDate x and endDate y
     *
     * @param  startDate
     * @param  endDate
     *
     * @return
     */
    List<Application> getApplicationsForACertainTime(DateMidnight startDate, DateMidnight endDate);


    /**
     * check if application is valid and may be send to boss to be allowed or rejected or if person's leave account has
     * too little residual number of vacation days, so that taking holiday isn't possible
     *
     * @param  application
     *
     * @return  boolean: true if application is okay, false if there are too little residual number of vacation days
     */
    boolean checkApplication(Application application);
}
