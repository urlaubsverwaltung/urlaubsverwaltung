package org.synyx.urlaubsverwaltung.security.ldap;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import javax.naming.Name;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Optional.empty;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_USER;
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.INACTIVE;
import static org.synyx.urlaubsverwaltung.person.Role.USER;
import static org.synyx.urlaubsverwaltung.security.ldap.SecurityTestUtil.authorityForRoleExists;
import static org.synyx.urlaubsverwaltung.testdatacreator.TestDataCreator.createPerson;


/**
 * Unit test for {@link LdapPersonContextMapper}.
 */
@RunWith(MockitoJUnitRunner.class)
public class LdapPersonContextMapperTest {

    private LdapPersonContextMapper sut;

    @Mock
    private PersonService personService;
    @Mock
    private LdapUserMapper ldapUserMapper;
    @Mock
    private DirContextOperations context;

    @Before
    public void setUp() {

        sut = new LdapPersonContextMapper(personService, ldapUserMapper);

        when(context.getDn()).thenReturn(mock(Name.class));
        when(context.getStringAttributes("cn")).thenReturn(new String[]{"First", "Last"});
        when(context.getStringAttribute(anyString())).thenReturn("Foo");
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsIfTryingToGetAuthoritiesForNullPerson() {

        sut.getGrantedAuthorities(null);
    }


    @Test(expected = IllegalStateException.class)
    public void ensureThrowsIfTryingToGetAuthoritiesOfPersonWithNoRoles() {

        final Person person = createPerson();
        person.setPermissions(emptyList());

        sut.getGrantedAuthorities(person);
    }


    @Test
    public void ensureReturnsCorrectListOfAuthoritiesUsingTheRolesOfTheGivenPerson() {

        final Person person = createPerson();
        person.setPermissions(Arrays.asList(USER, BOSS));

        final Collection<GrantedAuthority> authorities = sut.getGrantedAuthorities(person);

        Assert.assertEquals("Wrong number of authorities", 2, authorities.size());
        Assert.assertTrue("No authority for user role found",
            authorityForRoleExists(authorities, USER));
        Assert.assertTrue("No authority for boss role found",
            authorityForRoleExists(authorities, BOSS));
    }


    @Test
    public void ensureCreatesPersonIfPersonDoesNotExist() throws UnsupportedMemberAffiliationException {

        when(ldapUserMapper.mapFromContext(eq(context)))
            .thenReturn(new LdapUser("murygina", Optional.of("Aljona"), Optional.of("Murygina"),
                Optional.of("murygina@synyx.de")));
        when(personService.getPersonByUsername(anyString())).thenReturn(empty());
        when(personService.create(anyString(), anyString(), anyString(), anyString(), anyList(), anyList())).thenReturn(createPerson());
        when(personService.appointAsOfficeUserIfNoOfficeUserPresent(any())).then(returnsFirstArg());

        sut.mapUserFromContext(context, "murygina", null);

        verify(ldapUserMapper).mapFromContext(context);
        verify(personService).create("murygina", "Murygina", "Aljona", "murygina@synyx.de",
            singletonList(NOTIFICATION_USER), singletonList(USER));
    }


    @Test
    public void ensureSyncsPersonDataUsingLDAPAttributes() throws UnsupportedMemberAffiliationException {

        final Person person = createPerson();
        person.setPermissions(singletonList(USER));

        when(ldapUserMapper.mapFromContext(eq(context)))
            .thenReturn(new LdapUser("murygina", Optional.of("Aljona"), Optional.of("Murygina"),
                Optional.of("murygina@synyx.de")));
        when(personService.getPersonByUsername(anyString())).thenReturn(Optional.of(person));
        when(personService.save(any(Person.class))).thenReturn(person);

        sut.mapUserFromContext(context, "murygina", null);

        verify(ldapUserMapper).mapFromContext(context);
        assertThat(person.getEmail()).isEqualTo("murygina@synyx.de");
        assertThat(person.getFirstName()).isEqualTo("Aljona");
        assertThat(person.getLastName()).isEqualTo("Murygina");
        verify(personService).save(eq(person));
    }


    @Test
    public void ensureUsernameIsBasedOnLdapUsername() throws UnsupportedMemberAffiliationException {

        final String userIdentifier = "mgroehning";
        final String userNameSignedInWith = "mgroehning@simpsons.com";

        when(ldapUserMapper.mapFromContext(eq(context))).thenReturn(new LdapUser(userIdentifier, empty(), empty(), empty()));
        when(personService.getPersonByUsername(anyString())).thenReturn(empty());

        final Person person = createPerson(userIdentifier);
        when(personService.create("mgroehning", null, null, null, singletonList(NOTIFICATION_USER), singletonList(USER))).thenReturn(person);
        when(personService.appointAsOfficeUserIfNoOfficeUserPresent(any())).then(returnsFirstArg());

        final UserDetails userDetails = sut.mapUserFromContext(context, userNameSignedInWith, emptyList());
        assertThat(userDetails.getUsername()).isEqualTo(userIdentifier);
    }


    @Test(expected = DisabledException.class)
    public void ensureLoginIsNotPossibleIfUserIsDeactivated() throws UnsupportedMemberAffiliationException {

        final Person person = createPerson();
        person.setPermissions(singletonList(INACTIVE));

        when(personService.getPersonByUsername(anyString())).thenReturn(Optional.of(person));
        when(ldapUserMapper.mapFromContext(eq(context)))
            .thenReturn(new LdapUser(person.getUsername(), Optional.of(person.getFirstName()),
                Optional.of(person.getLastName()), Optional.of(person.getEmail())));

        sut.mapUserFromContext(context, person.getUsername(), null);
    }


    @Test(expected = BadCredentialsException.class)
    public void ensureLoginIsNotPossibleIfLdapUserCanNotBeCreatedBecauseOfInvalidUserIdentifier() throws UnsupportedMemberAffiliationException {

        when(ldapUserMapper.mapFromContext(eq(context)))
            .thenThrow(new InvalidSecurityConfigurationException("Bad!"));

        sut.mapUserFromContext(context, "username", null);
    }


    @Test(expected = BadCredentialsException.class)
    public void ensureLoginIsNotPossibleIfLdapUserHasNotSupportedMemberOfAttribute() throws UnsupportedMemberAffiliationException {

        when(ldapUserMapper.mapFromContext(eq(context)))
            .thenThrow(new UnsupportedMemberAffiliationException("Bad!"));

        sut.mapUserFromContext(context, "username", null);
    }


    @Test
    public void ensureAuthoritiesAreBasedOnRolesOfTheSignedInPerson() throws UnsupportedMemberAffiliationException {

        final Person person = createPerson("username");
        person.setPermissions(Arrays.asList(USER, BOSS));

        when(ldapUserMapper.mapFromContext(eq(context)))
            .thenReturn(new LdapUser("username", empty(), empty(), empty()));
        when(personService.getPersonByUsername(anyString())).thenReturn(Optional.of(person));
        when(personService.save(any(Person.class))).thenReturn(person);

        final UserDetails userDetails = sut.mapUserFromContext(context, "username", null);
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();

        Assert.assertEquals("Wrong number of authorities", 2, authorities.size());
        Assert.assertTrue("Missing authority for user role", authorityForRoleExists(authorities, USER));
        Assert.assertTrue("Missing authority for boss role", authorityForRoleExists(authorities, BOSS));
    }


    @Test
    public void ensureAddsOfficeRoleToSignedInUserIfNoUserWithOfficeRoleExistsYet() throws UnsupportedMemberAffiliationException {

        final Person person = createPerson("username");
        person.setPermissions(singletonList(USER));

        when(ldapUserMapper.mapFromContext(eq(context))).thenReturn(new LdapUser("username", empty(), empty(), empty()));
        when(personService.getPersonByUsername("username")).thenReturn(Optional.empty());
        when(personService.create("username", null, null, null,
            singletonList(NOTIFICATION_USER), singletonList(USER))).thenReturn(person);
        when(personService.appointAsOfficeUserIfNoOfficeUserPresent(person)).thenReturn(person);

        sut.mapUserFromContext(context, "username", null);
        verify(personService).appointAsOfficeUserIfNoOfficeUserPresent(any(Person.class));
    }
}
