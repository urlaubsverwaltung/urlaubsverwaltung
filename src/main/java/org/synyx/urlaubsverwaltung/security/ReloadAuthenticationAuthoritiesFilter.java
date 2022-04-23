package org.synyx.urlaubsverwaltung.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.web.filter.GenericFilterBean;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

import static java.lang.Boolean.TRUE;
import static java.util.stream.Collectors.toList;
import static org.synyx.urlaubsverwaltung.security.SessionServiceImpl.RELOAD_AUTHORITIES;

class ReloadAuthenticationAuthoritiesFilter extends GenericFilterBean {

    private final PersonService personService;

    ReloadAuthenticationAuthoritiesFilter(PersonService personService) {
        this.personService = personService;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        final HttpSession session = ((HttpServletRequest) request).getSession();
        if (session != null) {
            final Boolean reload = (Boolean) session.getAttribute(RELOAD_AUTHORITIES);
            if (TRUE.equals(reload)) {
                session.removeAttribute(RELOAD_AUTHORITIES);

                final Person signedInUser = personService.getSignedInUser();
                final List<GrantedAuthority> updatedAuthorities = signedInUser.getPermissions().stream()
                    .map(role -> new SimpleGrantedAuthority(role.name()))
                    .collect(toList());

                final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                final Authentication updatedAuthentication = new PreAuthenticatedAuthenticationToken(authentication.getPrincipal(), authentication.getCredentials(), updatedAuthorities);
                SecurityContextHolder.getContext().setAuthentication(updatedAuthentication);
            }
        }
        chain.doFilter(request, response);
    }
}
