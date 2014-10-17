/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.synyx.urlaubsverwaltung.security;

import org.apache.log4j.Logger;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.ldap.core.DirContextOperations;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;

import org.synyx.urlaubsverwaltung.core.mail.MailService;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.PersonService;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * @author  Johannes Reuter
 * @author  Aljona Murygina
 */
public class AuthoritiesPopulatorImpl implements LdapAuthoritiesPopulator {

    private static final Logger LOG = Logger.getLogger(AuthoritiesPopulatorImpl.class);

    @Autowired
    private PersonService personService;

    @Autowired
    private MailService mailService;

    @Override
    public Collection<GrantedAuthority> getGrantedAuthorities(DirContextOperations dco, String string) {

        Person person = personService.getPersonByLogin(string);

        // TODO: maybe think about a different solution
        if (person == null && personService.getActivePersons().size() == 0) {
            // if the system has no user yet, the first person that successfully signs in is created as user with office role
            person = new Person();
            person.setLoginName(string);

            List<Role> permissions = new ArrayList<Role>();
            permissions.add(Role.USER);
            permissions.add(Role.OFFICE);

            person.setPermissions(permissions);
            person.setActive(true);

            try {
                KeyPair keyPair = CryptoUtil.generateKeyPair();
                person.setPrivateKey(keyPair.getPrivate().getEncoded());
                person.setPublicKey(keyPair.getPublic().getEncoded());
            } catch (NoSuchAlgorithmException ex) {
                handleCreatingKeysException(string, ex);
            }

            personService.save(person);
        }

        Collection<GrantedAuthority> grantedAuthorities = new ArrayList<>();

        if (person != null) {
            for (Role role : person.getPermissions()) {
                grantedAuthorities.add(new GrantedAuthorityImpl(role.toString()));
            }
        }

        return grantedAuthorities;
    }


    /**
     * Needed for jmx demo: if an exception occurs while public and private key for new user are created, send jmx
     * notifications
     *
     * @param  login
     */
    private void handleCreatingKeysException(String login, Exception ex) {

        LOG.error("Beim Erstellen der Keys für den neuen Benutzer mit dem Login " + login
            + " ist ein Fehler aufgetreten.", ex);
        mailService.sendKeyGeneratingErrorNotification(login);
    }
}
