package org.synyx.urlaubsverwaltung.security;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.PersonService;
import org.synyx.urlaubsverwaltung.core.person.Role;
import org.synyx.urlaubsverwaltung.test.TestDataCreator;

import javax.naming.Name;
import javax.naming.NamingException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


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

        personService = mock(PersonService.class);
        ldapSyncService = mock(LdapSyncService.class);
        ldapUserMapper = mock(LdapUserMapper.class);

        personContextMapper = new PersonContextMapper(personService, ldapSyncService, ldapUserMapper);

        context = mock(DirContextOperations.class);

        when(context.getDn()).thenReturn(mock(Name.class));
        when(context.getStringAttributes("cn")).thenReturn(new String[] { "First", "Last" });
        when(context.getStringAttribute(anyString())).thenReturn("Foo");
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsIfTryingToGetAuthoritiesForNullPerson() {

        personContextMapper.getGrantedAuthorities(null);
    }


    @Test(expected = IllegalStateException.class)
    public void ensureThrowsIfTryingToGetAuthoritiesOfPersonWithNoRoles() {

        Person person = TestDataCreator.createPerson();
        person.setPermissions(Collections.emptyList());

        personContextMapper.getGrantedAuthorities(person);
    }


    @Test
    public void ensureReturnsCorrectListOfAuthoritiesUsingTheRolesOfTheGivenPerson() {

        Person person = TestDataCreator.createPerson();
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

        when(ldapUserMapper.mapFromContext(Mockito.eq(context)))
            .thenReturn(new LdapUser("murygina", Optional.of("Aljona"), Optional.of("Murygina"),
                    Optional.of("murygina@synyx.de")));
        when(personService.getPersonByLogin(anyString())).thenReturn(Optional.<Person>empty());
        when(ldapSyncService.createPerson(anyString(), Matchers.<Optional<String>>any(),
                    Matchers.<Optional<String>>any(), Matchers.<Optional<String>>any()))
            .thenReturn(TestDataCreator.createPerson());

        personContextMapper.mapUserFromContext(context, "murygina", null);

        verify(ldapUserMapper).mapFromContext(context);
        verify(ldapSyncService)
            .createPerson(Mockito.eq("murygina"), Mockito.eq(Optional.of("Aljona")),
                Mockito.eq(Optional.of("Murygina")), Mockito.eq(Optional.of("murygina@synyx.de")));
    }


    @Test
    public void ensureSyncsPersonDataUsingLDAPAttributes() throws NamingException,
        UnsupportedMemberAffiliationException {

        Person person = TestDataCreator.createPerson();
        person.setPermissions(Collections.singletonList(Role.USER));

        when(ldapUserMapper.mapFromContext(Mockito.eq(context)))
            .thenReturn(new LdapUser("murygina", Optional.of("Aljona"), Optional.of("Murygina"),
                    Optional.of("murygina@synyx.de")));
        when(personService.getPersonByLogin(anyString())).thenReturn(Optional.of(person));
        when(ldapSyncService.syncPerson(any(Person.class), Matchers.<Optional<String>>any(),
                    Matchers.<Optional<String>>any(), Matchers.<Optional<String>>any()))
            .thenReturn(person);

        personContextMapper.mapUserFromContext(context, "murygina", null);

        verify(ldapUserMapper).mapFromContext(context);
        verify(ldapSyncService)
            .syncPerson(Mockito.eq(person), Mockito.eq(Optional.of("Aljona")), Mockito.eq(Optional.of("Murygina")),
                Mockito.eq(Optional.of("murygina@synyx.de")));
    }


    @Test
    public void ensureUsernameIsBasedOnLdapUsername() throws NamingException, UnsupportedMemberAffiliationException {

        String userIdentifier = "mgroehning";
        String userNameSignedInWith = "mgroehning@simpsons.com";

        when(ldapUserMapper.mapFromContext(Mockito.eq(context)))
            .thenReturn(new LdapUser("mgroehning", Optional.<String>empty(), Optional.<String>empty(),
                    Optional.<String>empty()));
        when(personService.getPersonByLogin(anyString())).thenReturn(Optional.<Person>empty());
        when(ldapSyncService.createPerson(anyString(), Matchers.<Optional<String>>any(),
                    Matchers.<Optional<String>>any(), Matchers.<Optional<String>>any()))
            .thenReturn(TestDataCreator.createPerson());

        UserDetails userDetails = personContextMapper.mapUserFromContext(context, userNameSignedInWith, null);

        Assert.assertNotNull("Username should be set", userDetails.getUsername());
        Assert.assertEquals("Wrong username", userIdentifier, userDetails.getUsername());
    }


    @Test(expected = DisabledException.class)
    public void ensureLoginIsNotPossibleIfUserIsDeactivated() throws UnsupportedMemberAffiliationException,
        NamingException {

        Person person = TestDataCreator.createPerson();
        person.setPermissions(Collections.singletonList(Role.INACTIVE));

        when(personService.getPersonByLogin(anyString())).thenReturn(Optional.of(person));
        when(ldapUserMapper.mapFromContext(Mockito.eq(context)))
            .thenReturn(new LdapUser(person.getLoginName(), Optional.of(person.getFirstName()),
                    Optional.of(person.getLastName()), Optional.of(person.getEmail())));
        when(ldapSyncService.syncPerson(any(Person.class), Matchers.<Optional<String>>any(),
                    Matchers.<Optional<String>>any(), Matchers.<Optional<String>>any()))
            .thenReturn(person);

        personContextMapper.mapUserFromContext(context, person.getLoginName(), null);
    }


    @Test(expected = BadCredentialsException.class)
    public void ensureLoginIsNotPossibleIfLdapUserCanNotBeCreatedBecauseOfInvalidUserIdentifier()
        throws NamingException, UnsupportedMemberAffiliationException {

        when(ldapUserMapper.mapFromContext(Mockito.eq(context)))
            .thenThrow(new InvalidSecurityConfigurationException("Bad!"));

        personContextMapper.mapUserFromContext(context, "username", null);
    }


    @Test(expected = BadCredentialsException.class)
    public void ensureLoginIsNotPossibleIfLdapUserHasNotSupportedMemberOfAttribute() throws NamingException,
        UnsupportedMemberAffiliationException {

        when(ldapUserMapper.mapFromContext(Mockito.eq(context)))
            .thenThrow(new UnsupportedMemberAffiliationException("Bad!"));

        personContextMapper.mapUserFromContext(context, "username", null);
    }


    @Test
    public void ensureAuthoritiesAreBasedOnRolesOfTheSignedInPerson() throws NamingException,
        UnsupportedMemberAffiliationException {

        Person person = TestDataCreator.createPerson("username");
        person.setPermissions(Arrays.asList(Role.USER, Role.BOSS));

        when(ldapUserMapper.mapFromContext(Mockito.eq(context)))
            .thenReturn(new LdapUser("username", Optional.<String>empty(), Optional.<String>empty(),
                    Optional.<String>empty()));
        when(personService.getPersonByLogin(anyString())).thenReturn(Optional.of(person));
        when(ldapSyncService.syncPerson(any(Person.class), Matchers.<Optional<String>>any(),
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

        Person person = TestDataCreator.createPerson("username");
        person.setPermissions(Collections.singletonList(Role.USER));

        when(ldapUserMapper.mapFromContext(Mockito.eq(context)))
            .thenReturn(new LdapUser("username", Optional.<String>empty(), Optional.<String>empty(),
                    Optional.<String>empty()));

        when(personService.getPersonByLogin(anyString())).thenReturn(Optional.of(person));
        when(personService.getPersonsByRole(Role.OFFICE)).thenReturn(Collections.emptyList());
        when(ldapSyncService.syncPerson(any(Person.class), Matchers.<Optional<String>>any(),
                    Matchers.<Optional<String>>any(), Matchers.<Optional<String>>any()))
            .thenReturn(person);

        personContextMapper.mapUserFromContext(context, "username", null);

        verify(personService).getPersonsByRole(Role.OFFICE);
        verify(ldapSyncService).appointPersonAsOfficeUser(person);
    }
}
