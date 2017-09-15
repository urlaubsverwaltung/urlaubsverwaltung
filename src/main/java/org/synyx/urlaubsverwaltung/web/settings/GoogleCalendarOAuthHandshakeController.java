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
import java.util.Collections;

@Controller
@RequestMapping("/web")
public class GoogleCalendarOAuthHandshakeController {

    private final static Log logger = LogFactory.getLog(GoogleCalendarOAuthHandshakeController.class);
    private static final String APPLICATION_NAME = "Urlaubsverwaltung";
    private static final String REDIRECT_REL = "/google-api-handshake";
    private static final String REDIRECT_URL = "http://localhost:8080/web" + REDIRECT_REL;
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    private static HttpTransport httpTransport;

    private final SettingsService settingsService;

    private GoogleAuthorizationCodeFlow flow;

    @Autowired
    public GoogleCalendarOAuthHandshakeController(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    @PreAuthorize(SecurityRules.IS_OFFICE)
    @GetMapping(REDIRECT_REL)
    public String googleConnectionStatus(HttpServletRequest request) throws Exception {
        return "redirect:" + authorize();
    }

    @PreAuthorize(SecurityRules.IS_OFFICE)
    @GetMapping(value = REDIRECT_REL, params = "code")
    public String oauth2Callback(@RequestParam(value = "code") String code) {
        String error = null;

        try {
            TokenResponse response = flow.newTokenRequest(code).setRedirectUri(REDIRECT_URL).execute();
            Credential credential = flow.createAndStoreCredential(response, "userID");
            com.google.api.services.calendar.Calendar client =
                    new com.google.api.services.calendar.Calendar.Builder(httpTransport, JSON_FACTORY, credential)
                            .setApplicationName(APPLICATION_NAME).build();

            Settings settings = settingsService.getSettings();
            HttpResponse httpResponse = checkGoogleCalendar(client, settings);

            if (httpResponse.getStatusCode() == 200) {
                logger.info("OAuth Handshake was successful!");
                settings.getCalendarSettings().getGoogleCalendarSettings()
                        .setRefreshToken(credential.getRefreshToken());
                settingsService.save(settings);
            } else {
                error = "OAuth handshake error " + httpResponse.getStatusMessage();
                logger.warn(error);
            }

        } catch (Exception e) {
            error = "Exception while handling OAuth2 callback (" + e.getMessage() + ")."
                    + " Redirecting to google connection status page.";
            logger.error(error);
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

    private HttpResponse checkGoogleCalendar(Calendar client, Settings settings) throws IOException {
        String calendarId = settings.getCalendarSettings().getGoogleCalendarSettings().getCalendarId();
        Calendar.Calendars.Get metadata = client.calendars().get(calendarId);
        return metadata.buildHttpRequestUsingHead().execute();
    }

    private String authorize() throws Exception {
        AuthorizationCodeRequestUrl authorizationUrl;

        Details web = new Details();
        GoogleCalendarSettings googleCalendarSettings =
                settingsService.getSettings().getCalendarSettings().getGoogleCalendarSettings();

        web.setClientId(googleCalendarSettings.getClientId());
        web.setClientSecret(googleCalendarSettings.getClientSecret());

        GoogleClientSecrets clientSecrets = new GoogleClientSecrets();

        clientSecrets.setWeb(web);
        httpTransport = GoogleNetHttpTransport.newTrustedTransport();

        flow = new GoogleAuthorizationCodeFlow.Builder(httpTransport, JSON_FACTORY, clientSecrets,
                Collections.singleton(CalendarScopes.CALENDAR)).build();

        authorizationUrl = flow.newAuthorizationUrl().setRedirectUri(REDIRECT_URL);

        logger.info("using authorizationUrl " + authorizationUrl);
        return authorizationUrl.build();
    }
}