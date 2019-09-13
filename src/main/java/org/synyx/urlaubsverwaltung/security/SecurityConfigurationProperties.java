package org.synyx.urlaubsverwaltung.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("uv.security")
public class SecurityConfigurationProperties {

    public enum AuthenticationProvider {

        DEFAULT,
        OIDC,
        LDAP,
        ACTIVEDIRECTORY
    }

    private AuthenticationProvider auth;
    private boolean basicAuth;

    public AuthenticationProvider getAuth() {
        return auth;
    }

    public void setAuth(AuthenticationProvider auth) {
        this.auth = auth;
    }

    public boolean isBasicAuth() {
        return basicAuth;
    }

    public void setBasicAuth(boolean basicAuth) {
        this.basicAuth = basicAuth;
    }
}

