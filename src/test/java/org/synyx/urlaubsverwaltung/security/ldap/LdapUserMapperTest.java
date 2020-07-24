package org.synyx.urlaubsverwaltung.security.ldap;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ldap.core.DirContextOperations;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;

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
        when(attributes.get(IDENTIFIER_ATTRIBUTE))
            .thenReturn(new BasicAttribute(IDENTIFIER_ATTRIBUTE, "username"));
        when(attributes.get(FIRST_NAME_ATTRIBUTE)).thenReturn(null);
        when(attributes.get(LAST_NAME_ATTRIBUTE)).thenReturn(null);
        when(attributes.get(MAIL_ADDRESS_ATTRIBUTE)).thenReturn(null);

        LdapUser ldapUser = ldapUserMapper.mapFromAttributes(attributes);

        verify(attributes).get(IDENTIFIER_ATTRIBUTE);
        verify(attributes).get(FIRST_NAME_ATTRIBUTE);
        verify(attributes).get(LAST_NAME_ATTRIBUTE);
        verify(attributes).get(MAIL_ADDRESS_ATTRIBUTE);

        Assert.assertEquals("Wrong username", "username", ldapUser.getUsername());
        Assert.assertFalse("First name should be empty", ldapUser.getFirstName().isPresent());
        Assert.assertFalse("Last name should be empty", ldapUser.getLastName().isPresent());
        Assert.assertFalse("Email should be empty", ldapUser.getEmail().isPresent());
    }


    @Test
    void ensureCreatesLdapUserFromAttributes() throws NamingException {

        Attributes attributes = mock(Attributes.class);
        when(attributes.get(IDENTIFIER_ATTRIBUTE))
            .thenReturn(new BasicAttribute(IDENTIFIER_ATTRIBUTE, "geralt"));
        when(attributes.get(FIRST_NAME_ATTRIBUTE))
            .thenReturn(new BasicAttribute(FIRST_NAME_ATTRIBUTE, "Geralt"));
        when(attributes.get(LAST_NAME_ATTRIBUTE))
            .thenReturn(new BasicAttribute(LAST_NAME_ATTRIBUTE, "von Riva"));
        when(attributes.get(MAIL_ADDRESS_ATTRIBUTE))
            .thenReturn(new BasicAttribute(MAIL_ADDRESS_ATTRIBUTE, "geralt@riva.de"));

        LdapUser ldapUser = ldapUserMapper.mapFromAttributes(attributes);

        verify(attributes).get(IDENTIFIER_ATTRIBUTE);
        verify(attributes).get(FIRST_NAME_ATTRIBUTE);
        verify(attributes).get(LAST_NAME_ATTRIBUTE);
        verify(attributes).get(MAIL_ADDRESS_ATTRIBUTE);

        Assert.assertEquals("Wrong username", "geralt", ldapUser.getUsername());
        Assert.assertEquals("Wrong first name", "Geralt", ldapUser.getFirstName().get());
        Assert.assertEquals("Wrong last name", "von Riva", ldapUser.getLastName().get());
        Assert.assertEquals("Wrong email", "geralt@riva.de", ldapUser.getEmail().get());
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

        LdapUser ldapUser = ldapUserMapper.mapFromContext(ctx);

        verify(ctx, atLeastOnce()).getStringAttribute(IDENTIFIER_ATTRIBUTE);
        verify(ctx, atLeastOnce()).getStringAttribute(LAST_NAME_ATTRIBUTE);
        verify(ctx, atLeastOnce()).getStringAttribute(FIRST_NAME_ATTRIBUTE);
        verify(ctx, atLeastOnce()).getStringAttribute(MAIL_ADDRESS_ATTRIBUTE);

        Assert.assertEquals("Wrong username", "rick", ldapUser.getUsername());
        Assert.assertEquals("Wrong first name", "Rick", ldapUser.getFirstName().get());
        Assert.assertEquals("Wrong last name", "Grimes", ldapUser.getLastName().get());
        Assert.assertEquals("Wrong email", "rick@grimes.com", ldapUser.getEmail().get());
    }


    @Test
    void ensureThrowsIfMappingUserFromContextThatIsNotMemberOfMemberFilter() throws UnsupportedMemberAffiliationException {

        DirContextOperations ctx = mock(DirContextOperations.class);
        when(ctx.getStringAttribute(IDENTIFIER_ATTRIBUTE)).thenReturn("rick");
        when(ctx.getStringAttribute(FIRST_NAME_ATTRIBUTE)).thenReturn("Rick");
        when(ctx.getStringAttribute(LAST_NAME_ATTRIBUTE)).thenReturn("Grimes");
        when(ctx.getStringAttribute(MAIL_ADDRESS_ATTRIBUTE)).thenReturn("rick@grimes.com");

        when(ctx.getStringAttributes(MEMBER_OF_ATTRIBUTE))
            .thenReturn(new String[]{"CN=foo, DC=mydomain, DC=com"});

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

        when(ctx.getStringAttributes(MEMBER_OF_ATTRIBUTE))
            .thenReturn(new String[]{"CN=foo, DC=mydomain, DC=com"});

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

        when(ctx.getStringAttributes(MEMBER_OF_ATTRIBUTE))
            .thenReturn(new String[]{"CN=foo, DC=mydomain, DC=com"});

        try {
            ldapUserMapper.mapFromContext(ctx);
        } catch (UnsupportedMemberAffiliationException e) {
            Assert.fail("Should not throw on empty memberOf filter!");
        }

        verify(ctx, never()).getStringAttributes(MEMBER_OF_ATTRIBUTE);
    }
}
