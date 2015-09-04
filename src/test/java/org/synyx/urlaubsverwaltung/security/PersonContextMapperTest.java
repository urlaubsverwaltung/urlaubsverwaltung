package org.synyx.urlaubsverwaltung.security;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;

import org.springframework.ldap.core.DirContextOperations;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import org.synyx.urlaubsverwaltung.core.mail.MailService;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.PersonService;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import javax.naming.Name;


/**
 * Unit test for {@link org.synyx.urlaubsverwaltung.security.PersonContextMapper}.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class PersonContextMapperTest {

    private static final String IDENTIFIER_ATTRIBUTE = "uid";
    private static final String LAST_NAME_ATTRIBUTE = "sn";
    private static final String FIRST_NAME_ATTRIBUTE = "givenName";
    private static final String MAIL_ADDRESS_ATTRIBUTE = "mail";

    private PersonContextMapper personContextMapper;
    private PersonService personService;

    @Before
    public void setUp() {

        personService = Mockito.mock(PersonService.class);

        personContextMapper = new PersonContextMapper(personService, Mockito.mock(MailService.class),
                IDENTIFIER_ATTRIBUTE, LAST_NAME_ATTRIBUTE, FIRST_NAME_ATTRIBUTE, MAIL_ADDRESS_ATTRIBUTE);
    }


    @Test
    public void ensureReturnsEmptyListOfAuthoritiesForNullPerson() {

        Collection<GrantedAuthority> authorities = personContextMapper.getGrantedAuthorities(null);

        Assert.assertNotNull("Should not be null", authorities);
        Assert.assertTrue("Should be empty", authorities.isEmpty());
    }


    @Test
    public void ensureReturnsCorrectListOfAuthoritiesUsingTheRolesOfTheGivenPerson() {

        Person person = new Person();
        person.setPermissions(Arrays.asList(Role.USER, Role.BOSS));

        Collection<GrantedAuthority> authorities = personContextMapper.getGrantedAuthorities(person);

        Assert.assertEquals("Wrong number of authorities", 2, authorities.size());
        Assert.assertTrue("No authority for user role found",
            SecurityTestUtil.authorityForRoleExists(authorities, Role.USER));
        Assert.assertTrue("No authority for boss role found",
            SecurityTestUtil.authorityForRoleExists(authorities, Role.BOSS));
    }


    @Test
    public void ensureFirstCreatedPersonHasTheCorrectRoles() {

        Person person = personContextMapper.createPerson("murygina", Optional.of("Aljona"), Optional.of("Murygina"),
                Optional.of("murygina@synyx.de"), true);

        Collection<Role> roles = person.getPermissions();

        Assert.assertEquals("Wrong number of roles", 2, roles.size());
        Assert.assertTrue("Does not contain user role", roles.contains(Role.USER));
        Assert.assertTrue("Does not contain office role", roles.contains(Role.OFFICE));
    }


    @Test
    public void ensureFurtherCreatedPersonHasTheCorrectRoles() {

        Person person = personContextMapper.createPerson("murygina", Optional.of("Aljona"), Optional.of("Murygina"),
                Optional.of("murygina@synyx.de"), false);

        Collection<Role> roles = person.getPermissions();

        Assert.assertEquals("Wrong number of roles", 1, roles.size());
        Assert.assertTrue("Does not contain user role", roles.contains(Role.USER));
    }


    @Test
    public void ensurePersonCanBeCreatedWithOnlyLoginName() {

        Person person = personContextMapper.createPerson("murygina", Optional.empty(), Optional.empty(),
                Optional.empty(), true);

        Mockito.verify(personService).save(Mockito.eq(person));

        Assert.assertNotNull("Missing login name", person.getLoginName());
        Assert.assertEquals("Wrong login name", "murygina", person.getLoginName());

        Assert.assertNull("First name should be not set", person.getFirstName());
        Assert.assertNull("Last name should be not set", person.getLastName());
        Assert.assertNull("Mail address should be not set", person.getEmail());
    }


    @Test
    public void ensureCreatedPersonHasCorrectAttributes() {

        Person person = personContextMapper.createPerson("murygina", Optional.of("Aljona"), Optional.of("Murygina"),
                Optional.of("murygina@synyx.de"), true);

        Mockito.verify(personService).save(Mockito.eq(person));

        Assert.assertNotNull("Missing login name", person.getLoginName());
        Assert.assertNotNull("Missing first name", person.getFirstName());
        Assert.assertNotNull("Missing last name", person.getLastName());
        Assert.assertNotNull("Missing mail address", person.getEmail());

        Assert.assertEquals("Wrong login name", "murygina", person.getLoginName());
        Assert.assertEquals("Wrong first name", "Aljona", person.getFirstName());
        Assert.assertEquals("Wrong last name", "Murygina", person.getLastName());
        Assert.assertEquals("Wrong mail address", "murygina@synyx.de", person.getEmail());
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsIfNoLoginNameIsGiven() {

        personContextMapper.createPerson(null, Optional.of("Aljona"), Optional.of("Murygina"),
            Optional.of("murygina@synyx.de"), true);
    }


    @Test
    public void ensureCreatesPersonUsingLDAPAttributesIfPersonDoesNotExist() {

        DirContextOperations ctx = Mockito.mock(DirContextOperations.class);
        Mockito.when(ctx.getDn()).thenReturn(Mockito.mock(Name.class));
        Mockito.when(ctx.getStringAttributes("cn")).thenReturn(new String[] { "First", "Last" });
        Mockito.when(ctx.getStringAttribute(Mockito.anyString())).thenReturn("Foo");

        Mockito.when(personService.getPersonByLogin(Mockito.anyString())).thenReturn(Optional.empty());

        personContextMapper.mapUserFromContext(ctx, "murygina", null);

        Mockito.verify(ctx, Mockito.atLeastOnce()).getStringAttribute(IDENTIFIER_ATTRIBUTE);
        Mockito.verify(ctx, Mockito.atLeastOnce()).getStringAttribute(LAST_NAME_ATTRIBUTE);
        Mockito.verify(ctx, Mockito.atLeastOnce()).getStringAttribute(FIRST_NAME_ATTRIBUTE);
        Mockito.verify(ctx, Mockito.atLeastOnce()).getStringAttribute(MAIL_ADDRESS_ATTRIBUTE);

        Mockito.verify(personService).save(Mockito.any(Person.class));
    }


    @Test
    public void ensureUsernameIsBasedOnGivenIdentifierAttribute() {

        String userIdentifier = "mgroehning";
        String userNameSignedInWith = "mgroehning@simpsons.com";

        DirContextOperations ctx = Mockito.mock(DirContextOperations.class);
        Mockito.when(ctx.getDn()).thenReturn(Mockito.mock(Name.class));
        Mockito.when(ctx.getStringAttributes("cn")).thenReturn(new String[] { "First", "Last" });
        Mockito.when(ctx.getStringAttribute(Mockito.anyString())).thenReturn("Foo");

        Mockito.when(ctx.getStringAttribute(IDENTIFIER_ATTRIBUTE)).thenReturn(userIdentifier);

        Mockito.when(personService.getPersonByLogin(Mockito.anyString())).thenReturn(Optional.empty());

        UserDetails userDetails = personContextMapper.mapUserFromContext(ctx, userNameSignedInWith, null);

        Assert.assertNotNull("Username should be set", userDetails.getUsername());
        Assert.assertEquals("Wrong username", userIdentifier, userDetails.getUsername());
    }


    @Test
    public void ensureUsernameIsBasedOnSignedInUsernameIfNoValueForGivenIdentifierAttribute() {

        String userNameSignedInWith = "mgroehning@simpsons.com";

        DirContextOperations ctx = Mockito.mock(DirContextOperations.class);
        Mockito.when(ctx.getDn()).thenReturn(Mockito.mock(Name.class));
        Mockito.when(ctx.getStringAttributes("cn")).thenReturn(new String[] { "First", "Last" });
        Mockito.when(ctx.getStringAttribute(Mockito.anyString())).thenReturn("Foo");

        Mockito.when(ctx.getStringAttribute(IDENTIFIER_ATTRIBUTE)).thenReturn(null);

        Mockito.when(personService.getPersonByLogin(Mockito.anyString())).thenReturn(Optional.empty());

        UserDetails userDetails = personContextMapper.mapUserFromContext(ctx, userNameSignedInWith, null);

        Assert.assertNotNull("Username should be set", userDetails.getUsername());
        Assert.assertEquals("Wrong username", userNameSignedInWith, userDetails.getUsername());
    }


    @Test
    public void ensureAuthoritiesAreBasedOnRolesOfTheSignedInPerson() {

        Person person = new Person();
        person.setPermissions(Arrays.asList(Role.USER, Role.BOSS));

        DirContextOperations ctx = Mockito.mock(DirContextOperations.class);
        Mockito.when(ctx.getDn()).thenReturn(Mockito.mock(Name.class));
        Mockito.when(ctx.getStringAttributes("cn")).thenReturn(new String[] { "First", "Last" });
        Mockito.when(ctx.getStringAttribute(Mockito.anyString())).thenReturn("Foo");

        Mockito.when(personService.getPersonByLogin(Mockito.anyString())).thenReturn(Optional.of(person));

        UserDetails userDetails = personContextMapper.mapUserFromContext(ctx, "user", null);

        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();

        Assert.assertEquals("Wrong number of authorities", 2, authorities.size());
        Assert.assertTrue("Missing authority for user role",
            SecurityTestUtil.authorityForRoleExists(authorities, Role.USER));
        Assert.assertTrue("Missing authority for boss role",
            SecurityTestUtil.authorityForRoleExists(authorities, Role.BOSS));
    }
}
