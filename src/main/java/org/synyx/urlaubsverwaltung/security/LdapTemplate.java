package org.synyx.urlaubsverwaltung.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.stereotype.Component;


/**
 * LDAP template to fetch data from LDAP or Active Directory.
 */
@Component
@ConditionalOnExpression(
    "('${uv.security.auth}'=='activeDirectory' and '${uv.security.activeDirectory.sync.enabled}'=='true') or ('${uv.security.auth}'=='ldap' and '${uv.security.ldap.sync.enabled}'=='true')" // NOSONAR
)
public class LdapTemplate extends org.springframework.ldap.core.LdapTemplate {

    @Autowired
    public LdapTemplate(@Qualifier("ldapContextSourceSync") LdapContextSource contextSource) {

        super(contextSource);

        this.setIgnorePartialResultException(true);
        this.setIgnoreNameNotFoundException(true);
    }
}
