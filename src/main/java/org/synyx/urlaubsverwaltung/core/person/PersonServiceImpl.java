package org.synyx.urlaubsverwaltung.core.person;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;


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

        Person person = new Person(loginName, lastName, firstName, email);

        person.setNotifications(notifications);
        person.setPermissions(permissions);

        save(person);

        LOG.info("Created person: {}", person);

        return person;
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

        save(person);

        LOG.info("Updated person: {}", person);

        return person;
    }


    @Override
    public Person create(Person person) {

        save(person);

        LOG.info("Created person: {}", person);

        return person;
    }


    @Override
    public Person update(Person person) {

        if (person.getId() == null) {
            throw new IllegalArgumentException("Can not update a person that is not persisted yet");
        }

        save(person);

        LOG.info("Updated person: {}", person);

        return person;
    }


    @Override
    public void save(Person person) {

        personDAO.save(person);
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
                .filter(person -> !person.hasRole(Role.INACTIVE))
                .sorted(personComparator())
                .collect(Collectors.toList());
    }


    private Comparator<Person> personComparator() {

        return Comparator.comparing(p -> p.getNiceName().toLowerCase());
    }


    @Override
    public List<Person> getInactivePersons() {

        return personDAO.findAll()
                .stream()
                .filter(person -> person.hasRole(Role.INACTIVE))
                .sorted(personComparator())
                .collect(Collectors.toList());
    }


    @Override
    public List<Person> getPersonsByRole(final Role role) {

        return getActivePersons().stream().filter(person -> person.hasRole(role)).collect(Collectors.toList());
    }


    @Override
    public List<Person> getPersonsWithNotificationType(final MailNotification notification) {

        return getActivePersons().stream()
                .filter(person -> person.hasNotificationType(notification))
                .collect(Collectors.toList());
    }
}
