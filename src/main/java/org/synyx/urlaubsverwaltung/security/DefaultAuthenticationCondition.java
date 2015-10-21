package org.synyx.urlaubsverwaltung.security;

import org.synyx.urlaubsverwaltung.core.util.SystemPropertyCondition;


/**
 * Condition matching if default authentication is used.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class DefaultAuthenticationCondition extends SystemPropertyCondition {

    public DefaultAuthenticationCondition() {

        super(Authentication.PROPERTY_KEY, Authentication.Type.DEFAULT.getName());
    }
}
