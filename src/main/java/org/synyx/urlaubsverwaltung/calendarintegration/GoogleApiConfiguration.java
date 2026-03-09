package org.synyx.urlaubsverwaltung.calendarintegration;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GoogleApiConfiguration {

    @Bean
    GoogleAuthorizationCodeFlowFactory googleAuthorizationCodeFlowFactory() {
        return new GoogleAuthorizationCodeFlowFactory(GsonFactory.getDefaultInstance(), new NetHttpTransport());
    }

}
