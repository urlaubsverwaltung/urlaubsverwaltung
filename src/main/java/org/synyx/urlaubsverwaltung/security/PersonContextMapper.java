package org.synyx.urlaubsverwaltung.security;

import org.apache.log4j.Logger;

import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.ldap.userdetails.UserDetailsContextMapper;

import org.synyx.urlaubsverwaltung.core.mail.MailService;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.PersonService;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;


/**
 * Map granted authorities to application roles described in {@link org.synyx.urlaubsverwaltung.security.Role}.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class PersonContextMapper implements UserDetailsContextMapper {

    private static final Logger LOG = Logger.getLogger(PersonContextMapper.class);

    private final PersonService personService;
    private final MailService mailService;

    public PersonContextMapper(PersonService personService, MailService mailService) {

        this.personService = personService;
        this.mailService = mailService;
    }

    @Override
    public UserDetails mapUserFromContext(DirContextOperations ctx, String username,
        Collection<? extends GrantedAuthority> authorities) {

        Optional<Person> optionalPerson = personService.getPersonByLogin(username);

        /**
         * NOTE: If the system has no user yet, the first person that successfully signs in
         * is created as user with {@link Role#OFFICE}
         */
        boolean noActivePersonExistsYet = personService.getActivePersons().isEmpty();

        Person person;

        if (!optionalPerson.isPresent()) {
            person = createPerson(username, noActivePersonExistsYet);
        } else {
            person = optionalPerson.get();
        }

        org.springframework.security.ldap.userdetails.Person.Essence ldapUser =
            new org.springframework.security.ldap.userdetails.Person.Essence(ctx);

        ldapUser.setUsername(username);
        ldapUser.setAuthorities(getGrantedAuthorities(person));

        return ldapUser.createUserDetails();
    }


    /**
     * Creates a {@link Person} with the roles {@link Role#OFFICE} and {@link Role#USER}.
     *
     * @param  login  of the person to be created
     *
     * @return  the created person
     */
    Person createPerson(String login, boolean isFirst) {

        Person person = new Person();
        person.setLoginName(login);

        List<Role> permissions = new ArrayList<>();
        permissions.add(Role.USER);

        /**
         * NOTE: the first created person should be able to manage persons and their roles in the application
         */
        if (isFirst) {
            permissions.add(Role.OFFICE);
        }

        /**
         * NOTE: Do not change this to
         *
         * <pre>
         *     person.setPermissions(Arrays.asList(Role.USER, Role.OFFICE));
         * </pre>
         *
         * if you don't want to have errors!
         *
         */
        person.setPermissions(permissions);

        try {
            KeyPair keyPair = CryptoUtil.generateKeyPair();
            person.setPrivateKey(keyPair.getPrivate().getEncoded());
            person.setPublicKey(keyPair.getPublic().getEncoded());
        } catch (NoSuchAlgorithmException ex) {
            LOG.error("An error occurred while trying to create key pair for user with login " + login, ex);
            mailService.sendKeyGeneratingErrorNotification(login, ex.getMessage());
        }

        personService.save(person);

        LOG.info("Successfully auto-created person: " + person.toString());

        return person;
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
            for (final Role role : person.getPermissions()) {
                grantedAuthorities.add(() -> role.name());
            }
        }

        return grantedAuthorities;
    }


    @Override
    public void mapUserToContext(UserDetails user, DirContextAdapter ctx) {

        throw new UnsupportedOperationException("LdapUserDetailsMapper only supports reading from a context. Please"
            + "use a subclass if mapUserToContext() is required.");
    }
}
