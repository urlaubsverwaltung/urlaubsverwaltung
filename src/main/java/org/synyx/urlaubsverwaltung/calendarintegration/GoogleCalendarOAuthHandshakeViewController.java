package org.synyx.urlaubsverwaltung.calendarintegration;

import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.Calendar;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.security.GeneralSecurityException;

import static java.lang.invoke.MethodHandles.lookup;
import static org.apache.http.HttpStatus.SC_OK;
import static org.slf4j.LoggerFactory.getLogger;
import static org.synyx.urlaubsverwaltung.security.SecurityRules.IS_OFFICE;

@Controller
@RequestMapping("/web")
public class GoogleCalendarOAuthHandshakeViewController {

    private static final Logger LOG = getLogger(lookup().lookupClass());
    private static final String APPLICATION_NAME = "Urlaubsverwaltung";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    private static HttpTransport httpTransport;

    private final CalendarSettingsService calendarSettingsService;
    private final CalendarSyncService calendarSyncService;
    private final GoogleAuthorizationCodeFlowFactory googleAuthorizationCodeFlowFactory;

    @Autowired
    GoogleCalendarOAuthHandshakeViewController(
        CalendarSettingsService calendarSettingsService,
        CalendarSyncService calendarSyncService,
        GoogleAuthorizationCodeFlowFactory googleAuthorizationCodeFlowFactory
    ) throws GeneralSecurityException, IOException {

        this.calendarSettingsService = calendarSettingsService;
        this.calendarSyncService = calendarSyncService;
        this.googleAuthorizationCodeFlowFactory = googleAuthorizationCodeFlowFactory;

        httpTransport = GoogleNetHttpTransport.newTrustedTransport();
    }

    private static HttpResponse checkGoogleCalendar(Calendar client, CalendarSettings calendarSettings) throws IOException {
        final String calendarId = calendarSettings.getGoogleCalendarSettings().getCalendarId();
        return client.calendars()
            .get(calendarId)
            .buildHttpRequestUsingHead().execute();
    }

    @PreAuthorize(IS_OFFICE)
    @GetMapping("/google-api-handshake")
    public String googleConnectionStatus(HttpServletRequest request) throws GeneralSecurityException, IOException {
        return "redirect:" + authorize(request.getRequestURL().toString());
    }

    @PreAuthorize(IS_OFFICE)
    @GetMapping(value = "/google-api-handshake", params = "code")
    public String oauth2Callback(@RequestParam(value = "code") String code, HttpServletRequest request) throws GeneralSecurityException, IOException {

        final String redirectUrl = request.getRequestURL().toString();

        final GoogleAuthorizationCodeFlow flow = createGoogleAuthorizationCodeFlow();

        try {
            final TokenResponse response = flow.newTokenRequest(code).setRedirectUri(redirectUrl).execute();
            final Credential credential = flow.createAndStoreCredential(response, "userID");
            final Calendar client = new Calendar
                .Builder(httpTransport, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();

            CalendarSettings calendarSettings = calendarSettingsService.getCalendarSettings();
            final HttpResponse httpResponse = checkGoogleCalendar(client, calendarSettings);

            if (httpResponse.getStatusCode() == SC_OK) {
                String refreshToken = credential.getRefreshToken();
                if (refreshToken == null) {
                    LOG.warn("OAuth Handshake was successful, but refresh token is null!");
                } else {
                    LOG.info("OAuth Handshake was successful!");
                }
                calendarSettings.getGoogleCalendarSettings().setRefreshToken(refreshToken);
                calendarSettingsService.save(calendarSettings);
                calendarSyncService.checkCalendarSyncSettings();
            } else {
                LOG.warn("OAuth handshake error {}", httpResponse.getStatusMessage());
            }

        } catch (IOException e) {
            LOG.error("Exception while handling OAuth2 callback ({}) Redirecting to google connection status page.", e.getMessage(), e);
        }

        return "redirect:/web/settings/calendar-sync";
    }

    private String authorize(String redirectUri) throws GeneralSecurityException, IOException {

        final AuthorizationCodeRequestUrl authorizationUrl = createGoogleAuthorizationCodeFlow()
            .newAuthorizationUrl()
            .setRedirectUri(redirectUri);

        LOG.info("using authorizationUrl {}", authorizationUrl);

        return authorizationUrl.build();
    }

    private GoogleAuthorizationCodeFlow createGoogleAuthorizationCodeFlow() throws GeneralSecurityException, IOException {
        final GoogleCalendarSettings googleCalendarSettings = calendarSettingsService.getCalendarSettings().getGoogleCalendarSettings();
        return googleAuthorizationCodeFlowFactory.create(googleCalendarSettings.getClientId(), googleCalendarSettings.getClientSecret());
    }
}
