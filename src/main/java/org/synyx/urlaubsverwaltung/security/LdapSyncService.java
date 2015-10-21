package org.synyx.urlaubsverwaltung.security;

import org.apache.log4j.Logger;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.context.annotation.Conditional;

import org.springframework.stereotype.Service;

import java.util.List;

import javax.annotation.PostConstruct;


/**
 * Syncs the person data from configured LDAP.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Service
@Conditional(LdapAuthenticationCondition.class)
public class LdapSyncService {

    private static final Logger LOG = Logger.getLogger(LdapSyncService.class);

    private final LdapUserService ldapUserService;

    @Autowired
    public LdapSyncService(LdapUserService ldapUserService) {

        this.ldapUserService = ldapUserService;
    }

    @PostConstruct
    public void sync() {

        LOG.info("STARTING LDAP SYNC --------------------------------------------------------------------------------");

        String authentication = System.getProperty(Authentication.PROPERTY_KEY);

        if (authentication == null) {
            throw new IllegalStateException("LDAP sync is not possible if authentication type is not set!");
        }

        if (!authentication.toLowerCase().equals(Authentication.Type.LDAP.getName())) {
            throw new IllegalStateException("LDAP sync is not possible for authentication type '" + authentication
                + "'!");
        }

        List<LdapUser> users = ldapUserService.getLdapUsers();

        LOG.info("Found " + users.size() + " users");

        for (LdapUser user : users) {
            LOG.info(user.toString());
        }

        LOG.info("DONE LDAP SYNC ------------------------------------------------------------------------------------");
    }
}
