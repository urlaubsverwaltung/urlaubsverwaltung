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
import org.springframework.security.web.context.DelegatingSecurityContextRepository;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;
import org.synyx.urlaubsverwaltung.person.PersonService;

import static org.springframework.http.HttpMethod.GET;
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

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
    SecurityFilterChain webSecurityFilterChain(final HttpSecurity http, DelegatingSecurityContextRepository securityContextRepository) throws Exception {

        http
            .authorizeHttpRequests(authorizeHttpRequests ->
                authorizeHttpRequests
                    // Swagger API
                    .requestMatchers("/api", "/api/", "/swagger-ui/index.html").hasAuthority(USER.name())
                    // Assets
                    .requestMatchers("/favicons/**").permitAll()
                    .requestMatchers("/browserconfig.xml").permitAll()
                    .requestMatchers("/site.webmanifest").permitAll()
                    .requestMatchers("/css/**").permitAll()
                    .requestMatchers("/fonts/**").permitAll()
                    .requestMatchers("/images/**").permitAll()
                    .requestMatchers("/assets/**").permitAll()
                    // Web
                    .requestMatchers("/login*").permitAll()
                    .requestMatchers(GET, "/web/company/persons/*/calendar").permitAll()
                    .requestMatchers(GET, "/web/departments/*/persons/*/calendar").permitAll()
                    .requestMatchers(GET, "/web/persons/*/calendar").permitAll()
                    .requestMatchers("/web/absences/**").hasAuthority(USER.name())
                    .requestMatchers("/web/application/**").hasAuthority(USER.name())
                    .requestMatchers("/web/department/**").hasAnyAuthority(BOSS.name(), OFFICE.name())
                    .requestMatchers("/web/google-api-handshake/**").hasAuthority(OFFICE.name())
                    .requestMatchers("/web/overview").hasAuthority(USER.name())
                    .requestMatchers("/web/overtime/**").hasAuthority(USER.name())
                    .requestMatchers("/web/person/**").hasAuthority(USER.name())
                    .requestMatchers("/web/sicknote/**").hasAuthority(USER.name())
                    .requestMatchers("/web/settings/**").hasAuthority(OFFICE.name())
                    .requestMatchers(EndpointRequest.to(HealthEndpoint.class)).permitAll()
                    .requestMatchers(EndpointRequest.to(PrometheusScrapeEndpoint.class)).permitAll()
                    // TODO muss konfigurierbar werden!
                    .requestMatchers(EndpointRequest.toAnyEndpoint()).hasAuthority("ADMIN")
                    .anyRequest().authenticated()
            );

        http.oauth2Login(
            loginCustomizer -> loginCustomizer.authorizationEndpoint(
                endpointCustomizer -> endpointCustomizer.authorizationRequestResolver(new LoginHintAwareResolver(clientRegistrationRepository))
            )
        );
        http.logout(
            logoutCustomizer -> logoutCustomizer.logoutSuccessHandler(oidcClientInitiatedLogoutSuccessHandler)
        );

        http.securityContext(securityContext -> securityContext.securityContextRepository(securityContextRepository));
        http.addFilterAfter(new ReloadAuthenticationAuthoritiesFilter(personService, sessionService, securityContextRepository), BasicAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    DelegatingSecurityContextRepository securityContextRepository() {
        return new DelegatingSecurityContextRepository(
            new RequestAttributeSecurityContextRepository(),
            new HttpSessionSecurityContextRepository()
        );
    }
}
