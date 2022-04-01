package org.synyx.urlaubsverwaltung.security.ldap;

import org.springframework.security.core.AuthenticationException;

public class LdapPersonMappingException extends AuthenticationException {

    LdapPersonMappingException(String message) {
        super(message);
    }
}
