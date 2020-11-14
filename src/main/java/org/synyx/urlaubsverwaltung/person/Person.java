package org.synyx.urlaubsverwaltung.person;

import org.hibernate.annotations.LazyCollection;
import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import java.util.Collection;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableCollection;
import static javax.persistence.EnumType.STRING;
import static org.hibernate.annotations.LazyCollectionOption.FALSE;
import static org.springframework.util.StringUtils.hasText;

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
    @LazyCollection(FALSE)
    @Enumerated(STRING)
    private Collection<Role> permissions;

    @ElementCollection
    @LazyCollection(FALSE)
    @Enumerated(STRING)
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
            return emptyList();
        }

        return unmodifiableCollection(permissions);
    }

    public boolean hasRole(final Role role) {
        return getPermissions().stream()
            .anyMatch(permission -> permission.equals(role));
    }

    public Collection<MailNotification> getNotifications() {
        if (notifications == null) {
            notifications = emptyList();
        }

        return unmodifiableCollection(notifications);
    }

    public void setNotifications(Collection<MailNotification> notifications) {
        this.notifications = notifications;
    }

    public boolean hasNotificationType(final MailNotification notification) {
        return getNotifications().stream()
            .anyMatch(element -> element.equals(notification));
    }

    public String getNiceName() {

        final StringBuilder builder = new StringBuilder();
        if (hasText(this.firstName)) {
            builder.append(this.firstName);
            builder.append(" ");
        }
        if (hasText(this.lastName)) {
            builder.append(this.lastName);
        }

        final String niceName = builder.toString().trim();
        if (!hasText(niceName)) {
            return "---";
        }

        return niceName;
    }

    public String getGravatarURL() {
        if (hasText(this.email)) {
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
