/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.synyx.ausbildung.adressbuch.security;

import org.springframework.security.core.GrantedAuthority;


/**
 * @author  aljona
 */
public class UserAuthority implements GrantedAuthority {

    private static final long serialVersionUID = 1L;

    public String getAuthority() {

        return "ROLE_USER";
    }
}
