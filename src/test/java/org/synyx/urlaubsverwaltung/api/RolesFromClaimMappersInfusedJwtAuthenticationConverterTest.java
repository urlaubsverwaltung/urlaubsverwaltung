package org.synyx.urlaubsverwaltung.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.security.oidc.RolesFromClaimMapper;

import java.time.Instant;
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
import static org.synyx.urlaubsverwaltung.person.Role.USER;

@ExtendWith(MockitoExtension.class)
class RolesFromClaimMappersInfusedJwtAuthenticationConverterTest {

    private RolesFromClaimMappersInfusedJwtAuthenticationConverter sut;

    @Mock
    private PersonService personService;
    @Mock
    private RolesFromClaimMapper rolesFromClaimMapper;

    @BeforeEach
    void setUp() {
        sut = new RolesFromClaimMappersInfusedJwtAuthenticationConverter(personService, List.of(rolesFromClaimMapper));
    }

    @Test
    void ensuresToCombineApplicationAndClaimMappersAuthorities() {

        final Person person = new Person("seppl", "Hans", "Seppl", "seppl@example.org");
        person.setPermissions(List.of(USER));
        when(personService.getPersonByUsername("uniqueID")).thenReturn(Optional.of(person));

        final Map<String, Object> claims = Map.of(
            SUB, "uniqueID",
            GIVEN_NAME, "givenName",
            FAMILY_NAME, "familyNam",
            EMAIL, "email",
            "groups", List.of("urlaubsverwaltung_user")
        );

        when(rolesFromClaimMapper.mapClaimToRoles(claims)).thenReturn(List.of(new SimpleGrantedAuthority("OFFICE")));
        final Jwt jwt = new Jwt("tokenValue", Instant.now(), Instant.MAX, Map.of("header", "some"), claims);

        final AbstractAuthenticationToken authenticationToken = sut.convert(jwt);
        assertThat(authenticationToken.getAuthorities().stream().map(GrantedAuthority::getAuthority))
            .containsExactly("USER", "OFFICE");
    }

    @Test
    void ensuresToNotCombineAuthoritiesIfPersonWasNotFound() {

        when(personService.getPersonByUsername("uniqueID")).thenReturn(Optional.empty());

        final Map<String, Object> claims = Map.of(
            SUB, "uniqueID",
            GIVEN_NAME, "givenName",
            FAMILY_NAME, "familyNam",
            EMAIL, "email",
            "groups", List.of("urlaubsverwaltung_user")
        );

        when(rolesFromClaimMapper.mapClaimToRoles(claims)).thenReturn(List.of(new SimpleGrantedAuthority("OFFICE")));
        final Jwt jwt = new Jwt("tokenValue", Instant.now(), Instant.MAX, Map.of("header", "some"), claims);

        final AbstractAuthenticationToken authenticationToken = sut.convert(jwt);
        assertThat(authenticationToken.getAuthorities().stream().map(GrantedAuthority::getAuthority))
            .containsExactly("OFFICE");
    }

    @Test
    void ensuresToThrowDisabledExceptionIfUserIsInactive() {

        final Person person = new Person("seppl", "Hans", "Seppl", "seppl@example.org");
        person.setPermissions(List.of(INACTIVE));
        when(personService.getPersonByUsername("uniqueID")).thenReturn(Optional.of(person));

        final Map<String, Object> claims = Map.of(
            SUB, "uniqueID",
            GIVEN_NAME, "givenName",
            FAMILY_NAME, "familyNam",
            EMAIL, "email",
            "groups", List.of("urlaubsverwaltung_user")
        );

        final Jwt jwt = new Jwt("tokenValue", Instant.now(), Instant.MAX, Map.of("header", "some"), claims);

        assertThatThrownBy(() -> sut.convert(jwt))
            .isInstanceOf(DisabledException.class)
            .hasMessageContaining("");
    }
}
