package org.synyx.urlaubsverwaltung.security.oidc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static org.springframework.security.oauth2.core.oidc.IdTokenClaimNames.SUB;
import static org.springframework.security.oauth2.core.oidc.StandardClaimNames.EMAIL;
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
    void mapAuthoritiesFromIdToken() {
        final String uniqueID = "uniqueID";
        final String email = "test.me@example.com";

        final OidcUserAuthority oidcUserAuthority = getOidcUserAuthority(Map.of(
            SUB, uniqueID,
            EMAIL, email
        ));

        final Person personForLogin = new Person();
        personForLogin.setPermissions(List.of(USER));
        final Optional<Person> person = Optional.of(personForLogin);
        when(personService.getPersonByUsername(uniqueID)).thenReturn(person);

        final Collection<? extends GrantedAuthority> grantedAuthorities = sut.mapAuthorities(List.of(oidcUserAuthority));
        assertThat(grantedAuthorities.stream().map(GrantedAuthority::getAuthority)).containsOnly(USER.name());
    }

    @Test
    void mapAuthoritiesFromIdTokenByEmailFallback() {
        final String uniqueID = "uniqueID";
        final String email = "test.me@example.com";

        final OidcUserAuthority oidcUserAuthority = getOidcUserAuthority(Map.of(
            SUB, uniqueID,
            EMAIL, email
        ));

        final Person personForLogin = new Person();
        personForLogin.setUsername("idOfOtherIdentityProvider");
        personForLogin.setPermissions(List.of(USER));
        final Optional<Person> person = Optional.of(personForLogin);
        when(personService.getPersonByUsername(uniqueID)).thenReturn(Optional.empty());
        when(personService.getPersonByMailAddress(email)).thenReturn(person);

        final Collection<? extends GrantedAuthority> grantedAuthorities = sut.mapAuthorities(List.of(oidcUserAuthority));
        assertThat(grantedAuthorities.stream().map(GrantedAuthority::getAuthority)).containsOnly(USER.name());
    }

    @Test
    void ensureFallbackToUserInfoIfEmailIsMissingInIdToken() {
        final String uniqueID = "uniqueID";
        final String email = "test.me@example.com";

        final OidcUserAuthority oidcUserAuthorities = getOidcUserAuthority(
            Map.of(
                SUB, uniqueID
            ),
            Map.of(
                EMAIL, email
            )
        );

        final Person createdPerson = new Person();
        createdPerson.setPermissions(List.of(USER));

        when(personService.getPersonByUsername(uniqueID)).thenReturn(Optional.empty());

        final Collection<? extends GrantedAuthority> grantedAuthorities = sut.mapAuthorities(List.of(oidcUserAuthorities));
        assertThat(grantedAuthorities.stream().map(GrantedAuthority::getAuthority)).containsOnly(USER.name());
    }

    @Test
    void ensureThrowsExceptionIfEmailIsMissing() {
        final List<OidcUserAuthority> oidcUserAuthorities = List.of(getOidcUserAuthority(Map.of(
            SUB, "uniqueID"
        )));

        assertThatThrownBy(() -> sut.mapAuthorities(oidcUserAuthorities))
            .isInstanceOf(OidcPersonMappingException.class)
            .hasMessage("Can not retrieve the email for oidc person mapping");
    }
    @Test
    void ensureThrowsExceptionIfSubjectIsMissing() {
        final List<OidcUserAuthority> oidcUserAuthorities = List.of(getOidcUserAuthority(Map.of(
            EMAIL, "test.me@example.com"
        )));

        assertThatThrownBy(() -> sut.mapAuthorities(oidcUserAuthorities))
            .isInstanceOf(OidcPersonMappingException.class)
            .hasMessage("Can not retrieve the subject of the id token for oidc person mapping");
    }

    @Test
    void userIsDeactivated() {
        final String uniqueID = "uniqueID";
        final String email = "test.me@example.com";

        final OidcUserAuthority oidcUserAuthority = getOidcUserAuthority(Map.of(
            SUB, uniqueID,
            EMAIL, email
        ));

        final Person personForLogin = new Person();
        personForLogin.setPermissions(List.of(USER, INACTIVE));

        final Optional<Person> person = Optional.of(personForLogin);
        when(personService.getPersonByUsername(uniqueID)).thenReturn(person);

        final List<OidcUserAuthority> oidcUserAuthorities = List.of(oidcUserAuthority);
        assertThatThrownBy(() -> sut.mapAuthorities(oidcUserAuthorities))
            .isInstanceOf(DisabledException.class);
    }

    @Test
    void mapAuthoritiesWrongAuthority() {
        final List<SimpleGrantedAuthority> noRole = List.of(new SimpleGrantedAuthority("NO_ROLE"));
        assertThatThrownBy(() -> sut.mapAuthorities(noRole))
            .isInstanceOf(OidcPersonMappingException.class);
    }

    private OidcUserAuthority getOidcUserAuthority(Map<String, Object> idTokenClaims) {
        final OidcIdToken idToken = new OidcIdToken("tokenValue", Instant.now(), Instant.MAX, idTokenClaims);
        return new OidcUserAuthority(idToken);
    }

    private OidcUserAuthority getOidcUserAuthority(Map<String, Object> idTokenClaims, Map<String, Object> userInfoClaims) {
        final OidcIdToken idToken = new OidcIdToken("tokenValue", Instant.now(), Instant.MAX, idTokenClaims);
        final OidcUserInfo userInfo = new OidcUserInfo(userInfoClaims);
        return new OidcUserAuthority(idToken, userInfo);
    }
}
