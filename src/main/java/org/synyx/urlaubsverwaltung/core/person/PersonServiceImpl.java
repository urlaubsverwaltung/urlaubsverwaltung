package org.synyx.urlaubsverwaltung.core.person;

import org.apache.log4j.Logger;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import org.synyx.urlaubsverwaltung.core.keys.KeyPairService;

import java.security.KeyPair;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * Implementation for {@link PersonService}.
 *
 * @author  Aljona Murygina
 * @author  Johannes Reuter
 */
@Service("personService")
class PersonServiceImpl implements PersonService {

    private static final Logger LOG = Logger.getLogger(PersonServiceImpl.class);

    private final PersonDAO personDAO;
    private final KeyPairService keyPairService;

    @Autowired
    public PersonServiceImpl(PersonDAO personDAO, KeyPairService keyPairService) {

        this.personDAO = personDAO;
        this.keyPairService = keyPairService;
    }

    @Override
    public Person create(String loginName, String lastName, String firstName, String email,
        List<MailNotification> notifications, List<Role> permissions) {

        Person person = new Person(loginName, lastName, firstName, email);

        person.setNotifications(notifications);
        person.setPermissions(permissions);

        KeyPair keyPair = keyPairService.generate(loginName);
        person.setPrivateKey(keyPair.getPrivate().getEncoded());
        person.setPublicKey(keyPair.getPublic().getEncoded());

        save(person);

        LOG.info("Created: " + person.toString());

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

        LOG.info("Updated: " + person.toString());

        return person;
    }


    @Override
    public void save(Person person) {

        personDAO.save(person);
    }


    @Override
    public Optional<Person> getPersonByID(Integer id) {

        return Optional.ofNullable(personDAO.findOne(id));
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
            .collect(Collectors.toList());
    }


    @Override
    public List<Person> getInactivePersons() {

        return personDAO.findAll().stream().filter(person -> person.hasRole(Role.INACTIVE))
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
