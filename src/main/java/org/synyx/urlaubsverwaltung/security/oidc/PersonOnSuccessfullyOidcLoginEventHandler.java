package org.synyx.urlaubsverwaltung.security.oidc;

import org.apache.commons.validator.routines.EmailValidator;
import org.slf4j.Logger;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.Role;

import java.util.List;
import java.util.Optional;

import static java.lang.invoke.MethodHandles.lookup;
import static java.util.Optional.ofNullable;
import static org.slf4j.LoggerFactory.getLogger;

class PersonOnSuccessfullyOidcLoginEventHandler {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final PersonService personService;
    private final OidcSecurityProperties.UserMappings userMappings;

    PersonOnSuccessfullyOidcLoginEventHandler(PersonService personService, OidcSecurityProperties properties) {
        this.personService = personService;
        this.userMappings = properties.getUserMappings();
    }

    @EventListener
    public void handle(AuthenticationSuccessEvent event) {

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
            final Person createdPerson = personService.create(userUniqueID, lastName, firstName, emailAddress, List.of(), getRoles(oidcUser));
            personService.appointAsOfficeUserIfNoOfficeUserPresent(createdPerson);
        }
    }

    private List<Role> getRoles(OidcUser oidcUser) {
        return oidcUser.getAuthorities()
            .stream()
            .map(GrantedAuthority::getAuthority)
            .map(Role::valueOf)
            .toList();
    }

    private String extractIdentifier(OidcUser oidcUser) {
        return ofNullable(oidcUser.getIdToken()).map(oidcIdToken -> oidcIdToken.getClaimAsString(userMappings.getIdentifier()))
            .or(() -> ofNullable(oidcUser.getUserInfo()).map(oidcUserInfo -> oidcUserInfo.getClaimAsString(userMappings.getIdentifier())))
            .orElseThrow(() -> {
                LOG.error("Can not retrieve the subject for oidc person mapping");
                return new OidcPersonMappingException("Can not retrieve the subject for oidc person mapping");
            });
    }

    private String extractFamilyName(OidcUser oidcUser) {
        return ofNullable(oidcUser.getIdToken()).map(oidcIdToken -> oidcIdToken.getClaimAsString(userMappings.getFamilyName()))
            .or(() -> ofNullable(oidcUser.getUserInfo()).map(oidcIdToken -> oidcIdToken.getClaimAsString(userMappings.getFamilyName())))
            .orElseThrow(() -> {
                LOG.error("Can not retrieve the family name for oidc person mapping");
                return new OidcPersonMappingException("Can not retrieve the family name for oidc person mapping");
            });
    }

    private String extractGivenName(OidcUser oidcUser) {
        return ofNullable(oidcUser.getIdToken()).map(oidcIdToken -> oidcIdToken.getClaimAsString(userMappings.getGivenName()))
            .or(() -> ofNullable(oidcUser.getUserInfo()).map(oidcIdToken -> oidcIdToken.getClaimAsString(userMappings.getGivenName())))
            .orElseThrow(() -> {
                LOG.error("Can not retrieve the given name for oidc person mapping");
                return new OidcPersonMappingException("Can not retrieve the given name for oidc person mapping");
            });
    }

    private String extractMailAddress(OidcUser oidcUser) {
        return ofNullable(oidcUser.getIdToken()).map(oidcIdToken -> oidcIdToken.getClaimAsString(userMappings.getEmail()))
            .or(() -> ofNullable(oidcUser.getUserInfo()).map(oidcIdToken -> oidcIdToken.getClaimAsString(userMappings.getEmail())))
            .filter(email -> EmailValidator.getInstance().isValid(email))
            .orElse(null);
    }
}
