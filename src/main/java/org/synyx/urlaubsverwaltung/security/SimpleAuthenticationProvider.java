package org.synyx.urlaubsverwaltung.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.StandardPasswordEncoder;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.PersonService;
import org.synyx.urlaubsverwaltung.core.person.Role;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * Provides authentication with password, which is saved in database.
 *
 * @author  Daniel Hammann - <hammann@synyx.de>
 */
public class SimpleAuthenticationProvider implements AuthenticationProvider {

    private static final Logger LOG = LoggerFactory.getLogger(SimpleAuthenticationProvider.class);

    private final PersonService personService;

    public SimpleAuthenticationProvider(PersonService personService) {

        this.personService = personService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) {

        StandardPasswordEncoder encoder = new StandardPasswordEncoder();

        String username = authentication.getName();
        String rawPassword = authentication.getCredentials().toString();

        Optional<Person> userOptional = personService.getPersonByLogin(username);

        if (!userOptional.isPresent()) {
            LOG.info("No user found for username '{}'", username);

            throw new UsernameNotFoundException("No authentication possible for user = " + username);
        }

        Person person = userOptional.get();

        if (person.hasRole(Role.INACTIVE)) {
            LOG.info("User '{}' has been deactivated and can not sign in therefore", username);
            throw new DisabledException("User '" + username + "' has been deactivated");
        }

        Collection<Role> permissions = person.getPermissions();
        Collection<GrantedAuthority> grantedAuthorities = permissions.stream().map((role) ->
                    new SimpleGrantedAuthority(role.name())).collect(Collectors.toList());

        String userPassword = person.getPassword();

        if (encoder.matches(rawPassword, userPassword)) {
            LOG.info("User '{}' has signed in with roles: {}", username, grantedAuthorities);

            return new UsernamePasswordAuthenticationToken(username, userPassword, grantedAuthorities);
        } else {
            LOG.info("User '{}' has tried to sign in with a wrong password", username);

            throw new BadCredentialsException("The provided password is wrong");
        }
    }


    @Override
    public boolean supports(Class<?> authentication) {

        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}
