package org.synyx.urlaubsverwaltung.api;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
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

public class JwtToPersonGrantedAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    private final PersonService personService;

    public JwtToPersonGrantedAuthoritiesConverter(PersonService personService) {
        this.personService = personService;
    }

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        // in a real world scenario UV roles would be provided via oidc scopes from the oidc server
        // ... in our case user right mgmt resides inside this application ... so read granted authorities from
        // the database and fake it :-)
        final Person person = createOrUpdatePerson(jwt);
        Collection<GrantedAuthority> grantedAuthorities = mapAuthorities(person);
        return grantedAuthorities;
    }

    private Person createOrUpdatePerson(Jwt jwt) {

        final String userUniqueID = jwt.getSubject();
        // are those claims oidc standard? works for now via keycloak ...
        final String firstName = jwt.getClaimAsString("given_name");
        final String lastName = jwt.getClaimAsString("family_name");
        final String email = jwt.getClaimAsString("email");


        // this mechanism is based on OidcPersonAuthoritiesMapper.java - think about it!

        final Optional<Person> optionalPerson = personService.getPersonByUsername(userUniqueID);

        final Person person;

        if (optionalPerson.isPresent()) {

            // TODO compare existing user meta data with Person meta data

            Person tmpPerson = optionalPerson.get();
            tmpPerson.setFirstName(firstName);
            tmpPerson.setLastName(lastName);
            tmpPerson.setEmail(email);

            person = personService.update(tmpPerson);

            if (person.hasRole(INACTIVE)) {
                throw new DisabledException("User '" + person.getId() + "' has been deactivated");
            }

        } else {
            final Person createdPerson = personService.create(userUniqueID, lastName, firstName, email,
                singletonList(NOTIFICATION_USER), singletonList(USER));
            person = personService.appointAsOfficeUserIfNoOfficeUserPresent(createdPerson);
        }
        return person;
    }

    private Collection<GrantedAuthority> mapAuthorities(Person person) {

        return person.getPermissions()
            .stream()
            .map(Role::name)
            .map(SimpleGrantedAuthority::new)
            .collect(toList());
    }

}
