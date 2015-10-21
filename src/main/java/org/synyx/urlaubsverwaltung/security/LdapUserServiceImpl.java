package org.synyx.urlaubsverwaltung.security;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.context.annotation.Conditional;

import org.springframework.ldap.core.LdapTemplate;

import org.springframework.stereotype.Service;

import java.util.List;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Service
@Conditional(LdapOrActiveDirectoryAuthenticationCondition.class)
public class LdapUserServiceImpl implements LdapUserService {

    private final LdapTemplate ldapTemplate;

    @Autowired
    public LdapUserServiceImpl(LdapTemplate ldapTemplate) {

        this.ldapTemplate = ldapTemplate;
    }

    @Override
    public List<LdapUser> getLdapUsers() {

        return ldapTemplate.findAll(LdapUser.class);
    }
}
