package org.synyx.urlaubsverwaltung.security.ldap;

import org.springframework.security.core.GrantedAuthority;
import org.synyx.urlaubsverwaltung.person.Role;

import java.util.Collection;


/**
 * Helper class for tests concerning security relevant stuff.
 */
public class SecurityTestUtil {

    public static boolean authorityForRoleExists(Collection<? extends GrantedAuthority> authorities, final Role role) {

        return authorities.stream().anyMatch(authority -> authority.getAuthority().equals(role.name()));
    }
}
