package org.synyx.urlaubsverwaltung.security;

import java.util.List;


/**
 * Provides fetching of LDAP users data.
 */
public interface LdapUserService {

    /**
     * Get all LDAP users.
     *
     * @return  list of LDAP users
     */
    List<LdapUser> getLdapUsers();
}
