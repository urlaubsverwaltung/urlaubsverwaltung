package org.synyx.urlaubsverwaltung.security;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.mockito.Matchers;
import org.mockito.Mockito;

import org.springframework.ldap.core.DirContextOperations;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.PersonService;
import org.synyx.urlaubsverwaltung.core.person.Role;
import org.synyx.urlaubsverwaltung.test.TestDataCreator;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import javax.naming.Name;
import javax.naming.NamingException;


/**
 * Unit test for {@link org.synyx.urlaubsverwaltung.security.PersonContextMapper}.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class PersonContextMapperTest {

    private PersonContextMapper personContextMapper;

    private PersonService personService;
    private LdapSyncService ldapSyncService;
    private LdapUserMapper ldapUserMapper;

    private DirContextOperations context;

    @Before
    public void setUp() {

        personService = Mockito.mock(PersonService.class);
        ldapSyncService = Mockito.mock(LdapSyncService.class);
        ldapUserMapper = Mockito.mock(LdapUserMapper.class);

        personContextMapper = new PersonContextMapper(personService, ldapSyncService, ldapUserMapper);

        context = Mockito.mock(DirContextOperations.class);

        Mockito.when(context.getDn()).thenReturn(Mockito.mock(Name.class));
        Mockito.when(context.getStringAttributes("cn")).thenReturn(new String[] { "First", "Last" });
        Mockito.when(context.getStringAttribute(Mockito.anyString())).thenReturn("Foo");
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsIfTryingToGetAuthoritiesForNullPerson() {

        personContextMapper.getGrantedAuthorities(null);
    }


    @Test(expected = IllegalStateException.class)
    public void ensureThrowsIfTryingToGetAuthoritiesOfPersonWithNoRoles() {

        Person person = new Person();
        person.setPermissions(Collections.emptyList());

        personContextMapper.getGrantedAuthorities(person);
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
    public void ensureCreatesPersonIfPersonDoesNotExist() throws NamingException,
        UnsupportedMemberAffiliationException {

        Mockito.when(ldapUserMapper.mapFromContext(Mockito.eq(context)))
            .thenReturn(new LdapUser("murygina", Optional.of("Aljona"), Optional.of("Murygina"),
                    Optional.of("murygina@synyx.de")));
        Mockito.when(personService.getPersonByLogin(Mockito.anyString())).thenReturn(Optional.<Person>empty());
        Mockito.when(ldapSyncService.createPerson(Mockito.anyString(), Matchers.<Optional<String>>any(),
                    Matchers.<Optional<String>>any(), Matchers.<Optional<String>>any()))
            .thenReturn(TestDataCreator.createPerson());

        personContextMapper.mapUserFromContext(context, "murygina", null);

        Mockito.verify(ldapUserMapper).mapFromContext(context);
        Mockito.verify(ldapSyncService)
            .createPerson(Mockito.eq("murygina"), Mockito.eq(Optional.of("Aljona")),
                Mockito.eq(Optional.of("Murygina")), Mockito.eq(Optional.of("murygina@synyx.de")));
    }


    @Test
    public void ensureSyncsPersonDataUsingLDAPAttributes() throws NamingException,
        UnsupportedMemberAffiliationException {

        Person person = new Person();
        person.setPermissions(Collections.singletonList(Role.USER));

        Mockito.when(ldapUserMapper.mapFromContext(Mockito.eq(context)))
            .thenReturn(new LdapUser("murygina", Optional.of("Aljona"), Optional.of("Murygina"),
                    Optional.of("murygina@synyx.de")));
        Mockito.when(personService.getPersonByLogin(Mockito.anyString())).thenReturn(Optional.of(person));
        Mockito.when(ldapSyncService.syncPerson(Mockito.any(Person.class), Matchers.<Optional<String>>any(),
                    Matchers.<Optional<String>>any(), Matchers.<Optional<String>>any()))
            .thenReturn(person);

        personContextMapper.mapUserFromContext(context, "murygina", null);

        Mockito.verify(ldapUserMapper).mapFromContext(context);
        Mockito.verify(ldapSyncService)
            .syncPerson(Mockito.eq(person), Mockito.eq(Optional.of("Aljona")), Mockito.eq(Optional.of("Murygina")),
                Mockito.eq(Optional.of("murygina@synyx.de")));
    }


    @Test
    public void ensureUsernameIsBasedOnLdapUsername() throws NamingException, UnsupportedMemberAffiliationException {

        String userIdentifier = "mgroehning";
        String userNameSignedInWith = "mgroehning@simpsons.com";

        Mockito.when(ldapUserMapper.mapFromContext(Mockito.eq(context)))
            .thenReturn(new LdapUser("mgroehning", Optional.<String>empty(), Optional.<String>empty(),
                    Optional.<String>empty()));
        Mockito.when(personService.getPersonByLogin(Mockito.anyString())).thenReturn(Optional.<Person>empty());
        Mockito.when(ldapSyncService.createPerson(Mockito.anyString(), Matchers.<Optional<String>>any(),
                    Matchers.<Optional<String>>any(), Matchers.<Optional<String>>any()))
            .thenReturn(TestDataCreator.createPerson());

        UserDetails userDetails = personContextMapper.mapUserFromContext(context, userNameSignedInWith, null);

        Assert.assertNotNull("Username should be set", userDetails.getUsername());
        Assert.assertEquals("Wrong username", userIdentifier, userDetails.getUsername());
    }


    @Test(expected = BadCredentialsException.class)
    public void ensureLoginIsNotPossibleIfLdapUserCanNotBeCreatedBecauseOfInvalidUserIdentifier()
        throws NamingException, UnsupportedMemberAffiliationException {

        Mockito.when(ldapUserMapper.mapFromContext(Mockito.eq(context)))
            .thenThrow(new InvalidSecurityConfigurationException("Bad!"));

        personContextMapper.mapUserFromContext(context, "username", null);
    }


    @Test(expected = BadCredentialsException.class)
    public void ensureLoginIsNotPossibleIfLdapUserHasNotSupportedMemberOfAttribute() throws NamingException,
        UnsupportedMemberAffiliationException {

        Mockito.when(ldapUserMapper.mapFromContext(Mockito.eq(context)))
            .thenThrow(new UnsupportedMemberAffiliationException("Bad!"));

        personContextMapper.mapUserFromContext(context, "username", null);
    }


    @Test
    public void ensureAuthoritiesAreBasedOnRolesOfTheSignedInPerson() throws NamingException,
        UnsupportedMemberAffiliationException {

        Person person = new Person();
        person.setPermissions(Arrays.asList(Role.USER, Role.BOSS));

        Mockito.when(ldapUserMapper.mapFromContext(Mockito.eq(context)))
            .thenReturn(new LdapUser("username", Optional.<String>empty(), Optional.<String>empty(),
                    Optional.<String>empty()));
        Mockito.when(personService.getPersonByLogin(Mockito.anyString())).thenReturn(Optional.of(person));
        Mockito.when(ldapSyncService.syncPerson(Mockito.any(Person.class), Matchers.<Optional<String>>any(),
                    Matchers.<Optional<String>>any(), Matchers.<Optional<String>>any()))
            .thenReturn(person);

        UserDetails userDetails = personContextMapper.mapUserFromContext(context, "username", null);

        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();

        Assert.assertEquals("Wrong number of authorities", 2, authorities.size());
        Assert.assertTrue("Missing authority for user role",
            SecurityTestUtil.authorityForRoleExists(authorities, Role.USER));
        Assert.assertTrue("Missing authority for boss role",
            SecurityTestUtil.authorityForRoleExists(authorities, Role.BOSS));
    }


    @Test
    public void ensureAddsOfficeRoleToSignedInUserIfNoUserWithOfficeRoleExistsYet() throws NamingException,
        UnsupportedMemberAffiliationException {

        Person person = new Person();
        person.setPermissions(Collections.singletonList(Role.USER));

        Mockito.when(ldapUserMapper.mapFromContext(Mockito.eq(context)))
            .thenReturn(new LdapUser("username", Optional.<String>empty(), Optional.<String>empty(),
                    Optional.<String>empty()));

        Mockito.when(personService.getPersonByLogin(Mockito.anyString())).thenReturn(Optional.of(person));
        Mockito.when(personService.getPersonsByRole(Role.OFFICE)).thenReturn(Collections.emptyList());
        Mockito.when(ldapSyncService.syncPerson(Mockito.any(Person.class), Matchers.<Optional<String>>any(),
                    Matchers.<Optional<String>>any(), Matchers.<Optional<String>>any()))
            .thenReturn(person);

        personContextMapper.mapUserFromContext(context, "username", null);

        Mockito.verify(personService).getPersonsByRole(Role.OFFICE);
        Mockito.verify(ldapSyncService).appointPersonAsOfficeUser(person);
    }
}
