package org.synyx.urlaubsverwaltung.security.oidc;

import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("uv.security.oidc")
public class OidcSecurityProperties {

    /**
     * OIDC post logout redirect uri.
     * <p>
     * Redirects the user to the given url after logout.
     * Default is the base url of the request.
     */
    @NotEmpty
    private String postLogoutRedirectUri = "{baseUrl}";

    /**
     * OIDC end session (RP-initiated logout) endpoint of the provider.
     * <p>
     * Only required when the provider's {@code end_session_endpoint} cannot be
     * discovered automatically, e.g. when {@code issuer-uri} is not used for the
     * client registration (manual authorization-uri/token-uri/jwk-set-uri setup).
     * When left empty, the end session endpoint is looked up from the OIDC provider
     * metadata as usual and RP-initiated logout is skipped if that metadata is absent.
     */
    private String endSessionEndpoint;

    public String getPostLogoutRedirectUri() {
        return postLogoutRedirectUri;
    }

    public void setPostLogoutRedirectUri(String postLogoutRedirectUri) {
        this.postLogoutRedirectUri = postLogoutRedirectUri;
    }

    public String getEndSessionEndpoint() {
        return endSessionEndpoint;
    }

    public void setEndSessionEndpoint(String endSessionEndpoint) {
        this.endSessionEndpoint = endSessionEndpoint;
    }
}
