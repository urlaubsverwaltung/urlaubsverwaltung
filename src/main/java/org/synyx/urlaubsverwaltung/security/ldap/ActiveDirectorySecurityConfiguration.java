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
import org.synyx.urlaubsverwaltung.security.SecurityConfigurationProperties;

@Configuration
public class ActiveDirectorySecurityConfiguration {

    @Configuration
    @ConditionalOnProperty(name = "uv.security.auth", havingValue = "activeDirectory")
    public static class ActiveDirectoryAuthConfiguration {

        private final SecurityConfigurationProperties securityProperties;
        private final ActiveDirectorySecurityConfigurationProperties configurationProperties;

        @Autowired
        public ActiveDirectoryAuthConfiguration(SecurityConfigurationProperties securityProperties, ActiveDirectorySecurityConfigurationProperties configurationProperties) {
            this.securityProperties = securityProperties;
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

            return new LdapUserMapper(securityProperties);
        }

        @Bean
        public LdapPersonContextMapper personContextMapper(PersonService personService, LdapSyncService ldapSyncService,
                                                           LdapUserMapper ldapUserMapper) {
            return new LdapPersonContextMapper(personService, ldapSyncService, ldapUserMapper);
        }

        @Bean
        public LdapSyncService ldapSyncService(PersonService personService) {
            return new LdapSyncService(personService);
        }
    }

    @Configuration
    @ConditionalOnExpression("'${uv.security.auth}'=='activeDirectory' and '${uv.security.activeDirectory.sync.enabled}'=='true'")
    static class LdapAuthSyncConfiguration {

        private final SecurityConfigurationProperties securityProperties;
        private final ActiveDirectorySecurityConfigurationProperties adProperties;

        @Autowired
        public LdapAuthSyncConfiguration(SecurityConfigurationProperties securityProperties, ActiveDirectorySecurityConfigurationProperties adProperties) {
            this.securityProperties = securityProperties;
            this.adProperties = adProperties;
        }

        @Bean
        public ActiveDirectoryContextSourceSync ldapContextSourceSync() {
            return new ActiveDirectoryContextSourceSync(adProperties);
        }

        @Bean
        public LdapUserDataImporter ldapUserDataImporter(LdapUserService ldapUserService, LdapSyncService ldapSyncService,
                                                         PersonService personService) {
            return new LdapUserDataImporter(ldapUserService, ldapSyncService, personService);
        }

        @Bean
        public LdapUserServiceImpl ldapUserService(LdapTemplate ldapTemplate, LdapUserMapper ldapUserMapper) {
            return new LdapUserServiceImpl(ldapTemplate, ldapUserMapper, securityProperties);
        }

        @Bean
        public LdapTemplate ldapTemplate() {
            return new UVLdapTemplate(ldapContextSourceSync());
        }
    }
}
