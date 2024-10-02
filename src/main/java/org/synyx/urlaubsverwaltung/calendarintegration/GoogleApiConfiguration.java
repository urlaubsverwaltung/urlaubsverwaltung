package org.synyx.urlaubsverwaltung.calendarintegration;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.security.GeneralSecurityException;

@Configuration
public class GoogleApiConfiguration {

    @Bean
    GoogleAuthorizationCodeFlowFactory googleAuthorizationCodeFlowFactory() throws GeneralSecurityException, IOException {
        return new GoogleAuthorizationCodeFlowFactory(GsonFactory.getDefaultInstance(), GoogleNetHttpTransport.newTrustedTransport());
    }

}
