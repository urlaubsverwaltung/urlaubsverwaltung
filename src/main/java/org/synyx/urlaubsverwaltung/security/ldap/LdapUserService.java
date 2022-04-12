package org.synyx.urlaubsverwaltung.security.ldap;

import java.util.List;

/**
 * Provides fetching of LDAP users data.
 */
public interface LdapUserService {

    /**
     * Returns all LDAP users
     * if no error occurs while retrieving data or mapping into {@link LdapUser}
     * otherwise returns an empty list
     *
     * @return list of LDAP users
     */
    List<LdapUser> getLdapUsers();
}
