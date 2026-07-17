package org.synyx.urlaubsverwaltung.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.context.DelegatingSecurityContextRepository;
import org.springframework.web.filter.OncePerRequestFilter;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.tenancy.authentication.TenantIdProvider;
import org.synyx.urlaubsverwaltung.tenancy.tenant.TenantContextHolder;
import org.synyx.urlaubsverwaltung.tenancy.tenant.TenantId;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static java.lang.Boolean.TRUE;
import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;
import static org.synyx.urlaubsverwaltung.security.SessionServiceImpl.RELOAD_AUTHORITIES;

class ReloadAuthenticationAuthoritiesFilter extends OncePerRequestFilter {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final PersonService personService;
    private final SessionService sessionService;
    private final DelegatingSecurityContextRepository securityContextRepository;
    private final TenantContextHolder tenantContextHolder;
    private final TenantIdProvider tenantIdProvider;

    ReloadAuthenticationAuthoritiesFilter(PersonService personService, SessionService sessionService, DelegatingSecurityContextRepository securityContextRepository, TenantContextHolder tenantContextHolder, TenantIdProvider tenantIdProvider) {
        this.personService = personService;
        this.sessionService = sessionService;
        this.securityContextRepository = securityContextRepository;
        this.tenantContextHolder = tenantContextHolder;
        this.tenantIdProvider = tenantIdProvider;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        final HttpSession session = request.getSession(false);
        if (session == null) {
            return true;
        }

        final Boolean reload = (Boolean) session.getAttribute(RELOAD_AUTHORITIES);
        return !TRUE.equals(reload);
    }

    @Override
    public void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain chain) throws ServletException, IOException {
        final HttpSession session = request.getSession(false);

        if (session == null) {
            chain.doFilter(request, response);
            return;
        }

        sessionService.unmarkSessionToReloadAuthorities(session.getId());

        final SecurityContext context = SecurityContextHolder.getContext();
        final Authentication authentication = context.getAuthentication();

        if (!(authentication instanceof OAuth2AuthenticationToken oAuth2Auth)) {
            chain.doFilter(request, response);
            return;
        }

        final OAuth2User principal = oAuth2Auth.getPrincipal();
        if (principal == null) {
            LOG.warn("Skipping authorities reload: principal is null for session {}", session.getId());
            chain.doFilter(request, response);
            return;
        }

        final String authorizedClientRegistrationId = oAuth2Auth.getAuthorizedClientRegistrationId();
        final Optional<TenantId> resolvedTenantId = tenantIdProvider.resolve(oAuth2Auth);
        if (resolvedTenantId.isEmpty()) {
            LOG.warn("Could not resolve tenant from authentication; skipping authorities reload");
            chain.doFilter(request, response);
            return;
        }

        try {
            tenantContextHolder.setTenantId(resolvedTenantId.get());
            final Person signedInUser = personService.getSignedInUser();
            final List<SimpleGrantedAuthority> updatedAuthorities = getUpdatedAuthorities(signedInUser);
            final Authentication updatedAuthentication = new OAuth2AuthenticationToken(principal, updatedAuthorities, authorizedClientRegistrationId);

            context.setAuthentication(updatedAuthentication);
            securityContextRepository.saveContext(context, request, response);
            LOG.info("Updated authorities of person with the id {} from {} to {}", signedInUser.getId(), authentication.getAuthorities(), updatedAuthorities);
        } finally {
            tenantContextHolder.clear();
        }

        chain.doFilter(request, response);
    }

    private List<SimpleGrantedAuthority> getUpdatedAuthorities(Person signedInUser) {
        return signedInUser.getPermissions().stream()
            .map(role -> new SimpleGrantedAuthority(role.name()))
            .toList();
    }
}
