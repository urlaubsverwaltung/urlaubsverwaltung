package org.synyx.urlaubsverwaltung.domain;

import org.springframework.data.jpa.domain.AbstractPersistable;

import java.io.UnsupportedEncodingException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.Entity;


/**
 * @author  aljona
 */

@Entity
public class Person extends AbstractPersistable<Integer> {

    private static final long serialVersionUID = 1L;

    private String loginName;

    private String lastName;

    private String firstName;

    private String email;

    // Unterschrift der Person
    private Image sign;

    private Role role;

    // eventuell fuer die Mitarbeiter-Liste - nur als Idee bisher
    // private Image userPicture;

    public String getLoginName() {

        return loginName;
    }


    public void setLoginName(String loginName) {

        this.loginName = loginName;
    }


    public String getEmail() {

        return email;
    }


    public void setEmail(String email) {

        this.email = email;
    }


    public String getEMailHash() {

        byte[] bytesOfMessage;
        byte[] theDigest;

        try {
            bytesOfMessage = email.trim().toLowerCase(Locale.ENGLISH).getBytes("UTF-8");

            MessageDigest md = MessageDigest.getInstance("MD5");
            theDigest = md.digest(bytesOfMessage);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(Person.class.getName()).log(Level.SEVERE, null, ex);

            return "";
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(Person.class.getName()).log(Level.SEVERE, null, ex);

            return "";
        }

        return new String(theDigest);
    }


    public String getFirstName() {

        return firstName;
    }


    public void setFirstName(String firstName) {

        this.firstName = firstName;
    }


    public String getLastName() {

        return lastName;
    }


    public void setLastName(String lastName) {

        this.lastName = lastName;
    }


    public Image getSign() {

        return sign;
    }


    public void setSign(Image sign) {

        this.sign = sign;
    }

    public Role getRole() {

        return role;
    }

    public void setRole(Role role) {

        this.role = role;
    }

}
