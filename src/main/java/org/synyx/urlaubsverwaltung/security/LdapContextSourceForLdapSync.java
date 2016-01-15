package org.synyx.urlaubsverwaltung.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;

import org.springframework.ldap.core.support.LdapContextSource;

import org.springframework.stereotype.Component;


/**
 * Context source for syncing data from LDAP.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Component("ldapContextSource")
@ConditionalOnExpression("'${auth}'=='ldap'")
public class LdapContextSourceForLdapSync extends LdapContextSource {

    @Autowired
    public LdapContextSourceForLdapSync(@Value("${uv.security.ldap.url}") String url,
        @Value("${uv.security.ldap.sync.userSearchBase}") String userSearchBase,
        @Value("${uv.security.ldap.base}") String base,
        @Value("${uv.security.ldap.sync.userDn}") String userDn,
        @Value("${uv.security.ldap.sync.password}") String password) {

        super();

        this.setUrl(url);
        this.setBase(userSearchBase + "," + base);
        this.setUserDn(userDn);
        this.setPassword(password);
    }
}
