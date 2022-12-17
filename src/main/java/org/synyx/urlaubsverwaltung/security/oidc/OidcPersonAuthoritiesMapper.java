package org.synyx.urlaubsverwaltung.security.oidc;

import org.apache.commons.validator.routines.EmailValidator;
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

import static java.lang.invoke.MethodHandles.lookup;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.slf4j.LoggerFactory.getLogger;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_USER;
import static org.synyx.urlaubsverwaltung.person.Role.INACTIVE;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

public class OidcPersonAuthoritiesMapper implements GrantedAuthoritiesMapper {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final PersonService personService;

    public OidcPersonAuthoritiesMapper(PersonService personService) {
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

        final String userUniqueID = extractIdentifier(oidcUserAuthority);
        final String firstName = extractGivenName(oidcUserAuthority);
        final String lastName = extractFamilyName(oidcUserAuthority);
        final String emailAddress = extractMailAddress(oidcUserAuthority);

        Optional<Person> optionalPerson = personService.getPersonByUsername(userUniqueID);
        // try to fall back to uniqueness of mailAddress if userUniqueID is not found in database
        if (optionalPerson.isEmpty() && emailAddress != null) {
            optionalPerson = personService.getPersonByMailAddress(emailAddress);
        }

        final Person person;
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
            person = personService.update(existentPerson);

            if (person.hasRole(INACTIVE)) {
                throw new DisabledException("User '" + person.getId() + "' has been deactivated");
            }
        } else {
            final Person createdPerson = personService.create(
                userUniqueID,
                lastName,
                firstName,
                emailAddress,
                List.of(NOTIFICATION_USER),
                List.of(USER)
            );
            person = personService.appointAsOfficeUserIfNoOfficeUserPresent(createdPerson);
        }

        return person.getPermissions()
            .stream()
            .map(Role::name)
            .map(SimpleGrantedAuthority::new)
            .collect(toList());
    }

    private String extractIdentifier(OidcUserAuthority authority) {
        final String userUniqueID = authority.getIdToken().getSubject();
        if (userUniqueID == null || userUniqueID.isBlank()) {
            LOG.error("Can not retrieve the subject of the id token for oidc person mapping");
            throw new OidcPersonMappingException("Can not retrieve the subject of the id token for oidc person mapping");
        }
        return userUniqueID;
    }

    private String extractFamilyName(OidcUserAuthority authority) {
        return ofNullable(authority.getIdToken()).map(StandardClaimAccessor::getFamilyName)
            .or(() -> ofNullable(authority.getUserInfo()).map(StandardClaimAccessor::getFamilyName))
            .orElseThrow(() -> {
                LOG.error("Can not retrieve the lastname for oidc person mapping");
                return new OidcPersonMappingException("Can not retrieve the lastname for oidc person mapping");
            });
    }

    private String extractGivenName(OidcUserAuthority authority) {
        return ofNullable(authority.getIdToken()).map(StandardClaimAccessor::getGivenName)
            .or(() -> ofNullable(authority.getUserInfo()).map(StandardClaimAccessor::getGivenName))
            .orElseThrow(() -> {
                LOG.error("Can not retrieve the given name for oidc person mapping");
                return new OidcPersonMappingException("Can not retrieve the given name for oidc person mapping");
            });
    }

    private String extractMailAddress(OidcUserAuthority authority) {
        return ofNullable(authority.getIdToken()).map(StandardClaimAccessor::getEmail)
            .or(() -> ofNullable(authority.getUserInfo()).map(StandardClaimAccessor::getEmail))
            .filter(email -> EmailValidator.getInstance().isValid(email))
            .orElse(null);
    }
}
