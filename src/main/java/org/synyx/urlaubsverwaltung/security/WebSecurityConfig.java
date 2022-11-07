package org.synyx.urlaubsverwaltung.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.metrics.export.prometheus.PrometheusScrapeEndpoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.Role;

import static org.springframework.http.HttpMethod.GET;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class WebSecurityConfig {

    private final PersonService personService;
    private final SessionService sessionService;
    private final boolean isOauth2Enabled;

    private OidcClientInitiatedLogoutSuccessHandler oidcClientInitiatedLogoutSuccessHandler;

    public WebSecurityConfig(SecurityConfigurationProperties properties, PersonService personService, SessionService sessionService) {
        isOauth2Enabled = "oidc".equalsIgnoreCase(properties.getAuth().name());
        this.personService = personService;
        this.sessionService = sessionService;
    }

    @Bean
    public SecurityFilterChain webSecurityFilterChain(HttpSecurity http) throws Exception {

        http
            .authorizeHttpRequests((authorizeHttpRequests) ->
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

        if (isOauth2Enabled) {
            http.oauth2Login().and()
                .logout()
                .logoutSuccessHandler(oidcClientInitiatedLogoutSuccessHandler);
        } else {
            http.formLogin()
                .loginPage("/login").permitAll()
                .defaultSuccessUrl("/web/overview")
                .failureUrl("/login?login_error=1")
                .and()
                .logout()
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login");
        }

        http
            .addFilterAfter(new ReloadAuthenticationAuthoritiesFilter(personService, sessionService), BasicAuthenticationFilter.class);

        return http.build();
    }

    @Autowired(required = false)
    public void setOidcClientInitiatedLogoutSuccessHandler(OidcClientInitiatedLogoutSuccessHandler oidcClientInitiatedLogoutSuccessHandler) {
        this.oidcClientInitiatedLogoutSuccessHandler = oidcClientInitiatedLogoutSuccessHandler;
    }
}
