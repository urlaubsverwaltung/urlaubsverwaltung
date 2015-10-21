package org.synyx.urlaubsverwaltung.security;

/**
 * Condition matching if default authentication is used.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class DefaultAuthenticationCondition extends SystemPropertyCondition {

    private static final String AUTH_PROPERTY = "auth";
    private static final String AUTH_VALUE = "default";

    public DefaultAuthenticationCondition() {

        super(AUTH_PROPERTY, AUTH_VALUE);
    }
}
