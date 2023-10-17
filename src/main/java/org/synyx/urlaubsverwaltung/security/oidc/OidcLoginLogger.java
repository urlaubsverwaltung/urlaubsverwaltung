package org.synyx.urlaubsverwaltung.security.oidc;

import org.slf4j.Logger;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.util.Optional;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

@Transactional(readOnly = true)
class OidcLoginLogger {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final PersonService personService;

    OidcLoginLogger(PersonService personService) {
        this.personService = personService;
    }

    @EventListener
    public void handle(AuthenticationSuccessEvent event) {

        if (event.getAuthentication().getPrincipal() instanceof Jwt) {
            return;
        }

        final String userUniqueID = event.getAuthentication().getName();
        final Optional<Person> optionalPerson = personService.getPersonByUsername(userUniqueID);
        optionalPerson.ifPresentOrElse(
            person -> LOG.info("User '{}' has signed in", person.getId()),
            () -> LOG.error("Could not find signed-in user with id '{}'", userUniqueID)
        );
    }
}
