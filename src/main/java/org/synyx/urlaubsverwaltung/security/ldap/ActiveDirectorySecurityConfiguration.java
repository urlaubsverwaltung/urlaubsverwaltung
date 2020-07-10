package org.synyx.urlaubsverwaltung.security.ldap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.ldap.authentication.ad.ActiveDirectoryLdapAuthenticationProvider;
import org.synyx.urlaubsverwaltung.person.PersonService;

import static org.springframework.util.StringUtils.hasText;

@Configuration
public class ActiveDirectorySecurityConfiguration {

    @Configuration
    @ConditionalOnProperty(name = "uv.security.auth", havingValue = "activedirectory")
    public static class ActiveDirectoryAuthConfiguration {

        private final DirectoryServiceSecurityProperties directoryServiceSecurityProperties;
        private final ActiveDirectorySecurityProperties adProperties;

        @Autowired
        public ActiveDirectoryAuthConfiguration(DirectoryServiceSecurityProperties directoryServiceSecurityProperties, ActiveDirectorySecurityProperties adProperties) {
            this.directoryServiceSecurityProperties = directoryServiceSecurityProperties;
            this.adProperties = adProperties;
        }

        @Bean
        public AuthenticationProvider activeDirectoryAuthenticationProvider(LdapPersonContextMapper ldapPersonContextMapper) {
            final String domain = adProperties.getDomain();
            final String url = adProperties.getUrl();
            final String searchFilter = adProperties.getSearchFilter();

            final ActiveDirectoryLdapAuthenticationProvider authenticationProvider = new ActiveDirectoryLdapAuthenticationProvider(domain, url);
            authenticationProvider.setUserDetailsContextMapper(ldapPersonContextMapper);
            if (hasText(searchFilter)) {
                authenticationProvider.setSearchFilter(searchFilter);
            }

            return authenticationProvider;
        }

        @Bean
        public LdapUserMapper ldapUserMapper() {
            return new LdapUserMapper(directoryServiceSecurityProperties);
        }

        @Bean
        public LdapPersonContextMapper personContextMapper(PersonService personService, LdapUserMapper ldapUserMapper) {
            return new LdapPersonContextMapper(personService, ldapUserMapper);
        }
    }

    @Configuration
    @ConditionalOnExpression("'${uv.security.auth}'=='activedirectory' and '${uv.security.directory-service.active-directory.sync.enabled}'=='true'")
    static class LdapAuthSyncConfiguration {

        private final DirectoryServiceSecurityProperties directoryServiceSecurityProperties;
        private final ActiveDirectorySecurityProperties adProperties;

        @Autowired
        public LdapAuthSyncConfiguration(DirectoryServiceSecurityProperties directoryServiceSecurityProperties, ActiveDirectorySecurityProperties adProperties) {
            this.directoryServiceSecurityProperties = directoryServiceSecurityProperties;
            this.adProperties = adProperties;
        }

        @Bean
        public LdapUserDataImportConfiguration ldapUserDataImportConfiguration(LdapUserDataImporter ldapUserDataImporter) {
            return new LdapUserDataImportConfiguration(directoryServiceSecurityProperties, ldapUserDataImporter);
        }

        @Bean
        public LdapUserDataImporter ldapUserDataImporter(LdapUserService ldapUserService, PersonService personService) {
            return new LdapUserDataImporter(ldapUserService, personService);
        }

        @Bean
        public LdapUserServiceImpl ldapUserService(LdapTemplate ldapTemplate, LdapUserMapper ldapUserMapper) {
            return new LdapUserServiceImpl(ldapTemplate, ldapUserMapper, directoryServiceSecurityProperties);
        }

        @Bean
        public LdapTemplate ldapTemplate() {
            final LdapTemplate ldapTemplate = new LdapTemplate(ldapContextSourceSync());
            ldapTemplate.setIgnorePartialResultException(true);
            ldapTemplate.setIgnoreNameNotFoundException(true);

            return ldapTemplate;
        }

        private LdapContextSource ldapContextSourceSync() {
            final LdapContextSource ldapContextSource = new LdapContextSource();
            ldapContextSource.setUrl(adProperties.getUrl());
            ldapContextSource.setBase(adProperties.getSync().getUserSearchBase());
            ldapContextSource.setUserDn(adProperties.getSync().getUserDn());
            ldapContextSource.setPassword(adProperties.getSync().getPassword());

            return ldapContextSource;
        }
    }
}
