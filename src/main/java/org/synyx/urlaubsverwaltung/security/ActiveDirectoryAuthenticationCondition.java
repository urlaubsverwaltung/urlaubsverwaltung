package org.synyx.urlaubsverwaltung.security;

/**
 * Condition matching if authentication via Acitve Directory is used.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class ActiveDirectoryAuthenticationCondition extends SystemPropertyCondition {

    private static final String AUTH_PROPERTY = "auth";
    private static final String AUTH_VALUE = "activeDirectory";

    public ActiveDirectoryAuthenticationCondition() {

        super(AUTH_PROPERTY, AUTH_VALUE);
    }
}
