package org.synyx.urlaubsverwaltung.security;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;

import org.springframework.ldap.core.DirContextOperations;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class LdapUserMapperTest {

    private static final String IDENTIFIER_ATTRIBUTE = "uid";
    private static final String FIRST_NAME_ATTRIBUTE = "givenName";
    private static final String LAST_NAME_ATTRIBUTE = "sn";
    private static final String MAIL_ADDRESS_ATTRIBUTE = "mail";

    private LdapUserMapper ldapUserMapper;

    @Before
    public void setUp() {

        ldapUserMapper = new LdapUserMapper(IDENTIFIER_ATTRIBUTE, FIRST_NAME_ATTRIBUTE, LAST_NAME_ATTRIBUTE,
                MAIL_ADDRESS_ATTRIBUTE);
    }


    // Map user from attributes ----------------------------------------------------------------------------------------

    @Test
    public void ensureThrowsIfTryingToCreateLdapUserFromAttributesWithInvalidIdentifierAttribute()
        throws NamingException {

        Attributes attributes = Mockito.mock(Attributes.class);
        Mockito.when(attributes.get(IDENTIFIER_ATTRIBUTE)).thenReturn(null);

        try {
            ldapUserMapper.mapFromAttributes(attributes);
            Assert.fail("Should throw on empty username!");
        } catch (InvalidSecurityConfigurationException ex) {
            // Expected
        }

        Mockito.verify(attributes, Mockito.atLeastOnce()).get(IDENTIFIER_ATTRIBUTE);
        Mockito.verify(attributes, Mockito.never()).get(FIRST_NAME_ATTRIBUTE);
        Mockito.verify(attributes, Mockito.never()).get(LAST_NAME_ATTRIBUTE);
        Mockito.verify(attributes, Mockito.never()).get(MAIL_ADDRESS_ATTRIBUTE);
    }


    @Test
    public void ensureCreatesLdapUserFromAttributesWithOnlyUsernameGiven() throws NamingException {

        Attributes attributes = Mockito.mock(Attributes.class);
        Mockito.when(attributes.get(IDENTIFIER_ATTRIBUTE))
            .thenReturn(new BasicAttribute(IDENTIFIER_ATTRIBUTE, "username"));
        Mockito.when(attributes.get(FIRST_NAME_ATTRIBUTE)).thenReturn(null);
        Mockito.when(attributes.get(LAST_NAME_ATTRIBUTE)).thenReturn(null);
        Mockito.when(attributes.get(MAIL_ADDRESS_ATTRIBUTE)).thenReturn(null);

        LdapUser ldapUser = ldapUserMapper.mapFromAttributes(attributes);

        Mockito.verify(attributes).get(IDENTIFIER_ATTRIBUTE);
        Mockito.verify(attributes).get(FIRST_NAME_ATTRIBUTE);
        Mockito.verify(attributes).get(LAST_NAME_ATTRIBUTE);
        Mockito.verify(attributes).get(MAIL_ADDRESS_ATTRIBUTE);

        Assert.assertEquals("Wrong username", "username", ldapUser.getUsername());
        Assert.assertFalse("First name should be empty", ldapUser.getFirstName().isPresent());
        Assert.assertFalse("Last name should be empty", ldapUser.getLastName().isPresent());
        Assert.assertFalse("Email should be empty", ldapUser.getEmail().isPresent());
    }


    @Test
    public void ensureCreatesLdapUserFromAttributes() throws NamingException {

        Attributes attributes = Mockito.mock(Attributes.class);
        Mockito.when(attributes.get(IDENTIFIER_ATTRIBUTE))
            .thenReturn(new BasicAttribute(IDENTIFIER_ATTRIBUTE, "geralt"));
        Mockito.when(attributes.get(FIRST_NAME_ATTRIBUTE))
            .thenReturn(new BasicAttribute(FIRST_NAME_ATTRIBUTE, "Geralt"));
        Mockito.when(attributes.get(LAST_NAME_ATTRIBUTE))
            .thenReturn(new BasicAttribute(LAST_NAME_ATTRIBUTE, "von Riva"));
        Mockito.when(attributes.get(MAIL_ADDRESS_ATTRIBUTE))
            .thenReturn(new BasicAttribute(MAIL_ADDRESS_ATTRIBUTE, "geralt@riva.de"));

        LdapUser ldapUser = ldapUserMapper.mapFromAttributes(attributes);

        Mockito.verify(attributes).get(IDENTIFIER_ATTRIBUTE);
        Mockito.verify(attributes).get(FIRST_NAME_ATTRIBUTE);
        Mockito.verify(attributes).get(LAST_NAME_ATTRIBUTE);
        Mockito.verify(attributes).get(MAIL_ADDRESS_ATTRIBUTE);

        Assert.assertEquals("Wrong username", "geralt", ldapUser.getUsername());
        Assert.assertEquals("Wrong first name", "Geralt", ldapUser.getFirstName().get());
        Assert.assertEquals("Wrong last name", "von Riva", ldapUser.getLastName().get());
        Assert.assertEquals("Wrong email", "geralt@riva.de", ldapUser.getEmail().get());
    }


    // Map user from context -------------------------------------------------------------------------------------------

    @Test
    public void ensureThrowsIfTryingToCreateLdapUserFromContextWithInvalidIdentifierAttribute() {

        DirContextOperations ctx = Mockito.mock(DirContextOperations.class);
        Mockito.when(ctx.getStringAttribute(IDENTIFIER_ATTRIBUTE)).thenReturn(null);

        try {
            ldapUserMapper.mapFromContext(ctx);
            Assert.fail("Should throw on empty username!");
        } catch (InvalidSecurityConfigurationException ex) {
            // Expected
        }

        Mockito.verify(ctx, Mockito.atLeastOnce()).getStringAttribute(IDENTIFIER_ATTRIBUTE);
        Mockito.verify(ctx, Mockito.never()).getStringAttribute(LAST_NAME_ATTRIBUTE);
        Mockito.verify(ctx, Mockito.never()).getStringAttribute(FIRST_NAME_ATTRIBUTE);
        Mockito.verify(ctx, Mockito.never()).getStringAttribute(MAIL_ADDRESS_ATTRIBUTE);
    }


    @Test
    public void ensureCreatesLdapUserFromContext() {

        DirContextOperations ctx = Mockito.mock(DirContextOperations.class);
        Mockito.when(ctx.getStringAttribute(IDENTIFIER_ATTRIBUTE)).thenReturn("rick");
        Mockito.when(ctx.getStringAttribute(FIRST_NAME_ATTRIBUTE)).thenReturn("Rick");
        Mockito.when(ctx.getStringAttribute(LAST_NAME_ATTRIBUTE)).thenReturn("Grimes");
        Mockito.when(ctx.getStringAttribute(MAIL_ADDRESS_ATTRIBUTE)).thenReturn("rick@grimes.com");

        LdapUser ldapUser = ldapUserMapper.mapFromContext(ctx);

        Mockito.verify(ctx, Mockito.atLeastOnce()).getStringAttribute(IDENTIFIER_ATTRIBUTE);
        Mockito.verify(ctx, Mockito.atLeastOnce()).getStringAttribute(LAST_NAME_ATTRIBUTE);
        Mockito.verify(ctx, Mockito.atLeastOnce()).getStringAttribute(FIRST_NAME_ATTRIBUTE);
        Mockito.verify(ctx, Mockito.atLeastOnce()).getStringAttribute(MAIL_ADDRESS_ATTRIBUTE);

        Assert.assertEquals("Wrong username", "rick", ldapUser.getUsername());
        Assert.assertEquals("Wrong first name", "Rick", ldapUser.getFirstName().get());
        Assert.assertEquals("Wrong last name", "Grimes", ldapUser.getLastName().get());
        Assert.assertEquals("Wrong email", "rick@grimes.com", ldapUser.getEmail().get());
    }
}
