package org.synyx.urlaubsverwaltung.core.sync.providers.google;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.CredentialRefreshListener;
import com.google.api.client.auth.oauth2.TokenErrorResponse;
import com.google.api.client.auth.oauth2.TokenResponse;
import org.apache.log4j.Logger;

import java.io.IOException;

public class LoggingCredentialRefreshListener implements CredentialRefreshListener {

    private static final Logger LOG = Logger.getLogger(LoggingCredentialRefreshListener.class);

    @Override
    public void onTokenResponse(Credential credential, TokenResponse tokenResponse) throws IOException {

        LOG.info("google oauth 2.0 credentials has been refreshed with new access token");
    }

    @Override
    public void onTokenErrorResponse(Credential credential, TokenErrorResponse tokenErrorResponse) throws IOException {

        LOG.warn(String.format("google oauth 2.0 credentials can not be refreshed due to %s(%s) on %s",
                tokenErrorResponse.getError(),
                tokenErrorResponse.getErrorDescription(),
                tokenErrorResponse.getErrorUri()));
    }
}
