package org.synyx.urlaubsverwaltung.security.ldap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ldap.core.LdapTemplate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LdapUserServiceImplTest {

    private LdapUserService sut;

    @Mock
    private LdapTemplate ldapTemplate;
    @Mock
    private LdapUserMapper ldapUserMapper;

    @BeforeEach
    void setUp() {

        sut = new LdapUserServiceImpl(ldapTemplate, ldapUserMapper, new DirectoryServiceSecurityProperties());
    }

    @Test
    void ensureUsesLdapTemplateToFetchUsers() {

        sut.getLdapUsers();

        verify(ldapTemplate).search(any(), eq(ldapUserMapper));
    }
}
