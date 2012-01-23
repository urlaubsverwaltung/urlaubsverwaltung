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

import org.synyx.urlaubsverwaltung.domain.Person;
import org.synyx.urlaubsverwaltung.domain.Role;
import org.synyx.urlaubsverwaltung.service.CryptoService;
import org.synyx.urlaubsverwaltung.service.PersonService;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;

import java.util.ArrayList;
import java.util.Collection;


/**
 * @author  Johannes Reuter
 * @author  Aljona Murygina
 */
public class AuthoritiesPopulatorImpl implements LdapAuthoritiesPopulator {

    private static final Logger LOG = Logger.getLogger(AuthoritiesPopulatorImpl.class);

    private PersonService personService;
    private CryptoService cryptoService;

    @Autowired
    public AuthoritiesPopulatorImpl(PersonService personService, CryptoService cryptoService) {

        this.personService = personService;
        this.cryptoService = cryptoService;
    }

    @Override
    public Collection<GrantedAuthority> getGrantedAuthorities(DirContextOperations dco, String string) {

        Person person = personService.getPersonByLogin(string);
        Collection<GrantedAuthority> output = new ArrayList<GrantedAuthority>();

        if (person == null) {
            // if person isn't a member of the tool yet, a new person is created
            person = new Person();
            person.setLoginName(string);
            person.setRole(Role.USER);

            try {
                KeyPair keyPair = cryptoService.generateKeyPair();
                person.setPrivateKey(keyPair.getPrivate().getEncoded());
                person.setPublicKey(keyPair.getPublic().getEncoded());
            } catch (NoSuchAlgorithmException ex) {
                LOG.error("Beim Erstellen der Keys für den neuen Benutzer ist ein Fehler aufgetreten.", ex);
            }

            personService.save(person);
        }

        output.add(new GrantedAuthorityImpl(person.getRole().getRoleName()));

        return output;
    }
}
