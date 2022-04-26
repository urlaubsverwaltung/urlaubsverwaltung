package org.synyx.urlaubsverwaltung.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.List;

import static java.lang.Boolean.TRUE;
import static java.util.stream.Collectors.toList;
import static org.synyx.urlaubsverwaltung.security.SessionServiceImpl.RELOAD_AUTHORITIES;

class ReloadAuthenticationAuthoritiesFilter extends OncePerRequestFilter {

    private final PersonService personService;
    private final SessionService<HttpSession> sessionService;

    ReloadAuthenticationAuthoritiesFilter(PersonService personService, SessionService<HttpSession> sessionService) {
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
        session.removeAttribute(RELOAD_AUTHORITIES);
        sessionService.save(session);

        final Person signedInUser = personService.getSignedInUser();
        final List<GrantedAuthority> updatedAuthorities = signedInUser.getPermissions().stream()
            .map(role -> new SimpleGrantedAuthority(role.name()))
            .collect(toList());

        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        final Authentication updatedAuthentication = new PreAuthenticatedAuthenticationToken(authentication.getPrincipal(), authentication.getCredentials(), updatedAuthorities);
        SecurityContextHolder.getContext().setAuthentication(updatedAuthentication);

        chain.doFilter(request, response);
    }
}
