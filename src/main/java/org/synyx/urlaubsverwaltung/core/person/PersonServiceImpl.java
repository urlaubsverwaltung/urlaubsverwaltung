package org.synyx.urlaubsverwaltung.core.person;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import org.synyx.urlaubsverwaltung.core.mail.MailNotification;
import org.synyx.urlaubsverwaltung.security.Role;

import java.util.List;
import java.util.stream.Collectors;

import static org.synyx.urlaubsverwaltung.security.Role.*;


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

        return Optional.fromNullable(personDAO.findOne(id));
    }


    @Override
    public Optional<Person> getPersonByLogin(String loginName) {

        return Optional.fromNullable(personDAO.findByLoginName(loginName));
    }


    @Override
    public List<Person> getActivePersons() {

        return personDAO.findAll().stream().
                filter(person -> !person.hasRole(INACTIVE)).
                collect(Collectors.toList());

    }


    @Override
    public List<Person> getInactivePersons() {

        return personDAO.findAll().stream().
                filter(person -> person.hasRole(INACTIVE)).
                collect(Collectors.toList());
    }


    @Override
    public List<Person> getPersonsByRole(final Role role) {

        return getActivePersons().stream().
                filter(person -> person.hasRole(role)).
                collect(Collectors.toList());
    }


    @Override
    public List<Person> getPersonsWithNotificationType(final MailNotification notification) {

        return getActivePersons().stream().
                filter(person -> person.hasNotificationType(notification)).
                collect(Collectors.toList());
    }
}
