package org.synyx.urlaubsverwaltung.security.oidc;

import org.slf4j.Logger;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.oidc.StandardClaimAccessor;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.Role;

import java.util.List;
import java.util.Optional;

import static java.lang.invoke.MethodHandles.lookup;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.slf4j.LoggerFactory.getLogger;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_USER;

class PersonOnSuccessfullyOidcLoginEventHandler {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final PersonService personService;

    PersonOnSuccessfullyOidcLoginEventHandler(PersonService personService) {
        this.personService = personService;
    }

    @EventListener
    public void handle(AuthenticationSuccessEvent event) {

        final Authentication authentication = event.getAuthentication();
        final OidcUser user = (OidcUser) authentication.getPrincipal();

        final String userUniqueID = extractIdentifier(user);
        final String firstName = extractGivenName(user);
        final String lastName = extractFamilyName(user);
        final String emailAddress = extractMailAddress(user);

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
            final List<Role> permissions = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(Role::valueOf)
                .collect(toList());

            final Person createdPerson = personService.create(
                userUniqueID,
                lastName,
                firstName,
                emailAddress,
                List.of(NOTIFICATION_USER),
                permissions
            );
            personService.appointAsOfficeUserIfNoOfficeUserPresent(createdPerson);
        }
    }

    private String extractIdentifier(OidcUser oidcUser) {
        return ofNullable(oidcUser.getSubject())
            .or(() -> ofNullable(oidcUser.getUserInfo()).map(StandardClaimAccessor::getSubject))
            .orElseThrow(() -> {
                LOG.error("Can not retrieve the subject for oidc person mapping");
                return new OidcPersonMappingException("Can not retrieve the subject for oidc person mapping");
            });
    }

    private String extractFamilyName(OidcUser oidcUser) {
        return ofNullable(oidcUser.getFamilyName())
            .or(() -> ofNullable(oidcUser.getUserInfo()).map(StandardClaimAccessor::getFamilyName))
            .orElseThrow(() -> {
                LOG.error("Can not retrieve the family name for oidc person mapping");
                return new OidcPersonMappingException("Can not retrieve the family name for oidc person mapping");
            });
    }

    private String extractGivenName(OidcUser oidcUser) {
        return ofNullable(oidcUser.getGivenName())
            .or(() -> ofNullable(oidcUser.getUserInfo()).map(StandardClaimAccessor::getGivenName))
            .orElseThrow(() -> {
                LOG.error("Can not retrieve the given name for oidc person mapping");
                return new OidcPersonMappingException("Can not retrieve the given name for oidc person mapping");
            });
    }

    private String extractMailAddress(OidcUser oidcUser) {
        return ofNullable(oidcUser.getEmail())
            .or(() -> ofNullable(oidcUser.getUserInfo()).map(StandardClaimAccessor::getEmail))
            .orElseThrow(() -> {
                LOG.error("Can not retrieve the email for oidc person mapping");
                return new OidcPersonMappingException("Can not retrieve the email for oidc person mapping");
            });
    }

}
