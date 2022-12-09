package org.synyx.urlaubsverwaltung.security.oidc;

import org.slf4j.Logger;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.core.oidc.StandardClaimAccessor;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.Role;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static java.lang.String.format;
import static java.lang.invoke.MethodHandles.lookup;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.slf4j.LoggerFactory.getLogger;
import static org.synyx.urlaubsverwaltung.person.Role.INACTIVE;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

class OidcPersonAuthoritiesMapper implements GrantedAuthoritiesMapper {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final PersonService personService;

    OidcPersonAuthoritiesMapper(PersonService personService) {
        this.personService = personService;
    }

    @Override
    public Collection<? extends GrantedAuthority> mapAuthorities(Collection<? extends GrantedAuthority> authorities) {
        return authorities
            .stream()
            .filter(OidcUserAuthority.class::isInstance)
            .findFirst()
            .map(OidcUserAuthority.class::cast)
            .map(this::mapAuthorities)
            .orElseThrow(() -> new OidcPersonMappingException("oidc: The granted authority was not a 'OidcUserAuthority' and the user cannot be mapped."));
    }

    private Collection<? extends GrantedAuthority> mapAuthorities(OidcUserAuthority oidcUserAuthority) {
        return resolvePerson(oidcUserAuthority)
            .map(this::extractPermissions).orElse(List.of(USER))
            .stream()
            .map(Role::name)
            .map(SimpleGrantedAuthority::new)
            .collect(toList());
    }

    private Collection<Role> extractPermissions(Person person) {
        if (person.hasRole(INACTIVE)) {
            throw new DisabledException(format("User '%s' has been deactivated", person.getId()));
        }
        return person.getPermissions();
    }

    private Optional<Person> resolvePerson(OidcUserAuthority oidcUserAuthority) {
        final String userUniqueID = extractIdentifier(oidcUserAuthority);
        return personService.getPersonByUsername(userUniqueID)
            .or(() -> {
                // try to fall back to uniqueness of mailAddress if userUniqueID is not found in database
                final String emailAddress = extractMailAddress(oidcUserAuthority);
                return personService.getPersonByMailAddress(emailAddress);
            });
    }

    private String extractIdentifier(OidcUserAuthority authority) {
        final String userUniqueID = authority.getIdToken().getSubject();
        if (userUniqueID == null || userUniqueID.isBlank()) {
            LOG.error("Can not retrieve the subject of the id token for oidc person mapping");
            throw new OidcPersonMappingException("Can not retrieve the subject of the id token for oidc person mapping");
        }
        return userUniqueID;
    }

    private String extractMailAddress(OidcUserAuthority authority) {
        return ofNullable(authority.getIdToken())
            .map(StandardClaimAccessor::getEmail)
            .or(() -> ofNullable(authority.getUserInfo()).map(StandardClaimAccessor::getEmail))
            .orElseThrow(() -> {
                LOG.error("Can not retrieve the email for oidc person mapping");
                return new OidcPersonMappingException("Can not retrieve the email for oidc person mapping");
            });
    }
}
