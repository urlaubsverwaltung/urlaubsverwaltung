package org.synyx.urlaubsverwaltung.security.oidc;

import org.springframework.security.core.AuthenticationException;

public class OidcPersonMappingException extends AuthenticationException {
    public OidcPersonMappingException(String message) {
        super(message);
    }
}
