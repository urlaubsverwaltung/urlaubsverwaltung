package org.synyx.urlaubsverwaltung.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.stereotype.Component;
import org.synyx.urlaubsverwaltung.security.config.SecurityLdapConfigurationProperties;


/**
 * Context source for syncing data from LDAP.
 */
@Component("ldapContextSourceSync")
@ConditionalOnExpression("'${uv.security.auth}'=='ldap' and '${uv.security.ldap.sync.enabled}'=='true'")
public class LdapContextSourceForLdapSync extends LdapContextSource {

    @Autowired
    public LdapContextSourceForLdapSync(SecurityLdapConfigurationProperties ldapProperties) {

        super();

        final String base = ldapProperties.getBase();
        final String userSearchBase = ldapProperties.getSync().getUserSearchBase();

        this.setUrl(ldapProperties.getUrl());
        this.setBase(userSearchBase + "," + base);
        this.setUserDn(ldapProperties.getSync().getUserDn());
        this.setPassword(ldapProperties.getSync().getPassword());
    }
}
