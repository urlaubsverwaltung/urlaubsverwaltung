package org.synyx.urlaubsverwaltung.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;

public final class AuthenticationHelper {

    private AuthenticationHelper() {
        // ok
    }

    public static String userName(Authentication authentication) {
        String username = null;
        final Object principal = authentication.getPrincipal();
        if (principal instanceof org.springframework.security.ldap.userdetails.Person) {
            username = ((org.springframework.security.ldap.userdetails.Person) principal).getUsername();
        } else if (principal instanceof User) {
            username = ((User) principal).getUsername();
        } else if (principal instanceof DefaultOidcUser) {
            username = ((DefaultOidcUser) principal).getIdToken().getSubject();
        }
        return username;
    }
}
