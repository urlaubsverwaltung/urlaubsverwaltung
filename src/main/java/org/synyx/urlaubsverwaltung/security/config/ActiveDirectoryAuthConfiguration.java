package org.synyx.urlaubsverwaltung.security.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.ldap.authentication.ad.ActiveDirectoryLdapAuthenticationProvider;
import org.synyx.urlaubsverwaltung.security.PersonContextMapper;

@Configuration
@ConditionalOnProperty(name = "auth", havingValue = "activeDirectory")
public class ActiveDirectoryAuthConfiguration {

    @Bean
    public AuthenticationProvider activeDirectoryAuthenticationProvider(Environment environment, PersonContextMapper personContextMapper) {
        String domain = environment.getProperty("uv.security.activeDirectory.domain");
        String url = environment.getProperty("uv.security.activeDirectory.url");
        ActiveDirectoryLdapAuthenticationProvider authenticationProvider = new ActiveDirectoryLdapAuthenticationProvider(domain, url);
        authenticationProvider.setUserDetailsContextMapper(personContextMapper);
        return authenticationProvider;
    }

}
