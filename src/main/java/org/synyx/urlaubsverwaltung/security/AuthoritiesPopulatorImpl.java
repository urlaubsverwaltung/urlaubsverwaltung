/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.synyx.urlaubsverwaltung.security;

import java.util.ArrayList;
import java.util.Collection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;
import org.synyx.urlaubsverwaltung.dao.PersonDAO;
import org.synyx.urlaubsverwaltung.domain.Person;
import org.synyx.urlaubsverwaltung.domain.Role;

/**
 *
 * @author johannes
 */
public class AuthoritiesPopulatorImpl implements LdapAuthoritiesPopulator {
    
    private PersonDAO personDAO;
    
    @Autowired
    public AuthoritiesPopulatorImpl(PersonDAO personDAO) {
        this.personDAO = personDAO;
    }

    @Override
    public Collection<GrantedAuthority> getGrantedAuthorities(DirContextOperations dco, String string) {
        Person person = personDAO.getPersonByLogin(string);
        Collection<GrantedAuthority> output= new ArrayList<GrantedAuthority>();
        if(person==null) {
            //soll das so bleiben? hmmm, weiß net... überlegen....
            output.add(new GrantedAuthorityImpl(Role.USER.getRoleName()));
        } else {
            output.add(new GrantedAuthorityImpl(person.getRole().getRoleName()));
        }
        return output;
    }
    
}
