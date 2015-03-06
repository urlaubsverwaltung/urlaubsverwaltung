package org.synyx.urlaubsverwaltung.core.person;

import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import org.synyx.urlaubsverwaltung.core.mail.MailNotification;
import org.synyx.urlaubsverwaltung.security.Role;

import java.util.List;


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
    public Person getPersonByID(Integer id) {

        return personDAO.findOne(id);
    }


    @Override
    public Person getPersonByLogin(String loginName) {

        return personDAO.findByLoginName(loginName);
    }


    @Override
    public List<Person> getActivePersons() {

        return FluentIterable.from(personDAO.findAll()).filter(new Predicate<Person>() {

                    @Override
                    public boolean apply(Person person) {

                        return !person.hasRole(Role.INACTIVE);
                    }
                }).toList();
    }


    @Override
    public List<Person> getInactivePersons() {

        return FluentIterable.from(personDAO.findAll()).filter(new Predicate<Person>() {

                    @Override
                    public boolean apply(Person person) {

                        return person.hasRole(Role.INACTIVE);
                    }
                }).toList();
    }


    @Override
    public List<Person> getPersonsByRole(final Role role) {

        return Lists.newArrayList(Iterables.filter(getActivePersons(), new Predicate<Person>() {

                        @Override
                        public boolean apply(Person person) {

                            return person.hasRole(role);
                        }
                    }));
    }


    @Override
    public List<Person> getPersonsWithNotificationType(final MailNotification notification) {

        return Lists.newArrayList(Iterables.filter(getActivePersons(), new Predicate<Person>() {

                        @Override
                        public boolean apply(Person person) {

                            return person.hasNotificationType(notification);
                        }
                    }));
    }
}
