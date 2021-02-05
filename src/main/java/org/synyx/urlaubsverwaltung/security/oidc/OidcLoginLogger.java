package org.synyx.urlaubsverwaltung.security.oidc;

import org.slf4j.Logger;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.transaction.annotation.Transactional;
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
        parseUsername(event.getAuthentication())
            .ifPresent(username ->
                personService.getPersonByUsername(username).ifPresentOrElse(
                    person -> LOG.info("User '{}' has signed in", person.getId()),
                    () -> LOG.error("Could not find signed-in user with id '{}'", username)
                )
            );
    }

    private Optional<String> parseUsername(Authentication authentication) {
        if (authentication.getPrincipal() instanceof OidcUser) {
            final OidcUser user = (OidcUser) authentication.getPrincipal();
            return Optional.of(user.getIdToken().getSubject());

        } else if (authentication.getPrincipal() instanceof Jwt) {
            final Jwt jwt = (Jwt) authentication.getPrincipal();
            return Optional.of(jwt.getSubject());

        } else {
            return Optional.empty();
        }
    }
}
