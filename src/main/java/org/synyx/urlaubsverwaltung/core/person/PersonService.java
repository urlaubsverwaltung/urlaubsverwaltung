package org.synyx.urlaubsverwaltung.core.person;

import org.synyx.urlaubsverwaltung.security.Role;

import java.util.List;


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
     * returns all active persons ordered by first name.
     *
     * @return  returns all active persons
     */
    List<Person> getActivePersons();


    /**
     * returns all inactive persons ordered by first name.
     *
     * @return  returns all inactive persons
     */
    List<Person> getInactivePersons();


    /**
     * returns all persons except that one that has the given id.
     *
     * @return  returns all persons in a list
     */
    List<Person> getAllPersonsExcept(Person person);
}
