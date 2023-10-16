package org.synyx.urlaubsverwaltung.security.oidc;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.AuthenticatedPrincipalOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.security.oidc.OidcSecurityProperties.GroupClaim;

@Configuration
@EnableConfigurationProperties(OidcSecurityProperties.class)
class OidcSecurityConfiguration {

    private final OidcSecurityProperties properties;

    OidcSecurityConfiguration(OidcSecurityProperties properties) {
        this.properties = properties;
    }

    @Bean
    OAuth2AuthorizedClientService authorizedClientService(ClientRegistrationRepository clientRegistrationRepository) {
        return new InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository);
    }

    @Bean
    OAuth2AuthorizedClientRepository authorizedClientRepository(OAuth2AuthorizedClientService authorizedClientService) {
        return new AuthenticatedPrincipalOAuth2AuthorizedClientRepository(authorizedClientService);
    }

    @Bean
    OidcPersonAuthoritiesMapper oidcPersonAuthoritiesMapper(PersonService personService) {
        return new OidcPersonAuthoritiesMapper(personService, properties);
    }

    @Bean
    @ConditionalOnProperty(value = "uv.security.oidc.group-claim.enabled", havingValue = "true")
    UrlaubsverwaltungOAuth2UserService urlaubsverwaltungOAuth2UserService(OidcSecurityProperties oidcSecurityProperties) {
        final GroupClaim groupClaim = oidcSecurityProperties.getGroupClaim();
        return new UrlaubsverwaltungOAuth2UserService(new OidcUserService(), groupClaim.getClaimName(), groupClaim.getPermittedGroup());
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
