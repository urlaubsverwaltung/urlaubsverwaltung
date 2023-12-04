package org.synyx.urlaubsverwaltung.security.oidc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static org.springframework.security.oauth2.core.oidc.IdTokenClaimNames.SUB;
import static org.springframework.security.oauth2.core.oidc.StandardClaimNames.EMAIL;
import static org.springframework.security.oauth2.core.oidc.StandardClaimNames.FAMILY_NAME;
import static org.springframework.security.oauth2.core.oidc.StandardClaimNames.GIVEN_NAME;
import static org.synyx.urlaubsverwaltung.person.Role.INACTIVE;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
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
    void ensureToCombineAuthoritiesFromOIDCWithDatabaseAuthorities() {
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

        final Collection<? extends GrantedAuthority> grantedAuthorities = sut.mapAuthorities(List.of(oidcUserAuthority, new SimpleGrantedAuthority("OFFICE")));
        assertThat(grantedAuthorities.stream().map(GrantedAuthority::getAuthority)).containsOnly(USER.name(), "OIDC_USER", OFFICE.name());
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
        assertThat(grantedAuthorities.stream().map(GrantedAuthority::getAuthority)).containsOnly(USER.name(), "OIDC_USER");
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
        assertThat(grantedAuthorities.stream().map(GrantedAuthority::getAuthority)).containsOnly(USER.name(), "OIDC_USER");
    }

    @Test
    void ensureFallbackToUserInfoIfGivenNameIsMissingInIdToken() {
        final String uniqueID = "uniqueID";
        final String givenName = "given name";

        final OidcUserAuthority oidcUserAuthorities = getOidcUserAuthority(
            Map.of(
                SUB, uniqueID
            ),
            Map.of(
                GIVEN_NAME, givenName
            )
        );

        when(personService.getPersonByUsername(uniqueID)).thenReturn(Optional.empty());

        final Collection<? extends GrantedAuthority> grantedAuthorities = sut.mapAuthorities(List.of(oidcUserAuthorities));
        assertThat(grantedAuthorities.stream().map(GrantedAuthority::getAuthority)).contains(USER.name(), OFFICE.name());
    }

    @Test
    void ensureFallbackToUserInfoIfLastnameIsMissingInIdToken() {
        final String uniqueID = "uniqueID";
        final String givenName = "given name";
        final String email = "test.me@example.com";

        final OidcUserAuthority oidcUserAuthorities = getOidcUserAuthority(
            Map.of(
                SUB, uniqueID,
                GIVEN_NAME, givenName,
                EMAIL, email
            )
        );

        when(personService.getPersonByUsername(uniqueID)).thenReturn(Optional.empty());

        final Collection<? extends GrantedAuthority> grantedAuthorities = sut.mapAuthorities(List.of(oidcUserAuthorities));
        assertThat(grantedAuthorities.stream().map(GrantedAuthority::getAuthority)).contains(USER.name(), OFFICE.name());
    }

    @Test
    void ensureFallbackToUserInfoIfEmailIsMissingInIdToken() {
        final String uniqueID = "uniqueID";
        final String givenName = "given name";
        final String familyName = "family name";
        final String email = "test.me@example.com";

        final OidcUserAuthority oidcUserAuthorities = getOidcUserAuthority(
            Map.of(
                SUB, uniqueID,
                GIVEN_NAME, givenName,
                FAMILY_NAME, familyName
            ),
            Map.of(
                EMAIL, email
            )
        );

        when(personService.getPersonByUsername(uniqueID)).thenReturn(Optional.empty());

        final Collection<? extends GrantedAuthority> grantedAuthorities = sut.mapAuthorities(List.of(oidcUserAuthorities));
        assertThat(grantedAuthorities.stream().map(GrantedAuthority::getAuthority)).contains(USER.name(), OFFICE.name());
    }

    @Test
    void ensureEmailWillBeSetNullIfMissing() {

        final String uniqueID = "uniqueID";
        final String givenName = "given name";
        final String familyName = "family name";

        final OidcUserAuthority oidcUserAuthorities = getOidcUserAuthority(
            Map.of(
                SUB, uniqueID,
                GIVEN_NAME, givenName,
                FAMILY_NAME, familyName
            )
        );

        when(personService.getPersonByUsername(uniqueID)).thenReturn(Optional.empty());
        when(personService.getPersonByMailAddress(null)).thenReturn(Optional.empty());

        final Collection<? extends GrantedAuthority> grantedAuthorities = sut.mapAuthorities(List.of(oidcUserAuthorities));
        assertThat(grantedAuthorities.stream().map(GrantedAuthority::getAuthority)).contains(USER.name(), OFFICE.name());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "NotAnEmail"})
    @NullSource
    void ensureEmailWillBeSetNullIfNotValid(String email) {

        final String uniqueID = "uniqueID";
        final String givenName = "givenName";
        final String familyName = "family name";

        final OidcUserAuthority oidcUserAuthorities = getOidcUserAuthority(
            Map.of(
                SUB, uniqueID,
                GIVEN_NAME, givenName,
                FAMILY_NAME, familyName
            ),
            new HashMap<>() {{
                put(EMAIL, email);
            }}
        );

        when(personService.getPersonByUsername(uniqueID)).thenReturn(Optional.empty());

        final Collection<? extends GrantedAuthority> grantedAuthorities = sut.mapAuthorities(List.of(oidcUserAuthorities));
        assertThat(grantedAuthorities.stream().map(GrantedAuthority::getAuthority)).contains(USER.name(), OFFICE.name());
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
    void mapAuthoritiesFromUserInfoByCreate() {
        final String uniqueID = "uniqueID";
        final String givenName = "test";
        final String familyName = "me";
        final String email = "test.me@example.com";

        final OidcUserAuthority oidcUserAuthority = getOidcUserAuthority(
            Map.of(
                SUB, uniqueID
            ),
            Map.of(
                GIVEN_NAME, givenName,
                FAMILY_NAME, familyName,
                EMAIL, email
            ));

        when(personService.getPersonByUsername(uniqueID)).thenReturn(Optional.empty());

        final Collection<? extends GrantedAuthority> grantedAuthorities = sut.mapAuthorities(List.of(oidcUserAuthority));
        assertThat(grantedAuthorities.stream().map(GrantedAuthority::getAuthority)).contains(USER.name(), OFFICE.name());
    }

    @Test
    void mapAuthoritiesFromUserInfoBySync() {
        final String uniqueID = "uniqueID";
        final String givenName = "test";
        final String familyName = "me";
        final String email = "test.me@example.com";

        final OidcUserAuthority oidcUserAuthority = getOidcUserAuthority(
            Map.of(
                SUB, uniqueID
            ),
            Map.of(
                GIVEN_NAME, givenName,
                FAMILY_NAME, familyName,
                EMAIL, email
            ));

        when(personService.getPersonByUsername(uniqueID)).thenReturn(Optional.empty());

        final Collection<? extends GrantedAuthority> grantedAuthorities = sut.mapAuthorities(List.of(oidcUserAuthority));
        assertThat(grantedAuthorities.stream().map(GrantedAuthority::getAuthority)).contains(USER.name(), OFFICE.name());
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
