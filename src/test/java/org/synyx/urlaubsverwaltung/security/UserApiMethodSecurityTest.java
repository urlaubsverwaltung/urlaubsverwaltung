package org.synyx.urlaubsverwaltung.security;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.core.oidc.IdTokenClaimNames;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.synyx.urlaubsverwaltung.department.Department;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.person.Role.DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

@RunWith(MockitoJUnitRunner.class)
public class UserApiMethodSecurityTest {

    private UserApiMethodSecurity sut;

    @Mock
    private PersonService personService;
    @Mock
    private DepartmentService departmentService;

    @Before
    public void setUp() {
        sut = new UserApiMethodSecurity(personService, departmentService);
    }

    @Test
    public void isInDepartmentOfAuthenticatedHeadPersonId() {
        final Person member = new Person("Member", "lastname", "firstName", "email");
        when(personService.getPersonByID(1)).thenReturn(Optional.of(member));

        final String usernameDepartmentHead = "Head";
        final Person departmentHead = new Person(usernameDepartmentHead, "lastname", "firstName", "email");
        departmentHead.setPermissions(List.of(DEPARTMENT_HEAD));
        when(personService.getPersonByUsername(usernameDepartmentHead)).thenReturn(Optional.of(departmentHead));

        final Department department = new Department();
        department.setMembers(List.of(member, departmentHead));
        department.setDepartmentHeads(List.of(departmentHead));
        when(departmentService.getManagedDepartmentsOfDepartmentHead(departmentHead)).thenReturn(List.of(department));

        final Authentication authentication = getAuthenticationToken(usernameDepartmentHead);
        final boolean inDepartmentOfAuthenticatedHeadPersonId = sut.isInDepartmentOfDepartmentHead(authentication, 1);
        assertThat(inDepartmentOfAuthenticatedHeadPersonId).isTrue();
    }

    @Test
    public void isNotInDepartmentOfAuthenticatedHeadPersonId() {
        final Person member = new Person("Member", "lastname", "firstName", "email");
        when(personService.getPersonByID(1)).thenReturn(Optional.of(member));

        final String usernameDepartmentHead = "Head";
        final Person departmentHead = new Person(usernameDepartmentHead, "lastname", "firstName", "email");
        departmentHead.setPermissions(List.of(DEPARTMENT_HEAD));
        when(personService.getPersonByUsername(usernameDepartmentHead)).thenReturn(Optional.of(departmentHead));

        when(departmentService.getManagedDepartmentsOfDepartmentHead(departmentHead)).thenReturn(List.of(new Department()));

        final Authentication authentication = getAuthenticationToken(usernameDepartmentHead);
        final boolean inDepartmentOfAuthenticatedHeadPersonId = sut.isInDepartmentOfDepartmentHead(authentication, 1);
        assertThat(inDepartmentOfAuthenticatedHeadPersonId).isFalse();
    }

    @Test
    public void isInDepartmentOfAuthenticatedHeadPersonIdButHasNoDepartmentHeadRole() {
        final String usernameDepartmentHead = "DepartmentHead";
        final Person departmentHead = new Person(usernameDepartmentHead, "lastname", "firstName", "email");
        departmentHead.setPermissions(List.of(USER));
        when(personService.getPersonByUsername(usernameDepartmentHead)).thenReturn(Optional.of(departmentHead));

        final Authentication authentication = getAuthenticationToken(usernameDepartmentHead);
        final boolean inDepartmentOfAuthenticatedHeadPersonId = sut.isInDepartmentOfDepartmentHead(authentication, 1);
        assertThat(inDepartmentOfAuthenticatedHeadPersonId).isFalse();
    }

    @Test
    public void isInDepartmentOfAuthenticatedHeadPersonIdButNoPersonFound() {
        final String usernameDepartmentHead = "DepartmentHead";
        final Person departmentHead = new Person(usernameDepartmentHead, "lastname", "firstName", "email");
        departmentHead.setPermissions(List.of(USER));
        when(personService.getPersonByUsername(usernameDepartmentHead)).thenReturn(Optional.of(departmentHead));

        final Authentication authentication = getAuthenticationToken(usernameDepartmentHead);
        final boolean inDepartmentOfAuthenticatedHeadPersonId = sut.isInDepartmentOfDepartmentHead(authentication, 1);
        assertThat(inDepartmentOfAuthenticatedHeadPersonId).isFalse();
    }

