package org.synyx.urlaubsverwaltung.api;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.security.oidc.RolesFromClaimMapper;

import java.util.List;

import static org.springframework.security.config.http.SessionCreationPolicy.NEVER;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

@Configuration
class RestApiSecurityConfig {

    @Bean
    @Order(1)
    SecurityFilterChain apiSecurityFilterChain(final HttpSecurity http,
                                               final PersonService personService,
                                               final List<RolesFromClaimMapper> claimMappers) throws Exception {
        return http
            .securityMatcher("/api/**")
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(requests ->
                requests
                    .requestMatchers("/api/**").hasAuthority(USER.name())
                    .anyRequest().authenticated()
            ).sessionManagement(
                sessionManagement -> sessionManagement.sessionCreationPolicy(NEVER)
            ).oauth2ResourceServer(
                oauth2ResourceServer -> oauth2ResourceServer.jwt(
                    jwtConfigurer -> jwtConfigurer.jwtAuthenticationConverter(new RolesFromClaimMappersInfusedJwtAuthenticationConverter(personService, claimMappers)))
            ).build();
    }
}
