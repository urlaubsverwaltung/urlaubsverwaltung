package org.synyx.urlaubsverwaltung.security.oidc;

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
import java.util.Optional;

import static java.lang.invoke.MethodHandles.lookup;
import static java.util.Collections.singletonList;
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

        final Optional<String> optionalFirstName = extractGivenName(oidcUserAuthority);
        final Optional<String> optionalLastName = extractFamilyName(oidcUserAuthority);
        final Optional<String> optionalMailAddress = extractMailAddress(oidcUserAuthority);

        final String userUniqueID = oidcUserAuthority.getIdToken().getSubject();

        Optional<Person> optionalPerson = personService.getPersonByUsername(userUniqueID);
        // try to fall back to uniqueness of mailAddress if userUniqueID is not found in database
        if (optionalPerson.isEmpty() && optionalMailAddress.isPresent()) {
            optionalPerson = personService.getPersonByMailAddress(optionalMailAddress.get());
        }

        final Person person;

        if (optionalPerson.isPresent()) {

            final Person tmpPerson = optionalPerson.get();

            if (!userUniqueID.equals(tmpPerson.getUsername())) {
                LOG.info("No person with given userUniqueID was found. Falling back to matching mail address for " +
                    "person lookup. Existing username '{}' is replaced with '{}'.", tmpPerson.getUsername(), userUniqueID);
                tmpPerson.setUsername(userUniqueID);
            }
            optionalFirstName.ifPresent(tmpPerson::setFirstName);
            optionalLastName.ifPresent(tmpPerson::setLastName);
            optionalMailAddress.ifPresent(tmpPerson::setEmail);

            person = personService.save(tmpPerson);

            if (person.hasRole(INACTIVE)) {
                throw new DisabledException("User '" + person.getId() + "' has been deactivated");
            }

        } else {
            final Person createdPerson = personService.create(userUniqueID, optionalLastName.orElse(null),
                optionalFirstName.orElse(null), optionalMailAddress.orElse(null), singletonList(NOTIFICATION_USER), singletonList(USER));
            person = personService.appointAsOfficeUserIfNoOfficeUserPresent(createdPerson);
        }

        return person.getPermissions()
            .stream()
            .map(Role::name)
            .map(SimpleGrantedAuthority::new)
            .collect(toList());
    }

    private Optional<String> extractFamilyName(OidcUserAuthority authority) {
        final Optional<String> familyName = ofNullable(authority.getIdToken().getFamilyName());
        if (familyName.isPresent()) {
            return familyName;
        } else {
            return ofNullable(authority.getUserInfo().getFamilyName());
        }
    }

    private Optional<String> extractGivenName(OidcUserAuthority authority) {
        final Optional<String> givenName = ofNullable(authority.getIdToken().getGivenName());
        if (givenName.isPresent()) {
            return givenName;
        } else {
            return ofNullable(authority.getUserInfo().getGivenName());
        }
    }

    private Optional<String> extractMailAddress(OidcUserAuthority authority) {
        final Optional<String> mailAddress = ofNullable(authority.getIdToken().getEmail());
        if (mailAddress.isPresent()) {
            return mailAddress;
        } else {
            return ofNullable(authority.getUserInfo().getEmail());
        }
    }
}
