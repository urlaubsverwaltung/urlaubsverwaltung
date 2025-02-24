package org.synyx.urlaubsverwaltung.calendarintegration;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.services.calendar.CalendarScopes;

import java.util.Collections;

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

        return new GoogleAuthorizationCodeFlow.Builder(netHttpTransport, jsonFactory, clientSecrets,
            Collections.singleton(CalendarScopes.CALENDAR))
            .setApprovalPrompt("force")
            .setAccessType("offline")
            .build();


    }
}
