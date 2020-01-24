package org.synyx.urlaubsverwaltung.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.metrics.export.prometheus.PrometheusScrapeEndpoint;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;

import static org.springframework.http.HttpMethod.GET;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    private static final String OFFICE = "OFFICE";
    private static final String BOSS = "BOSS";
    private static final String USER = "USER";
    private static final String ADMIN = "ADMIN";

    private boolean isOauth2Enabled;
    private SecurityContextLogoutHandler oidcLogoutHandler;

    public WebSecurityConfig(SecurityConfigurationProperties properties) {
        isOauth2Enabled = "oidc".equalsIgnoreCase(properties.getAuth().name());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http
            .csrf()
            .disable()
            .authorizeRequests()
            // TODO move to common url static or resources
            .antMatchers("/favicons/**").permitAll()
            .antMatchers("/browserconfig.xml").permitAll()
            .antMatchers("/manifest.json").permitAll()
            .antMatchers("/css/**").permitAll()
            .antMatchers("/fonts/**").permitAll()
            .antMatchers("/images/**").permitAll()
            .antMatchers("/assets/**").permitAll()
            .antMatchers("/login*").permitAll()
            // WEB
            .antMatchers(GET, "/web/company/calendar").permitAll()
            .antMatchers(GET, "/web/departments/*/calendar").permitAll()
            .antMatchers(GET, "/web/persons/*/calendar").permitAll()
            .antMatchers("/web/overview").hasAuthority(USER)
            .antMatchers("/web/application/**").hasAuthority(USER)
            .antMatchers("/web/sicknote/**").hasAuthority(USER)
            .antMatchers("/web/person/**").hasAuthority(USER)
            .antMatchers("/web/overtime/**").hasAuthority(USER)
            .antMatchers("/web/department/**").hasAnyAuthority(BOSS, OFFICE)
            .antMatchers("/web/settings/**").hasAuthority(OFFICE)
            .antMatchers("/web/google-api-handshake/**").hasAuthority(OFFICE)
            .requestMatchers(EndpointRequest.to(HealthEndpoint.class)).permitAll()
            .requestMatchers(EndpointRequest.to(PrometheusScrapeEndpoint.class)).permitAll()
            // TODO muss konfigurierbar werden!
            .requestMatchers(EndpointRequest.toAnyEndpoint()).hasAuthority(ADMIN)
            .anyRequest()
            .authenticated();

        if (isOauth2Enabled) {
            http.oauth2Login().and()
                .logout()
                .addLogoutHandler(oidcLogoutHandler);
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
    }

    @Autowired(required = false)
    public void setOidcLogoutHandler(SecurityContextLogoutHandler oidcLogoutHandler) {
        this.oidcLogoutHandler = oidcLogoutHandler;
    }
}
