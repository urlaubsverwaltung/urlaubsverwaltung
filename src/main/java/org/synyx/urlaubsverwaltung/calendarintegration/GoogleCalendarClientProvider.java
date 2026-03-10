package org.synyx.urlaubsverwaltung.calendarintegration;

import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.http.BasicAuthentication;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.Calendar;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

@Service
class GoogleCalendarClientProvider {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private static final String APPLICATION_NAME = "Urlaubsverwaltung";
    private static final String GOOGLEAPIS_OAUTH2_V4_TOKEN = "https://www.googleapis.com/oauth2/v4/token";


    /**
     * Build and return an authorized google calendar client.
     *
     * @return an authorized calendar client service
     */
    Optional<Calendar> getCalendarClient(GoogleCalendarSettings googleCalendarSettings) {
        return createCalendarClient(googleCalendarSettings);
    }

    private Optional<Calendar> createCalendarClient(GoogleCalendarSettings googleCalendarSettings) {

        final String refreshToken = googleCalendarSettings.getRefreshToken();

        final TokenResponse tokenResponse = new TokenResponse();
        tokenResponse.setRefreshToken(refreshToken);

        final JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

        final NetHttpTransport httpTransport = new NetHttpTransport();
        final Credential credential = createCredentialWithRefreshToken(httpTransport, jsonFactory, tokenResponse, googleCalendarSettings);

        final Calendar calendar = new Calendar.Builder(httpTransport, jsonFactory, credential)
            .setApplicationName(APPLICATION_NAME)
            .build();

        LOG.debug("Created new google calendar client");

        return Optional.of(calendar);
    }

    private Credential createCredentialWithRefreshToken(HttpTransport transport, JsonFactory jsonFactory, TokenResponse tokenResponse, GoogleCalendarSettings googleCalendarSettings) {

        final String clientId = googleCalendarSettings.getClientId();
        final String clientSecret = googleCalendarSettings.getClientSecret();

        return new Credential.Builder(BearerToken.authorizationHeaderAccessMethod())
            .setTransport(transport)
            .setJsonFactory(jsonFactory)
            .setTokenServerUrl(new GenericUrl(GOOGLEAPIS_OAUTH2_V4_TOKEN))
            .setClientAuthentication(new BasicAuthentication(clientId, clientSecret))
            .build()
            .setFromTokenResponse(tokenResponse);
    }
}
