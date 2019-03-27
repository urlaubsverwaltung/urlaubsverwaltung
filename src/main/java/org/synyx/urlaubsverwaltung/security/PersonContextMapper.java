package org.synyx.urlaubsverwaltung.security;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.ldap.userdetails.UserDetailsContextMapper;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.PersonService;
import org.synyx.urlaubsverwaltung.core.person.Role;

import javax.naming.NamingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;


/**
 * Map granted authorities to application roles described in {@link Role}.
 */
@Component
@ConditionalOnExpression("'${auth}'=='activeDirectory' or '${auth}'=='ldap'")
public class PersonContextMapper implements UserDetailsContextMapper {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final PersonService personService;
    private final LdapSyncService ldapSyncService;
    private final LdapUserMapper ldapUserMapper;

    @Autowired
    public PersonContextMapper(PersonService personService, LdapSyncService ldapSyncService,
        LdapUserMapper ldapUserMapper) {

        this.personService = personService;
        this.ldapSyncService = ldapSyncService;
        this.ldapUserMapper = ldapUserMapper;
    }

    @Override
    public UserDetails mapUserFromContext(DirContextOperations ctx, String username,
        Collection<? extends GrantedAuthority> authorities) {

        LdapUser ldapUser;

        try {
            ldapUser = ldapUserMapper.mapFromContext(ctx);
        } catch (InvalidSecurityConfigurationException | NamingException | UnsupportedMemberAffiliationException ex) {
            LOG.info("User '{}' can not sign in!", username, ex);
            throw new BadCredentialsException("No authentication possible for user = " + username, ex);
        }

        String login = ldapUser.getUsername();

        Optional<Person> optionalPerson = personService.getPersonByLogin(login);

        Person person;

        if (optionalPerson.isPresent()) {
            Person existentPerson = optionalPerson.get();

            if (existentPerson.hasRole(Role.INACTIVE)) {
                LOG.info("User '{}' has been deactivated and can not sign in therefore", username);

                throw new DisabledException("User '" + username + "' has been deactivated");
            }

            person = ldapSyncService.syncPerson(existentPerson, ldapUser.getFirstName(), ldapUser.getLastName(),
                    ldapUser.getEmail());
        } else {
            LOG.info("No user found for username '{}'", username);

            person = ldapSyncService.createPerson(login, ldapUser.getFirstName(), ldapUser.getLastName(),
                    ldapUser.getEmail());
        }

        /**
         * NOTE: If the system has no office user yet, grant office permissions to successfully signed in user
         */
        boolean noOfficeUserYet = personService.getPersonsByRole(Role.OFFICE).isEmpty();

        // TODO: Think about if this logic could be dangerous?!
        if (noOfficeUserYet) {
            ldapSyncService.appointPersonAsOfficeUser(person);
        }

        org.springframework.security.ldap.userdetails.Person.Essence user =
            new org.springframework.security.ldap.userdetails.Person.Essence(ctx);

        user.setUsername(login);
        user.setAuthorities(getGrantedAuthorities(person));

        LOG.info("User '{}' has signed in with roles: {}", username, person.getPermissions());

        return user.createUserDetails();
    }


    /**
     * Gets the granted authorities using the {@link Role}s of the given {@link Person}.
     *
     * @param  person  to get the granted authorities for, may not be {@code null}
     *
     * @return  the granted authorities for the person
     */
    Collection<GrantedAuthority> getGrantedAuthorities(Person person) {

        Assert.notNull(person, "Person must be given.");

        Collection<Role> permissions = person.getPermissions();

        if (permissions.isEmpty()) {
            throw new IllegalStateException("Every user must have at least one role, data seems to be corrupt.");
        }

        Collection<GrantedAuthority> grantedAuthorities = new ArrayList<>();

        permissions.forEach(role -> grantedAuthorities.add(role::name));

        return grantedAuthorities;
    }


    @Override
    public void mapUserToContext(UserDetails user, DirContextAdapter ctx) {

        throw new UnsupportedOperationException("PersonContextMapper only supports reading from a context. Please"
            + "use a subclass if mapUserToContext() is required.");
    }
}
