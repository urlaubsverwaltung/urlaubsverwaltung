package org.synyx.urlaubsverwaltung.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.io.IOException;
import java.util.List;

import static java.lang.Boolean.TRUE;
import static java.lang.invoke.MethodHandles.lookup;
import static java.util.stream.Collectors.toList;
import static org.slf4j.LoggerFactory.getLogger;
import static org.synyx.urlaubsverwaltung.security.SessionServiceImpl.RELOAD_AUTHORITIES;

class ReloadAuthenticationAuthoritiesFilter extends OncePerRequestFilter {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final PersonService personService;
    private final SessionService sessionService;

    ReloadAuthenticationAuthoritiesFilter(PersonService personService, SessionService sessionService) {
        this.personService = personService;
        this.sessionService = sessionService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        final HttpSession session = request.getSession();
        if (session == null) {
            return true;
        }

        final Boolean reload = (Boolean) session.getAttribute(RELOAD_AUTHORITIES);
        return !TRUE.equals(reload);
    }

    @Override
    public void doFilterInternal(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull FilterChain chain) throws ServletException, IOException {
        final HttpSession session = request.getSession();
        sessionService.unmarkSessionToReloadAuthorities(session.getId());

        final Person signedInUser = personService.getSignedInUser();
        final List<GrantedAuthority> updatedAuthorities = getUpdatedAuthorities(signedInUser);
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        try {
            final Authentication updatedAuthentication = getUpdatedAuthentication(updatedAuthorities, authentication);
            SecurityContextHolder.getContext().setAuthentication(updatedAuthentication);
            LOG.info("Updated authorities of person with the id {} from {} to {}", signedInUser.getId(), authentication.getAuthorities(), updatedAuthorities);
        } catch (ReloadAuthenticationException e) {
            LOG.error(e.getMessage());
        }

        chain.doFilter(request, response);
    }

    private List<GrantedAuthority> getUpdatedAuthorities(Person signedInUser) {
        return signedInUser.getPermissions().stream()
            .map(role -> new SimpleGrantedAuthority(role.name()))
            .collect(toList());
    }

    private Authentication getUpdatedAuthentication(List<GrantedAuthority> updatedAuthorities, Authentication authentication) throws ReloadAuthenticationException {
        final Authentication updatedAuthentication;
        if (authentication instanceof OAuth2AuthenticationToken) {
            final OAuth2AuthenticationToken oAuth2Auth = (OAuth2AuthenticationToken) authentication;
            updatedAuthentication = new OAuth2AuthenticationToken(oAuth2Auth.getPrincipal(), updatedAuthorities, oAuth2Auth.getAuthorizedClientRegistrationId());
        } else if (authentication instanceof UsernamePasswordAuthenticationToken) {
            updatedAuthentication = new UsernamePasswordAuthenticationToken(authentication.getPrincipal(), authentication.getCredentials(), updatedAuthorities);
        } else {
            throw new ReloadAuthenticationException("Could not update authentication with updated authorities, because of type mismatch");
        }

        return updatedAuthentication;
    }
}
