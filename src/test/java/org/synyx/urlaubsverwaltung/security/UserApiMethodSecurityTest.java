package org.synyx.urlaubsverwaltung.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.IdTokenClaimNames;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
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
import static org.synyx.urlaubsverwaltung.person.Role.SECOND_STAGE_AUTHORITY;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

@ExtendWith(MockitoExtension.class)
class UserApiMethodSecurityTest {

    private UserApiMethodSecurity sut;

    @Mock
    private PersonService personService;
    @Mock
    private DepartmentService departmentService;

    @BeforeEach
    void setUp() {
        sut = new UserApiMethodSecurity(personService, departmentService);
    }

    @Test
    void isInDepartmentOfAuthenticatedSSAPersonId() {
        final Person member = new Person("Member", "lastname", "firstName", "email");
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(member));

        final String usernameSSA = "SSA";
        final Person ssa = new Person(usernameSSA, "lastname", "firstName", "email");
        ssa.setPermissions(List.of(SECOND_STAGE_AUTHORITY));
        when(personService.getPersonByUsername(usernameSSA)).thenReturn(Optional.of(ssa));

        when(departmentService.isSecondStageAuthorityAllowedToManagePerson(ssa, member)).thenReturn(true);

        final Authentication authentication = getAuthenticationToken(usernameSSA);
        final boolean inDepartmentOfAuthenticatedSSAPersonId = sut.isInDepartmentOfSecondStageAuthority(authentication, 1L);
        assertThat(inDepartmentOfAuthenticatedSSAPersonId).isTrue();
    }

    @Test
    void isNotInDepartmentOfAuthenticatedSSAPersonId() {
        final Person notMember = new Person("Member", "lastname", "firstName", "email");
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(notMember));

        final String usernameSSA = "SSA";
        final Person departmentHead = new Person(usernameSSA, "lastname", "firstName", "email");
        departmentHead.setPermissions(List.of(SECOND_STAGE_AUTHORITY));
        when(personService.getPersonByUsername(usernameSSA)).thenReturn(Optional.of(departmentHead));

        when(departmentService.isSecondStageAuthorityAllowedToManagePerson(departmentHead, notMember)).thenReturn(false);

        final Authentication authentication = getAuthenticationToken(usernameSSA);
        final boolean inDepartmentOfAuthenticatedSSAPersonId = sut.isInDepartmentOfSecondStageAuthority(authentication, 1L);
        assertThat(inDepartmentOfAuthenticatedSSAPersonId).isFalse();
    }

    @Test
    void isInDepartmentOfAuthenticatedSSAPersonIdButHasNoDepartmentHeadRole() {
        final String usernameSSA = "SSA";
        final Person ssa = new Person(usernameSSA, "lastname", "firstName", "email");
        ssa.setPermissions(List.of(USER));
        when(personService.getPersonByUsername(usernameSSA)).thenReturn(Optional.of(ssa));

        final Authentication authentication = getAuthenticationToken(usernameSSA);
        final boolean inDepartmentOfAuthenticatedSSAPersonId = sut.isInDepartmentOfSecondStageAuthority(authentication, 1L);
        assertThat(inDepartmentOfAuthenticatedSSAPersonId).isFalse();
    }

    @Test
    void isInDepartmentOfAuthenticatedSSAPersonIdButNoPersonFound() {
        final String usernameSSA = "ssa";
        final Person ssa = new Person(usernameSSA, "lastname", "firstName", "email");
        ssa.setPermissions(List.of(USER));
        when(personService.getPersonByUsername(usernameSSA)).thenReturn(Optional.of(ssa));

        final Authentication authentication = getAuthenticationToken(usernameSSA);
        final boolean inDepartmentOfAuthenticatedSSAPersonId = sut.isInDepartmentOfSecondStageAuthority(authentication, 1L);
        assertThat(inDepartmentOfAuthenticatedSSAPersonId).isFalse();
    }

    @Test
    void isInDepartmentOfAuthenticatedSSAPersonIdButIsNotLoggedIn() {
        when(personService.getPersonByID(1L)).thenReturn(Optional.empty());

        final String usernameSSA = "ssa";
        final Person ssa = new Person(usernameSSA, "lastname", "firstName", "email");
        ssa.setPermissions(List.of(SECOND_STAGE_AUTHORITY));
        when(personService.getPersonByUsername(usernameSSA)).thenReturn(Optional.of(ssa));

        final Authentication authentication = getAuthenticationToken(usernameSSA);
        final boolean inDepartmentOfAuthenticatedSSAPersonId = sut.isInDepartmentOfSecondStageAuthority(authentication, 1L);
        assertThat(inDepartmentOfAuthenticatedSSAPersonId).isFalse();
    }

    @Test
    void isInDepartmentOfAuthenticatedHeadPersonId() {
        final Person member = new Person("Member", "lastname", "firstName", "email");
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(member));

        final String usernameDepartmentHead = "Head";
        final Person departmentHead = new Person(usernameDepartmentHead, "lastname", "firstName", "email");
        departmentHead.setPermissions(List.of(DEPARTMENT_HEAD));
        when(personService.getPersonByUsername(usernameDepartmentHead)).thenReturn(Optional.of(departmentHead));

        when(departmentService.isDepartmentHeadAllowedToManagePerson(departmentHead, member)).thenReturn(true);

        final Authentication authentication = getAuthenticationToken(usernameDepartmentHead);
        final boolean inDepartmentOfAuthenticatedHeadPersonId = sut.isInDepartmentOfDepartmentHead(authentication, 1L);
        assertThat(inDepartmentOfAuthenticatedHeadPersonId).isTrue();
    }

    @Test
    void isNotInDepartmentOfAuthenticatedHeadPersonId() {
        final Person notMember = new Person("Member", "lastname", "firstName", "email");
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(notMember));

        final String usernameDepartmentHead = "Head";
        final Person departmentHead = new Person(usernameDepartmentHead, "lastname", "firstName", "email");
        departmentHead.setPermissions(List.of(DEPARTMENT_HEAD));
        when(personService.getPersonByUsername(usernameDepartmentHead)).thenReturn(Optional.of(departmentHead));

        when(departmentService.isDepartmentHeadAllowedToManagePerson(departmentHead, notMember)).thenReturn(false);

        final Authentication authentication = getAuthenticationToken(usernameDepartmentHead);
        final boolean inDepartmentOfAuthenticatedHeadPersonId = sut.isInDepartmentOfDepartmentHead(authentication, 1L);
        assertThat(inDepartmentOfAuthenticatedHeadPersonId).isFalse();
    }

    @Test
    void isInDepartmentOfAuthenticatedHeadPersonIdButHasNoDepartmentHeadRole() {
        final String usernameDepartmentHead = "DepartmentHead";
        final Person departmentHead = new Person(usernameDepartmentHead, "lastname", "firstName", "email");
        departmentHead.setPermissions(List.of(USER));
        when(personService.getPersonByUsername(usernameDepartmentHead)).thenReturn(Optional.of(departmentHead));

        final Authentication authentication = getAuthenticationToken(usernameDepartmentHead);
        final boolean inDepartmentOfAuthenticatedHeadPersonId = sut.isInDepartmentOfDepartmentHead(authentication, 1L);
        assertThat(inDepartmentOfAuthenticatedHeadPersonId).isFalse();
    }

    @Test
    void isInDepartmentOfAuthenticatedHeadPersonIdButNoPersonFound() {
        final String usernameDepartmentHead = "DepartmentHead";
        final Person departmentHead = new Person(usernameDepartmentHead, "lastname", "firstName", "email");
        departmentHead.setPermissions(List.of(USER));
        when(personService.getPersonByUsername(usernameDepartmentHead)).thenReturn(Optional.of(departmentHead));

        final Authentication authentication = getAuthenticationToken(usernameDepartmentHead);
        final boolean inDepartmentOfAuthenticatedHeadPersonId = sut.isInDepartmentOfDepartmentHead(authentication, 1L);
        assertThat(inDepartmentOfAuthenticatedHeadPersonId).isFalse();
    }

    @Test
    void isInDepartmentOfAuthenticatedHeadPersonIdButIsNotLoggedIn() {
        when(personService.getPersonByID(1L)).thenReturn(Optional.empty());

        final String usernameDepartmentHead = "Head";
        final Person departmentHead = new Person(usernameDepartmentHead, "lastname", "firstName", "email");
        departmentHead.setPermissions(List.of(DEPARTMENT_HEAD));
        when(personService.getPersonByUsername(usernameDepartmentHead)).thenReturn(Optional.of(departmentHead));

        final Authentication authentication = getAuthenticationToken(usernameDepartmentHead);
        final boolean inDepartmentOfAuthenticatedHeadPersonId = sut.isInDepartmentOfDepartmentHead(authentication, 1L);
        assertThat(inDepartmentOfAuthenticatedHeadPersonId).isFalse();
    }

    @Test
    void isSamePersonIdNoPerson() {

        when(personService.getPersonByID(1L)).thenReturn(Optional.empty());

        final boolean isSamePerson = sut.isSamePersonId(mock(Authentication.class), 1L);
        assertThat(isSamePerson).isFalse();
    }

    @Test
    void isSamePersonIdWithOidc() {

        final String username = "Hans";
        final TestingAuthenticationToken authentication = getAuthenticationToken(username);

        when(personService.getPersonByID(1L))
            .thenReturn(Optional.of(new Person(username, "lastname", "firstName", "email")));

        final boolean isSamePerson = sut.isSamePersonId(authentication, 1L);
        assertThat(isSamePerson).isTrue();
    }

    @Test
    void isNotSamePersonIdWithOidc() {
        final String username = "Hans";
        final Authentication authentication = getAuthenticationToken(username);
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(new Person("differentUsername", "lastname", "firstName", "email")));

        final boolean isSamePerson = sut.isSamePersonId(authentication, 1L);
        assertThat(isSamePerson).isFalse();
    }

    private TestingAuthenticationToken getAuthenticationToken(final String username) {
        final Instant now = Instant.now();
        final OidcIdToken token = new OidcIdToken("token", now, now.plusSeconds(60), Map.of(IdTokenClaimNames.SUB, username));
        final DefaultOidcUser oidcUser = new DefaultOidcUser(List.of(new OidcUserAuthority(token)), token);
        return new TestingAuthenticationToken(oidcUser, List.of());
    }
}
