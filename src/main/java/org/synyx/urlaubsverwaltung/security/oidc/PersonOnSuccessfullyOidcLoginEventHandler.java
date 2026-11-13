package org.synyx.urlaubsverwaltung.security.oidc;

import org.apache.commons.validator.routines.EmailValidator;
import org.slf4j.Logger;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.jwt.Jwt;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.PersonUpdate;
import org.synyx.urlaubsverwaltung.person.Role;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.lang.invoke.MethodHandles.lookup;
import static java.util.Optional.ofNullable;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.security.core.authority.AuthorityUtils.authorityListToSet;
import static org.springframework.security.oauth2.core.oidc.StandardClaimNames.FAMILY_NAME;
import static org.springframework.security.oauth2.core.oidc.StandardClaimNames.GIVEN_NAME;
import static org.springframework.security.oauth2.core.oidc.StandardClaimNames.SUB;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

class PersonOnSuccessfullyOidcLoginEventHandler {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final PersonService personService;
    private final boolean permissionsFromClaims;

    /**
     * @param permissionsFromClaims whether a claim mapper is enabled - then the identity provider grants the roles
     *                              and every login overwrites the permissions of the person with them
     */
    PersonOnSuccessfullyOidcLoginEventHandler(PersonService personService, boolean permissionsFromClaims) {
        this.personService = personService;
        this.permissionsFromClaims = permissionsFromClaims;
    }

    @EventListener
    public void handle(AuthenticationSuccessEvent event) {

        if (event.getAuthentication().getPrincipal() instanceof Jwt) {
            return;
        }

        final OidcUser oidcUser = (OidcUser) event.getAuthentication().getPrincipal();

        final String userUniqueID = extractIdentifier(oidcUser);
        final String firstName = extractGivenName(oidcUser);
        final String lastName = extractFamilyName(oidcUser);
        final String emailAddress = extractMailAddress(oidcUser);
        final Boolean emailVerified = oidcUser.getEmailVerified();

        Optional<Person> optionalPerson = personService.getPersonByUsername(userUniqueID);
        // try to fall back to uniqueness of mailAddress if userUniqueID is not found in database
        if (optionalPerson.isEmpty()) {
            optionalPerson = personService.getPersonByMailAddress(emailAddress);
        }

        if (optionalPerson.isPresent()) {

            final Person existentPerson = optionalPerson.get();

            if (!userUniqueID.equals(existentPerson.getUsername())) {
                LOG.info("No person with given userUniqueID was found. Falling back to matching mail address for " +
                    "person lookup. Existing username '{}' is replaced with '{}'.", existentPerson.getUsername(), userUniqueID);
            }

            final PersonUpdate personUpdate = PersonUpdate.ofPersonalData(userUniqueID, firstName, lastName, emailAddress).withEmailVerified(emailVerified);
            personService.update(existentPerson.getIdAsPersonId(),
                permissionsFromClaims ? personUpdate.withPermissions(rolesFromClaims(oidcUser)) : personUpdate);

        } else {
            final Person createdPerson = personService.create(userUniqueID, firstName, lastName, emailAddress, emailVerified);
            if (permissionsFromClaims) {
                personService.update(createdPerson.getIdAsPersonId(), PersonUpdate.ofPermissions(rolesFromClaims(oidcUser)));
            } else {
                personService.appointAsOfficeUserIfNoOfficeUserPresent(createdPerson.getIdAsPersonId());
            }
        }
    }

    private static List<Role> rolesFromClaims(OidcUser oidcUser) {
        final Set<String> authorities = authorityListToSet(oidcUser.getAuthorities());
        // USER always: an inactive person is refused before by the OidcPersonAuthoritiesMapper
        return Stream.of(Role.values())
            .filter(role -> role == USER || authorities.contains(role.name()))
            .toList();
    }

    private String extractIdentifier(OidcUser oidcUser) {
        return getClaimAsString(oidcUser, () -> SUB)
            .orElseThrow(() -> {
                LOG.error("Can not retrieve the subject for oidc person mapping");
                return new OidcPersonMappingException("Can not retrieve the subject for oidc person mapping");
            });
    }

    private String extractFamilyName(OidcUser oidcUser) {
        return getClaimAsString(oidcUser, () -> FAMILY_NAME)
            .orElseThrow(() -> {
                LOG.error("Can not retrieve the family name for oidc person mapping");
                return new OidcPersonMappingException("Can not retrieve the family name for oidc person mapping");
            });
    }

    private String extractGivenName(OidcUser oidcUser) {
        return getClaimAsString(oidcUser, () -> GIVEN_NAME)
            .orElseThrow(() -> {
                LOG.error("Can not retrieve the given name for oidc person mapping");
                return new OidcPersonMappingException("Can not retrieve the given name for oidc person mapping");
            });
    }

    private String extractMailAddress(OidcUser oidcUser) {
        return getClaimAsString(oidcUser, () -> StandardClaimNames.EMAIL)
            .filter(email -> EmailValidator.getInstance().isValid(email))
            .orElse(null);
    }

    private Optional<String> getClaimAsString(OidcUser oidcUser, Supplier<String> claimSupplier) {
        return ofNullable(oidcUser.getIdToken()).map(oidcIdToken -> oidcIdToken.getClaimAsString(claimSupplier.get()))
            .or(() -> ofNullable(oidcUser.getUserInfo()).map(oidcIdToken -> oidcIdToken.getClaimAsString(claimSupplier.get())));
    }
}
