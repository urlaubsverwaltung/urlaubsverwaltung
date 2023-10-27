package org.synyx.urlaubsverwaltung.security.oidc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.oauth2.core.oidc.IdTokenClaimNames.SUB;
import static org.springframework.security.oauth2.core.oidc.StandardClaimNames.EMAIL;
import static org.springframework.security.oauth2.core.oidc.StandardClaimNames.FAMILY_NAME;
import static org.springframework.security.oauth2.core.oidc.StandardClaimNames.GIVEN_NAME;

@ExtendWith(MockitoExtension.class)
class RolesFromClaimMappersInfusedOAuth2UserServiceTest {

    private RolesFromClaimMappersInfusedOAuth2UserService sut;

    @Mock
    private OAuth2UserService<OidcUserRequest, OidcUser> oAuth2UserService;
    @Mock
    private RolesFromClaimMapper rolesFromClaimMapper;

    @BeforeEach
    void setUp() {
        sut = new RolesFromClaimMappersInfusedOAuth2UserService(oAuth2UserService, List.of(rolesFromClaimMapper));
    }

    @Test
    void ensureToCombineOidcUserAndClaimMapperAuthorities() {

        final OidcUserRequest oidcUserRequest = mock(OidcUserRequest.class);

        final GrantedAuthority user = new SimpleGrantedAuthority("USER");
        final Map<String, Object> claims = Map.of(
            SUB, "uniqueID",
            GIVEN_NAME, "givenName",
            FAMILY_NAME, "familyNam",
            EMAIL, "email"
        );
        final OidcIdToken oidcIdToken = new OidcIdToken("tokenValue", Instant.now(), Instant.MAX, claims);
        final DefaultOidcUser oidcUser = new DefaultOidcUser(List.of(user), oidcIdToken);
        when(oAuth2UserService.loadUser(oidcUserRequest)).thenReturn(oidcUser);

        final GrantedAuthority office = new SimpleGrantedAuthority("OFFICE");
        when(rolesFromClaimMapper.mapClaimToRoles(claims)).thenReturn(List.of(office));

        final OidcUser expectedOidcUser = sut.loadUser(oidcUserRequest);
        assertThat(expectedOidcUser.getAuthorities().stream().map(GrantedAuthority::getAuthority))
            .containsExactly("OFFICE", "USER");
    }
}
