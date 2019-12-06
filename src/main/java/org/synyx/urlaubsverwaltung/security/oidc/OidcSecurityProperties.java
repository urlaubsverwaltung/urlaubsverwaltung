package org.synyx.urlaubsverwaltung.security.oidc;

import org.hibernate.validator.constraints.URL;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;

/**
 * @author Florian Krupicka - krupicka@synyx.de
 */
@Validated
@ConfigurationProperties("uv.security.oidc")
public class OidcSecurityProperties {

    /**
     * The OpenId Connect issuer URI (e.g. your OIDC server).
     */
    @URL
    private String issuerUri;

    /**
     * OIDC client identifier to authenticate the UV application against your OIDC authentication server.
     */
    @NotEmpty
    private String clientId;

    /**
     * OIDC client secret to authenticate the UV application against your OIDC authentication server.
     */
    @NotEmpty
    private String clientSecret;

    /**
     * The path after the given {@link OidcSecurityProperties#issuerUri} where the logout is defined
     * for the used provider.
     *
     * <p>e.g. for <i>keycloak</i> this would be <pre>'/protocol/openid-connect/logout'<pre>
     * because the logout is defined at 'https://$provider/auth/realms/$realm/protocol/openid-connect/logout'
     * and the {@link OidcSecurityProperties#issuerUri} should be defined with 'https://$provider/auth/realms/$realm'
     * </p>
     */
    @NotEmpty
    private String logoutPath;

    public String getIssuerUri() {
        return issuerUri;
    }

    public void setIssuerUri(String issuerUri) {
        this.issuerUri = issuerUri;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getLogoutPath() {
        return logoutPath;
    }

    public void setLogoutPath(String logoutPath) {
        this.logoutPath = logoutPath;
    }
}
