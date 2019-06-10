package org.synyx.urlaubsverwaltung.security.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.ldap.authentication.ad.ActiveDirectoryLdapAuthenticationProvider;
import org.synyx.urlaubsverwaltung.security.PersonContextMapper;

@Configuration
@ConditionalOnProperty(name = "uv.security.auth", havingValue = "activeDirectory")
public class ActiveDirectoryAuthConfiguration {

    private final SecurityActiveDirectoryConfigurationProperties configurationProperties;

    @Autowired
    public ActiveDirectoryAuthConfiguration(SecurityActiveDirectoryConfigurationProperties configurationProperties) {
        this.configurationProperties = configurationProperties;
    }

    @Bean
    public AuthenticationProvider activeDirectoryAuthenticationProvider(Environment environment, PersonContextMapper personContextMapper) {

        String domain = configurationProperties.getDomain();
        String url = configurationProperties.getUrl();

        final ActiveDirectoryLdapAuthenticationProvider authenticationProvider = new ActiveDirectoryLdapAuthenticationProvider(domain, url);
        authenticationProvider.setUserDetailsContextMapper(personContextMapper);

        return authenticationProvider;
    }
}
