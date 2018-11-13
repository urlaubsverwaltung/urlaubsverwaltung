package org.synyx.urlaubsverwaltung.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;

import org.springframework.ldap.core.support.LdapContextSource;

import org.springframework.stereotype.Component;


/**
 * Context source for syncing data from Active Directory.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Component("ldapContextSourceSync")
@ConditionalOnExpression("'${auth}'=='activeDirectory' and '${uv.security.activeDirectory.sync}'=='true'")
public class LdapContextSourceForActiveDirectorySync extends LdapContextSource {

    @Autowired
    public LdapContextSourceForActiveDirectorySync(@Value("${uv.security.activeDirectory.url}") String url,
        @Value("${uv.security.activeDirectory.sync.userSearchBase}") String userSearchBase,
        @Value("${uv.security.activeDirectory.sync.userDn}") String userDn,
        @Value("${uv.security.activeDirectory.sync.password}") String password) {

        super();

        this.setUrl(url);
        this.setBase(userSearchBase);
        this.setUserDn(userDn);
        this.setPassword(password);
    }
}
