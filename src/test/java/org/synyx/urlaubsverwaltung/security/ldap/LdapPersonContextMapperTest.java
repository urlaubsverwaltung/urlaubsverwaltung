package org.synyx.urlaubsverwaltung.security.ldap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.Role;

import javax.naming.Name;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static java.util.Optional.empty;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_USER;
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.INACTIVE;
import static org.synyx.urlaubsverwaltung.person.Role.USER;


/**
 * Unit test for {@link LdapPersonContextMapper}.
 */
@ExtendWith(MockitoExtension.class)
class LdapPersonContextMapperTest {

    private LdapPersonContextMapper sut;

    @Mock
    private PersonService personService;
    @Mock
    private LdapUserMapper ldapUserMapper;
    @Mock
    private DirContextOperations context;

    @BeforeEach
    void setUp() {
        sut = new LdapPersonContextMapper(personService, ldapUserMapper);
    }

    @Test
    void ensureThrowsIfTryingToGetAuthoritiesOfPersonWithNoRoles() {
        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setPermissions(emptyList());

        assertThatIllegalStateException().isThrownBy(() -> sut.getGrantedAuthorities(person));
    }

    @Test
    void ensureReturnsCorrectListOfAuthoritiesUsingTheRolesOfTheGivenPerson() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setPermissions(List.of(USER, BOSS));

