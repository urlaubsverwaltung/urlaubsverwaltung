/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.synyx.ausbildung.adressbuch.security;

import org.springframework.security.authentication.dao.SaltSource;
import org.springframework.security.core.userdetails.UserDetails;


/**
 * @author  aljona
 */
public class UserSaltSource implements SaltSource {

    public Object getSalt(UserDetails ud) {

        return ud.getUsername();
    }
}
