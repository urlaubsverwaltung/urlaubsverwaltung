package org.synyx.urlaubsverwaltung.core.person;

import lombok.Data;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.springframework.data.jpa.domain.AbstractPersistable;
import org.springframework.util.StringUtils;
import org.synyx.urlaubsverwaltung.core.mail.MailNotification;
import org.synyx.urlaubsverwaltung.security.Role;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;


/**
 * This class describes a person.
 *
 * @author  Johannes Reuter
 * @author  Aljona Murygina
 */

@Entity
@Data
public class Person extends AbstractPersistable<Integer> {

    private static final long serialVersionUID = 765672310978437L;

    private String loginName;

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

    public boolean hasRole(final Role role) {

        return getPermissions().stream().filter(permission -> permission.equals(role)).findFirst().isPresent();
    }


    public boolean isPrivilegedUser() {

        return hasRole(Role.DEPARTMENT_HEAD) || hasRole(Role.BOSS) || hasRole(Role.OFFICE);
    }


    public Collection<MailNotification> getNotifications() {

        if (notifications == null) {
            notifications = new ArrayList<>();
        }

        return notifications;
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
}
