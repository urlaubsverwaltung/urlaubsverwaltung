package org.synyx.urlaubsverwaltung.security;

import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;

import org.springframework.ldap.core.LdapTemplate;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class LdapUserServiceImplTest {

    private LdapTemplate ldapTemplate;

    private LdapUserService ldapUserService;

    @Before
    public void setUp() {

        ldapTemplate = Mockito.mock(LdapTemplate.class);
        ldapUserService = new LdapUserServiceImpl(ldapTemplate);
    }


    @Test
    public void ensureUsesLdapTemplateToFetchUsers() {

        ldapUserService.getLdapUsers();

        Mockito.verify(ldapTemplate).findAll(LdapUser.class);
    }
}
