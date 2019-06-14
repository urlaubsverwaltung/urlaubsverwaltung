package org.synyx.urlaubsverwaltung.security.oidc;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.Role;
import org.synyx.urlaubsverwaltung.security.PersonSyncService;

import java.util.Collection;
import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;

/**
 * @author Florian Krupicka - krupicka@synyx.de
 */
public class OidcPersonAuthoritiesMapper implements GrantedAuthoritiesMapper {

    private final PersonService personService;
    private final PersonSyncService personSyncService;

    public OidcPersonAuthoritiesMapper(PersonService personService, PersonSyncService personSyncService) {

        this.personService = personService;
        this.personSyncService = personSyncService;
    }

    @Override
    public Collection<? extends GrantedAuthority> mapAuthorities(Collection<? extends GrantedAuthority> authorities) {

        final Optional<? extends GrantedAuthority> authority = authorities.stream().findFirst();

        return authority.map(this::mapAuthorities).orElseThrow(RuntimeException::new);
    }

    private Collection<? extends GrantedAuthority> mapAuthorities(GrantedAuthority grantedAuthority) {
        if (grantedAuthority instanceof OidcUserAuthority) {

            final OidcUserAuthority oidcUserAuthority = (OidcUserAuthority) grantedAuthority;
            final Optional<String> firstName = extractGivenName(oidcUserAuthority);
            final Optional<String> lastName = extractFamilyName(oidcUserAuthority);
            final Optional<String> mailAddress = extractMailAddress(oidcUserAuthority);

            final String userUniqueID = oidcUserAuthority.getIdToken().getSubject();

            final Optional<Person> maybePerson = personService.getPersonByLogin(userUniqueID);

            final Person person;

            if (maybePerson.isPresent()) {
                person = personSyncService.syncPerson(maybePerson.get(), firstName, lastName, mailAddress);
            } else {
                person = personSyncService.createPerson(userUniqueID, firstName, lastName, mailAddress);

                /*
                 * NOTE: If the system has no office user yet, grant office permissions to successfully signed in user
                 */
                final boolean noOfficeUserYet = personService.getPersonsByRole(OFFICE).isEmpty();
                if (noOfficeUserYet) {
                    personSyncService.appointPersonAsOfficeUser(person);
                }
            }

            return person.getPermissions()
                .stream()
                .map(Role::name)
                .map(SimpleGrantedAuthority::new)
                .collect(toList());
        } else {
            throw new OidcPersonMappingException("oidc: The granted authority was not a 'OidcUserAuthority' and the user cannot be mapped.");
        }
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