    @Test
    public void isInDepartmentOfAuthenticatedHeadPersonIdButIsNotLoggedIn() {
        when(personService.getPersonByID(1)).thenReturn(Optional.empty());

        final String usernameDepartmentHead = "Head";
        final Person departmentHead = new Person(usernameDepartmentHead, "lastname", "firstName", "email");
        departmentHead.setPermissions(List.of(DEPARTMENT_HEAD));
        when(personService.getPersonByUsername(usernameDepartmentHead)).thenReturn(Optional.of(departmentHead));

        final Authentication authentication = getAuthenticationToken(usernameDepartmentHead);
        final boolean inDepartmentOfAuthenticatedHeadPersonId = sut.isInDepartmentOfDepartmentHead(authentication, 1);
        assertThat(inDepartmentOfAuthenticatedHeadPersonId).isFalse();
    }

    @Test
    public void isSamePersonIdNoPerson() {

        when(personService.getPersonByID(1)).thenReturn(Optional.empty());

        final boolean isSamePerson = sut.isSamePersonId(mock(Authentication.class), 1);
        assertThat(isSamePerson).isFalse();
    }

    @Test
    public void isSamePersonIdWithOidc() {

        final String username = "Hans";
        final TestingAuthenticationToken authentication = getAuthenticationToken(username);

        when(personService.getPersonByID(1))
            .thenReturn(Optional.of(new Person(username, "lastname", "firstName", "email")));

        final boolean isSamePerson = sut.isSamePersonId(authentication, 1);
        assertThat(isSamePerson).isTrue();
    }

    @Test
    public void isNotSamePersonIdWithOidc() {
        final String username = "Hans";
        final Authentication authentication = getAuthenticationToken(username);
        when(personService.getPersonByID(1)).thenReturn(Optional.of(new Person("differentUsername", "lastname", "firstName", "email")));

        final boolean isSamePerson = sut.isSamePersonId(authentication, 1);
        assertThat(isSamePerson).isFalse();
    }

    @Test
    public void isSamePersonIdWithSimpleAuthentication() {

        final String username = "Hans";
        final User user = new User(username, "password", List.of());
        final TestingAuthenticationToken authentication = new TestingAuthenticationToken(user, List.of());

        when(personService.getPersonByID(1)).thenReturn(Optional.of(new Person(username, "lastname", "firstName", "email")));

        final boolean isSamePerson = sut.isSamePersonId(authentication, 1);
        assertThat(isSamePerson).isTrue();
    }

    @Test
    public void isDifferentPersonIdWithSimpleAuthentication() {

        final User user = new User("username", "password", List.of());
        final TestingAuthenticationToken authentication = new TestingAuthenticationToken(user, List.of());

        when(personService.getPersonByID(1)).thenReturn(Optional.of(new Person("differentUsername", "lastname", "firstName", "email")));

        final boolean isSamePerson = sut.isSamePersonId(authentication, 1);
        assertThat(isSamePerson).isFalse();
    }

    @Test
    public void isSamePersonIdWithLdap() {

        final String username = "Hans";
        final org.springframework.security.ldap.userdetails.Person ldapUser = mock(org.springframework.security.ldap.userdetails.Person.class);
        final TestingAuthenticationToken authentication = new TestingAuthenticationToken(ldapUser, List.of());

        when(ldapUser.getUsername()).thenReturn(username);
        when(personService.getPersonByID(1)).thenReturn(Optional.of(new Person(username, "lastname", "firstName", "email")));

        final boolean isSamePerson = sut.isSamePersonId(authentication, 1);
        assertThat(isSamePerson).isTrue();
    }

    @Test
    public void isDifferentPersonIdWithLdap() {

        final org.springframework.security.ldap.userdetails.Person ldapUser = mock(org.springframework.security.ldap.userdetails.Person.class);
        final TestingAuthenticationToken authentication = new TestingAuthenticationToken(ldapUser, List.of());

        when(ldapUser.getUsername()).thenReturn("username");
        when(personService.getPersonByID(1)).thenReturn(Optional.of(new Person("differentUsername", "lastname", "firstName", "email")));

        final boolean isSamePerson = sut.isSamePersonId(authentication, 1);
        assertThat(isSamePerson).isFalse();
    }

    private TestingAuthenticationToken getAuthenticationToken(final String username) {
        final Instant now = Instant.now();
        final OidcIdToken token = new OidcIdToken("token", now, now.plusSeconds(60), Map.of(IdTokenClaimNames.SUB, username));
        final DefaultOidcUser oidcUser = new DefaultOidcUser(List.of(new OidcUserAuthority(token)), token);
        return new TestingAuthenticationToken(oidcUser, List.of());
    }
}
