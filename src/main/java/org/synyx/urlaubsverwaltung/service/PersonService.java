package org.synyx.urlaubsverwaltung.service;

import org.joda.time.DateMidnight;

import org.synyx.urlaubsverwaltung.domain.Person;
import org.synyx.urlaubsverwaltung.domain.Urlaubsanspruch;

import java.util.List;


/**
 * use this service to access to the person-data (firstname, email, vacation-days per year, etc. etc.)
 *
 * @author  johannes
 */
public interface PersonService {

    /**
     * use this to save resp. edit someones profile
     *
     * @param  person  the data to save
     */
    void save(Person person);


    /**
     * use this to delete someones profile
     *
     * @param  person  the profile to delete
     */
    void delete(Person person);


    /**
     * finds a person in the database by his/her id
     *
     * @param  id  the id of the person
     *
     * @return  returns the profile as a Person-object
     */
    Person getPersonByID(Integer id);


    /**
     * finds a person in the database by login name
     *
     * @param  loginName
     *
     * @return
     */
    Person getPersonByLogin(String loginName);


    /**
     * returns all profiles in the database as a list of person-objects
     *
     * @return  returns all profiles in the database as a list of person-objects
     */
    List<Person> getAllPersons();


    /**
     * this method is used by a schedule-job. it deletes existing resturlaub for all persons execution in march(april?)
     */
    void deleteResturlaub();


    /**
     * this method is used by a schedule-job. it sends mails to all persons who have soon decaying resturlaub-days.
     * (execution in march, but before deleteResturlaub =)
     */
    List<Person> getPersonsWithResturlaub();


    /**
     * this method is used by a schedule-job. it transfers unused vacation-days from the old year as resturlaub to the
     * new one and adds the amount of regular vacation-days (execution at 1.1. 0:00)
     */
    void updateVacationDays();


    /**
     * get all persons that have days off (=urlaub haben) this week.
     *
     * @param  requestsOfThisWeek
     *
     * @return
     */
    void getAllUrlauberForThisWeekAndPutItInAnEmail(DateMidnight startDate, DateMidnight endDate);


    Urlaubsanspruch getUrlaubsanspruchByPersonAndYear(Person person, Integer year);


    List<Urlaubsanspruch> getUrlaubsanspruchByPersonForAllYears(Person person);


    void setUrlaubsanspruchForPerson(Person person, Integer year, Integer days);
}
