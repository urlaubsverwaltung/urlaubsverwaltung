package org.synyx.urlaubsverwaltung.service;

import org.joda.time.DateMidnight;

import org.synyx.urlaubsverwaltung.domain.Person;

import java.util.List;
import org.synyx.urlaubsverwaltung.domain.Role;
import org.synyx.urlaubsverwaltung.view.PersonForm;


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
     * Creates or updates a {@link Person} with the values of the given {@link PersonForm} incl. creating/updating {@link Account} information.
     * 
     * @param person
     * @param personForm 
     */
    void createOrUpdate(Person person, PersonForm personForm);


    /**
     * use this to deactivate someones profile, i.e. this person has no right to login, to apply for leave, etc. but
     * information about the person remains for office. Notice: only person is deactivated, his active entitlement and
     * account won't be deactivated!
     *
     * @param  person  the profile to deactivate
     */
    void deactivate(Person person);


    /**
     * use this to activate someones profile (e.g. after unintended deactivating of a person), i.e. this person has once
     * again his user rights)
     *
     * @param  person  the profile to activate
     */
    void activate(Person person);


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
     * finds a person in the database by the given {@link Role}
     *
     * @param  role
     *
     * @return
     */
    List<Person> getPersonsByRole(Role role);


    /**
     * returns all active persons ordered by last name
     *
     * @return  returns all active persons in a list
     */
    List<Person> getAllPersons();


    /**
     * returns all inactive persons ordered by last name
     *
     * @return  returns all inactive persons in a list
     */
    List<Person> getInactivePersons();


    /**
     * returns all persons except that one that has the given id
     *
     * @return  returns all persons in a list
     */
    List<Person> getAllPersonsExceptOne(Integer id);


    /**
     * this method get all persons with remainingVacationDaysExpire == true to be able to send a reminder email to these persons
     */
    List<Person> getPersonsWithExpiringRemainingVacationDays();


    /**
     * get all persons that have days off this week.
     *
     * @param  startDate
     * @param  endDate
     */
    void getAllPersonsOnHolidayForThisWeekAndPutItInAnEmail(DateMidnight startDate, DateMidnight endDate);
}
