package org.synyx.urlaubsverwaltung.security;

/**
 * Condition matching if authentication via LDAP is used.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class LdapAuthenticationCondition extends SystemPropertyCondition {

    private static final String AUTH_PROPERTY = "auth";
    private static final String AUTH_VALUE = "ldap";

    public LdapAuthenticationCondition() {

        super(AUTH_PROPERTY, AUTH_VALUE);
    }
}
