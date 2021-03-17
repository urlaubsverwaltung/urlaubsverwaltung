package org.synyx.urlaubsverwaltung.security.ldap;

import org.slf4j.Logger;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.ldap.userdetails.Person.Essence;
import org.springframework.security.ldap.userdetails.UserDetailsContextMapper;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.Role;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

import static java.lang.invoke.MethodHandles.lookup;
import static java.util.Collections.singletonList;
import static org.slf4j.LoggerFactory.getLogger;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_USER;
import static org.synyx.urlaubsverwaltung.person.Role.INACTIVE;
import static org.synyx.urlaubsverwaltung.person.Role.USER;


/**
 * Map granted authorities to application roles described in {@link Role}.
 */
public class LdapPersonContextMapper implements UserDetailsContextMapper {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final PersonService personService;
    private final LdapUserMapper ldapUserMapper;

    LdapPersonContextMapper(PersonService personService, LdapUserMapper ldapUserMapper) {
        this.personService = personService;
        this.ldapUserMapper = ldapUserMapper;
    }

    @Override
    public UserDetails mapUserFromContext(DirContextOperations ctx, String username, Collection<? extends GrantedAuthority> authorities) {

        final LdapUser ldapUser;

        try {
            ldapUser = ldapUserMapper.mapFromContext(ctx);
        } catch (InvalidSecurityConfigurationException | UnsupportedMemberAffiliationException ex) {
            throw new BadCredentialsException("No authentication possible for user = " + username, ex);
        }

        final String ldapUsername = ldapUser.getUsername();
        final Optional<String> firstName = ldapUser.getFirstName();
        final Optional<String> lastName = ldapUser.getLastName();
        final Optional<String> email = ldapUser.getEmail();

        final Person person;

        final Optional<Person> maybePerson = personService.getPersonByUsername(ldapUsername);
        if (maybePerson.isPresent()) {
            final Person existentPerson = maybePerson.get();

            if (existentPerson.hasRole(INACTIVE)) {
                LOG.info("User '{}' has been deactivated and can not sign in therefore", existentPerson.getId());

                throw new DisabledException("User '" + existentPerson.getId() + "' has been deactivated");
            }

            firstName.ifPresent(existentPerson::setFirstName);
            lastName.ifPresent(existentPerson::setLastName);
            email.ifPresent(existentPerson::setEmail);

            person = personService.save(existentPerson);
        } else {
            LOG.info("No user found for username '{}'", username);

            final Person createdPerson = personService.create(ldapUsername, lastName.orElse(null), firstName.orElse(null),
                email.orElse(null), singletonList(NOTIFICATION_USER), singletonList(USER));

            person = personService.appointAsOfficeUserIfNoOfficeUserPresent(createdPerson);
        }

        /*
         * NOTE: If the system has no office user yet, grant office permissions to successfully signed in user
         */
        final Essence user = new Essence(ctx);
        user.setUsername(ldapUsername);
        user.setAuthorities(getGrantedAuthorities(person));

        LOG.info("User '{}' has signed in with roles: {}", person.getId(), person.getPermissions());

        return user.createUserDetails();
    }


    /**
     * Gets the granted authorities using the {@link Role}s of the given {@link Person}.
     *
     * @param person to get the granted authorities for, may not be {@code null}
     * @return the granted authorities for the person
     */
    Collection<GrantedAuthority> getGrantedAuthorities(Person person) {
        final Collection<Role> permissions = person.getPermissions();
        if (permissions.isEmpty()) {
            throw new IllegalStateException("Every user must have at least one role, data seems to be corrupt.");
        }

        final Collection<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        permissions.forEach(role -> grantedAuthorities.add(role::name));

        return grantedAuthorities;
    }


    @Override
    public void mapUserToContext(UserDetails user, DirContextAdapter ctx) {

        throw new UnsupportedOperationException("LdapPersonContextMapper only supports reading from a context. Please "
            + "use a subclass if mapUserToContext() is required.");
    }
}
