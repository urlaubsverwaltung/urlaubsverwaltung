package org.synyx.urlaubsverwaltung.security;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class SecurityProvider {

    public boolean loggedInUserRequestsOwnData(Authentication authentication, Integer id) {
        return ((CustomPrincipal) authentication.getPrincipal()).getId().equals(id);
    }
}
