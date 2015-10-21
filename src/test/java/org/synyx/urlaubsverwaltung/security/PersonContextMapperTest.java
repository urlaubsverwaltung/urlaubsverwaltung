package org.synyx.urlaubsverwaltung.security;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.mockito.Matchers;
import org.mockito.Mockito;

import org.springframework.ldap.core.DirContextOperations;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.PersonService;
import org.synyx.urlaubsverwaltung.core.person.Role;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
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
    private LdapSyncService ldapSyncService;

    @Before
    public void setUp() {

        personService = Mockito.mock(PersonService.class);
        ldapSyncService = Mockito.mock(LdapSyncService.class);

        personContextMapper = new PersonContextMapper(personService, ldapSyncService, IDENTIFIER_ATTRIBUTE,
                FIRST_NAME_ATTRIBUTE, LAST_NAME_ATTRIBUTE, MAIL_ADDRESS_ATTRIBUTE);
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
    public void ensureCreatesPersonUsingLDAPAttributesIfPersonDoesNotExist() {

        DirContextOperations ctx = Mockito.mock(DirContextOperations.class);
        Mockito.when(ctx.getDn()).thenReturn(Mockito.mock(Name.class));
        Mockito.when(ctx.getStringAttributes("cn")).thenReturn(new String[] { "First", "Last" });
        Mockito.when(ctx.getStringAttribute(FIRST_NAME_ATTRIBUTE)).thenReturn("Aljona");
        Mockito.when(ctx.getStringAttribute(LAST_NAME_ATTRIBUTE)).thenReturn("Murygina");
        Mockito.when(ctx.getStringAttribute(MAIL_ADDRESS_ATTRIBUTE)).thenReturn("murygina@synyx.de");

        Mockito.when(personService.getPersonByLogin(Mockito.anyString())).thenReturn(Optional.empty());

        personContextMapper.mapUserFromContext(ctx, "murygina", null);

        Mockito.verify(ctx, Mockito.atLeastOnce()).getStringAttribute(IDENTIFIER_ATTRIBUTE);
        Mockito.verify(ctx, Mockito.atLeastOnce()).getStringAttribute(LAST_NAME_ATTRIBUTE);
        Mockito.verify(ctx, Mockito.atLeastOnce()).getStringAttribute(FIRST_NAME_ATTRIBUTE);
        Mockito.verify(ctx, Mockito.atLeastOnce()).getStringAttribute(MAIL_ADDRESS_ATTRIBUTE);

        Mockito.verify(ldapSyncService)
            .createPerson(Mockito.eq("murygina"), Mockito.eq(Optional.of("Aljona")),
                Mockito.eq(Optional.of("Murygina")), Mockito.eq(Optional.of("murygina@synyx.de")));
    }


    @Test
    public void ensureSyncsPersonDataUsingLDAPAttributes() {

        Person person = new Person();
        person.setPermissions(Collections.singletonList(Role.USER));

        DirContextOperations ctx = Mockito.mock(DirContextOperations.class);
        Mockito.when(ctx.getDn()).thenReturn(Mockito.mock(Name.class));
        Mockito.when(ctx.getStringAttributes("cn")).thenReturn(new String[] { "First", "Last" });
        Mockito.when(ctx.getStringAttribute(FIRST_NAME_ATTRIBUTE)).thenReturn("Aljona");
        Mockito.when(ctx.getStringAttribute(LAST_NAME_ATTRIBUTE)).thenReturn("Murygina");
        Mockito.when(ctx.getStringAttribute(MAIL_ADDRESS_ATTRIBUTE)).thenReturn("murygina@synyx.de");

        Mockito.when(personService.getPersonByLogin(Mockito.anyString())).thenReturn(Optional.of(person));
        Mockito.when(ldapSyncService.syncPerson(Mockito.any(Person.class), Matchers.<Optional<String>>any(),
                    Matchers.<Optional<String>>any(), Matchers.<Optional<String>>any()))
            .thenReturn(person);

        personContextMapper.mapUserFromContext(ctx, "murygina", null);

        Mockito.verify(ctx, Mockito.atLeastOnce()).getStringAttribute(IDENTIFIER_ATTRIBUTE);
        Mockito.verify(ctx, Mockito.atLeastOnce()).getStringAttribute(LAST_NAME_ATTRIBUTE);
        Mockito.verify(ctx, Mockito.atLeastOnce()).getStringAttribute(FIRST_NAME_ATTRIBUTE);
        Mockito.verify(ctx, Mockito.atLeastOnce()).getStringAttribute(MAIL_ADDRESS_ATTRIBUTE);

        Mockito.verify(ldapSyncService)
            .syncPerson(Mockito.eq(person), Mockito.eq(Optional.of("Aljona")), Mockito.eq(Optional.of("Murygina")),
                Mockito.eq(Optional.of("murygina@synyx.de")));
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
        Mockito.when(ldapSyncService.syncPerson(Mockito.any(Person.class), Matchers.<Optional<String>>any(),
                    Matchers.<Optional<String>>any(), Matchers.<Optional<String>>any()))
            .thenReturn(person);

        UserDetails userDetails = personContextMapper.mapUserFromContext(ctx, "user", null);

        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();

        Assert.assertEquals("Wrong number of authorities", 2, authorities.size());
        Assert.assertTrue("Missing authority for user role",
            SecurityTestUtil.authorityForRoleExists(authorities, Role.USER));
        Assert.assertTrue("Missing authority for boss role",
            SecurityTestUtil.authorityForRoleExists(authorities, Role.BOSS));
    }


    @Test
    public void ensureAddsOfficeRoleToSignedInUserIfNoUserWithOfficeRoleExistsYet() {

        Person person = new Person();
        person.setPermissions(Collections.singletonList(Role.USER));

        DirContextOperations ctx = Mockito.mock(DirContextOperations.class);
        Mockito.when(ctx.getDn()).thenReturn(Mockito.mock(Name.class));
        Mockito.when(ctx.getStringAttributes("cn")).thenReturn(new String[] { "First", "Last" });
        Mockito.when(ctx.getStringAttribute(Mockito.anyString())).thenReturn("Foo");

        Mockito.when(personService.getPersonByLogin(Mockito.anyString())).thenReturn(Optional.of(person));
        Mockito.when(personService.getPersonsByRole(Role.OFFICE)).thenReturn(Collections.emptyList());
        Mockito.when(ldapSyncService.syncPerson(Mockito.any(Person.class), Matchers.<Optional<String>>any(),
                    Matchers.<Optional<String>>any(), Matchers.<Optional<String>>any()))
            .thenReturn(person);

        personContextMapper.mapUserFromContext(ctx, "user", null);

        Mockito.verify(personService).getPersonsByRole(Role.OFFICE);
        Mockito.verify(ldapSyncService).appointPersonAsOfficeUser(person);
    }
}
