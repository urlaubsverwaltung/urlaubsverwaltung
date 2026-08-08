package org.synyx.urlaubsverwaltung.security.oidc;

import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

import java.util.HashMap;
import java.util.Map;

/**
 * Decorates a {@link ClientRegistrationRepository} by injecting a manually configured
 * {@code end_session_endpoint} into a {@link ClientRegistration}'s provider configuration metadata.
 * <p>
 * This is needed when the client registration is not built from an OIDC issuer (no discovery
 * via {@code issuer-uri}, e.g. because individual endpoints are configured manually for a
 * reverse-proxy setup with different public/internal URLs). In that case the provider configuration
 * metadata is empty and {@code OidcClientInitiatedLogoutSuccessHandler} silently falls back to
 * default logout behaviour instead of redirecting to the provider's end session endpoint.
 */
class EndSessionEndpointClientRegistrationRepository implements ClientRegistrationRepository {

    private final ClientRegistrationRepository delegate;
    private final String endSessionEndpoint;

    EndSessionEndpointClientRegistrationRepository(ClientRegistrationRepository delegate, String endSessionEndpoint) {
        this.delegate = delegate;
        this.endSessionEndpoint = endSessionEndpoint;
    }

    @Override
    public ClientRegistration findByRegistrationId(String registrationId) {
        final ClientRegistration original = delegate.findByRegistrationId(registrationId);
        if (original == null) {
            return null;
        }

        final Map<String, Object> configurationMetadata = new HashMap<>(original.getProviderDetails().getConfigurationMetadata());
        configurationMetadata.put("end_session_endpoint", endSessionEndpoint);

        return ClientRegistration.withClientRegistration(original)
            .providerConfigurationMetadata(configurationMetadata)
            .build();
    }
}
