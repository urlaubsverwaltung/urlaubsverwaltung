package org.synyx.urlaubsverwaltung.security;

import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.metrics.export.prometheus.PrometheusScrapeEndpoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.Role;

import static org.springframework.http.HttpMethod.GET;

@Configuration
@EnableMethodSecurity
class SecurityWebConfiguration {

    private final PersonService personService;
    private final SessionService sessionService;
    private final OidcClientInitiatedLogoutSuccessHandler oidcClientInitiatedLogoutSuccessHandler;
    private final ClientRegistrationRepository clientRegistrationRepository;

    SecurityWebConfiguration(PersonService personService, SessionService sessionService,
                             OidcClientInitiatedLogoutSuccessHandler oidcClientInitiatedLogoutSuccessHandler,
                             ClientRegistrationRepository clientRegistrationRepository) {
        this.personService = personService;
        this.sessionService = sessionService;
        this.oidcClientInitiatedLogoutSuccessHandler = oidcClientInitiatedLogoutSuccessHandler;
        this.clientRegistrationRepository = clientRegistrationRepository;
    }

    @Bean
    SecurityFilterChain webSecurityFilterChain(final HttpSecurity http) throws Exception {

        http
            .authorizeHttpRequests(authorizeHttpRequests ->
                authorizeHttpRequests
                    .requestMatchers("/favicons/**").permitAll()
                    .requestMatchers("/browserconfig.xml").permitAll()
                    .requestMatchers("/manifest.json").permitAll()
                    .requestMatchers("/css/**").permitAll()
                    .requestMatchers("/fonts/**").permitAll()
                    .requestMatchers("/images/**").permitAll()
                    .requestMatchers("/assets/**").permitAll()
                    // WEB
                    .requestMatchers("/login*").permitAll()
                    .requestMatchers(GET, "/web/company/persons/*/calendar").permitAll()
                    .requestMatchers(GET, "/web/departments/*/persons/*/calendar").permitAll()
                    .requestMatchers(GET, "/web/persons/*/calendar").permitAll()
                    .requestMatchers("/web/absences/**").hasAuthority(Role.USER.name())
                    .requestMatchers("/web/application/**").hasAuthority(Role.USER.name())
                    .requestMatchers("/web/department/**").hasAnyAuthority(Role.BOSS.name(), Role.OFFICE.name())
                    .requestMatchers("/web/google-api-handshake/**").hasAuthority(Role.OFFICE.name())
                    .requestMatchers("/web/overview").hasAuthority(Role.USER.name())
                    .requestMatchers("/web/overtime/**").hasAuthority(Role.USER.name())
                    .requestMatchers("/web/person/**").hasAuthority(Role.USER.name())
                    .requestMatchers("/web/sicknote/**").hasAuthority(Role.USER.name())
                    .requestMatchers("/web/settings/**").hasAuthority(Role.OFFICE.name())
                    .requestMatchers(EndpointRequest.to(HealthEndpoint.class)).permitAll()
                    .requestMatchers(EndpointRequest.to(PrometheusScrapeEndpoint.class)).permitAll()
                    // TODO muss konfigurierbar werden!
                    .requestMatchers(EndpointRequest.toAnyEndpoint()).hasAuthority("ADMIN")
                    .anyRequest()
                    .authenticated()
            );

        http.oauth2Login(
            loginCustomizer -> loginCustomizer.authorizationEndpoint(
                endpointCustomizer -> endpointCustomizer.authorizationRequestResolver(new LoginHintAwareResolver(clientRegistrationRepository))
            )
        );
        http.logout(
            logoutCustomizer -> logoutCustomizer.logoutSuccessHandler(oidcClientInitiatedLogoutSuccessHandler)
        );

        http.addFilterAfter(new ReloadAuthenticationAuthoritiesFilter(personService, sessionService), BasicAuthenticationFilter.class);

        return http.build();
    }
}
