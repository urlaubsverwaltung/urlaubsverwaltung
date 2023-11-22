package org.synyx.urlaubsverwaltung.security.oidc;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.util.List;

@Configuration
@EnableConfigurationProperties({OidcSecurityProperties.class, RolesFromClaimMappersProperties.class})
class OidcSecurityConfiguration {

    private final OidcSecurityProperties properties;

    OidcSecurityConfiguration(OidcSecurityProperties properties) {
        this.properties = properties;
    }

    @Bean
    PersonOnSuccessfullyOidcLoginEventHandler personOnSuccessfullyOidcLoginEventHandler(PersonService personService) {
        return new PersonOnSuccessfullyOidcLoginEventHandler(personService);
    }

    @Bean
    OidcPersonAuthoritiesMapper oidcPersonAuthoritiesMapper(PersonService personService) {
        return new OidcPersonAuthoritiesMapper(personService);
    }

    @Bean
    OAuth2UserService<OidcUserRequest, OidcUser> oidcUserOAuth2UserService(List<RolesFromClaimMapper> rolesFromClaimMappers) {
        final OidcUserService defaultOidcUserService = new OidcUserService();
        return new RolesFromClaimMappersInfusedOAuth2UserService(defaultOidcUserService, rolesFromClaimMappers);
    }

    @Bean
    OidcLoginLogger oidcLoginLogger(PersonService personService) {
        return new OidcLoginLogger(personService);
    }

    @Bean
    OidcClientInitiatedLogoutSuccessHandler oidcClientInitiatedLogoutSuccessHandler(ClientRegistrationRepository clientRegistrationRepository) {
        final OidcClientInitiatedLogoutSuccessHandler oidcClientInitiatedLogoutSuccessHandler = new OidcClientInitiatedLogoutSuccessHandler(clientRegistrationRepository);
        oidcClientInitiatedLogoutSuccessHandler.setPostLogoutRedirectUri(properties.getPostLogoutRedirectUri());
        return oidcClientInitiatedLogoutSuccessHandler;
    }
}
