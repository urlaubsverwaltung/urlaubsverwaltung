package org.synyx.urlaubsverwaltung.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.ldap.authentication.BindAuthenticator;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;
import org.springframework.security.ldap.authentication.LdapAuthenticator;
import org.springframework.security.ldap.search.FilterBasedLdapUserSearch;
import org.springframework.security.ldap.userdetails.DefaultLdapAuthoritiesPopulator;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;
import org.synyx.urlaubsverwaltung.security.PersonContextMapper;

@Configuration
@ConditionalOnProperty(name = "auth", havingValue = "ldap")
public class LdapAuthConfiguration {

    @Autowired
    private Environment environment;

    @Bean
    public LdapContextSource ldapContextSource() {
        LdapContextSource source = new LdapContextSource();
        source.setUserDn(environment.getProperty("uv.security.ldap.managerDn"));
        source.setPassword(environment.getProperty("uv.security.ldap.managerPassword"));
        source.setBase(environment.getProperty("uv.security.ldap.base"));
        source.setUrl(environment.getProperty("uv.security.ldap.url"));
        return source;
    }

    @Bean
    public LdapAuthoritiesPopulator authoritiesPopulator() {
        return new DefaultLdapAuthoritiesPopulator(ldapContextSource(), null);
    }

    @Bean
    public FilterBasedLdapUserSearch ldapUserSearch() {

        String searchBase = environment.getProperty("uv.security.ldap.userSearchBase");
        String searchFilter = environment.getProperty("uv.security.ldap.userSearchFilter");

        return new FilterBasedLdapUserSearch(searchBase, searchFilter, ldapContextSource());
    }

    @Bean
    public LdapAuthenticator authenticator() {
        BindAuthenticator authenticator = new BindAuthenticator(ldapContextSource());
        authenticator.setUserSearch(ldapUserSearch());
        return authenticator;
    }

    @Bean
    public AuthenticationProvider ldapAuthenticationProvider(PersonContextMapper personContextMapper) {

        LdapAuthenticationProvider ldapAuthenticationProvider = new LdapAuthenticationProvider(authenticator(), authoritiesPopulator());
        ldapAuthenticationProvider.setUserDetailsContextMapper(personContextMapper);

        return ldapAuthenticationProvider;
    }

}
