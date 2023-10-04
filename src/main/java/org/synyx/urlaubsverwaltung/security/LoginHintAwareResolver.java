package org.synyx.urlaubsverwaltung.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter.DEFAULT_AUTHORIZATION_REQUEST_BASE_URI;

class LoginHintAwareResolver implements OAuth2AuthorizationRequestResolver {

    private static final String OIDC_PARAMETER_LOGIN_HINT = "login_hint";

    private final DefaultOAuth2AuthorizationRequestResolver defaultResolver;

    LoginHintAwareResolver(ClientRegistrationRepository clientRegistrationRepository) {
        this.defaultResolver = new DefaultOAuth2AuthorizationRequestResolver(clientRegistrationRepository,
            DEFAULT_AUTHORIZATION_REQUEST_BASE_URI);
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
        return addLoginHintIfPresent(request, defaultResolver.resolve(request));
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
        return addLoginHintIfPresent(request, defaultResolver.resolve(request, clientRegistrationId));
    }

    private OAuth2AuthorizationRequest addLoginHintIfPresent(HttpServletRequest request, OAuth2AuthorizationRequest defaultAuthorizationRequest) {
        final String loginHint = request.getParameter(OIDC_PARAMETER_LOGIN_HINT);

        if (loginHint == null || loginHint.isEmpty()) {
            return defaultAuthorizationRequest;
        } else {
            final Map<String, Object> additionalParameters = new HashMap<>(defaultAuthorizationRequest.getAdditionalParameters());
            additionalParameters.put(OIDC_PARAMETER_LOGIN_HINT, loginHint);

            return OAuth2AuthorizationRequest.from(defaultAuthorizationRequest)
                .additionalParameters(additionalParameters)
                .build();
        }
    }
}
