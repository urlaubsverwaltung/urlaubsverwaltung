package org.synyx.urlaubsverwaltung.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.security.oidc.RolesFromClaimMapper;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.time.temporal.ChronoUnit.MINUTES;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static org.springframework.security.oauth2.core.oidc.StandardClaimNames.EMAIL;
import static org.springframework.security.oauth2.core.oidc.StandardClaimNames.FAMILY_NAME;
import static org.springframework.security.oauth2.core.oidc.StandardClaimNames.GIVEN_NAME;
import static org.springframework.security.oauth2.core.oidc.StandardClaimNames.SUB;
import static org.synyx.urlaubsverwaltung.person.Role.INACTIVE;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

@ExtendWith(MockitoExtension.class)
class JwtToPersonGrantedAuthoritiesConverterTest {

    private RolesFromClaimMappersInfusedJwtAuthenticationConverter sut;

    @Mock
    private PersonService personService;
    @Mock
    private RolesFromClaimMapper claimMapper;

    @BeforeEach
    void setUp() {
        sut = new RolesFromClaimMappersInfusedJwtAuthenticationConverter(personService, List.of(claimMapper));
    }

    @Test
    void ensureToRetrieveAuthoritiesFromDatabase() {

        final String uniqueID = "uniqueID";
        final String givenName = "test";
        final String familyName = "me";
        final String email = "test.me@example.com";

        final Map<String, Object> claims = Map.of(
            SUB, uniqueID,
            GIVEN_NAME, givenName,
            FAMILY_NAME, familyName,
            EMAIL, email
        );
        final Jwt jwt = new Jwt("token", Instant.now(), Instant.now().plus(5, MINUTES), Map.of("header", "value"), claims);

        final Person person = new Person("username", "lastName", "firstName", "email");
        person.setPermissions(List.of(USER));
        when(personService.getPersonByUsername(uniqueID)).thenReturn(Optional.of(person));

        final AbstractAuthenticationToken token = sut.convert(jwt);
        assertThat(token.getAuthorities().stream().map(GrantedAuthority::getAuthority)).containsExactly(USER.name());
    }

    @Test
    void ensureToThrowExceptionIfInactive() {

        final String uniqueID = "uniqueID";
        final String givenName = "test";
        final String familyName = "me";
        final String email = "test.me@example.com";

        final Map<String, Object> claims = Map.of(
            SUB, uniqueID,
            GIVEN_NAME, givenName,
            FAMILY_NAME, familyName,
            EMAIL, email
        );
        final Jwt jwt = new Jwt("token", Instant.now(), Instant.now().plus(5, MINUTES), Map.of("header", "value"), claims);

        final Person person = new Person("username", "lastName", "firstName", "email");
        person.setId(42L);
        person.setPermissions(List.of(INACTIVE));
        when(personService.getPersonByUsername(uniqueID)).thenReturn(Optional.of(person));

        assertThatThrownBy(() -> sut.convert(jwt))
            .isInstanceOf(DisabledException.class);
    }

    @Test
    void ensureToReturnEmptyListOf() {

        final String uniqueID = "uniqueID";
        final String givenName = "test";
        final String familyName = "me";
        final String email = "test.me@example.com";

        final Map<String, Object> claims = Map.of(
            SUB, uniqueID,
            GIVEN_NAME, givenName,
            FAMILY_NAME, familyName,
            EMAIL, email
        );
        final Jwt jwt = new Jwt("token", Instant.now(), Instant.now().plus(5, MINUTES), Map.of("header", "value"), claims);

        when(personService.getPersonByUsername(uniqueID)).thenReturn(Optional.empty());

        final AbstractAuthenticationToken token = sut.convert(jwt);
        assertThat(token.getAuthorities()).isEmpty();
    }
}