        final Collection<GrantedAuthority> authorities = sut.getGrantedAuthorities(person);
        assertThat(authorities).hasSize(2);
        assertThat(authorityForRoleExists(authorities, USER)).isTrue();
        assertThat(authorityForRoleExists(authorities, BOSS)).isTrue();
    }

    @Test
    void ensureCreatePersonIfPersonDoesNotExist() throws UnsupportedMemberAffiliationException {

        when(context.getDn()).thenReturn(mock(Name.class));
        when(context.getStringAttributes("cn")).thenReturn(new String[]{"First", "Last"});
        when(context.getStringAttribute(anyString())).thenReturn("Foo");

        when(ldapUserMapper.mapFromContext(context)).thenReturn(new LdapUser("murygina", "Aljona", "Murygina", "murygina@synyx.de", List.of()));
        when(personService.getPersonByUsername(anyString())).thenReturn(empty());

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setPermissions(List.of(Role.USER));
        when(personService.create(anyString(), anyString(), anyString(), anyString(), anyList(), anyList())).thenReturn(person);
        when(personService.appointAsOfficeUserIfNoOfficeUserPresent(any())).then(returnsFirstArg());

        sut.mapUserFromContext(context, "murygina", null);

        verify(ldapUserMapper).mapFromContext(context);
        verify(personService).create("murygina", "Murygina", "Aljona", "murygina@synyx.de", List.of(NOTIFICATION_USER), List.of(USER));
    }

    @Test
    void ensureSyncsPersonDataUsingLDAPAttributes() throws UnsupportedMemberAffiliationException {

        when(context.getDn()).thenReturn(mock(Name.class));
        when(context.getStringAttributes("cn")).thenReturn(new String[]{"First", "Last"});
        when(context.getStringAttribute(anyString())).thenReturn("Foo");

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setPermissions(List.of(USER));

        when(ldapUserMapper.mapFromContext(context)).thenReturn(new LdapUser("murygina", "Aljona", "Murygina", "murygina@synyx.de", List.of()));
        when(personService.getPersonByUsername(anyString())).thenReturn(Optional.of(person));
        when(personService.save(any(Person.class))).thenReturn(person);

        sut.mapUserFromContext(context, "murygina", null);

        verify(ldapUserMapper).mapFromContext(context);
        assertThat(person.getEmail()).isEqualTo("murygina@synyx.de");
        assertThat(person.getFirstName()).isEqualTo("Aljona");
        assertThat(person.getLastName()).isEqualTo("Murygina");
        verify(personService).save(person);
    }

    @Test
    void ensureUsernameIsBasedOnLdapUsername() throws UnsupportedMemberAffiliationException {

        when(context.getDn()).thenReturn(mock(Name.class));
        when(context.getStringAttributes("cn")).thenReturn(new String[]{"First", "Last"});
        when(context.getStringAttribute(anyString())).thenReturn("Foo");

        final String userIdentifier = "mgroehning";
        final String userNameSignedInWith = "mgroehning@simpsons.com";

        when(ldapUserMapper.mapFromContext(context)).thenReturn(new LdapUser(userIdentifier, null, null, null, List.of()));
        when(personService.getPersonByUsername(anyString())).thenReturn(empty());

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setPermissions(List.of(Role.USER));
        when(personService.create("mgroehning", null, null, null, List.of(NOTIFICATION_USER), List.of(USER))).thenReturn(person);
        when(personService.appointAsOfficeUserIfNoOfficeUserPresent(any())).then(returnsFirstArg());

        final UserDetails userDetails = sut.mapUserFromContext(context, userNameSignedInWith, emptyList());
        assertThat(userDetails.getUsername()).isEqualTo(userIdentifier);
    }

    @Test
    void ensureLoginIsNotPossibleIfUserIsDeactivated() throws UnsupportedMemberAffiliationException {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setPermissions(List.of(INACTIVE));

        final String username = person.getUsername();

        when(personService.getPersonByUsername(anyString())).thenReturn(Optional.of(person));
        when(ldapUserMapper.mapFromContext(context)).thenReturn(new LdapUser(username, person.getFirstName(), person.getLastName(), person.getEmail(), List.of()));
        assertThatThrownBy(() -> sut.mapUserFromContext(context, username, null))
            .isInstanceOf(DisabledException.class);
    }

    @Test
    void ensureLoginIsNotPossibleIfLdapUserCanNotBeCreatedBecauseOfInvalidUserIdentifier() throws UnsupportedMemberAffiliationException {
        when(ldapUserMapper.mapFromContext(context)).thenThrow(new InvalidSecurityConfigurationException("Bad!"));

        assertThatThrownBy(() ->
            sut.mapUserFromContext(context, "username", null)).isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void ensureLoginIsNotPossibleIfLdapUserHasNotSupportedMemberOfAttribute() throws UnsupportedMemberAffiliationException {
        when(ldapUserMapper.mapFromContext(context)).thenThrow(new UnsupportedMemberAffiliationException("Bad!"));

        assertThatThrownBy(() ->
            sut.mapUserFromContext(context, "username", null)).isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void ensureAuthoritiesAreBasedOnRolesOfTheSignedInPerson() throws UnsupportedMemberAffiliationException {

        when(context.getDn()).thenReturn(mock(Name.class));
        when(context.getStringAttributes("cn")).thenReturn(new String[]{"First", "Last"});
        when(context.getStringAttribute(anyString())).thenReturn("Foo");

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setPermissions(List.of(USER, BOSS));

        when(ldapUserMapper.mapFromContext(context)).thenReturn(new LdapUser("username", null, null, null, List.of()));
        when(personService.getPersonByUsername(anyString())).thenReturn(Optional.of(person));
        when(personService.save(any(Person.class))).thenReturn(person);

        final UserDetails userDetails = sut.mapUserFromContext(context, "username", null);
        assertThat(userDetails.getAuthorities()).hasSize(2);
        assertThat(authorityForRoleExists(userDetails.getAuthorities(), USER)).isTrue();
        assertThat(authorityForRoleExists(userDetails.getAuthorities(), BOSS)).isTrue();
    }

    @Test
    void ensureAddsOfficeRoleToSignedInUserIfNoUserWithOfficeRoleExistsYet() throws UnsupportedMemberAffiliationException {

        when(context.getDn()).thenReturn(mock(Name.class));
        when(context.getStringAttributes("cn")).thenReturn(new String[]{"First", "Last"});
        when(context.getStringAttribute(anyString())).thenReturn("Foo");

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setPermissions(List.of(USER));

        when(ldapUserMapper.mapFromContext(context)).thenReturn(new LdapUser("username", null, null, null, List.of()));
        when(personService.getPersonByUsername("username")).thenReturn(Optional.empty());
        when(personService.create("username", null, null, null, List.of(NOTIFICATION_USER), List.of(USER))).thenReturn(person);
        when(personService.appointAsOfficeUserIfNoOfficeUserPresent(person)).thenReturn(person);

        sut.mapUserFromContext(context, "username", null);
        verify(personService).appointAsOfficeUserIfNoOfficeUserPresent(any(Person.class));
    }

    private boolean authorityForRoleExists(Collection<? extends GrantedAuthority> authorities, final Role role) {
        return authorities.stream().anyMatch(authority -> authority.getAuthority().equals(role.name()));
    }
}


