package org.synyx.urlaubsverwaltung.security;

import java.security.Principal;

public class CustomPrincipal implements Principal {

    private Integer id;
    private String username;

    public CustomPrincipal(Integer id, String username) {
        this.id = id;
        this.username = username;
    }

    @Override
    public String getName() {
        return username;
    }

    public Integer getId() {
        return id;
    }
}
