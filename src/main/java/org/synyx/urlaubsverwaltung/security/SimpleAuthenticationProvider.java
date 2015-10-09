package org.synyx.urlaubsverwaltung.security;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.StandardPasswordEncoder;

import org.springframework.stereotype.Component;


/**
 * Daniel Hammann - <hammann@synyx.de>.
 *
 * <p>Provides authentication with password, which is saved in database.</p>
 */

@Component
public class SimpleAuthenticationProvider implements AuthenticationProvider {

    private final DevUserDetailsService userDetailsService;

    public SimpleAuthenticationProvider(DevUserDetailsService userDetailsService) {

        this.userDetailsService = userDetailsService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        StandardPasswordEncoder encoder = new StandardPasswordEncoder();

        String username = authentication.getName();
        String rawPassword = authentication.getCredentials().toString();

        UserDetails user = userDetailsService.loadUserByUsername(username);

        if (user == null) {
            throw new UsernameNotFoundException("No authentication possible for user = " + username);
        }

        if (encoder.matches(rawPassword, user.getPassword())) {
            return new UsernamePasswordAuthenticationToken(username, user.getPassword(), user.getAuthorities());
        } else {
            throw new BadCredentialsException("Bad password");
        }
    }


    @Override
    public boolean supports(Class<?> authentication) {

        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}
