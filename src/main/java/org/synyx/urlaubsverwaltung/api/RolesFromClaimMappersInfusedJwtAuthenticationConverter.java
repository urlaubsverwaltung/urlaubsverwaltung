package org.synyx.urlaubsverwaltung.api;

import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.Role;
import org.synyx.urlaubsverwaltung.security.oidc.RolesFromClaimMapper;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.synyx.urlaubsverwaltung.person.Role.INACTIVE;

class RolesFromClaimMappersInfusedJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final PersonService personService;
    private final List<RolesFromClaimMapper> claimMappers;

    RolesFromClaimMappersInfusedJwtAuthenticationConverter(PersonService personService, List<RolesFromClaimMapper> claimMappers) {
        this.personService = personService;
        this.claimMappers = claimMappers;
    }

    @Override
    public AbstractAuthenticationToken convert(@NonNull final Jwt jwt) {

        final Collection<SimpleGrantedAuthority> applicationAuthorities = applicationAuthorities(jwt);

        final List<GrantedAuthority> combinedAuthorities = Stream.concat(
            applicationAuthorities.stream(),
            getAuthoritiesFromClaimMappers(jwt)
        ).distinct().toList();

        return new JwtAuthenticationToken(jwt, combinedAuthorities);
    }

    private List<SimpleGrantedAuthority> applicationAuthorities(final Jwt jwt) {

        final String userUniqueID = jwt.getSubject();
        final Optional<Person> maybePerson = personService.getPersonByUsername(userUniqueID);

        if (maybePerson.isPresent()) {
            final Person person = maybePerson.get();

            if (person.hasRole(INACTIVE)) {
                throw new DisabledException("User '" + person.getId() + "' cannot access api, because account has been deactivated");
            }

            return person.getPermissions()
                .stream()
                .map(Role::name)
                .map(SimpleGrantedAuthority::new)
                .toList();
        }

        return List.of();
    }

    private Stream<GrantedAuthority> getAuthoritiesFromClaimMappers(final Jwt jwt) {
        return claimMappers.stream()
            .flatMap(rolesFromClaimMapper -> rolesFromClaimMapper.mapClaimToRoles(jwt.getClaims()).stream());
    }
}
