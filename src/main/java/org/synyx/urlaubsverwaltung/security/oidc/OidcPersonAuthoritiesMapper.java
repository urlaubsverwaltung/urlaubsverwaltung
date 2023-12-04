package org.synyx.urlaubsverwaltung.security.oidc;

import org.apache.commons.validator.routines.EmailValidator;
import org.slf4j.Logger;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.Role;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.lang.invoke.MethodHandles.lookup;
import static java.util.Optional.ofNullable;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.security.oauth2.core.oidc.StandardClaimNames.EMAIL;
import static org.springframework.security.oauth2.core.oidc.StandardClaimNames.SUB;
import static org.synyx.urlaubsverwaltung.person.Role.INACTIVE;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

class OidcPersonAuthoritiesMapper implements GrantedAuthoritiesMapper {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final PersonService personService;

    OidcPersonAuthoritiesMapper(PersonService personService) {
        this.personService = personService;
    }

    @Override
    public Collection<? extends GrantedAuthority> mapAuthorities(Collection<? extends GrantedAuthority> authorities) {

        final Collection<? extends GrantedAuthority> applicationAuthorities = authorities
            .stream()
            .filter(OidcUserAuthority.class::isInstance)
            .findFirst()
            .map(OidcUserAuthority.class::cast)
            .map(this::mapAuthorities)
            .orElseThrow(() -> new OidcPersonMappingException("oidc: The granted authority was not a 'OidcUserAuthority' and the user cannot be mapped."));

        return Stream.concat(applicationAuthorities.stream(), authorities.stream()).toList();
    }

    private Collection<? extends GrantedAuthority> mapAuthorities(OidcUserAuthority oidcUserAuthority) {
        return resolvePerson(oidcUserAuthority)
            .map(this::extractPermissions).orElseGet(this::generateListOfRoles)
            .stream()
            .map(Role::name)
            .map(SimpleGrantedAuthority::new)
            .toList();
    }


    private List<Role> generateListOfRoles() {
        if (personService.getActivePersonsByRole(OFFICE).isEmpty()) {
            return List.of(OFFICE, USER);
        }
        return List.of(USER);
    }

    private Collection<Role> extractPermissions(Person person) {
        if (person.hasRole(INACTIVE)) {
            throw new DisabledException(format("User '%s' has been deactivated", person.getId()));
        }
        return person.getPermissions();
    }

    private Optional<Person> resolvePerson(OidcUserAuthority oidcUserAuthority) {
        return personService.getPersonByUsername(extractIdentifier(oidcUserAuthority))
            .or(() -> {
                // try to fall back to uniqueness of mailAddress if userUniqueID is not found in database
                final String emailAddress = extractMailAddress(oidcUserAuthority);
                return personService.getPersonByMailAddress(emailAddress);
            });
    }

    private String extractIdentifier(OidcUserAuthority authority) {
        return getClaimAsString(authority, () -> SUB)
            .orElseThrow(() -> {
                LOG.error("Can not retrieve the subject of the id token for oidc person mapping on {} ", authority);
                return new OidcPersonMappingException("Can not retrieve the subject of the id token for oidc person mapping");
            });
    }

    private String extractMailAddress(OidcUserAuthority authority) {
        return getClaimAsString(authority, () -> EMAIL)
            .filter(email -> EmailValidator.getInstance().isValid(email))
            .orElse(null);
    }

    private Optional<String> getClaimAsString(OidcUserAuthority authority, Supplier<String> claimAccessor) {
        return ofNullable(authority.getIdToken()).map(oidcIdToken -> oidcIdToken.getClaimAsString(claimAccessor.get()))
            .or(() -> ofNullable(authority.getUserInfo()).map(oidcIdToken -> oidcIdToken.getClaimAsString(claimAccessor.get())));
    }
}
