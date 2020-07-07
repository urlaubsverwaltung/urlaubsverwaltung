package org.synyx.urlaubsverwaltung.security.ldap;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.query.ContainerCriteria;
import org.synyx.urlaubsverwaltung.security.ldap.DirectoryServiceSecurityProperties.SecurityFilter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LdapUserServiceImplTest {

    @Mock
    private LdapTemplate ldapTemplate;
    @Mock
    private LdapUserMapper ldapUserMapper;

    @Test
    void getLdapUsersWithoutMembersOf() {
        final SecurityFilter securityFilter = new SecurityFilter();
        securityFilter.setMemberOf("");
        securityFilter.setObjectClass("objectClass");

        final DirectoryServiceSecurityProperties properties = new DirectoryServiceSecurityProperties();
        properties.setFilter(securityFilter);

        ArgumentCaptor<ContainerCriteria> captor = ArgumentCaptor.forClass(ContainerCriteria.class);

        final LdapUserService sut = new LdapUserServiceImpl(ldapTemplate, ldapUserMapper, properties);
        sut.getLdapUsers();

        verify(ldapTemplate).search(captor.capture(), eq(ldapUserMapper));
        assertThat(captor.getValue().filter().encode()).isEqualTo("(objectClass=objectClass)");
    }

    @Test
    void getLdapUsersWithMembersOf() {
        final SecurityFilter securityFilter = new SecurityFilter();
        securityFilter.setMemberOf("membersOf");
        securityFilter.setObjectClass("objectClass");

        final DirectoryServiceSecurityProperties properties = new DirectoryServiceSecurityProperties();
        properties.setFilter(securityFilter);

        ArgumentCaptor<ContainerCriteria> captor = ArgumentCaptor.forClass(ContainerCriteria.class);

        final LdapUserService sut = new LdapUserServiceImpl(ldapTemplate, ldapUserMapper, properties);
        sut.getLdapUsers();

        verify(ldapTemplate).search(captor.capture(), eq(ldapUserMapper));
        assertThat(captor.getValue().filter().encode()).isEqualTo("(&(objectClass=objectClass)(memberOf=membersOf))");
    }
}
