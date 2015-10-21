package org.synyx.urlaubsverwaltung.security;

import java.util.List;


/**
 * Provides fetching of LDAP users data.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public interface LdapUserService {

    /**
     * Get all LDAP users.
     *
     * @return  list of LDAP users
     */
    List<LdapUser> getLdapUsers();
}
