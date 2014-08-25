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
        Collection<GrantedAuthority> output = new ArrayList<GrantedAuthority>();

        if (person == null) {
            // if person isn't a member of the tool yet, a new person is created
            person = new Person();
            person.setLoginName(string);

            List<Role> permissions = new ArrayList<Role>();
            permissions.add(Role.USER);

            // there has to be at least one user with office role to be able to set rights to users
            // so the first person that logins, will be office
            // TODO: think about a different solution...
            if (personService.getPersonsByRole(Role.OFFICE).size() == 0) {
                permissions.add(Role.OFFICE);
            }

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

        for (Role role : person.getPermissions()) {
            output.add(new GrantedAuthorityImpl(role.toString()));
        }

        return output;
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
