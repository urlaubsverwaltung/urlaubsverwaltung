package org.synyx.urlaubsverwaltung.person;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.security.CustomPrincipal;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static java.lang.invoke.MethodHandles.lookup;
import static java.util.stream.Collectors.toList;
import static org.slf4j.LoggerFactory.getLogger;
import static org.synyx.urlaubsverwaltung.person.Role.INACTIVE;


/**
 * Implementation for {@link PersonService}.
 */
@Service("personService")
class PersonServiceImpl implements PersonService {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final PersonDAO personDAO;

    @Autowired
    PersonServiceImpl(PersonDAO personDAO) {

        this.personDAO = personDAO;
    }

    @Override
    public Person create(String loginName, String lastName, String firstName, String email,
                         List<MailNotification> notifications, List<Role> permissions) {

        final Person person = new Person(loginName, lastName, firstName, email);

        person.setNotifications(notifications);
        person.setPermissions(permissions);

        LOG.info("Create person: {}", person);

        return save(person);
    }


    @Override
    public Person update(Integer id, String loginName, String lastName, String firstName, String email,
                         List<MailNotification> notifications, List<Role> permissions) {

        Person person = getPersonByID(id).orElseThrow(() ->
            new IllegalArgumentException("Can not find a person for ID = " + id));

        person.setLoginName(loginName);
        person.setLastName(lastName);
        person.setFirstName(firstName);
        person.setEmail(email);

        person.setNotifications(notifications);
        person.setPermissions(permissions);

        LOG.info("Update person: {}", person);

        return save(person);
    }


    @Override
    public Person create(Person person) {

        LOG.info("Create person: {}", person);

        return save(person);
    }


    @Override
    public Person update(Person person) {

        if (person.getId() == null) {
            throw new IllegalArgumentException("Can not update a person that is not persisted yet");
        }

        LOG.info("Updated person: {}", person);

        return save(person);
    }


    @Override
    public Person save(Person person) {

        return personDAO.save(person);
    }


    @Override
    public Optional<Person> getPersonByID(Integer id) {

        return personDAO.findById(id);
    }


    @Override
    public Optional<Person> getPersonByLogin(String loginName) {

        return Optional.ofNullable(personDAO.findByLoginName(loginName));
    }


    @Override
    public List<Person> getActivePersons() {

        return personDAO.findAll()
            .stream()
            .filter(person -> !person.hasRole(INACTIVE))
            .sorted(personComparator())
            .collect(toList());
    }


    private Comparator<Person> personComparator() {

        return Comparator.comparing(p -> p.getNiceName().toLowerCase());
    }


    @Override
    public List<Person> getInactivePersons() {

        return personDAO.findAll()
            .stream()
            .filter(person -> person.hasRole(INACTIVE))
            .sorted(personComparator())
            .collect(toList());
    }


    @Override
    public List<Person> getActivePersonsByRole(final Role role) {

        return getActivePersons().stream().filter(person -> person.hasRole(role)).collect(toList());
    }


    @Override
    public List<Person> getPersonsWithNotificationType(final MailNotification notification) {

        return getActivePersons().stream()
            .filter(person -> person.hasNotificationType(notification))
            .collect(toList());
    }


    @Override
    public Person getSignedInUser() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            throw new IllegalStateException("No authentication found in context.");
        }

        String username = ((CustomPrincipal)authentication.getPrincipal()).getName();

        Optional<Person> person = getPersonByLogin(username);

        if (!person.isPresent()) {
            throw new IllegalStateException("Can not get the person for the signed in user with username = " + username);
        }

        return person.get();
    }
}
