package org.synyx.urlaubsverwaltung.security;

import org.apache.log4j.Logger;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.ldap.userdetails.UserDetailsContextMapper;

import org.springframework.util.Assert;

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

    private final String identifierAttribute;
    private final String lastNameAttribute;
    private final String firstNameAttribute;
    private final String mailAddressAttribute;

    public PersonContextMapper(PersonService personService, MailService mailService,
        @Value("security.identifier") String identifierAttribute,
        @Value("security.lastName") String lastNameAttribute,
        @Value("security.firstName") String firstNameAttribute,
        @Value("security.mailAddress") String mailAddressAttribute) {

        this.personService = personService;
        this.mailService = mailService;

        this.identifierAttribute = identifierAttribute;
        this.lastNameAttribute = lastNameAttribute;
        this.firstNameAttribute = firstNameAttribute;
        this.mailAddressAttribute = mailAddressAttribute;
    }

    @Override
    public UserDetails mapUserFromContext(DirContextOperations ctx, String username,
        Collection<? extends GrantedAuthority> authorities) {

        String userIdentifier = ctx.getStringAttribute(identifierAttribute);
        String login = userIdentifier == null ? username : userIdentifier;

        Optional<Person> optionalPerson = personService.getPersonByLogin(login);

        /**
         * NOTE: If the system has no user yet, the first person that successfully signs in
         * is created as user with {@link Role#OFFICE}
         */
        boolean noActivePersonExistsYet = personService.getActivePersons().isEmpty();

        Optional<String> lastName = Optional.ofNullable(ctx.getStringAttribute(lastNameAttribute));
        Optional<String> firstName = Optional.ofNullable(ctx.getStringAttribute(firstNameAttribute));
        Optional<String> mailAddress = Optional.ofNullable(ctx.getStringAttribute(mailAddressAttribute));

        Person person;

        if (optionalPerson.isPresent()) {
            person = syncPerson(optionalPerson.get(), firstName, lastName, mailAddress);
        } else {
            person = createPerson(login, firstName, lastName, mailAddress, noActivePersonExistsYet);
        }

        org.springframework.security.ldap.userdetails.Person.Essence user =
            new org.springframework.security.ldap.userdetails.Person.Essence(ctx);

        user.setUsername(login);
        user.setAuthorities(getGrantedAuthorities(person));

        return user.createUserDetails();
    }


    /**
     * Sync the data of the given {@link Person}.
     *
     * @param  person  to update the attributes for
     * @param  firstName  to be updated, is optional
     * @param  lastName  to be updated, is optional
     * @param  mailAddress  to be updated, is optional
     *
     * @return  the updated person
     */
    Person syncPerson(Person person, Optional<String> firstName, Optional<String> lastName,
        Optional<String> mailAddress) {

        firstName.ifPresent(person::setFirstName);
        lastName.ifPresent(person::setLastName);
        mailAddress.ifPresent(person::setEmail);

        personService.save(person);

        LOG.info("Successfully synced person data: " + person.toString());

        return person;
    }


    /**
     * Creates a {@link Person} with the role {@link Role#USER} resp. with the roles {@link Role#USER} and
     * {@link Role#OFFICE} if this is the first person that is created.
     *
     * @param  login  of the person to be created, is mandatory to create a person
     * @param  firstName  of the person to be created, is optional
     * @param  lastName  of the person to be created, is optional
     * @param  mailAddress  of the person to be created, is optional
     * @param  isFirst  describes if this is the first person that is created, if {@code true} then the person gets the
     *                  role {@link Role#OFFICE}
     *
     * @return  the created person
     */
    Person createPerson(String login, Optional<String> firstName, Optional<String> lastName,
        Optional<String> mailAddress, boolean isFirst) {

        Assert.notNull(login, "Missing login name!");

        Person person = new Person();
        person.setLoginName(login);

        firstName.ifPresent(person::setFirstName);
        lastName.ifPresent(person::setLastName);
        mailAddress.ifPresent(person::setEmail);

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
            person.getPermissions().stream().forEach(role -> grantedAuthorities.add(role::toString));
        }

        return grantedAuthorities;
    }


    @Override
    public void mapUserToContext(UserDetails user, DirContextAdapter ctx) {

        throw new UnsupportedOperationException("LdapUserDetailsMapper only supports reading from a context. Please"
            + "use a subclass if mapUserToContext() is required.");
    }
}
