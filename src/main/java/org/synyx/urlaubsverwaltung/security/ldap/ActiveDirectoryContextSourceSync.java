package org.synyx.urlaubsverwaltung.security.ldap;

import org.springframework.ldap.core.support.LdapContextSource;


/**
 * Context source for syncing data from Active Directory.
 */
public class ActiveDirectoryContextSourceSync extends LdapContextSource {

    ActiveDirectoryContextSourceSync(ActiveDirectorySecurityConfigurationProperties adProperties) {

        super();

        this.setUrl(adProperties.getUrl());
        this.setBase(adProperties.getSync().getUserSearchBase());
        this.setUserDn(adProperties.getSync().getUserDn());
        this.setPassword(adProperties.getSync().getPassword());
    }
}
