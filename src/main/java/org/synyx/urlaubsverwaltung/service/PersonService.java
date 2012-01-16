package org.synyx.urlaubsverwaltung.service;

import org.joda.time.DateMidnight;

import org.synyx.urlaubsverwaltung.domain.HolidayEntitlement;
import org.synyx.urlaubsverwaltung.domain.Person;

import java.util.List;


/**
 * use this service to access to the person-data (firstname, email, vacation-days per year, etc. )
 *
 * @author  Johannes Reuter
 * @author  Aljona Murygina
 */

public interface PersonService {

    /**
     * use this to save resp. edit someones profile
     *
     * @param  person  the data to save
     */
    void save(Person person);


    /**
     * use this to deactivate someones profile, i.e. this person has no right to login, to apply for leave, etc. but
     * information about the person remains for office
     *
     * @param  person  the profile to deactivate
     */
    void deactivate(Person person);


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
     * returns all active persons ordered by last name
     *
     * @return  returns all persons in a list
     */
    List<Person> getAllPersons();


    /**
     * returns all persons except that one that has the given id
     *
     * @return  returns all persons in a list
     */
    List<Person> getAllPersonsExceptOne(Integer id);


    /**
     * this method get all person with remaining vacation days to send an email to these persons that they have
     * remaining vacation days that decay soon
     */
    List<Person> getPersonsWithRemainingVacationDays();


    /**
     * this method is used by a schedule-job. it transfers unused vacation-days from the old year as resturlaub to the
     * new one and adds the amount of regular vacation-days (execution at 1.1. 0:00)
     *
     * @param  year
     */
    void updateVacationDays(int year);


    /**
     * get all persons that have days off this week.
     *
     * @param  startDate
     * @param  endDate
     */
    void getAllPersonsOnHolidayForThisWeekAndPutItInAnEmail(DateMidnight startDate, DateMidnight endDate);


    /**
     * get a HolidayEntitlement for a certain year and person
     *
     * @param  person
     * @param  year
     *
     * @return
     */
    HolidayEntitlement getHolidayEntitlementByPersonAndYear(Person person, int year);


    /**
     * get a list of HolidayEntitlement for a certain person
     *
     * @param  person
     *
     * @return
     */
    List<HolidayEntitlement> getHolidayEntitlementByPersonForAllYears(Person person);
}
