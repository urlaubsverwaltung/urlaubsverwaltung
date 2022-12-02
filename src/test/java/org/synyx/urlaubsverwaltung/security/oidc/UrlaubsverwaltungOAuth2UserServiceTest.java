package org.synyx.urlaubsverwaltung.security.oidc;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UrlaubsverwaltungOAuth2UserServiceTest {

    private UrlaubsverwaltungOAuth2UserService sut;

    @Mock
    private OidcUserService oidcUserService;

    @BeforeEach
    void setUp() {
        sut = new UrlaubsverwaltungOAuth2UserService(oidcUserService, "groups", "urlaubsverwaltung_user");
    }

    @Test
    void ensuresCorrectClaimAndOidcUserWasGeneratedCorrectly() {

        final OidcIdToken idToken = new OidcIdToken("fake-token-value", null, null, Map.of("groups", List.of("urlaubsverwaltung_user", "offline_access"), "sub", "9f074702-aebc-4072-94cb-df22dfb28368"));
        final OidcUserRequest userRequest = mock(OidcUserRequest.class);
        final OidcUser currentUser = new DefaultOidcUser(List.of(new SimpleGrantedAuthority("USER")), idToken);
        when(oidcUserService.loadUser(userRequest)).thenReturn(currentUser);

        final OidcUser oidcUser = sut.loadUser(userRequest);
        assertThat(oidcUser).isNotNull();
        assertThat(oidcUser.getAuthorities().toArray()).contains(new SimpleGrantedAuthority("urlaubsverwaltung_user"));
        assertThat(oidcUser.getIdToken()).isEqualTo(currentUser.getIdToken());
        assertThat(oidcUser.getUserInfo()).isEqualTo(currentUser.getUserInfo());
    }

    @Test
    void exceptionIsThrownForOidcUserWithoutPermission() {

        final OidcIdToken idToken = new OidcIdToken("fake-token-value", null, null, Map.of("groups", List.of("offline_access"), "sub", "9f074702-aebc-4072-94cb-df22dfb28368", "name", "Fabienne Fee"));
        final OidcUserRequest userRequest = mock(OidcUserRequest.class);
        final OidcUser currentUser = new DefaultOidcUser(List.of(new SimpleGrantedAuthority("USER")), idToken);
        when(oidcUserService.loadUser(userRequest)).thenReturn(currentUser);

        assertThatThrownBy(() -> sut.loadUser(userRequest))
            .isInstanceOf(MissingGroupsClaimAuthorityException.class)
            .hasMessage("User 'Fabienne Fee' has not required permission to access urlaubsverwaltung!");
    }

    @Test
    void exceptionIsThrownMissingGroupsClaim() {

        final OidcIdToken idToken = new OidcIdToken("fake-token-value", null, null, Map.of("sub", "9f074702-aebc-4072-94cb-df22dfb28368", "name", "Fabienne Fee"));
        final OidcUserRequest userRequest = mock(OidcUserRequest.class);
        final OidcUser currentUser = new DefaultOidcUser(List.of(new SimpleGrantedAuthority("USER")), idToken);
        when(oidcUserService.loadUser(userRequest)).thenReturn(currentUser);

        assertThatThrownBy(() -> sut.loadUser(userRequest))
            .isInstanceOf(MissingGroupsClaimAuthorityException.class)
            .hasMessage("User 'Fabienne Fee' has not required permission to access urlaubsverwaltung!");
    }
}
