package org.synyx.urlaubsverwaltung.person;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.springframework.data.jpa.domain.AbstractPersistable;
import org.springframework.util.StringUtils;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.util.Collection;
import java.util.Collections;


/**
 * This class describes a person.
 */
@Entity
public class Person extends AbstractPersistable<Integer> {

    private static final long serialVersionUID = 765672310978437L;

    private String username;
    private String password;
    private String lastName;
    private String firstName;
    private String email;

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

    public Person(String username, String lastName, String firstName, String email) {
        this.username = username;
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setPermissions(Collection<Role> permissions) {
        this.permissions = permissions;
    }

    public Collection<Role> getPermissions() {
        if (permissions == null) {
            return Collections.emptyList();
        }

        return Collections.unmodifiableCollection(permissions);
    }

    public boolean hasRole(final Role role) {
        return getPermissions().stream().anyMatch(permission -> permission.equals(role));
    }

    public Collection<MailNotification> getNotifications() {
        if (notifications == null) {
            notifications = Collections.emptyList();
        }

        return Collections.unmodifiableCollection(notifications);
    }


    public void setNotifications(Collection<MailNotification> notifications) {
        this.notifications = notifications;
    }


    public boolean hasNotificationType(final MailNotification notification) {
        return getNotifications().stream().anyMatch(element -> element.equals(notification));
    }


    public String getNiceName() {

        StringBuilder builder = new StringBuilder();
        if (StringUtils.hasText(this.firstName)) {
            builder.append(this.firstName);
            builder.append(" ");
        }
        if (StringUtils.hasText(this.lastName)) {
            builder.append(this.lastName);
        }
        String nicename = builder.toString().trim();

        if (!StringUtils.hasText(nicename)) {
            return "---";
        }

        return nicename;
    }

    public String getGravatarURL() {
        if (StringUtils.hasText(this.email)) {
            return GravatarUtil.createImgURL(this.email);
        }

        return "";
    }


    @Override
    public void setId(Integer id) { // NOSONAR - needed for setting ID in form
        super.setId(id);
    }

    @Override
    public String toString() {
        return "Person{id='" + super.getId() + "'}";
    }
}
