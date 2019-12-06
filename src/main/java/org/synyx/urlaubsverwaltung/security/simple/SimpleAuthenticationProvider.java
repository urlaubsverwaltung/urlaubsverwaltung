package org.synyx.urlaubsverwaltung.security.simple;

import org.slf4j.Logger;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.Role;

import java.util.Collection;
import java.util.Optional;

import static java.lang.invoke.MethodHandles.lookup;
import static java.util.stream.Collectors.toList;
import static org.slf4j.LoggerFactory.getLogger;
import static org.synyx.urlaubsverwaltung.person.Role.INACTIVE;

/**
 * Provides authentication with password, which is saved in database.
 */
public class SimpleAuthenticationProvider implements AuthenticationProvider {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final PersonService personService;
    private final PasswordEncoder passwordEncoder;

    SimpleAuthenticationProvider(PersonService personService, PasswordEncoder passwordEncoder) {

        this.personService = personService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Authentication authenticate(final Authentication authentication) {

        final String providedUsername = authentication.getName();

        final Optional<Person> userOptional = personService.getPersonByUsername(providedUsername);
        if (userOptional.isEmpty()) {
            final String cleanedProvidedUsername = providedUsername.replaceAll("[\n|\r|\t]", "_");
            LOG.info("No user found for provided username '{}'", cleanedProvidedUsername);
            throw new UsernameNotFoundException("No authentication possible for user = " + cleanedProvidedUsername);
        }

        final Person person = userOptional.get();

        if (person.hasRole(INACTIVE)) {
            LOG.info("User '{}' has been deactivated and can not sign in therefore", person.getId());
            throw new DisabledException("User with the id '" + person.getId() + "' has been deactivated");
        }

        final Collection<Role> permissions = person.getPermissions();
        final Collection<GrantedAuthority> grantedAuthorities = permissions.stream()
            .map(role -> new SimpleGrantedAuthority(role.name()))
            .collect(toList());

        final String providedPassword = authentication.getCredentials().toString();
        final String userPassword = person.getPassword();

        if (passwordEncoder.matches(providedPassword, userPassword)) {
            LOG.info("User '{}' has signed in with roles: {}", person.getId(), grantedAuthorities);

            return new UsernamePasswordAuthenticationToken(new User(person.getUsername(), userPassword, grantedAuthorities), userPassword, grantedAuthorities);
        } else {
            LOG.info("User '{}' has tried to sign in with a wrong password", person.getId());

            throw new BadCredentialsException("The provided password is wrong");
        }
    }


    @Override
    public boolean supports(Class<?> authentication) {

        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}
