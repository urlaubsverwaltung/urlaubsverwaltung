package org.synyx.urlaubsverwaltung.security.simple;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.synyx.urlaubsverwaltung.person.PersonService;

@Configuration
@ConditionalOnProperty(name = "uv.security.auth", havingValue = "default")
public class DefaultAuthConfiguration {

    @Bean
    public AuthenticationProvider defaultAuthenticationProvider(PersonService personService, PasswordEncoder passwordEncoder) {
        return new SimpleAuthenticationProvider(personService, passwordEncoder);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new CustomPasswordEncoder();
    }
}
