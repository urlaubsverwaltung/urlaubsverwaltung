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

import java.util.Optional;
import java.util.function.Supplier;

import static java.lang.invoke.MethodHandles.lookup;
import static java.util.Optional.ofNullable;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.security.oauth2.core.oidc.StandardClaimNames.FAMILY_NAME;
import static org.springframework.security.oauth2.core.oidc.StandardClaimNames.GIVEN_NAME;
import static org.springframework.security.oauth2.core.oidc.StandardClaimNames.SUB;

class PersonOnSuccessfullyOidcLoginEventHandler {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final PersonService personService;

    PersonOnSuccessfullyOidcLoginEventHandler(PersonService personService) {
        this.personService = personService;
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
                existentPerson.setUsername(userUniqueID);
            }

            existentPerson.setFirstName(firstName);
            existentPerson.setLastName(lastName);
            existentPerson.setEmail(emailAddress);
            personService.update(existentPerson);

        } else {
            final Person createdPerson = personService.create(userUniqueID, firstName, lastName, emailAddress);
            personService.appointAsOfficeUserIfNoOfficeUserPresent(createdPerson);
        }
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
