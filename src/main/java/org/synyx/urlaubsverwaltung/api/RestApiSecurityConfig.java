package org.synyx.urlaubsverwaltung.api;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.security.oidc.OidcSecurityProperties;

import static org.springframework.security.config.http.SessionCreationPolicy.NEVER;

@Configuration
public class RestApiSecurityConfig {

    static void noSessions(HttpSecurity http) throws Exception {

        http
            .antMatcher("/api/**")
            .sessionManagement()
            .sessionCreationPolicy(NEVER);

    }

    static void authenticatedApi(HttpSecurity http) throws Exception {

        http
            .authorizeRequests()
            .antMatchers("/api/**").authenticated()
            .anyRequest().authenticated();

    }

    @Configuration
    @Conditional(NonOidcSecurityAuthCondition.class)
    @Order(1)
    static class BasicAuthRestApiSecurityConfig extends WebSecurityConfigurerAdapter {

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            noSessions(http);
            authenticatedApi(http);
            http.httpBasic();
        }

    }

    @Configuration
    @ConditionalOnProperty(value = "uv.security.auth", havingValue = "oidc")
    @Order(1)
    static class Oauth2RestApiSecurityConfig extends WebSecurityConfigurerAdapter {

        private final String issuerUri;
        private final PersonService personService;

        public Oauth2RestApiSecurityConfig(OidcSecurityProperties properties, PersonService personService) {
            this.issuerUri = properties.getIssuerUri();
            this.personService = personService;
        }

        @Override
        protected void configure(HttpSecurity http) throws Exception {

            noSessions(http);
            authenticatedApi(http);

            http.oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt
                .jwtAuthenticationConverter(jwtAuthenticationConverter())
                .decoder(jwtDecoder())
            ));

            http.oauth2Login();
        }

        @Bean
        public JwtAuthenticationConverter jwtAuthenticationConverter() {

            JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
            jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(new JwtToPersonGrantedAuthoritiesConverter(personService));
            return jwtAuthenticationConverter;
        }

        @Bean
        public JwtDecoder jwtDecoder() {
            return JwtDecoders.fromIssuerLocation(issuerUri);
        }
    }

    static class NonOidcSecurityAuthCondition implements Condition {

        private static final String UV_SECURITY_AUTH = "uv.security.auth";
        private static final String OIDC = "oidc";

        @Override
        public boolean matches(ConditionContext conditionContext, AnnotatedTypeMetadata annotatedTypeMetadata) {
            String value = conditionContext.getEnvironment().getProperty(UV_SECURITY_AUTH);
            return !OIDC.equalsIgnoreCase(value);
        }
    }
}
