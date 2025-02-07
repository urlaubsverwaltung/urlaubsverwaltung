package org.synyx.urlaubsverwaltung.person;

import org.springframework.data.domain.Page;
import org.synyx.urlaubsverwaltung.search.PageableSearchQuery;

import java.util.List;
import java.util.Optional;


/**
 * Service provides access to {@link Person} entities.
 */
public interface PersonService {

    /**
     * Create a new person using the given attributes.
     * <p>
     * Sets default mail notifications and roles
     *
     * @param username  with that the person can sign in
     * @param firstName of the person
     * @param lastName  of the person
     * @param email     address to get mail notifications
     * @return created person
     */
    Person create(String username, String firstName, String lastName, String email);

    /**
     * Create a new person using the given attributes, generating and setting key pair.
     *
     * @param username      with that the person can sign in
     * @param firstName     of the person
     * @param lastName      of the person
     * @param email         address to get mail notifications
     * @param notifications contains types of mail notifications the person will get
     * @param permissions   contains the roles of the person
     * @return created person
     */
    Person create(String username, String firstName, String lastName, String email,
                  List<MailNotification> notifications, List<Role> permissions);

    /**
     * Updates the given person.
     *
     * @param person to be saved
     * @return updated person
     */
    Person update(Person person);

    /**
     * Deletes a {@link Person} in the database by its primary key.
     *
     * @param person       the person to be deleted
     * @param signedInUser the person who wants to delete the given person
     */
    void delete(Person person, Person signedInUser);

    /**
     * finds a {@link Person} in the database by its primary key.
     *
     * @param id Long the id of the person
     * @return optional {@link Person} for the given id
     */
    Optional<Person> getPersonByID(Long id);

    /**
     * finds a {@link Person} in the database by username.
     *
     * @param username of the person
     * @return optional {@link Person} for the given username
     */
    Optional<Person> getPersonByUsername(String username);

    /**
     * finds a {@link Person} in the database by mail address.
     *
     * @param mailAddress of the person
     * @return optional {@link Person} for the given mail address
     */
    Optional<Person> getPersonByMailAddress(String mailAddress);

    /**
     * returns all active persons ordered by first name.
     *
     * @return returns all active persons
     */
    List<Person> getActivePersons();

    /**
     * returns all inactive persons ordered by first name.
     *
     * @return returns all active persons
     */
    List<Person> getInactivePersons();

    /**
     * returns all persons ordered by id.
     *
     * @return returns all persons
     */
    List<Person> getAllPersons();

    /**
     * Find all active persons matching the given query.
     *
     * @param personPageableSearchQuery search query containing pageable and an optional query for firstname/lastname
     * @return paginated active persons matching the search query
     */
    Page<Person> getActivePersons(PageableSearchQuery personPageableSearchQuery);

    /**
     * finds all {@link Person}s in the database that have the given {@link Role}.
     *
     * @param role {@link Role}
     * @return {@link List} of {@link Person}
     */
    List<Person> getActivePersonsByRole(Role role);

    /**
     * returns all {@link Person}s that have the given {@link MailNotification} type.
     *
     * @param notification by which the persons are filtered
     * @return list of persons with the given notification type
     */
    List<Person> getActivePersonsWithNotificationType(MailNotification notification);

    /**
     * Find all inactive persons matching the given query.
     *
     * @param personPageableSearchQuery search query containing pageable and an optional query for firstname/lastname
     * @return paginated inactive persons matching the search query
     */
    Page<Person> getInactivePersons(PageableSearchQuery personPageableSearchQuery);

    /**
     * This method allows to get the signed in user.
     *
     * @return user that is signed in
     */
    Person getSignedInUser();

    /**
     * Adds {@link Role#OFFICE} to the roles of the given person if no
     * other active user with a office role is defined.
     *
     * @param person that maybe gets the role {@link Role#OFFICE}
     * @return saved {@link Person} with {@link Role#OFFICE} rights
     * if no other active person with {@link Role#OFFICE} is available.
     */
    Person appointAsOfficeUserIfNoOfficeUserPresent(Person person);

    /**
     * Returns the number of all users that do not have the role INACTIVE.
     * These users are called active users.
     *
     * @return number of active users
     */
    int numberOfActivePersons();

    /**
     * Returns the number of persons that have {@link Role#OFFICE} role but exclude the given id from the request
     *
     * @param excludingId without the user with this id
     * @return number of persons with {@link Role#OFFICE} excluding the person with the id
     */
    int numberOfPersonsWithOfficeRoleExcludingPerson(long excludingId);
}
