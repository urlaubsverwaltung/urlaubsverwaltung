package org.synyx.urlaubsverwaltung.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.ldap.authentication.BindAuthenticator;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;
import org.springframework.security.ldap.authentication.LdapAuthenticator;
import org.springframework.security.ldap.authentication.ad.ActiveDirectoryLdapAuthenticationProvider;
import org.springframework.security.ldap.search.FilterBasedLdapUserSearch;
import org.springframework.security.ldap.userdetails.DefaultLdapAuthoritiesPopulator;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;
import org.synyx.urlaubsverwaltung.core.person.PersonService;
import org.synyx.urlaubsverwaltung.security.PersonContextMapper;
import org.synyx.urlaubsverwaltung.security.SimpleAuthenticationProvider;

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
