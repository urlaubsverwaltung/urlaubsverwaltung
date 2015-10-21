package org.synyx.urlaubsverwaltung.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.context.annotation.Conditional;

import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.ldap.userdetails.UserDetailsContextMapper;

import org.springframework.stereotype.Component;

import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.PersonService;
import org.synyx.urlaubsverwaltung.core.person.Role;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;


/**
 * Map granted authorities to application roles described in {@link Role}.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Component
@Conditional(LdapOrActiveDirectoryAuthenticationCondition.class)
public class PersonContextMapper implements UserDetailsContextMapper {

    private final PersonService personService;
    private final LdapSyncService ldapSyncService;

    private final String identifierAttribute;
    private final String firstNameAttribute;
    private final String lastNameAttribute;
    private final String mailAddressAttribute;

    @Autowired
    public PersonContextMapper(PersonService personService, LdapSyncService ldapSyncService,
        @Value("${security.identifier}") String identifierAttribute,
        @Value("${security.firstName}") String firstNameAttribute,
        @Value("${security.lastName}") String lastNameAttribute,
        @Value("${security.mailAddress}") String mailAddressAttribute) {

        this.personService = personService;
        this.ldapSyncService = ldapSyncService;

        this.identifierAttribute = identifierAttribute;
        this.firstNameAttribute = firstNameAttribute;
        this.lastNameAttribute = lastNameAttribute;
        this.mailAddressAttribute = mailAddressAttribute;
    }

    @Override
    public UserDetails mapUserFromContext(DirContextOperations ctx, String username,
        Collection<? extends GrantedAuthority> authorities) {

        String userIdentifier = ctx.getStringAttribute(identifierAttribute);
        String login = userIdentifier == null ? username : userIdentifier;

        Optional<Person> optionalPerson = personService.getPersonByLogin(login);

        Optional<String> firstName = Optional.ofNullable(ctx.getStringAttribute(firstNameAttribute));
        Optional<String> lastName = Optional.ofNullable(ctx.getStringAttribute(lastNameAttribute));
        Optional<String> mailAddress = Optional.ofNullable(ctx.getStringAttribute(mailAddressAttribute));

        Person person;

        if (optionalPerson.isPresent()) {
            person = ldapSyncService.syncPerson(optionalPerson.get(), firstName, lastName, mailAddress);
        } else {
            person = ldapSyncService.createPerson(login, firstName, lastName, mailAddress);
        }

        /**
         * NOTE: If the system has no office user yet, grant office permissions to successfully signed in user
         */
        boolean noOfficeUserYet = personService.getPersonsByRole(Role.OFFICE).isEmpty();

        // TODO: Think about if this logic could be dangerous?!
        if (noOfficeUserYet) {
            ldapSyncService.appointPersonAsOfficeUser(person);
        }

        org.springframework.security.ldap.userdetails.Person.Essence ldapUser =
            new org.springframework.security.ldap.userdetails.Person.Essence(ctx);

        ldapUser.setUsername(login);
        ldapUser.setAuthorities(getGrantedAuthorities(person));

        return ldapUser.createUserDetails();
    }


    /**
     * Gets the granted authorities using the {@link Role}s of the given {@link Person}.
     *
     * @param  person  to get the granted authorities for
     *
     * @return  the granted authorities for the person
     */
    Collection<GrantedAuthority> getGrantedAuthorities(Person person) {

        Collection<GrantedAuthority> grantedAuthorities = new ArrayList<>();

        if (person != null) {
            person.getPermissions().stream().forEach(role -> grantedAuthorities.add(role::name));
        }

        return grantedAuthorities;
    }


    @Override
    public void mapUserToContext(UserDetails user, DirContextAdapter ctx) {

        throw new UnsupportedOperationException("LdapUserDetailsMapper only supports reading from a context. Please"
            + "use a subclass if mapUserToContext() is required.");
    }
}
