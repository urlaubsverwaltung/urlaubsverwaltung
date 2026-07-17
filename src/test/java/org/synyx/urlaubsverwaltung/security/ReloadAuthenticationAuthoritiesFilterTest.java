package org.synyx.urlaubsverwaltung.security;

import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.context.DelegatingSecurityContextRepository;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.tenancy.authentication.TenantIdProvider;
import org.synyx.urlaubsverwaltung.tenancy.tenant.TenantContextHolder;
import org.synyx.urlaubsverwaltung.tenancy.tenant.TenantId;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.type;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

@ExtendWith(MockitoExtension.class)
class ReloadAuthenticationAuthoritiesFilterTest {

    private ReloadAuthenticationAuthoritiesFilter sut;

    @Mock
    private PersonService personService;
    @Mock
    private SessionService sessionService;
    @Mock
    private DelegatingSecurityContextRepository securityContextRepository;
    @Mock
    private TenantContextHolder tenantContextHolder;
    @Mock
    private TenantIdProvider tenantIdProvider;

    @BeforeEach
    void setUp() {
        sut = new ReloadAuthenticationAuthoritiesFilter(personService, sessionService, securityContextRepository, tenantContextHolder, tenantIdProvider);
    }

    @Test
    void ensuresFilterSetsOAuth2AuthenticationWithNewAuthorities() throws ServletException, IOException {

        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final MockFilterChain filterChain = new MockFilterChain();

        request.getSession().setAttribute("reloadAuthorities", true);

        final Person signedInUser = new Person("marlene", "Muster", "Marlene", "muster@example.org");
        signedInUser.setPermissions(List.of(USER, OFFICE));
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(prepareOAuth2Authentication());

        when(tenantIdProvider.resolve(any(OAuth2AuthenticationToken.class))).thenReturn(Optional.of(new TenantId("myTenantId")));

        sut.doFilterInternal(request, response, filterChain);

        final List<String> updatedAuthorities = context.getAuthentication().getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .toList();
        assertThat(updatedAuthorities).containsExactly("USER", "OFFICE");

        assertThat(context.getAuthentication())
            .asInstanceOf(type(OAuth2AuthenticationToken.class))
            .extracting(OAuth2AuthenticationToken::getAuthorizedClientRegistrationId)
            .isEqualTo("authorizedClientRegistrationId");
        verify(sessionService).unmarkSessionToReloadAuthorities(request.getSession().getId());
        verify(securityContextRepository).saveContext(context, request, response);
    }

    @Test
    void shouldNotFilterWhenNoSessionExists() {

        final MockHttpServletRequest request = new MockHttpServletRequest();

        final boolean shouldNotFilter = sut.shouldNotFilter(request);
        assertThat(shouldNotFilter).isTrue();
    }

    @Test
    void shouldNotFilterWhenSessionExistsButReloadIsNotDefined() {

        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.getSession(); // create a session without the reload attribute

        final boolean shouldNotFilter = sut.shouldNotFilter(request);
        assertThat(shouldNotFilter).isTrue();
    }

    @Test
    void shouldNotFilterWhenSessionExistsButReloadIsFalse() {

        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.getSession().setAttribute("reloadAuthorities", false);

        final boolean shouldNotFilter = sut.shouldNotFilter(request);
        assertThat(shouldNotFilter).isTrue();
    }

    @Test
    void shouldFilterWhenSessionExistsAndReloadIsTrue() {

        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.getSession().setAttribute("reloadAuthorities", true);

        final boolean shouldNotFilter = sut.shouldNotFilter(request);
        assertThat(shouldNotFilter).isFalse();
    }

    @Test
    void doFilterInternalContinuesFilterChainWhenNoSessionExists() throws ServletException, IOException {

        final MockHttpServletRequest request = new MockHttpServletRequest();
        // no session created
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final MockFilterChain filterChain = new MockFilterChain();

        sut.doFilterInternal(request, response, filterChain);

        assertThat(filterChain.getRequest()).isEqualTo(request);
        assertThat(filterChain.getResponse()).isEqualTo(response);
        verifyNoInteractions(personService, sessionService, securityContextRepository, tenantContextHolder, tenantIdProvider);
    }

    @Test
    void doFilterInternalSkipsReloadWhenPrincipalIsNull() throws ServletException, IOException {

        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.getSession().setAttribute("reloadAuthorities", true);
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final MockFilterChain filterChain = new MockFilterChain();

        final OAuth2AuthenticationToken authentication = mock(OAuth2AuthenticationToken.class);
        when(authentication.getPrincipal()).thenReturn(null);

        final SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(authentication);

        sut.doFilterInternal(request, response, filterChain);

        // Verify that the filter chain was continued without updating authorities
        assertThat(filterChain.getRequest()).isEqualTo(request);
        assertThat(filterChain.getResponse()).isEqualTo(response);
        verify(sessionService).unmarkSessionToReloadAuthorities(request.getSession().getId());
        verify(personService, never()).getSignedInUser();
        verify(securityContextRepository, never()).saveContext(context, request, response);
    }

    @Test
    void doFilterInternalSkipsReloadWhenAuthenticationIsNotOAuth2() throws ServletException, IOException {

        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.getSession().setAttribute("reloadAuthorities", true);
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final MockFilterChain filterChain = new MockFilterChain();

        // Use a non-OAuth2 authentication type (e.g., UsernamePasswordAuthenticationToken)
        final Authentication nonOAuth2Auth = new UsernamePasswordAuthenticationToken("user", "password");

        final SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(nonOAuth2Auth);

        sut.doFilterInternal(request, response, filterChain);

        // Verify that the filter chain was continued without updating authorities
        assertThat(filterChain.getRequest()).isEqualTo(request);
        assertThat(filterChain.getResponse()).isEqualTo(response);
        verify(sessionService).unmarkSessionToReloadAuthorities(request.getSession().getId());
        verify(personService, never()).getSignedInUser();
        verify(securityContextRepository, never()).saveContext(context, request, response);
    }

    @Test
    void doFilterInternalSkipsReloadWhenTenantIdCannotBeResolved() throws ServletException, IOException {

        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.getSession().setAttribute("reloadAuthorities", true);
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final MockFilterChain filterChain = new MockFilterChain();

        final SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(prepareOAuth2Authentication());

        when(tenantIdProvider.resolve(any(OAuth2AuthenticationToken.class))).thenReturn(Optional.empty());

        sut.doFilterInternal(request, response, filterChain);

        // Verify that the filter chain was continued without updating authorities
        assertThat(filterChain.getRequest()).isEqualTo(request);
        assertThat(filterChain.getResponse()).isEqualTo(response);
        verify(sessionService).unmarkSessionToReloadAuthorities(request.getSession().getId());
        verify(personService, never()).getSignedInUser();
        verify(securityContextRepository, never()).saveContext(context, request, response);
        verifyNoInteractions(tenantContextHolder);
    }

    private OAuth2AuthenticationToken prepareOAuth2Authentication() {
        final OAuth2AuthenticationToken authentication = mock(OAuth2AuthenticationToken.class);
        final OidcUser oidcUser = mock(OidcUser.class);
        when(authentication.getPrincipal()).thenReturn(oidcUser);
        when(authentication.getAuthorizedClientRegistrationId()).thenReturn("authorizedClientRegistrationId");
        return authentication;
    }
}
