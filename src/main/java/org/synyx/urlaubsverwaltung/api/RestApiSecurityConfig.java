package org.synyx.urlaubsverwaltung.api;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.synyx.urlaubsverwaltung.security.SecurityConfigurationProperties;

import static org.springframework.security.config.http.SessionCreationPolicy.NEVER;

@Configuration
@Order(1)
public class RestApiSecurityConfig extends WebSecurityConfigurerAdapter {

    private final boolean isOauth2Enabled;

    public RestApiSecurityConfig(SecurityConfigurationProperties properties) {
        isOauth2Enabled = "oidc".equalsIgnoreCase(properties.getAuth().name());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http
            .antMatcher("/api/**")
            .sessionManagement()
              .sessionCreationPolicy(NEVER)
            .and()
            .authorizeRequests()
              .antMatchers("/api/**").authenticated()
              .anyRequest().authenticated();

        if (isOauth2Enabled) {
            http.oauth2Login();
        } else {
            http.httpBasic();
        }
    }
}
