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

        final Collection<Role> permissions = resolvePerson(oidcUserAuthority)
            .map(this::extractPermissions)
            .orElseGet(() -> List.of(USER));

        return withOfficeRoleIfNoOfficeUserPresent(permissions)
            .stream()
            .map(Role::name)
            .map(SimpleGrantedAuthority::new)
            .toList();
    }

    /**
     * The person signing in is appointed as office user when there is no active office user, see
     * {@link PersonService#appointAsOfficeUserIfNoOfficeUserPresent(Person)}. Authorities are mapped
     * before the person is created or updated, therefore the office role has to be anticipated here.
     * Otherwise the person would have to sign in a second time to make use of it.
     *
     * @param permissions of the person signing in, or {@link Role#USER} if the person does not exist yet
     * @return the given permissions, with {@link Role#OFFICE} added if no active office user is present
     */
    private Collection<Role> withOfficeRoleIfNoOfficeUserPresent(Collection<Role> permissions) {

        if (permissions.contains(OFFICE) || !personService.getActivePersonsByRole(OFFICE).isEmpty()) {
            return permissions;
        }

        return Stream.concat(Stream.of(OFFICE), permissions.stream()).toList();
    }

    private Collection<Role> extractPermissions(Person person) {
        if (person.hasRole(INACTIVE)) {
            throw new DisabledException("User '%s' has been deactivated".formatted(person.getId()));
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
