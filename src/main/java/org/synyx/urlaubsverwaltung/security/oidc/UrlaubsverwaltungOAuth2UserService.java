package org.synyx.urlaubsverwaltung.security.oidc;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class UrlaubsverwaltungOAuth2UserService implements OAuth2UserService<OidcUserRequest, OidcUser> {
    private final OidcUserService delegate;
    private final String claimName;

    public UrlaubsverwaltungOAuth2UserService(OidcUserService delegate, String claimName) {
        this.delegate = delegate;
        this.claimName = claimName;
    }

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {

        final OidcUser oidcUser = delegate.loadUser(userRequest);

        final List<GrantedAuthority> fromGroupsClaim = parseAuthoritiesFromGroupsClaim(oidcUser.getClaims());

        final List<GrantedAuthority> combinedAuthorities = Stream.concat(oidcUser.getAuthorities().stream(), fromGroupsClaim.stream()).collect(toList());

        return new DefaultOidcUser(combinedAuthorities, oidcUser.getIdToken(), oidcUser.getUserInfo());
    }

    private List<GrantedAuthority> parseAuthoritiesFromGroupsClaim(Map<String, Object> claims) {
        return extractFromList(claims, claimName)
            .stream()
            .map(role -> new SimpleGrantedAuthority(String.valueOf(role)))
            .collect(toList());
    }

    private List<String> extractFromList(Map<String, Object> myMap, String key) {
        Object roles = myMap.get(key);
        if (roles instanceof List) {
            return (List<String>) roles;
        }
        return Collections.emptyList();
    }
}
