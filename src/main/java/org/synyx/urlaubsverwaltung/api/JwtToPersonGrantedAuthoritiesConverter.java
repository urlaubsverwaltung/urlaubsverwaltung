package org.synyx.urlaubsverwaltung.api;

import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.Role;
import org.synyx.urlaubsverwaltung.security.oidc.OidcSecurityProperties;

import java.util.List;
import java.util.Optional;

import static org.synyx.urlaubsverwaltung.person.Role.INACTIVE;

class JwtToPersonGrantedAuthoritiesConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final PersonService personService;
    private final OidcSecurityProperties properties;

    JwtToPersonGrantedAuthoritiesConverter(PersonService personService, OidcSecurityProperties properties) {
        this.personService = personService;
        this.properties = properties;
    }

    @Override
    public AbstractAuthenticationToken convert(@NonNull Jwt jwt) {
        return new JwtAuthenticationToken(jwt, applicationAuthorities(jwt));
    }

    private List<SimpleGrantedAuthority> applicationAuthorities(Jwt jwt) {

        final String userUniqueID = jwt.getClaimAsString(properties.getUserMappings().getIdentifier());
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
}
