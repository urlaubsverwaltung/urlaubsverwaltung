package org.synyx.urlaubsverwaltung.security;

import org.synyx.urlaubsverwaltung.core.util.SystemPropertyCondition;


/**
 * Condition matching if authentication via Active Directory is used.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class ActiveDirectoryAuthenticationCondition extends SystemPropertyCondition {

    public ActiveDirectoryAuthenticationCondition() {

        super(Authentication.PROPERTY_KEY, Authentication.Type.ACTIVE_DIRECTORY.getName());
    }
}
