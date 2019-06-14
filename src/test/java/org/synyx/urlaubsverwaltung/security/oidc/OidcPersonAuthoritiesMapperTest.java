package org.synyx.urlaubsverwaltung.security.oidc;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.oidc.IdTokenClaimNames;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.security.PersonSyncService;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

@RunWith(MockitoJUnitRunner.class)
public class OidcPersonAuthoritiesMapperTest {

    private OidcPersonAuthoritiesMapper sut;

    @Mock
    private PersonService personService;
    @Mock
    private PersonSyncService personSyncService;

    @Before
    public void setUp() {
        sut = new OidcPersonAuthoritiesMapper(personService, personSyncService);
    }

    @Test
    public void mapAuthoritiesFromIdTokenBySync() {
        final String uniqueID = "uniqueID";
        final String givenName = "test";
        final String familyName = "me";
        final String email = "test.me@example.com";

        final OidcUserAuthority oidcUserAuthority = getOidcUserAuthority(uniqueID, givenName, familyName, email);

        final Person personForLogin = new Person();
        personForLogin.setPermissions(List.of(USER));
        final Optional<Person> person = Optional.of(personForLogin);
        when(personService.getPersonByLogin(uniqueID)).thenReturn(person);

        when(personSyncService.syncPerson(personForLogin, Optional.of(givenName), Optional.of(familyName), Optional.of(email))).thenReturn(personForLogin);

        final Collection<? extends GrantedAuthority> grantedAuthorities = sut.mapAuthorities(List.of(oidcUserAuthority));
        assertThat(grantedAuthorities.stream().map(GrantedAuthority::getAuthority)).containsOnly(USER.name());
    }

    @Test
    public void mapAuthoritiesFromIdTokenByCreate() {
        final String uniqueID = "uniqueID";
        final String givenName = "test";
        final String familyName = "me";
        final String email = "test.me@example.com";

        final OidcUserAuthority oidcUserAuthority = getOidcUserAuthority(uniqueID, givenName, familyName, email);

        final Person personForLogin = new Person();
        personForLogin.setPermissions(List.of(USER));

        when(personService.getPersonByLogin(uniqueID)).thenReturn(Optional.empty());
        when(personSyncService.createPerson(uniqueID, Optional.of(givenName), Optional.of(familyName), Optional.of(email))).thenReturn(personForLogin);
        when(personSyncService.appointAsOfficeUserIfNoOfficeUserPresent(personForLogin)).thenReturn(personForLogin);

        final Collection<? extends GrantedAuthority> grantedAuthorities = sut.mapAuthorities(List.of(oidcUserAuthority));
        assertThat(grantedAuthorities.stream().map(GrantedAuthority::getAuthority)).containsOnly(USER.name());
    }

    @Test
    public void mapAuthoritiesFromUserInfoByCreate() {
        final String uniqueID = "uniqueID";
        final String givenName = "test";
        final String familyName = "me";
        final String email = "test.me@example.com";

        final OidcUserAuthority oidcUserAuthority = getOidcUserAuthorityWithUserInfo(uniqueID, givenName, familyName, email);

        final Person personForLogin = new Person();
        personForLogin.setPermissions(List.of(USER));

        when(personService.getPersonByLogin(uniqueID)).thenReturn(Optional.empty());
        when(personSyncService.createPerson(uniqueID, Optional.of(givenName), Optional.of(familyName), Optional.of(email))).thenReturn(personForLogin);
        when(personSyncService.appointAsOfficeUserIfNoOfficeUserPresent(personForLogin)).thenReturn(personForLogin);

        final Collection<? extends GrantedAuthority> grantedAuthorities = sut.mapAuthorities(List.of(oidcUserAuthority));
        assertThat(grantedAuthorities.stream().map(GrantedAuthority::getAuthority)).containsOnly(USER.name());
    }

    @Test
    public void mapAuthoritiesFromUserInfoBySync() {
        final String uniqueID = "uniqueID";
        final String givenName = "test";
        final String familyName = "me";
        final String email = "test.me@example.com";

        final OidcUserAuthority oidcUserAuthority = getOidcUserAuthorityWithUserInfo(uniqueID, givenName, familyName, email);

        final Person personForLogin = new Person();
        personForLogin.setPermissions(List.of(USER));

        when(personService.getPersonByLogin(uniqueID)).thenReturn(Optional.empty());
        when(personSyncService.createPerson(uniqueID, Optional.of(givenName), Optional.of(familyName), Optional.of(email))).thenReturn(personForLogin);
        when(personSyncService.appointAsOfficeUserIfNoOfficeUserPresent(personForLogin)).thenReturn(personForLogin);

        final Collection<? extends GrantedAuthority> grantedAuthorities = sut.mapAuthorities(List.of(oidcUserAuthority));
        assertThat(grantedAuthorities.stream().map(GrantedAuthority::getAuthority)).containsOnly(USER.name());
    }

    @Test(expected = OidcPersonMappingException.class)
    public void mapAuthoritiesWrongAuthority() {

        sut.mapAuthorities(List.of(new SimpleGrantedAuthority("NO_ROLE")));
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
