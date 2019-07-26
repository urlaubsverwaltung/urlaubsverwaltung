package org.synyx.urlaubsverwaltung.security.config;

import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.metrics.export.prometheus.PrometheusScrapeEndpoint;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http
            .csrf()
                .disable()
            .authorizeRequests()
                // TODO move to common url static or resources
                .antMatchers("/css/**").permitAll()
                .antMatchers("/fonts/**").permitAll()
                .antMatchers("/images/**").permitAll()
                .antMatchers("/assets/**").permitAll()
                // WEB
                .antMatchers("/web/overview").hasAuthority("USER")
                .antMatchers("/web/application/**").hasAuthority("USER")
                .antMatchers("/web/sicknote/**").hasAuthority("USER")
                .antMatchers("/web/staff/**").hasAuthority("USER")
                .antMatchers("/web/overtime/**").hasAuthority("USER")
                .antMatchers("/web/department/**").hasAnyAuthority("BOSS", "OFFICE")
                .antMatchers("/web/settings/**").hasAuthority("OFFICE")
                .antMatchers("/web/google-api-handshake/**").hasAuthority("OFFICE")
                .requestMatchers(EndpointRequest.to(HealthEndpoint.class)).permitAll()
                .requestMatchers(EndpointRequest.to(PrometheusScrapeEndpoint.class)).permitAll()
                // TODO muss konfigurierbar werden!
                .requestMatchers(EndpointRequest.toAnyEndpoint()).hasAuthority("ADMIN")
                .anyRequest()
                    .authenticated()
                .and()
                    .formLogin()
                        .loginPage("/login").permitAll()
                            .defaultSuccessUrl("/web/overview")
                            .failureUrl("/login?login_error=1")
                .and()
                    .logout()
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login");
    }
}
