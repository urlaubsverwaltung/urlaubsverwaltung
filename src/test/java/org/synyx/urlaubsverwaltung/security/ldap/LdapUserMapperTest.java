package org.synyx.urlaubsverwaltung.security.ldap;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ldap.core.DirContextOperations;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
    void ensureThrowsIfTryingToCreateLdapUserFromAttributesWithInvalidIdentifierAttribute()
        throws NamingException {

        Attributes attributes = mock(Attributes.class);
        when(attributes.get(IDENTIFIER_ATTRIBUTE)).thenReturn(null);

        try {
            ldapUserMapper.mapFromAttributes(attributes);
            Assert.fail("Should throw on empty username!");
        } catch (InvalidSecurityConfigurationException ex) {
            // Expected
        }

        verify(attributes, atLeastOnce()).get(IDENTIFIER_ATTRIBUTE);
        verify(attributes, never()).get(FIRST_NAME_ATTRIBUTE);
        verify(attributes, never()).get(LAST_NAME_ATTRIBUTE);
        verify(attributes, never()).get(MAIL_ADDRESS_ATTRIBUTE);
    }

    @Test
    void ensureCreatesLdapUserFromAttributesWithOnlyUsernameGiven() throws NamingException {

        Attributes attributes = mock(Attributes.class);
        when(attributes.get(IDENTIFIER_ATTRIBUTE)).thenReturn(new BasicAttribute(IDENTIFIER_ATTRIBUTE, "username"));
        when(attributes.get(FIRST_NAME_ATTRIBUTE)).thenReturn(null);
        when(attributes.get(LAST_NAME_ATTRIBUTE)).thenReturn(null);
        when(attributes.get(MAIL_ADDRESS_ATTRIBUTE)).thenReturn(null);

        final LdapUser ldapUser = ldapUserMapper.mapFromAttributes(attributes);
        assertThat(ldapUser.getUsername()).isEqualTo("username");
        assertThat(ldapUser.getFirstName()).isEmpty();
        assertThat(ldapUser.getLastName()).isEmpty();
        assertThat(ldapUser.getEmail()).isEmpty();
    }

    @Test
    void ensureCreatesLdapUserFromAttributes() throws NamingException {

        final Attributes attributes = mock(Attributes.class);
        when(attributes.get(IDENTIFIER_ATTRIBUTE)).thenReturn(new BasicAttribute(IDENTIFIER_ATTRIBUTE, "geralt"));
        when(attributes.get(FIRST_NAME_ATTRIBUTE)).thenReturn(new BasicAttribute(FIRST_NAME_ATTRIBUTE, "Geralt"));
        when(attributes.get(LAST_NAME_ATTRIBUTE)).thenReturn(new BasicAttribute(LAST_NAME_ATTRIBUTE, "von Riva"));
        when(attributes.get(MAIL_ADDRESS_ATTRIBUTE)).thenReturn(new BasicAttribute(MAIL_ADDRESS_ATTRIBUTE, "geralt@riva.de"));

        final LdapUser ldapUser = ldapUserMapper.mapFromAttributes(attributes);
        assertThat(ldapUser.getUsername()).isEqualTo("geralt");
        assertThat(ldapUser.getFirstName()).isPresent().hasValue("Geralt");
        assertThat(ldapUser.getLastName()).isPresent().hasValue("von Riva");
        assertThat(ldapUser.getEmail()).isPresent().hasValue("geralt@riva.de");
    }

    // Map user from context -------------------------------------------------------------------------------------------
    @Test
    void ensureThrowsIfTryingToCreateLdapUserFromContextWithInvalidIdentifierAttribute() throws UnsupportedMemberAffiliationException {

        DirContextOperations ctx = mock(DirContextOperations.class);
        when(ctx.getStringAttribute(IDENTIFIER_ATTRIBUTE)).thenReturn(null);

        try {
            ldapUserMapper.mapFromContext(ctx);
            Assert.fail("Should throw on empty username!");
        } catch (InvalidSecurityConfigurationException ex) {
            // Expected
        }

        verify(ctx, atLeastOnce()).getStringAttribute(IDENTIFIER_ATTRIBUTE);
        verify(ctx, never()).getStringAttribute(LAST_NAME_ATTRIBUTE);
        verify(ctx, never()).getStringAttribute(FIRST_NAME_ATTRIBUTE);
        verify(ctx, never()).getStringAttribute(MAIL_ADDRESS_ATTRIBUTE);
    }


    @Test
    void ensureCreatesLdapUserFromContext() throws UnsupportedMemberAffiliationException {

        DirContextOperations ctx = mock(DirContextOperations.class);
        when(ctx.getStringAttribute(IDENTIFIER_ATTRIBUTE)).thenReturn("rick");
        when(ctx.getStringAttribute(FIRST_NAME_ATTRIBUTE)).thenReturn("Rick");
        when(ctx.getStringAttribute(LAST_NAME_ATTRIBUTE)).thenReturn("Grimes");
        when(ctx.getStringAttribute(MAIL_ADDRESS_ATTRIBUTE)).thenReturn("rick@grimes.com");
        when(ctx.getStringAttributes(MEMBER_OF_ATTRIBUTE)).thenReturn(new String[]{MEMBER_OF_FILTER});

        final LdapUser ldapUser = ldapUserMapper.mapFromContext(ctx);
        assertThat(ldapUser.getUsername()).isEqualTo("rick");
        assertThat(ldapUser.getFirstName()).isPresent().hasValue("Rick");
        assertThat(ldapUser.getLastName()).isPresent().hasValue("Grimes");
        assertThat(ldapUser.getEmail()).isPresent().hasValue("rick@grimes.com");
    }

    @Test
    void ensureThrowsIfMappingUserFromContextThatIsNotMemberOfMemberFilter() throws UnsupportedMemberAffiliationException {

        DirContextOperations ctx = mock(DirContextOperations.class);
        when(ctx.getStringAttribute(IDENTIFIER_ATTRIBUTE)).thenReturn("rick");
        when(ctx.getStringAttribute(FIRST_NAME_ATTRIBUTE)).thenReturn("Rick");
        when(ctx.getStringAttribute(LAST_NAME_ATTRIBUTE)).thenReturn("Grimes");
        when(ctx.getStringAttribute(MAIL_ADDRESS_ATTRIBUTE)).thenReturn("rick@grimes.com");
        when(ctx.getStringAttributes(MEMBER_OF_ATTRIBUTE)).thenReturn(new String[]{"CN=foo, DC=mydomain, DC=com"});

        assertThatThrownBy(() -> ldapUserMapper.mapFromContext(ctx))
            .isInstanceOf(UnsupportedMemberAffiliationException.class);
    }


    @Test
    void ensureNoMemberOfCheckIfMemberOfFilterIsNull() {

        final DirectoryServiceSecurityProperties directoryServiceSecurityProperties = new DirectoryServiceSecurityProperties();
        directoryServiceSecurityProperties.setIdentifier(IDENTIFIER_ATTRIBUTE);
        directoryServiceSecurityProperties.setFirstName(FIRST_NAME_ATTRIBUTE);
        directoryServiceSecurityProperties.setLastName(LAST_NAME_ATTRIBUTE);
        directoryServiceSecurityProperties.setMailAddress(MAIL_ADDRESS_ATTRIBUTE);
        directoryServiceSecurityProperties.getFilter().setMemberOf(null);

        LdapUserMapper ldapUserMapper = new LdapUserMapper(directoryServiceSecurityProperties);

        DirContextOperations ctx = mock(DirContextOperations.class);
        when(ctx.getStringAttribute(IDENTIFIER_ATTRIBUTE)).thenReturn("rick");
        when(ctx.getStringAttribute(FIRST_NAME_ATTRIBUTE)).thenReturn("Rick");
        when(ctx.getStringAttribute(LAST_NAME_ATTRIBUTE)).thenReturn("Grimes");
        when(ctx.getStringAttribute(MAIL_ADDRESS_ATTRIBUTE)).thenReturn("rick@grimes.com");
        when(ctx.getStringAttributes(MEMBER_OF_ATTRIBUTE)).thenReturn(new String[]{"CN=foo, DC=mydomain, DC=com"});

        try {
            ldapUserMapper.mapFromContext(ctx);
        } catch (UnsupportedMemberAffiliationException e) {
            Assert.fail("Should not throw on empty memberOf filter!");
        }

        verify(ctx, never()).getStringAttributes(MEMBER_OF_ATTRIBUTE);
    }


    @Test
    void ensureNoMemberOfCheckIfMemberOfFilterIsEmpty() {

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

        try {
            ldapUserMapper.mapFromContext(ctx);
        } catch (UnsupportedMemberAffiliationException e) {
            Assert.fail("Should not throw on empty memberOf filter!");
        }

        verify(ctx, never()).getStringAttributes(MEMBER_OF_ATTRIBUTE);
    }
}
