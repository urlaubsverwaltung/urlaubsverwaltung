package org.synyx.urlaubsverwaltung.security.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
@ConditionalOnProperty(name = "uv.security.auth", havingValue = "ldap")
public class LdapAuthConfiguration {

    private final SecurityLdapConfigurationProperties ldapProperties;

    @Autowired
    public LdapAuthConfiguration(SecurityLdapConfigurationProperties ldapProperties) {
        this.ldapProperties = ldapProperties;
    }

    @Bean
    public LdapContextSource ldapContextSource() {

        LdapContextSource source = new LdapContextSource();
        source.setUserDn(ldapProperties.getManagerDn());
        source.setPassword(ldapProperties.getManagerPassword());
        source.setBase(ldapProperties.getBase());
        source.setUrl(ldapProperties.getUrl());

        return source;
    }

    @Bean
    public LdapAuthoritiesPopulator authoritiesPopulator() {

        return new DefaultLdapAuthoritiesPopulator(ldapContextSource(), null);
    }

    @Bean
    public FilterBasedLdapUserSearch ldapUserSearch() {

        String searchBase = ldapProperties.getUserSearchBase();
        String searchFilter = ldapProperties.getUserSearchFilter();

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
