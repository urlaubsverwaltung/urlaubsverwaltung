/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.synyx.urlaubsverwaltung.domain;

import org.springframework.data.jpa.domain.AbstractPersistable;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import org.synyx.ausbildung.adressbuch.security.UserAuthority;

import java.util.ArrayList;
import java.util.Collection;

import javax.persistence.Column;
import javax.persistence.Entity;


/**
 * @author  aljona
 */

@Entity
public class User extends AbstractPersistable<Integer> implements UserDetails {

    private static final long serialVersionUID = 1L;

    @Column(unique = true)
    private String login;

    private String password;

    public String getLogin() {

        return login;
    }


    public void setLogin(String login) {

        this.login = login;
    }


    public String getPassword() {

        return password;
    }


    public void setPassword(String password) {

        this.password = password;
    }


    public Collection<GrantedAuthority> getAuthorities() {

        Collection<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new UserAuthority());

        return authorities;
    }


    public String getUsername() {

        return getLogin();
    }


    public boolean isAccountNonExpired() {

        return true;
    }


    public boolean isAccountNonLocked() {

        return true;
    }


    public boolean isCredentialsNonExpired() {

        return true;
    }


    public boolean isEnabled() {

        return true;
    }
}
