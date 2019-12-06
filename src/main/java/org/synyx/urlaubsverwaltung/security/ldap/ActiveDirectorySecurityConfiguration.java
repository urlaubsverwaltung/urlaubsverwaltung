package org.synyx.urlaubsverwaltung.security.ldap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.ldap.authentication.ad.ActiveDirectoryLdapAuthenticationProvider;
import org.synyx.urlaubsverwaltung.person.PersonService;

@Configuration
public class ActiveDirectorySecurityConfiguration {

    @Configuration
    @ConditionalOnProperty(name = "uv.security.auth", havingValue = "activedirectory")
    public static class ActiveDirectoryAuthConfiguration {

        private final DirectoryServiceSecurityProperties directoryServiceSecurityProperties;
        private final ActiveDirectorySecurityConfigurationProperties configurationProperties;

        @Autowired
        public ActiveDirectoryAuthConfiguration(DirectoryServiceSecurityProperties directoryServiceSecurityProperties, ActiveDirectorySecurityConfigurationProperties configurationProperties) {
            this.directoryServiceSecurityProperties = directoryServiceSecurityProperties;
            this.configurationProperties = configurationProperties;
        }

        @Bean
        public AuthenticationProvider activeDirectoryAuthenticationProvider(LdapPersonContextMapper ldapPersonContextMapper) {

            String domain = configurationProperties.getDomain();
            String url = configurationProperties.getUrl();

            final ActiveDirectoryLdapAuthenticationProvider authenticationProvider = new ActiveDirectoryLdapAuthenticationProvider(domain, url);
            authenticationProvider.setUserDetailsContextMapper(ldapPersonContextMapper);

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
    @ConditionalOnExpression("'${uv.security.auth}'=='activedirectory' and '${uv.security.activedirectory.sync.enabled}'=='true'")
    static class LdapAuthSyncConfiguration {

        private final DirectoryServiceSecurityProperties directoryServiceSecurityProperties;
        private final ActiveDirectorySecurityConfigurationProperties adProperties;

        @Autowired
        public LdapAuthSyncConfiguration(DirectoryServiceSecurityProperties directoryServiceSecurityProperties, ActiveDirectorySecurityConfigurationProperties adProperties) {
            this.directoryServiceSecurityProperties = directoryServiceSecurityProperties;
            this.adProperties = adProperties;
        }

        @Bean
        public ActiveDirectoryContextSourceSync ldapContextSourceSync() {
            return new ActiveDirectoryContextSourceSync(adProperties);
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
            return new UVLdapTemplate(ldapContextSourceSync());
        }
    }
}
