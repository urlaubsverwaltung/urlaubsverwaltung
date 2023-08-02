package org.synyx.urlaubsverwaltung.api;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.synyx.urlaubsverwaltung.security.SecurityConfigurationProperties;

import static org.springframework.security.config.http.SessionCreationPolicy.NEVER;

@Configuration
class RestApiSecurityConfig {

    private final boolean isOauth2Enabled;

    RestApiSecurityConfig(SecurityConfigurationProperties properties) {
        isOauth2Enabled = "oidc".equalsIgnoreCase(properties.getAuth().name());
    }

    @Bean
    @Order(1)
    SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {

        http.securityMatcher("/api/**", "/api/", "/api")
            .authorizeHttpRequests(
                authorizeHttpRequests -> authorizeHttpRequests.anyRequest().authenticated()
            ).sessionManagement(
                sessionManagement -> sessionManagement.sessionCreationPolicy(NEVER)
            );

        if (isOauth2Enabled) {
            http.securityMatcher("/api/**").oauth2Login();
        } else {
            http.securityMatcher("/api/**").httpBasic();
        }

        return http.build();
    }
}
