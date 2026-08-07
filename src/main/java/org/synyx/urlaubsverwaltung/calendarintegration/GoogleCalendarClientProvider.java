package org.synyx.urlaubsverwaltung.calendarintegration;

import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.CredentialRefreshListener;
import com.google.api.client.auth.oauth2.TokenErrorResponse;
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
    private static final String GOOGLEAPIS_TOKEN_SERVER_URL = "https://oauth2.googleapis.com/token";

    private final CalendarSettingsService calendarSettingsService;

    GoogleCalendarClientProvider(CalendarSettingsService calendarSettingsService) {
        this.calendarSettingsService = calendarSettingsService;
    }

    /**
     * Build and return an authorized google calendar client.
     * <p>
     * The client uses the persisted access token as long as it is valid and refreshes it automatically
     * (with the persisted refresh token) as soon as it is about to expire. Refreshed tokens are persisted.
     *
     * @return an authorized calendar client service, or {@link Optional#empty()} if no refresh token is available
     */
    Optional<Calendar> getCalendarClient(GoogleCalendarSettings googleCalendarSettings) {

        final String refreshToken = googleCalendarSettings.getRefreshToken();
        if (refreshToken == null || refreshToken.isBlank()) {
            LOG.warn("No google calendar refresh token available - please connect the application " +
                "with the google calendar again (settings > calendar sync).");
            return Optional.empty();
        }

        final JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
        final NetHttpTransport httpTransport = new NetHttpTransport();

        final Credential credential = createCredential(httpTransport, jsonFactory, googleCalendarSettings);

        final Calendar calendar = new Calendar.Builder(httpTransport, jsonFactory, credential)
            .setApplicationName(APPLICATION_NAME)
            .build();

        LOG.debug("Created new google calendar client");

        return Optional.of(calendar);
    }

    private Credential createCredential(HttpTransport transport, JsonFactory jsonFactory, GoogleCalendarSettings googleCalendarSettings) {

        final String clientId = googleCalendarSettings.getClientId();
        final String clientSecret = googleCalendarSettings.getClientSecret();

        final Credential credential = new Credential.Builder(BearerToken.authorizationHeaderAccessMethod())
            .setTransport(transport)
            .setJsonFactory(jsonFactory)
            .setTokenServerUrl(new GenericUrl(GOOGLEAPIS_TOKEN_SERVER_URL))
            .setClientAuthentication(new BasicAuthentication(clientId, clientSecret))
            .addRefreshListener(new PersistTokensRefreshListener())
            .build();

        // the credential refreshes the access token automatically with the refresh token
        // as soon as it is missing or about to expire (see Credential#intercept)
        credential.setRefreshToken(googleCalendarSettings.getRefreshToken());
        credential.setAccessToken(googleCalendarSettings.getAccessToken());
        credential.setExpirationTimeMilliseconds(googleCalendarSettings.getAccessTokenExpirationMillis());

        return credential;
    }

    private class PersistTokensRefreshListener implements CredentialRefreshListener {

        @Override
        public void onTokenResponse(Credential credential, TokenResponse tokenResponse) {

            LOG.info("Google calendar access token has been refreshed - persisting new access token.");

            final CalendarSettings calendarSettings = calendarSettingsService.getCalendarSettings();
            final GoogleCalendarSettings googleCalendarSettings = calendarSettings.getGoogleCalendarSettings();

            googleCalendarSettings.setAccessToken(credential.getAccessToken());
            googleCalendarSettings.setAccessTokenExpirationMillis(credential.getExpirationTimeMilliseconds());

            if (tokenResponse.getRefreshToken() != null) {
                // google may rotate the refresh token
                googleCalendarSettings.setRefreshToken(tokenResponse.getRefreshToken());
            }

            calendarSettingsService.save(calendarSettings);
        }

        @Override
        public void onTokenErrorResponse(Credential credential, TokenErrorResponse tokenErrorResponse) {
            final String error = tokenErrorResponse == null ? "unknown" : tokenErrorResponse.getError();
            final String description = tokenErrorResponse == null ? "" : tokenErrorResponse.getErrorDescription();
            LOG.warn("Could not refresh google calendar access token: {} {} - the application probably has to be " +
                "connected with the google calendar again (settings > calendar sync).", error, description);
        }
    }
}
