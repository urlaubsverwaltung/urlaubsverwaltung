package org.synyx.urlaubsverwaltung;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;
import org.synyx.urlaubsverwaltung.security.CustomPrincipal;

import java.util.ArrayList;
import java.util.List;

public class WithMockCustomUserSecurityContextFactory implements WithSecurityContextFactory<WithMockCustomUser> {

    @Override
    public SecurityContext createSecurityContext(WithMockCustomUser customUser) {
        final SecurityContext context = SecurityContextHolder.createEmptyContext();

        final CustomPrincipal principal = new CustomPrincipal(customUser.id(), customUser.username());

        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        for (String authority : customUser.authorities()) {
            grantedAuthorities.add(new SimpleGrantedAuthority(authority));
        }

        final Authentication auth = new UsernamePasswordAuthenticationToken(principal, "password", grantedAuthorities);
        context.setAuthentication(auth);
        return context;
    }
}
