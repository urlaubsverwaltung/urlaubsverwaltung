package org.synyx.urlaubsverwaltung.security.ldap;

import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;


/**
 * LDAP template to fetch data from LDAP or Active Directory.
 */
public class UVLdapTemplate extends LdapTemplate {

    UVLdapTemplate(LdapContextSource contextSource) {

        super(contextSource);

        this.setIgnorePartialResultException(true);
        this.setIgnoreNameNotFoundException(true);
    }
}
