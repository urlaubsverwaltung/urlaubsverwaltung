package org.synyx.urlaubsverwaltung.security.oidc;

import org.slf4j.Logger;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

public class OidcLogoutHandler extends SecurityContextLogoutHandler {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final OidcSecurityProperties properties;

    OidcLogoutHandler(OidcSecurityProperties properties) {
        this.properties = properties;
    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        super.logout(request, response, authentication);

        final String returnTo = generateReturnToLink(request);
        final OidcUser user = (OidcUser) authentication.getPrincipal();

        final String logoutUrl = generateLogoutUri(returnTo, user);
        redirectTo(logoutUrl, response);
    }

    private String generateLogoutUri(String returnTo, OidcUser user) {
        final UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(properties.getLogoutUri())
            .queryParam("id_token_hint", user.getIdToken().getTokenValue())
            .queryParam("redirect_uri", returnTo)
            .queryParam("client_id", properties.getClientId())
            .queryParam("returnTo", returnTo);
        return builder.toUriString();
    }

    private void redirectTo(String goTo, HttpServletResponse response) {
        try {
            response.sendRedirect(goTo);
        } catch (IOException e) {
            LOG.debug("Failure on redirecting the logged out user to {}", goTo, e);
        }
    }

    private String generateReturnToLink(HttpServletRequest request) {
        return request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
    }
}
