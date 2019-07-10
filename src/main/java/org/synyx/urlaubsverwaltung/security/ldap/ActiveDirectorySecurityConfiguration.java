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
import org.synyx.urlaubsverwaltung.security.PersonSyncService;

@Configuration
public class ActiveDirectorySecurityConfiguration {

    @Configuration
    @ConditionalOnProperty(name = "uv.security.auth", havingValue = "activeDirectory")
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
        public LdapPersonContextMapper personContextMapper(PersonService personService, PersonSyncService personSyncService,
                                                           LdapUserMapper ldapUserMapper) {
            return new LdapPersonContextMapper(personService, personSyncService, ldapUserMapper);
        }

        @Bean
        public PersonSyncService ldapSyncService(PersonService personService) {
            return new PersonSyncService(personService);
        }
    }

    @Configuration
    @ConditionalOnExpression("'${uv.security.auth}'=='activeDirectory' and '${uv.security.activeDirectory.sync.enabled}'=='true'")
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
        public LdapUserDataImporter ldapUserDataImporter(LdapUserService ldapUserService, PersonSyncService personSyncService,
                                                         PersonService personService) {
            return new LdapUserDataImporter(ldapUserService, personSyncService, personService);
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
