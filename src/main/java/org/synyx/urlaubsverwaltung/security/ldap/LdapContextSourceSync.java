package org.synyx.urlaubsverwaltung.security.ldap;

import org.springframework.ldap.core.support.LdapContextSource;


/**
 * Context source for syncing data from LDAP.
 */
public class LdapContextSourceSync extends LdapContextSource {

    LdapContextSourceSync(LdapSecurityConfigurationProperties ldapProperties) {

        super();

        final String base = ldapProperties.getBase();
        final String userSearchBase = ldapProperties.getSync().getUserSearchBase();

        this.setUrl(ldapProperties.getUrl());
        this.setBase(userSearchBase + "," + base);
        this.setUserDn(ldapProperties.getSync().getUserDn());
        this.setPassword(ldapProperties.getSync().getPassword());
    }
}
