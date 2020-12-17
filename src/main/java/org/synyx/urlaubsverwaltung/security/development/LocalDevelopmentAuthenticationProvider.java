package org.synyx.urlaubsverwaltung.security.development;

import org.slf4j.Logger;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.util.Collection;
import java.util.Optional;

import static java.lang.invoke.MethodHandles.lookup;
import static java.util.stream.Collectors.toList;
import static org.slf4j.LoggerFactory.getLogger;
import static org.synyx.urlaubsverwaltung.person.Role.INACTIVE;

/**
 * Provides authentication with password, which is saved in database.
 */
class LocalDevelopmentAuthenticationProvider implements AuthenticationProvider {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final PersonService personService;

    LocalDevelopmentAuthenticationProvider(PersonService personService) {
        this.personService = personService;
    }

    @Override
    public Authentication authenticate(final Authentication authentication) {

        final String username = authentication.getName();

        final Optional<Person> userOptional = personService.getPersonByUsername(username);
        if (userOptional.isEmpty()) {
            LOG.info("No user found for provided username '{}'", username);
            throw new UsernameNotFoundException("No authentication possible for user = " + username);
        }

        final Person person = userOptional.get();
        if (person.hasRole(INACTIVE)) {
            LOG.info("User '{}' has been deactivated and can not sign in therefore", person.getId());
            throw new DisabledException("User with the id '" + person.getId() + "' has been deactivated");
        }

        final Collection<GrantedAuthority> grantedAuthorities = person.getPermissions().stream()
            .map(role -> new SimpleGrantedAuthority(role.name()))
            .collect(toList());

        return new UsernamePasswordAuthenticationToken(person.getUsername(), "", grantedAuthorities);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}
