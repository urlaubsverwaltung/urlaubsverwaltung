package org.synyx.urlaubsverwaltung.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.stereotype.Component;
import org.synyx.urlaubsverwaltung.security.config.SecurityActiveDirectoryConfigurationProperties;


/**
 * Context source for syncing data from Active Directory.
 */
@Component("ldapContextSourceSync")
@ConditionalOnExpression("'${auth}'=='activeDirectory' and '${uv.security.activeDirectory.sync}'=='true'")
public class LdapContextSourceForActiveDirectorySync extends LdapContextSource {

    @Autowired
    public LdapContextSourceForActiveDirectorySync(SecurityActiveDirectoryConfigurationProperties adProperties) {

        super();

        this.setUrl(adProperties.getUrl());
        this.setBase(adProperties.getSync().getUserSearchBase());
        this.setUserDn(adProperties.getSync().getUserDn());
        this.setPassword(adProperties.getSync().getPassword());
    }
}
