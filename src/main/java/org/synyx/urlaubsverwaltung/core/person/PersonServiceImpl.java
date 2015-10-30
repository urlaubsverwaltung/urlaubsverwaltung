package org.synyx.urlaubsverwaltung.core.person;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.synyx.urlaubsverwaltung.core.person.Role.INACTIVE;


/**
 * Implementation for {@link PersonService}.
 *
 * @author  Aljona Murygina
 * @author  Johannes Reuter
 */
@Service("personService")
class PersonServiceImpl implements PersonService {

    private final PersonDAO personDAO;

    @Autowired
    public PersonServiceImpl(PersonDAO personDAO) {

        this.personDAO = personDAO;
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

        return personDAO.findAll().stream().filter(person -> !person.hasRole(INACTIVE)).collect(Collectors.toList());
    }


    @Override
    public List<Person> getInactivePersons() {

        return personDAO.findAll().stream().filter(person -> person.hasRole(INACTIVE)).collect(Collectors.toList());
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
