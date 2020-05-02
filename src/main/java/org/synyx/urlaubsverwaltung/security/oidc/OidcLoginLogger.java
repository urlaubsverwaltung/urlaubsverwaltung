package org.synyx.urlaubsverwaltung.security.oidc;

import org.slf4j.Logger;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
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

    public OidcLoginLogger(PersonService personService) {

        this.personService = personService;
    }

    @EventListener
    public void handle(AuthenticationSuccessEvent event) {
        final Authentication authentication = event.getAuthentication();
        final OidcUser user = (OidcUser) authentication.getPrincipal();
        final String userUniqueID = user.getIdToken().getSubject();

        final Optional<Person> optionalPerson = personService.getPersonByUsername(userUniqueID);
        optionalPerson.ifPresentOrElse(
            person -> LOG.info("User '{}' has signed in", person.getId()),
            () -> LOG.error("Could not find signed-in user with id '{}'", userUniqueID)
        );
    }
}
