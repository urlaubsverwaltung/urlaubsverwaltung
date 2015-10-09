package org.synyx.urlaubsverwaltung.core.person;

import com.google.common.base.MoreObjects;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import org.springframework.data.jpa.domain.AbstractPersistable;

import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import javax.persistence.*;


/**
 * This class describes a person.
 *
 * @author  Johannes Reuter
 * @author  Aljona Murygina
 */

@Entity
public class Person extends AbstractPersistable<Integer> {

    private static final long serialVersionUID = 765672310978437L;

    private String loginName;

    private String password;

    private String lastName;

    private String firstName;

    private String email;

    // private key of person - RSA
    // private key has to be saved as byte[] in database
    // when retrieved from database, byte[] have to be transformed back to private key
    @Column(columnDefinition = "longblob")
    private byte[] privateKey;

    // public key of person
    // saving like private key
    @Column(columnDefinition = "longblob")
    private byte[] publicKey;

    @ElementCollection
    @LazyCollection(LazyCollectionOption.FALSE)
    @Enumerated(EnumType.STRING)
    private Collection<Role> permissions;

    @ElementCollection
    @LazyCollection(LazyCollectionOption.FALSE)
    @Enumerated(EnumType.STRING)
    private Collection<MailNotification> notifications;

    public Person() {

        /* OK */
    }


    public Person(String loginName, String lastName, String firstName, String email) {

        this.loginName = loginName;
        this.lastName = lastName;
        this.firstName = firstName;
        this.email = email;
    }

    public String getEmail() {

        return email;
    }


    public void setEmail(String email) {

        this.email = email;
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


    public String getPassword() {

        return password;
    }


    public void setPassword(String password) {

        this.password = password;
    }


    public byte[] getPrivateKey() {

        if (privateKey == null) {
            return null;
        }

        return Arrays.copyOf(privateKey, privateKey.length);
    }


    public void setPrivateKey(byte[] privateKey) {

        if (privateKey != null) {
            this.privateKey = Arrays.copyOf(privateKey, privateKey.length);
        } else {
            this.privateKey = null;
        }
    }


    public byte[] getPublicKey() {

        if (publicKey == null) {
            return null;
        }

        return Arrays.copyOf(publicKey, publicKey.length);
    }


    public void setPublicKey(byte[] publicKey) {

        if (publicKey != null) {
            this.publicKey = Arrays.copyOf(publicKey, publicKey.length);
        } else {
            this.publicKey = null;
        }
    }


    public void setPermissions(Collection<Role> permissions) {

        this.permissions = permissions;
    }


    public Collection<Role> getPermissions() {

        return permissions;
    }


    public boolean hasRole(final Role role) {

        return getPermissions().stream().filter(permission -> permission.equals(role)).findFirst().isPresent();
    }


    public Collection<MailNotification> getNotifications() {

        if (notifications == null) {
            notifications = new ArrayList<>();
        }

        return notifications;
    }


    public void setNotifications(Collection<MailNotification> notifications) {

        this.notifications = notifications;
    }


    public boolean hasNotificationType(final MailNotification notification) {

        return getNotifications().stream().filter(element -> element.equals(notification)).findFirst().isPresent();
    }


    public String getNiceName() {

        if (StringUtils.hasText(this.firstName) && StringUtils.hasText(this.lastName)) {
            return this.firstName + " " + this.lastName;
        }

        return this.loginName;
    }


    @Override
    public String toString() {

        MoreObjects.ToStringHelper toStringHelper = MoreObjects.toStringHelper(this);

        toStringHelper.add("id", getId());
        toStringHelper.add("loginName", getLoginName());
        toStringHelper.add("lastName", getLastName());
        toStringHelper.add("firstName", getFirstName());
        toStringHelper.add("email", getEmail());
        toStringHelper.add("permissions", getPermissions());

        return toStringHelper.toString();
    }
}
