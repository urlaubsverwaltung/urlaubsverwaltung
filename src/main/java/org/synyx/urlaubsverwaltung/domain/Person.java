package org.synyx.urlaubsverwaltung.domain;

import org.springframework.data.jpa.domain.AbstractPersistable;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.OneToMany;


/**
 * @author  Johannes Reuter
 * @author  Aljona Murygina
 */

@Entity
public class Person extends AbstractPersistable<Integer> {

    private static final long serialVersionUID = 765672310978437L;

    private String loginName;

    private String lastName;

    private String firstName;

    private String email;

    // private key of person - RSA
    // private key has to be saved as byte[] in database
    // when retrieved from database, byte[] have to be transformed back to private key
    private byte[] privateKey;

    // public key of person
    // saving like private key
    private byte[] publicKey;

    private Role role;

    @OneToMany
    private List<HolidayEntitlement> entitlements;

    @OneToMany
    private List<HolidaysAccount> accounts;

    @OneToMany
    private List<Application> applications;

    private boolean active;

    public boolean isActive() {

        return active;
    }


    public void setActive(boolean active) {

        this.active = active;
    }


    public List<HolidaysAccount> getAccounts() {

        return accounts;
    }


    public void setAccounts(List<HolidaysAccount> accounts) {

        this.accounts = accounts;
    }


    public List<Application> getApplications() {

        return applications;
    }


    public void setApplications(List<Application> applications) {

        this.applications = applications;
    }


    public String getEmail() {

        return email;
    }


    public void setEmail(String email) {

        this.email = email;
    }


    public List<HolidayEntitlement> getEntitlements() {

        return entitlements;
    }


    public void setEntitlements(List<HolidayEntitlement> entitlements) {

        this.entitlements = entitlements;
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


    public String getLoginName() {

        return loginName;
    }


    public void setLoginName(String loginName) {

        this.loginName = loginName;
    }


    public byte[] getPrivateKey() {

        return privateKey.clone();
    }


    public void setPrivateKey(byte[] privateKey) {

        this.privateKey = privateKey.clone();
    }


    public byte[] getPublicKey() {

        return publicKey.clone();
    }


    public void setPublicKey(byte[] publicKey) {

        this.publicKey = publicKey.clone();
    }


    public Role getRole() {

        return role;
    }


    public void setRole(Role role) {

        this.role = role;
    }
}
