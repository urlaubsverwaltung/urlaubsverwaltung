package org.synyx.urlaubsverwaltung.security;

import org.synyx.urlaubsverwaltung.core.util.SystemPropertyCondition;


/**
 * Condition matching if authentication via LDAP is used.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class LdapAuthenticationCondition extends SystemPropertyCondition {

    public LdapAuthenticationCondition() {

        super(Authentication.PROPERTY_KEY, Authentication.Type.LDAP.getName());
    }
}
