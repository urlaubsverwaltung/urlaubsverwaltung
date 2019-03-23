package org.synyx.urlaubsverwaltung.security;

/**
 * Is thrown in case of the security configuration (LDAP/AD) seems to be invalid.
 */
public class InvalidSecurityConfigurationException extends IllegalStateException {

    public InvalidSecurityConfigurationException(String message) {

        super(message);
    }
}
