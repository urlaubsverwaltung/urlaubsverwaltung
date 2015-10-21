package org.synyx.urlaubsverwaltung.security;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;

import org.springframework.core.type.AnnotatedTypeMetadata;


/**
 * Condition matching if either LDAP authentication or Active Directory authentication is used.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class LdapOrActiveDirectoryAuthenticationCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {

        LdapAuthenticationCondition ldapCondition = new LdapAuthenticationCondition();
        ActiveDirectoryAuthenticationCondition activeDirectoryCondition = new ActiveDirectoryAuthenticationCondition();

        return ldapCondition.matches(context, metadata) || activeDirectoryCondition.matches(context, metadata);
    }
}
