package org.synyx.urlaubsverwaltung.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;

@Configuration
@Order(1)
public class RestApiSecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http
            .antMatcher("/api/**")
                .sessionManagement()
                    .sessionCreationPolicy(SessionCreationPolicy.NEVER)
            .and()
                .httpBasic()
            .and()
                .authorizeRequests()
                    .antMatchers("/api/sicknotes/**").hasAuthority("OFFICE")
                    .antMatchers("/api/**").authenticated()
                    .anyRequest()
                        .authenticated();
    }

}
