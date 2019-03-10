package org.synyx.urlaubsverwaltung.security;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.ldap.core.DirContextOperations;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class LdapUserMapperTest {

    private static final String IDENTIFIER_ATTRIBUTE = "uid";
    private static final String FIRST_NAME_ATTRIBUTE = "givenName";
    private static final String LAST_NAME_ATTRIBUTE = "sn";
    private static final String MAIL_ADDRESS_ATTRIBUTE = "mail";
    private static final String MEMBER_OF_ATTRIBUTE = "memberOf";

    private static final String MEMBER_OF_FILTER = "CN=mygroup,DC=mydomain,DC=com";

    private LdapUserMapper ldapUserMapper;

    @Before
    public void setUp() {

        ldapUserMapper = new LdapUserMapper(IDENTIFIER_ATTRIBUTE, FIRST_NAME_ATTRIBUTE, LAST_NAME_ATTRIBUTE,
                MAIL_ADDRESS_ATTRIBUTE, MEMBER_OF_FILTER);
    }


    // Map user from attributes ----------------------------------------------------------------------------------------

    @Test
    public void ensureThrowsIfTryingToCreateLdapUserFromAttributesWithInvalidIdentifierAttribute()
        throws NamingException {

        Attributes attributes = mock(Attributes.class);
        when(attributes.get(IDENTIFIER_ATTRIBUTE)).thenReturn(null);

        try {
            ldapUserMapper.mapFromAttributes(attributes);
            Assert.fail("Should throw on empty username!");
        } catch (InvalidSecurityConfigurationException ex) {
            // Expected
        }

        verify(attributes, Mockito.atLeastOnce()).get(IDENTIFIER_ATTRIBUTE);
        verify(attributes, Mockito.never()).get(FIRST_NAME_ATTRIBUTE);
        verify(attributes, Mockito.never()).get(LAST_NAME_ATTRIBUTE);
        verify(attributes, Mockito.never()).get(MAIL_ADDRESS_ATTRIBUTE);
    }


    @Test
    public void ensureCreatesLdapUserFromAttributesWithOnlyUsernameGiven() throws NamingException {

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
    public void ensureCreatesLdapUserFromAttributes() throws NamingException {

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
    public void ensureThrowsIfTryingToCreateLdapUserFromContextWithInvalidIdentifierAttribute() throws NamingException,
        UnsupportedMemberAffiliationException {

        DirContextOperations ctx = mock(DirContextOperations.class);
        when(ctx.getStringAttribute(IDENTIFIER_ATTRIBUTE)).thenReturn(null);

        try {
            ldapUserMapper.mapFromContext(ctx);
            Assert.fail("Should throw on empty username!");
        } catch (InvalidSecurityConfigurationException ex) {
            // Expected
        }

        verify(ctx, Mockito.atLeastOnce()).getStringAttribute(IDENTIFIER_ATTRIBUTE);
        verify(ctx, Mockito.never()).getStringAttribute(LAST_NAME_ATTRIBUTE);
        verify(ctx, Mockito.never()).getStringAttribute(FIRST_NAME_ATTRIBUTE);
        verify(ctx, Mockito.never()).getStringAttribute(MAIL_ADDRESS_ATTRIBUTE);
    }


    @Test
    public void ensureCreatesLdapUserFromContext() throws NamingException, UnsupportedMemberAffiliationException {

        DirContextOperations ctx = mock(DirContextOperations.class);
        when(ctx.getStringAttribute(IDENTIFIER_ATTRIBUTE)).thenReturn("rick");
        when(ctx.getStringAttribute(FIRST_NAME_ATTRIBUTE)).thenReturn("Rick");
        when(ctx.getStringAttribute(LAST_NAME_ATTRIBUTE)).thenReturn("Grimes");
        when(ctx.getStringAttribute(MAIL_ADDRESS_ATTRIBUTE)).thenReturn("rick@grimes.com");
        when(ctx.getStringAttributes(MEMBER_OF_ATTRIBUTE)).thenReturn(new String[] { MEMBER_OF_FILTER });

        LdapUser ldapUser = ldapUserMapper.mapFromContext(ctx);

        verify(ctx, Mockito.atLeastOnce()).getStringAttribute(IDENTIFIER_ATTRIBUTE);
        verify(ctx, Mockito.atLeastOnce()).getStringAttribute(LAST_NAME_ATTRIBUTE);
        verify(ctx, Mockito.atLeastOnce()).getStringAttribute(FIRST_NAME_ATTRIBUTE);
        verify(ctx, Mockito.atLeastOnce()).getStringAttribute(MAIL_ADDRESS_ATTRIBUTE);

        Assert.assertEquals("Wrong username", "rick", ldapUser.getUsername());
        Assert.assertEquals("Wrong first name", "Rick", ldapUser.getFirstName().get());
        Assert.assertEquals("Wrong last name", "Grimes", ldapUser.getLastName().get());
        Assert.assertEquals("Wrong email", "rick@grimes.com", ldapUser.getEmail().get());
    }


    @Test(expected = UnsupportedMemberAffiliationException.class)
    public void ensureThrowsIfMappingUserFromContextThatIsNotMemberOfMemberFilter() throws NamingException,
        UnsupportedMemberAffiliationException {

        DirContextOperations ctx = mock(DirContextOperations.class);
        when(ctx.getStringAttribute(IDENTIFIER_ATTRIBUTE)).thenReturn("rick");
        when(ctx.getStringAttribute(FIRST_NAME_ATTRIBUTE)).thenReturn("Rick");
        when(ctx.getStringAttribute(LAST_NAME_ATTRIBUTE)).thenReturn("Grimes");
        when(ctx.getStringAttribute(MAIL_ADDRESS_ATTRIBUTE)).thenReturn("rick@grimes.com");

        when(ctx.getStringAttributes(MEMBER_OF_ATTRIBUTE))
            .thenReturn(new String[] { "CN=foo, DC=mydomain, DC=com" });

        ldapUserMapper.mapFromContext(ctx);
    }


    @Test
    public void ensureNoMemberOfCheckIfMemberOfFilterIsNull() throws NamingException {

        ldapUserMapper = new LdapUserMapper(IDENTIFIER_ATTRIBUTE, FIRST_NAME_ATTRIBUTE, LAST_NAME_ATTRIBUTE,
                MAIL_ADDRESS_ATTRIBUTE, null);

        DirContextOperations ctx = mock(DirContextOperations.class);
        when(ctx.getStringAttribute(IDENTIFIER_ATTRIBUTE)).thenReturn("rick");
        when(ctx.getStringAttribute(FIRST_NAME_ATTRIBUTE)).thenReturn("Rick");
        when(ctx.getStringAttribute(LAST_NAME_ATTRIBUTE)).thenReturn("Grimes");
        when(ctx.getStringAttribute(MAIL_ADDRESS_ATTRIBUTE)).thenReturn("rick@grimes.com");

        when(ctx.getStringAttributes(MEMBER_OF_ATTRIBUTE))
            .thenReturn(new String[] { "CN=foo, DC=mydomain, DC=com" });

        try {
            ldapUserMapper.mapFromContext(ctx);
        } catch (UnsupportedMemberAffiliationException e) {
            Assert.fail("Should not throw on empty memberOf filter!");
        }

        verify(ctx, Mockito.never()).getStringAttributes(MEMBER_OF_ATTRIBUTE);
    }


    @Test
    public void ensureNoMemberOfCheckIfMemberOfFilterIsEmpty() throws NamingException {

        ldapUserMapper = new LdapUserMapper(IDENTIFIER_ATTRIBUTE, FIRST_NAME_ATTRIBUTE, LAST_NAME_ATTRIBUTE,
                MAIL_ADDRESS_ATTRIBUTE, "");

        DirContextOperations ctx = mock(DirContextOperations.class);
        when(ctx.getStringAttribute(IDENTIFIER_ATTRIBUTE)).thenReturn("rick");
        when(ctx.getStringAttribute(FIRST_NAME_ATTRIBUTE)).thenReturn("Rick");
        when(ctx.getStringAttribute(LAST_NAME_ATTRIBUTE)).thenReturn("Grimes");
        when(ctx.getStringAttribute(MAIL_ADDRESS_ATTRIBUTE)).thenReturn("rick@grimes.com");

        when(ctx.getStringAttributes(MEMBER_OF_ATTRIBUTE))
            .thenReturn(new String[] { "CN=foo, DC=mydomain, DC=com" });

        try {
            ldapUserMapper.mapFromContext(ctx);
        } catch (UnsupportedMemberAffiliationException e) {
            Assert.fail("Should not throw on empty memberOf filter!");
        }

        verify(ctx, Mockito.never()).getStringAttributes(MEMBER_OF_ATTRIBUTE);
    }
}
