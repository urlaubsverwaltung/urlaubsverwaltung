package org.synyx.urlaubsverwaltung.person;

import org.joda.time.DateMidnight;

import org.synyx.urlaubsverwaltung.person.web.PersonForm;
import org.synyx.urlaubsverwaltung.security.Role;

import java.util.Collection;
import java.util.List;
import java.util.Locale;


/**
 * Service provides access to {@link Person} entities.
 *
 * @author  Johannes Reuter
 * @author  Aljona Murygina
 */

public interface PersonService {

    /**
     * saves a {@link Person}.
     *
     * @param  person {@link Person}
     */
    void save(Person person);


    /**
     * Creates or updates a {@link Person} with the values of the given {@link PersonForm} incl. creating/updating
     * {@link org.synyx.urlaubsverwaltung.account.Account} information.
     *
     * @param  person {@link Person}
     * @param  personForm {@link PersonForm}
     */
    void createOrUpdate(Person person, PersonForm personForm, Locale locale);


    /**
     * Updates role and state (active/inactive) of the given {@link Person}. Please notice: if state is inactive, then
     * role is inactive too
     *
     * @param  person {@link Person}
     * @param  permissions
     */
    void editPermissions(Person person, Collection<Role> permissions);


    /**
     * use this to deactivate someones profile, i.e. this person has no right to login, to apply for leave, etc. but
     * information about the person remains for office. Notice: only person is deactivated, his active entitlement and
     * account won't be deactivated!
     *
     * @param  person {@link Person}
     */
    void deactivate(Person person);


    /**
     * use this to activate someones profile (e.g. after unintended deactivating of a person), i.e. this person has once
     * again his user rights)
     *
     * @param  person {@link Person}
     */
    void activate(Person person);


    /**
     * finds a {@link Person} in the database by its primary key.
     *
     * @param  id  Integer the id of the person
     *
     * @return  {@link Person} for the given id
     */
    Person getPersonByID(Integer id);


    /**
     * finds a {@link Person} in the database by login name.
     *
     * @param  loginName
     *
     * @return  {@link Person} for the given login name
     */
    Person getPersonByLogin(String loginName);


    /**
     * finds all {@link Person}s in the database that have the given {@link Role}.
     *
     * @param  role {@link Role}
     *
     * @return  {@link List} of {@link Person}
     */
    List<Person> getPersonsByRole(Role role);


    /**
     * returns all active persons ordered by last name.
     *
     * @return  returns all active persons in a list
     */
    List<Person> getAllPersons();


    /**
     * returns all inactive persons ordered by last name.
     *
     * @return  returns all inactive persons in a list
     */
    List<Person> getInactivePersons();


    /**
     * returns all persons except that one that has the given id.
     *
     * @return  returns all persons in a list
     */
    List<Person> getAllPersonsExceptOne(Integer id);


    /**
     * this method get all persons with remainingVacationDaysExpire == true to be able to send a reminder email to these
     * persons.
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
