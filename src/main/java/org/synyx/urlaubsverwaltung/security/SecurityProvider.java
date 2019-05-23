package org.synyx.urlaubsverwaltung.security;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service
public class SecurityProvider {

    public boolean loggedInUserRequestsOwnData(Authentication authentication, Integer id) {

        Assert.notNull(id, "Missing ID");
        Assert.notNull(authentication, "Missing authentication");

        return ((CustomPrincipal) authentication.getPrincipal()).getId().equals(id);
    }
}
