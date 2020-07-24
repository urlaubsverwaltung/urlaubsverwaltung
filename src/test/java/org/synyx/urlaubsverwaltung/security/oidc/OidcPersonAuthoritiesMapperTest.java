package org.synyx.urlaubsverwaltung.security.oidc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.oidc.IdTokenClaimNames;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_USER;
import static org.synyx.urlaubsverwaltung.person.Role.INACTIVE;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

@ExtendWith(MockitoExtension.class)
class OidcPersonAuthoritiesMapperTest {

    private OidcPersonAuthoritiesMapper sut;

    @Mock
    private PersonService personService;

    @BeforeEach
    void setUp() {
        sut = new OidcPersonAuthoritiesMapper(personService);
    }

    @Test
    void mapAuthoritiesFromIdTokenBySync() {
        final String uniqueID = "uniqueID";
        final String givenName = "test";
        final String familyName = "me";
        final String email = "test.me@example.com";

        final OidcUserAuthority oidcUserAuthority = getOidcUserAuthority(uniqueID, givenName, familyName, email);

        final Person personForLogin = new Person();
        personForLogin.setPermissions(List.of(USER));
        final Optional<Person> person = Optional.of(personForLogin);
        when(personService.getPersonByUsername(uniqueID)).thenReturn(person);

        when(personService.save(personForLogin)).thenReturn(personForLogin);

        final Collection<? extends GrantedAuthority> grantedAuthorities = sut.mapAuthorities(List.of(oidcUserAuthority));
        assertThat(grantedAuthorities.stream().map(GrantedAuthority::getAuthority)).containsOnly(USER.name());
    }

    @Test
    void mapAuthoritiesFromIdTokenByCreate() {
        final String uniqueID = "uniqueID";
        final String givenName = "test";
        final String familyName = "me";
        final String email = "test.me@example.com";

        final OidcUserAuthority oidcUserAuthority = getOidcUserAuthority(uniqueID, givenName, familyName, email);

        final Person personForLogin = new Person();
        personForLogin.setPermissions(List.of(USER));

        when(personService.getPersonByUsername(uniqueID)).thenReturn(Optional.empty());
        when(personService.create(uniqueID, familyName, givenName, email, singletonList(NOTIFICATION_USER), singletonList(USER))).thenReturn(personForLogin);
        when(personService.appointAsOfficeUserIfNoOfficeUserPresent(personForLogin)).thenReturn(personForLogin);

        final Collection<? extends GrantedAuthority> grantedAuthorities = sut.mapAuthorities(List.of(oidcUserAuthority));
        assertThat(grantedAuthorities.stream().map(GrantedAuthority::getAuthority)).containsOnly(USER.name());
    }

    @Test
    void userIsDeactivated() {
        final String uniqueID = "uniqueID";
        final String givenName = "test";
        final String familyName = "me";
        final String email = "test.me@example.com";

        final OidcUserAuthority oidcUserAuthority = getOidcUserAuthority(uniqueID, givenName, familyName, email);

        final Person personForLogin = new Person();
        personForLogin.setPermissions(List.of(USER, INACTIVE));

        final Optional<Person> person = Optional.of(personForLogin);
        when(personService.getPersonByUsername(uniqueID)).thenReturn(person);
        when(personService.save(personForLogin)).thenReturn(personForLogin);

        assertThatThrownBy(() -> sut.mapAuthorities(List.of(oidcUserAuthority))).isInstanceOf(DisabledException.class);
    }

    @Test
    void mapAuthoritiesFromUserInfoByCreate() {
        final String uniqueID = "uniqueID";
        final String givenName = "test";
        final String familyName = "me";
        final String email = "test.me@example.com";

        final OidcUserAuthority oidcUserAuthority = getOidcUserAuthorityWithUserInfo(uniqueID, givenName, familyName, email);

        final Person personForLogin = new Person();
        personForLogin.setPermissions(List.of(USER));

        when(personService.getPersonByUsername(uniqueID)).thenReturn(Optional.empty());
        when(personService.create(uniqueID, familyName, givenName, email, singletonList(NOTIFICATION_USER), singletonList(USER))).thenReturn(personForLogin);
        when(personService.appointAsOfficeUserIfNoOfficeUserPresent(personForLogin)).thenReturn(personForLogin);

        final Collection<? extends GrantedAuthority> grantedAuthorities = sut.mapAuthorities(List.of(oidcUserAuthority));
        assertThat(grantedAuthorities.stream().map(GrantedAuthority::getAuthority)).containsOnly(USER.name());
    }

    @Test
    void mapAuthoritiesFromUserInfoBySync() {
        final String uniqueID = "uniqueID";
        final String givenName = "test";
        final String familyName = "me";
        final String email = "test.me@example.com";

        final OidcUserAuthority oidcUserAuthority = getOidcUserAuthorityWithUserInfo(uniqueID, givenName, familyName, email);

        final Person personForLogin = new Person();
        personForLogin.setPermissions(List.of(USER));

        when(personService.getPersonByUsername(uniqueID)).thenReturn(Optional.empty());
        when(personService.create(uniqueID, familyName, givenName, email, singletonList(NOTIFICATION_USER), singletonList(USER))).thenReturn(personForLogin);
        when(personService.appointAsOfficeUserIfNoOfficeUserPresent(personForLogin)).thenReturn(personForLogin);

        final Collection<? extends GrantedAuthority> grantedAuthorities = sut.mapAuthorities(List.of(oidcUserAuthority));
        assertThat(grantedAuthorities.stream().map(GrantedAuthority::getAuthority)).containsOnly(USER.name());
    }

    @Test
    void mapAuthoritiesWrongAuthority() {
        assertThatThrownBy(() -> sut.mapAuthorities(List.of(new SimpleGrantedAuthority("NO_ROLE"))))
            .isInstanceOf(OidcPersonMappingException.class);
    }

    private OidcUserAuthority getOidcUserAuthority(String uniqueID, String givenName, String familyName, String email) {
        final Map<String, Object> claims = Map.of(
            IdTokenClaimNames.SUB, uniqueID,
            StandardClaimNames.GIVEN_NAME, givenName,
            StandardClaimNames.FAMILY_NAME, familyName,
            StandardClaimNames.EMAIL, email
        );

        final Instant issuedAt = Instant.now();
        final Instant expiresAt = Instant.MAX;
        final OidcIdToken oidcIdToken = new OidcIdToken("tokenValue", issuedAt, expiresAt, claims);
        return new OidcUserAuthority(oidcIdToken);
    }

    private OidcUserAuthority getOidcUserAuthorityWithUserInfo(String uniqueID, String givenName, String familyName, String email) {
        final Map<String, Object> claims = Map.of(
            IdTokenClaimNames.SUB, uniqueID,
            StandardClaimNames.GIVEN_NAME, givenName,
            StandardClaimNames.FAMILY_NAME, familyName,
            StandardClaimNames.EMAIL, email
        );
        final OidcUserInfo userInfo = new OidcUserInfo(claims);

        final Instant issuedAt = Instant.now();
        final Instant expiresAt = Instant.MAX;
        final OidcIdToken oidcIdToken = new OidcIdToken("tokenValue", issuedAt, expiresAt, Map.of(IdTokenClaimNames.SUB, uniqueID));

        return new OidcUserAuthority(oidcIdToken, userInfo);
    }
}
