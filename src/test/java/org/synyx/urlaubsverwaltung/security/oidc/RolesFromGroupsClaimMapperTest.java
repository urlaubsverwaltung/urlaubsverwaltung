package org.synyx.urlaubsverwaltung.security.oidc;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.security.oauth2.core.oidc.IdTokenClaimNames.SUB;
import static org.springframework.security.oauth2.core.oidc.StandardClaimNames.EMAIL;
import static org.springframework.security.oauth2.core.oidc.StandardClaimNames.FAMILY_NAME;
import static org.springframework.security.oauth2.core.oidc.StandardClaimNames.GIVEN_NAME;

@ExtendWith(MockitoExtension.class)
class RolesFromGroupsClaimMapperTest {

    @Test
    void ensureToNotCheckClaimAndGroupIfDisabled() {

        final RolesFromClaimMappersProperties properties = new RolesFromClaimMappersProperties();
        properties.setAuthorityCheckEnabled(false);
        final RolesFromClaimMapperConverter converter = new RolesFromClaimMapperConverter(properties);
        final RolesFromGroupsClaimMapper sut = new RolesFromGroupsClaimMapper(converter, properties);

        final Map<String, Object> claims = Map.of(
            SUB, "uniqueID",
            GIVEN_NAME, "givenName",
            FAMILY_NAME, "familyNam",
            EMAIL, "email"
        );

        final List<GrantedAuthority> authorities = sut.mapClaimToRoles(claims);
        assertThat(authorities.stream().map(GrantedAuthority::getAuthority))
            .isEmpty();
    }

    @Test
    void ensureToMapClaimsFromGroupsClaim() {

        final RolesFromClaimMappersProperties properties = new RolesFromClaimMappersProperties();
        final RolesFromClaimMapperConverter converter = new RolesFromClaimMapperConverter(properties);
        final RolesFromGroupsClaimMapper sut = new RolesFromGroupsClaimMapper(converter, properties);

        final Map<String, Object> claims = Map.of(
            SUB, "uniqueID",
            GIVEN_NAME, "givenName",
            FAMILY_NAME, "familyNam",
            EMAIL, "email",
            "groups", List.of("urlaubsverwaltung_user", "urlaubsverwaltung_office")
        );

        final List<GrantedAuthority> authorities = sut.mapClaimToRoles(claims);
        assertThat(authorities.stream().map(GrantedAuthority::getAuthority))
            .containsExactly("USER", "OFFICE");
    }

    @Test
    void ensureToMapClaimsFromGroupsClaimWithDifferentRolePrefix() {

        final RolesFromClaimMappersProperties properties = new RolesFromClaimMappersProperties();
        properties.setRolePrefix("otherprefix_");
        final RolesFromClaimMapperConverter converter = new RolesFromClaimMapperConverter(properties);
        final RolesFromGroupsClaimMapper sut = new RolesFromGroupsClaimMapper(converter, properties);

        final Map<String, Object> claims = Map.of(
            SUB, "uniqueID",
            GIVEN_NAME, "givenName",
            FAMILY_NAME, "familyNam",
            EMAIL, "email",
            "groups", List.of("otherprefix_user", "otherprefix_office")
        );

        final List<GrantedAuthority> authorities = sut.mapClaimToRoles(claims);
        assertThat(authorities.stream().map(GrantedAuthority::getAuthority))
            .containsExactly("USER", "OFFICE");
    }

    @Test
    void ensureToThrowExceptionIfNeededGroupIsNotGiven() {

        final RolesFromClaimMappersProperties properties = new RolesFromClaimMappersProperties();
        final RolesFromClaimMapperConverter converter = new RolesFromClaimMapperConverter(properties);
        final RolesFromGroupsClaimMapper sut = new RolesFromGroupsClaimMapper(converter, properties);

        final Map<String, Object> claims = Map.of(
            SUB, "uniqueID",
            GIVEN_NAME, "givenName",
            FAMILY_NAME, "familyNam",
            EMAIL, "email",
            "groups", List.of("urlaubsverwaltung_office")
        );

        assertThatThrownBy(() -> sut.mapClaimToRoles(claims))
            .isInstanceOf(MissingClaimAuthorityException.class)
            .hasMessageContaining("User has not required permission 'urlaubsverwaltung_user' to access urlaubsverwaltung!");
    }

    @Test
    void ensureToThrowExceptionIfNeededRoleIsNotGiven() {

        final RolesFromClaimMappersProperties properties = new RolesFromClaimMappersProperties();
        final RolesFromClaimMapperConverter converter = new RolesFromClaimMapperConverter(properties);
        final RolesFromGroupsClaimMapper sut = new RolesFromGroupsClaimMapper(converter, properties);

        final Map<String, Object> claims = Map.of(
            SUB, "uniqueID",
            GIVEN_NAME, "givenName",
            FAMILY_NAME, "familyNam",
            EMAIL, "email"
        );

        assertThatThrownBy(() -> sut.mapClaimToRoles(claims))
            .isInstanceOf(MissingClaimAuthorityException.class)
            .hasMessageContaining("User has not required permission 'urlaubsverwaltung_user' to access urlaubsverwaltung! The claim 'groups' is missing!");
    }
}
