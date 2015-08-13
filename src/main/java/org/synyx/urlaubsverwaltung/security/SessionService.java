package org.synyx.urlaubsverwaltung.security;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.stereotype.Service;

import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.PersonService;

import java.util.Optional;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Service
public class SessionService {

    private final PersonService personService;

    @Autowired
    public SessionService(PersonService personService) {

        this.personService = personService;
    }

    /**
     * This method allows to get a person by logged-in user.
     *
     * @return  Person that is logged in
     */
    public Person getLoggedUser() {

        String user = SecurityContextHolder.getContext().getAuthentication().getName();

        Optional<Person> person = personService.getPersonByLogin(user);

        if (!person.isPresent()) {
            throw new IllegalStateException("Can not get the person for the signed in user with username = " + user);
        }

        return person.get();
    }


    public boolean isOffice() {

        return getLoggedUser().hasRole(Role.OFFICE);
    }


    public boolean isBoss() {

        return getLoggedUser().hasRole(Role.BOSS);
    }
}
