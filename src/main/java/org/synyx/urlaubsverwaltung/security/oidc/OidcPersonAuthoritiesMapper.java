package org.synyx.urlaubsverwaltung.security.oidc;

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

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_USER;
import static org.synyx.urlaubsverwaltung.person.Role.INACTIVE;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

/**
 * @author Florian Krupicka - krupicka@synyx.de
 */
public class OidcPersonAuthoritiesMapper implements GrantedAuthoritiesMapper {

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

        final Optional<String> firstName = extractGivenName(oidcUserAuthority);
        final Optional<String> lastName = extractFamilyName(oidcUserAuthority);
        final Optional<String> mailAddress = extractMailAddress(oidcUserAuthority);

        final String userUniqueID = oidcUserAuthority.getIdToken().getSubject();

        Optional<Person> optionalPerson = personService.getPersonByUsername(userUniqueID);
        // try to fall back to uniqueness of mailAddress if userUniqueID is not found in database
        if (optionalPerson.isEmpty() && mailAddress.isPresent()) {
            optionalPerson = personService.getPersonByMailAddress(mailAddress.get());
        }

        final Person person;

        if (optionalPerson.isPresent()) {

            Person tmpPerson = optionalPerson.get();

            // this overrides the exiting username with the user unique id of oidc provider
            if (!userUniqueID.equals(tmpPerson.getUsername())) {
                tmpPerson.setUsername(userUniqueID);
            }
            firstName.ifPresent(tmpPerson::setFirstName);
            lastName.ifPresent(tmpPerson::setLastName);
            mailAddress.ifPresent(tmpPerson::setEmail);

            person = personService.save(tmpPerson);

            if (person.hasRole(INACTIVE)) {
                throw new DisabledException("User '" + person.getId() + "' has been deactivated");
            }

        } else {
            final Person createdPerson = personService.create(userUniqueID, lastName.orElse(null),
                firstName.orElse(null), mailAddress.orElse(null), singletonList(NOTIFICATION_USER), singletonList(USER));
            person = personService.appointAsOfficeUserIfNoOfficeUserPresent(createdPerson);
        }

        return person.getPermissions()
            .stream()
            .map(Role::name)
            .map(SimpleGrantedAuthority::new)
            .collect(toList());
    }


    private Optional<String> extractFamilyName(OidcUserAuthority authority) {

        final Optional<String> familyName = Optional.ofNullable(authority.getIdToken().getFamilyName());
        if (familyName.isPresent()) {
            return familyName;
        } else {
            return Optional.ofNullable(authority.getUserInfo().getFamilyName());
        }
    }

    private Optional<String> extractGivenName(OidcUserAuthority authority) {

        final Optional<String> givenName = Optional.ofNullable(authority.getIdToken().getGivenName());
        if (givenName.isPresent()) {
            return givenName;
        } else {
            return Optional.ofNullable(authority.getUserInfo().getGivenName());
        }

    }

    private Optional<String> extractMailAddress(OidcUserAuthority authority) {

        final Optional<String> mailAddress = Optional.ofNullable(authority.getIdToken().getEmail());
        if (mailAddress.isPresent()) {
            return mailAddress;
        } else {
            return Optional.ofNullable(authority.getUserInfo().getEmail());
        }
    }
}
