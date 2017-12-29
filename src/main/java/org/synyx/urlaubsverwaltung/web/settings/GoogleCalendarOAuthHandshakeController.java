package org.synyx.urlaubsverwaltung.web.settings;

import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets.Details;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.synyx.urlaubsverwaltung.core.settings.GoogleCalendarSettings;
import org.synyx.urlaubsverwaltung.core.settings.Settings;
import org.synyx.urlaubsverwaltung.core.settings.SettingsService;
import org.synyx.urlaubsverwaltung.security.SecurityRules;
import org.synyx.urlaubsverwaltung.web.ControllerConstants;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

import static org.synyx.urlaubsverwaltung.core.sync.providers.google.GoogleCalendarSyncProvider.APPLICATION_NAME;

@Controller
@RequestMapping("/web")
public class GoogleCalendarOAuthHandshakeController {

    private static final Log LOG = LogFactory.getLog(GoogleCalendarOAuthHandshakeController.class);

    private static final String REDIRECT_REL = "/google-api-handshake";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    private final HttpTransport httpTransport;

    private final SettingsService settingsService;

    private GoogleAuthorizationCodeFlow flow;

    @Autowired
    public GoogleCalendarOAuthHandshakeController(SettingsService settingsService)
            throws GeneralSecurityException, IOException {

        this.settingsService = settingsService;
        this.httpTransport = GoogleNetHttpTransport.newTrustedTransport();
    }

    @PreAuthorize(SecurityRules.IS_OFFICE)
    @GetMapping(REDIRECT_REL)
    public String googleConnectionStatus(HttpServletRequest request) {
        return "redirect:" + authorize();
    }

    @PreAuthorize(SecurityRules.IS_OFFICE)
    @GetMapping(value = REDIRECT_REL, params = "code")
    public String oauth2Callback(@RequestParam(value = "code") String code) {
        LOG.debug("Authorization code: " + code);
        String error = null;

        try {
            LOG.info("Starting OAuth 2.0 handshake...");
            String redirectUrl = getRedirectUrl();
            TokenResponse response = flow.newTokenRequest(code).setRedirectUri(redirectUrl).execute();
            String refreshToken = response.getRefreshToken();
            LOG.debug(String.format("Got token response with AccessToken=%s valid for %s seconds, RefreshToken=%s and Scope=%s and Type=%s",
                    response.getAccessToken(), response.getExpiresInSeconds().toString(), refreshToken, response.getScope(), response.getTokenType()));
            Credential credential = flow.createAndStoreCredential(response, "userID");
            LOG.info("Successfully finished OAuth 2.0 Handshake");

            Settings settings = settingsService.getSettings();
            LOG.info("Save refresh token...");
            if (refreshToken != null && !refreshToken.isEmpty()) {
                settings.getCalendarSettings().getGoogleCalendarSettings()
                        .setRefreshToken(credential.getRefreshToken());
                settingsService.save(settings);
                LOG.info("Refresh token successfully saved.");
            } else {
                error = "No refresh token found in OAuth 2.0 handshake response";
                LOG.warn(error);
            }

            if (error != null) {
                String calendarId = settings.getCalendarSettings().getGoogleCalendarSettings().getCalendarId();
                LOG.info(String.format("Try to connect to calendar %s ...", calendarId));
                com.google.api.services.calendar.Calendar client =
                        new com.google.api.services.calendar.Calendar.Builder(httpTransport, JSON_FACTORY, credential)
                                .setApplicationName(APPLICATION_NAME).build();
                HttpResponse httpResponse = checkGoogleCalendar(client, calendarId);

                if (httpResponse.getStatusCode() == HttpStatus.SC_OK) {
                    LOG.info(String.format("Connection to calendar %s was successful established!", calendarId));
                } else {
                    error = String.format("Error while connecting to calendar %s: %d %s",
                            calendarId, httpResponse.getStatusCode(), httpResponse.getStatusMessage());
                    LOG.warn(error);
                }
            }

        } catch (IOException e) {
            error = String.format("Exception while handling OAuth2 callback (%s). Redirecting to calendar settings.",
                    e.getMessage());
            LOG.error(error, e);
        }

        StringBuilder buf = new StringBuilder();
        buf.append("redirect:/web/settings");
        if (error != null) {
            buf.append("?");
            buf.append(ControllerConstants.OAUTH_ERROR_ATTRIBUTE);
            buf.append("=");
            buf.append(error);
        }
        buf.append("#calendar");

        return buf.toString();
    }

    private String getRedirectUrl() {
        String redirectBaseUrl = settingsService.getSettings().getCalendarSettings().getGoogleCalendarSettings().getRedirectBaseUrl();
        return redirectBaseUrl + "/web" + REDIRECT_REL;
    }

    private static HttpResponse checkGoogleCalendar(Calendar client, String calendarId) throws IOException {
        Calendar.Calendars.Get metadata = client.calendars().get(calendarId);

        return metadata.buildHttpRequestUsingHead().execute();
    }

    private String authorize() {
        AuthorizationCodeRequestUrl authorizationUrl;

        Details web = new Details();
        GoogleCalendarSettings googleCalendarSettings =
                settingsService.getSettings().getCalendarSettings().getGoogleCalendarSettings();

        web.setClientId(googleCalendarSettings.getClientId());
        web.setClientSecret(googleCalendarSettings.getClientSecret());

        GoogleClientSecrets clientSecrets = new GoogleClientSecrets();

        clientSecrets.setWeb(web);

        flow = new GoogleAuthorizationCodeFlow.Builder(httpTransport, JSON_FACTORY, clientSecrets,
                Collections.singleton(CalendarScopes.CALENDAR)).build();

        authorizationUrl = flow.newAuthorizationUrl().setRedirectUri(getRedirectUrl());

        LOG.info("using authorizationUrl " + authorizationUrl);
        return authorizationUrl.build();
    }
}
