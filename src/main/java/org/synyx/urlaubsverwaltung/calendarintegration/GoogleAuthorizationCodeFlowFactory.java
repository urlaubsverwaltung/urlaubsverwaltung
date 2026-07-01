package org.synyx.urlaubsverwaltung.calendarintegration;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;

import static com.google.api.services.calendar.CalendarScopes.CALENDAR;
import static java.util.Collections.singleton;

public class GoogleAuthorizationCodeFlowFactory {

    private final JsonFactory jsonFactory;
    private final NetHttpTransport netHttpTransport;

    GoogleAuthorizationCodeFlowFactory(JsonFactory jsonFactory, NetHttpTransport netHttpTransport) {
        this.jsonFactory = jsonFactory;
        this.netHttpTransport = netHttpTransport;
    }

    public GoogleAuthorizationCodeFlow create(String clientId, String clientSecret) {

        final GoogleClientSecrets.Details web = new GoogleClientSecrets.Details();
        web.setClientId(clientId);
        web.setClientSecret(clientSecret);

        final GoogleClientSecrets clientSecrets = new GoogleClientSecrets();
        clientSecrets.setWeb(web);

        // access type "offline" is required to receive a refresh token.
        // the consent prompt is set on the authorization url (see GoogleCalendarOAuthHandshakeViewController)
        // because google's oauth server no longer supports the deprecated approval_prompt=force parameter.
        return new GoogleAuthorizationCodeFlow.Builder(netHttpTransport, jsonFactory, clientSecrets, singleton(CALENDAR))
            .setAccessType("offline")
            .build();
    }
}
