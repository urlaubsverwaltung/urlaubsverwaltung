package org.synyx.urlaubsverwaltung.core.person;

import java.util.List;
import java.util.Optional;


/**
 * Service provides access to {@link Person} entities.
 *
 * @author  Johannes Reuter
 * @author  Aljona Murygina
 */

public interface PersonService {

    /**
     * Create a new person using the given attributes, generating and setting key pair.
     *
     * @param  loginName  with that the person can sign in
     * @param  lastName  of the person
     * @param  firstName  of the person
     * @param  email  address to get mail notifications
     * @param  notifications  contains types of mail notifications the person will get
     * @param  permissions  contains the roles of the person
     *
     * @return  created person
     */
    Person create(String loginName, String lastName, String firstName, String email,
        List<MailNotification> notifications, List<Role> permissions);


    /**
     * Updates a person using the given attributes.
     *
     * @param  id  identifies the person to be updated
     * @param  loginName  with that the person can sign in
     * @param  lastName  of the person
     * @param  firstName  of the person
     * @param  email  address to get mail notifications
     * @param  notifications  contains types of mail notifications the person will get
     * @param  permissions  contains the roles of the person
     *
     * @return  updated person
     */
    Person update(Integer id, String loginName, String lastName, String firstName, String email,
        List<MailNotification> notifications, List<Role> permissions);


    /**
     * Updates the given person.
     *
     * @param  person  to be saved
     *
     * @return  updated person
     */
    Person update(Person person);


    /**
     * saves a {@link Person}.
     *
     * @param  person  {@link Person}
     */
    void save(Person person);


    /**
     * finds a {@link Person} in the database by its primary key.
     *
     * @param  id  Integer the id of the person
     *
     * @return  optional {@link Person} for the given id
     */
    Optional<Person> getPersonByID(Integer id);


    /**
     * finds a {@link Person} in the database by login name.
     *
     * @param  loginName  of the person
     *
     * @return  optional {@link Person} for the given login name
     */
    Optional<Person> getPersonByLogin(String loginName);


    /**
     * finds all {@link Person}s in the database that have the given {@link Role}.
     *
     * @param  role  {@link Role}
     *
     * @return  {@link List} of {@link Person}
     */
    List<Person> getPersonsByRole(Role role);


    /**
     * returns all {@link Person}s that have the given {@link MailNotification} type.
     *
     * @param  notification  by which the persons are filtered
     *
     * @return  list of persons with the given notification type
     */
    List<Person> getPersonsWithNotificationType(MailNotification notification);


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
}
