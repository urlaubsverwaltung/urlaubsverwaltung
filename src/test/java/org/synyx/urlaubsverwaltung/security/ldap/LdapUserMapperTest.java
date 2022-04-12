package org.synyx.urlaubsverwaltung.security.ldap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ldap.core.DirContextOperations;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LdapUserMapperTest {

    private static final String IDENTIFIER_ATTRIBUTE = "uid";
    private static final String FIRST_NAME_ATTRIBUTE = "givenName";
    private static final String LAST_NAME_ATTRIBUTE = "sn";
    private static final String MAIL_ADDRESS_ATTRIBUTE = "mail";
    private static final String MEMBER_OF_ATTRIBUTE = "memberOf";

    private static final String MEMBER_OF_FILTER = "CN=mygroup,DC=mydomain,DC=com";

    private LdapUserMapper ldapUserMapper;

    @BeforeEach
    void setUp() {

        final DirectoryServiceSecurityProperties directoryServiceSecurityProperties = new DirectoryServiceSecurityProperties();
        directoryServiceSecurityProperties.setIdentifier(IDENTIFIER_ATTRIBUTE);
        directoryServiceSecurityProperties.setFirstName(FIRST_NAME_ATTRIBUTE);
        directoryServiceSecurityProperties.setLastName(LAST_NAME_ATTRIBUTE);
        directoryServiceSecurityProperties.setMailAddress(MAIL_ADDRESS_ATTRIBUTE);
        directoryServiceSecurityProperties.getFilter().setMemberOf(MEMBER_OF_FILTER);

        ldapUserMapper = new LdapUserMapper(directoryServiceSecurityProperties);
    }

    // Map user from attributes ----------------------------------------------------------------------------------------
    @Test
    void ensureCreatesLdapUserFromAttributes() throws NamingException {

        final Attributes attributes = mock(Attributes.class);
        when(attributes.get(IDENTIFIER_ATTRIBUTE)).thenReturn(new BasicAttribute(IDENTIFIER_ATTRIBUTE, "geralt"));
        when(attributes.get(FIRST_NAME_ATTRIBUTE)).thenReturn(new BasicAttribute(FIRST_NAME_ATTRIBUTE, "Geralt"));
        when(attributes.get(LAST_NAME_ATTRIBUTE)).thenReturn(new BasicAttribute(LAST_NAME_ATTRIBUTE, "von Riva"));
        when(attributes.get(MAIL_ADDRESS_ATTRIBUTE)).thenReturn(new BasicAttribute(MAIL_ADDRESS_ATTRIBUTE, "geralt@riva.de"));

        final LdapUser ldapUser = ldapUserMapper.mapFromAttributes(attributes);
        assertThat(ldapUser.getUsername()).isEqualTo("geralt");
        assertThat(ldapUser.getFirstName()).isEqualTo("Geralt");
        assertThat(ldapUser.getLastName()).isEqualTo("von Riva");
        assertThat(ldapUser.getEmail()).isEqualTo("geralt@riva.de");
    }

    @Test
    void ensureThrowsExceptionWithoutUsername() {

        final Attributes attributes = mock(Attributes.class);
        when(attributes.get(IDENTIFIER_ATTRIBUTE)).thenReturn(null);
        when(attributes.get(FIRST_NAME_ATTRIBUTE)).thenReturn(new BasicAttribute(FIRST_NAME_ATTRIBUTE, "Geralt"));
        when(attributes.get(LAST_NAME_ATTRIBUTE)).thenReturn(new BasicAttribute(LAST_NAME_ATTRIBUTE, "von Riva"));
        when(attributes.get(MAIL_ADDRESS_ATTRIBUTE)).thenReturn(new BasicAttribute(MAIL_ADDRESS_ATTRIBUTE, "geralt@riva.de"));

        assertThrows(InvalidSecurityConfigurationException.class, () -> ldapUserMapper.mapFromAttributes(attributes));

        verify(attributes).get(IDENTIFIER_ATTRIBUTE);
        verify(attributes, never()).get(FIRST_NAME_ATTRIBUTE);
        verify(attributes, never()).get(LAST_NAME_ATTRIBUTE);
        verify(attributes, never()).get(MAIL_ADDRESS_ATTRIBUTE);
    }

    @Test
    void ensureThrowsExceptionWithoutFirstname() {

        final Attributes attributes = mock(Attributes.class);
        when(attributes.get(IDENTIFIER_ATTRIBUTE)).thenReturn(new BasicAttribute(IDENTIFIER_ATTRIBUTE, "geralt"));
        when(attributes.get(FIRST_NAME_ATTRIBUTE)).thenReturn(null);
        when(attributes.get(LAST_NAME_ATTRIBUTE)).thenReturn(new BasicAttribute(LAST_NAME_ATTRIBUTE, "von Riva"));
        when(attributes.get(MAIL_ADDRESS_ATTRIBUTE)).thenReturn(new BasicAttribute(MAIL_ADDRESS_ATTRIBUTE, "geralt@riva.de"));

        assertThrows(InvalidSecurityConfigurationException.class, () -> ldapUserMapper.mapFromAttributes(attributes));

        verify(attributes).get(IDENTIFIER_ATTRIBUTE);
        verify(attributes).get(FIRST_NAME_ATTRIBUTE);
        verify(attributes, never()).get(LAST_NAME_ATTRIBUTE);
        verify(attributes, never()).get(MAIL_ADDRESS_ATTRIBUTE);
    }

    @Test
    void ensureThrowsExceptionWithoutLastname() {

        final Attributes attributes = mock(Attributes.class);
        when(attributes.get(IDENTIFIER_ATTRIBUTE)).thenReturn(new BasicAttribute(IDENTIFIER_ATTRIBUTE, "geralt"));
        when(attributes.get(FIRST_NAME_ATTRIBUTE)).thenReturn(new BasicAttribute(FIRST_NAME_ATTRIBUTE, "Geralt"));
        when(attributes.get(LAST_NAME_ATTRIBUTE)).thenReturn(null);
        when(attributes.get(MAIL_ADDRESS_ATTRIBUTE)).thenReturn(new BasicAttribute(MAIL_ADDRESS_ATTRIBUTE, "geralt@riva.de"));

        assertThrows(InvalidSecurityConfigurationException.class, () -> ldapUserMapper.mapFromAttributes(attributes));

        verify(attributes).get(IDENTIFIER_ATTRIBUTE);
        verify(attributes).get(FIRST_NAME_ATTRIBUTE);
        verify(attributes).get(LAST_NAME_ATTRIBUTE);
        verify(attributes, never()).get(MAIL_ADDRESS_ATTRIBUTE);
    }

    @Test
    void ensureThrowsExceptionWithoutEmail() {

        final Attributes attributes = mock(Attributes.class);
        when(attributes.get(IDENTIFIER_ATTRIBUTE)).thenReturn(new BasicAttribute(IDENTIFIER_ATTRIBUTE, "geralt"));
        when(attributes.get(FIRST_NAME_ATTRIBUTE)).thenReturn(new BasicAttribute(FIRST_NAME_ATTRIBUTE, "Geralt"));
        when(attributes.get(LAST_NAME_ATTRIBUTE)).thenReturn(new BasicAttribute(LAST_NAME_ATTRIBUTE, "von Riva"));
        when(attributes.get(MAIL_ADDRESS_ATTRIBUTE)).thenReturn(null);

        assertThrows(InvalidSecurityConfigurationException.class, () -> ldapUserMapper.mapFromAttributes(attributes));

        verify(attributes).get(IDENTIFIER_ATTRIBUTE);
        verify(attributes).get(FIRST_NAME_ATTRIBUTE);
        verify(attributes).get(LAST_NAME_ATTRIBUTE);
        verify(attributes).get(MAIL_ADDRESS_ATTRIBUTE);
    }

    // Map user from context -------------------------------------------------------------------------------------------
    @Test
    void ensureCreatesLdapUserFromContext() throws UnsupportedMemberAffiliationException {

        final DirContextOperations ctx = mock(DirContextOperations.class);
        when(ctx.getStringAttribute(IDENTIFIER_ATTRIBUTE)).thenReturn("rick");
        when(ctx.getStringAttribute(FIRST_NAME_ATTRIBUTE)).thenReturn("Rick");
        when(ctx.getStringAttribute(LAST_NAME_ATTRIBUTE)).thenReturn("Grimes");
        when(ctx.getStringAttribute(MAIL_ADDRESS_ATTRIBUTE)).thenReturn("rick@grimes.com");
        when(ctx.getStringAttributes(MEMBER_OF_ATTRIBUTE)).thenReturn(new String[]{MEMBER_OF_FILTER});

        final LdapUser ldapUser = ldapUserMapper.mapFromContext(ctx);
        assertThat(ldapUser.getUsername()).isEqualTo("rick");
        assertThat(ldapUser.getFirstName()).isEqualTo("Rick");
        assertThat(ldapUser.getLastName()).isEqualTo("Grimes");
        assertThat(ldapUser.getEmail()).isEqualTo("rick@grimes.com");
    }

    @Test
    void ensureThrowsExceptionWithoutIdentifierFromContext() {

        final DirContextOperations ctx = mock(DirContextOperations.class);
        when(ctx.getStringAttribute(IDENTIFIER_ATTRIBUTE)).thenReturn(null);
        when(ctx.getStringAttribute(FIRST_NAME_ATTRIBUTE)).thenReturn("Rick");
        when(ctx.getStringAttribute(LAST_NAME_ATTRIBUTE)).thenReturn("Grimes");
        when(ctx.getStringAttribute(MAIL_ADDRESS_ATTRIBUTE)).thenReturn("rick@grimes.com");
        when(ctx.getStringAttributes(MEMBER_OF_ATTRIBUTE)).thenReturn(new String[]{MEMBER_OF_FILTER});

        assertThrows(InvalidSecurityConfigurationException.class, () -> ldapUserMapper.mapFromContext(ctx));

        verify(ctx, atLeastOnce()).getStringAttribute(IDENTIFIER_ATTRIBUTE);
        verify(ctx, never()).getStringAttribute(LAST_NAME_ATTRIBUTE);
        verify(ctx, never()).getStringAttribute(FIRST_NAME_ATTRIBUTE);
        verify(ctx, never()).getStringAttribute(MAIL_ADDRESS_ATTRIBUTE);
        verify(ctx, never()).getStringAttribute(MEMBER_OF_ATTRIBUTE);
    }

    @Test
    void ensureThrowsExceptionWithoutFirstnameFromContext() {

        final DirContextOperations ctx = mock(DirContextOperations.class);
        when(ctx.getStringAttribute(IDENTIFIER_ATTRIBUTE)).thenReturn("rick");
        when(ctx.getStringAttribute(FIRST_NAME_ATTRIBUTE)).thenReturn(null);
        when(ctx.getStringAttribute(LAST_NAME_ATTRIBUTE)).thenReturn("Grimes");
        when(ctx.getStringAttribute(MAIL_ADDRESS_ATTRIBUTE)).thenReturn("rick@grimes.com");
        when(ctx.getStringAttributes(MEMBER_OF_ATTRIBUTE)).thenReturn(new String[]{MEMBER_OF_FILTER});

        assertThrows(InvalidSecurityConfigurationException.class, () -> ldapUserMapper.mapFromContext(ctx));

        verify(ctx).getStringAttribute(IDENTIFIER_ATTRIBUTE);
        verify(ctx).getStringAttribute(FIRST_NAME_ATTRIBUTE);
        verify(ctx, never()).getStringAttribute(LAST_NAME_ATTRIBUTE);
        verify(ctx, never()).getStringAttribute(MAIL_ADDRESS_ATTRIBUTE);
        verify(ctx, never()).getStringAttribute(MEMBER_OF_ATTRIBUTE);
    }

    @Test
    void ensureThrowsExceptionWithoutLastnameFromContext() {

        final DirContextOperations ctx = mock(DirContextOperations.class);
        when(ctx.getStringAttribute(IDENTIFIER_ATTRIBUTE)).thenReturn("rick");
        when(ctx.getStringAttribute(FIRST_NAME_ATTRIBUTE)).thenReturn("Rick");
        when(ctx.getStringAttribute(LAST_NAME_ATTRIBUTE)).thenReturn(null);
        when(ctx.getStringAttribute(MAIL_ADDRESS_ATTRIBUTE)).thenReturn("rick@grimes.com");
        when(ctx.getStringAttributes(MEMBER_OF_ATTRIBUTE)).thenReturn(new String[]{MEMBER_OF_FILTER});

        assertThrows(InvalidSecurityConfigurationException.class, () -> ldapUserMapper.mapFromContext(ctx));

        verify(ctx, atLeastOnce()).getStringAttribute(IDENTIFIER_ATTRIBUTE);
        verify(ctx).getStringAttribute(FIRST_NAME_ATTRIBUTE);
        verify(ctx).getStringAttribute(LAST_NAME_ATTRIBUTE);
        verify(ctx, never()).getStringAttribute(MAIL_ADDRESS_ATTRIBUTE);
        verify(ctx, never()).getStringAttribute(MEMBER_OF_ATTRIBUTE);
    }

    @Test
    void ensureThrowsExceptionWithoutMailFromContext() {

        final DirContextOperations ctx = mock(DirContextOperations.class);
        when(ctx.getStringAttribute(IDENTIFIER_ATTRIBUTE)).thenReturn("rick");
        when(ctx.getStringAttribute(FIRST_NAME_ATTRIBUTE)).thenReturn("Rick");
        when(ctx.getStringAttribute(LAST_NAME_ATTRIBUTE)).thenReturn("Grimes");
        when(ctx.getStringAttribute(MAIL_ADDRESS_ATTRIBUTE)).thenReturn(null);
        when(ctx.getStringAttributes(MEMBER_OF_ATTRIBUTE)).thenReturn(new String[]{MEMBER_OF_FILTER});

        assertThrows(InvalidSecurityConfigurationException.class, () -> ldapUserMapper.mapFromContext(ctx));

        verify(ctx).getStringAttribute(IDENTIFIER_ATTRIBUTE);
        verify(ctx).getStringAttribute(LAST_NAME_ATTRIBUTE);
        verify(ctx).getStringAttribute(FIRST_NAME_ATTRIBUTE);
        verify(ctx).getStringAttribute(MAIL_ADDRESS_ATTRIBUTE);
        verify(ctx, never()).getStringAttribute(MEMBER_OF_ATTRIBUTE);
    }

    @Test
    void ensureThrowsIfMappingUserFromContextThatIsNotMemberOfMemberFilter() {

        final DirContextOperations ctx = mock(DirContextOperations.class);
        when(ctx.getStringAttribute(IDENTIFIER_ATTRIBUTE)).thenReturn("rick");
        when(ctx.getStringAttribute(FIRST_NAME_ATTRIBUTE)).thenReturn("Rick");
        when(ctx.getStringAttribute(LAST_NAME_ATTRIBUTE)).thenReturn("Grimes");
        when(ctx.getStringAttribute(MAIL_ADDRESS_ATTRIBUTE)).thenReturn("rick@grimes.com");
        when(ctx.getStringAttributes(MEMBER_OF_ATTRIBUTE)).thenReturn(new String[]{"CN=foo, DC=mydomain, DC=com"});

        assertThatThrownBy(() -> ldapUserMapper.mapFromContext(ctx))
            .isInstanceOf(UnsupportedMemberAffiliationException.class);
    }

    @Test
    void ensureNoMemberOfCheckIfMemberOfFilterIsNull() throws UnsupportedMemberAffiliationException {

        final DirectoryServiceSecurityProperties directoryServiceSecurityProperties = new DirectoryServiceSecurityProperties();
        directoryServiceSecurityProperties.setIdentifier(IDENTIFIER_ATTRIBUTE);
        directoryServiceSecurityProperties.setFirstName(FIRST_NAME_ATTRIBUTE);
        directoryServiceSecurityProperties.setLastName(LAST_NAME_ATTRIBUTE);
        directoryServiceSecurityProperties.setMailAddress(MAIL_ADDRESS_ATTRIBUTE);
        directoryServiceSecurityProperties.getFilter().setMemberOf(null);

        final LdapUserMapper ldapUserMapper = new LdapUserMapper(directoryServiceSecurityProperties);

        final DirContextOperations ctx = mock(DirContextOperations.class);
        when(ctx.getStringAttribute(IDENTIFIER_ATTRIBUTE)).thenReturn("rick");
        when(ctx.getStringAttribute(FIRST_NAME_ATTRIBUTE)).thenReturn("Rick");
        when(ctx.getStringAttribute(LAST_NAME_ATTRIBUTE)).thenReturn("Grimes");
        when(ctx.getStringAttribute(MAIL_ADDRESS_ATTRIBUTE)).thenReturn("rick@grimes.com");
        when(ctx.getStringAttributes(MEMBER_OF_ATTRIBUTE)).thenReturn(new String[]{"CN=foo, DC=mydomain, DC=com"});

        final LdapUser ldapUser = ldapUserMapper.mapFromContext(ctx);
        assertThat(ldapUser.getMemberOf()).isEmpty();

        verify(ctx, never()).getStringAttributes(MEMBER_OF_ATTRIBUTE);
    }

    @Test
    void ensureNoMemberOfCheckIfMemberOfFilterIsEmpty() throws UnsupportedMemberAffiliationException {

        final DirectoryServiceSecurityProperties directoryServiceSecurityProperties = new DirectoryServiceSecurityProperties();
        directoryServiceSecurityProperties.setIdentifier(IDENTIFIER_ATTRIBUTE);
        directoryServiceSecurityProperties.setFirstName(FIRST_NAME_ATTRIBUTE);
        directoryServiceSecurityProperties.setLastName(LAST_NAME_ATTRIBUTE);
        directoryServiceSecurityProperties.setMailAddress(MAIL_ADDRESS_ATTRIBUTE);
        directoryServiceSecurityProperties.getFilter().setMemberOf("");

        LdapUserMapper ldapUserMapper = new LdapUserMapper(directoryServiceSecurityProperties);

        DirContextOperations ctx = mock(DirContextOperations.class);
        when(ctx.getStringAttribute(IDENTIFIER_ATTRIBUTE)).thenReturn("rick");
        when(ctx.getStringAttribute(FIRST_NAME_ATTRIBUTE)).thenReturn("Rick");
        when(ctx.getStringAttribute(LAST_NAME_ATTRIBUTE)).thenReturn("Grimes");
        when(ctx.getStringAttribute(MAIL_ADDRESS_ATTRIBUTE)).thenReturn("rick@grimes.com");
        when(ctx.getStringAttributes(MEMBER_OF_ATTRIBUTE)).thenReturn(new String[]{"CN=foo, DC=mydomain, DC=com"});

        final LdapUser ldapUser = ldapUserMapper.mapFromContext(ctx);
        assertThat(ldapUser.getMemberOf()).isEmpty();

        verify(ctx, never()).getStringAttributes(MEMBER_OF_ATTRIBUTE);
    }
}
