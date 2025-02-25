package org.synyx.urlaubsverwaltung.person;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import org.synyx.urlaubsverwaltung.tenancy.tenant.AbstractTenantAwareEntity;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Stream;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.FetchType.EAGER;
import static jakarta.persistence.GenerationType.SEQUENCE;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableCollection;
import static org.springframework.util.StringUtils.hasText;
import static org.synyx.urlaubsverwaltung.person.Role.INACTIVE;
import static org.synyx.urlaubsverwaltung.person.Role.privilegedRoles;

/**
 * This class describes a person.
 */
@Entity
public class Person extends AbstractTenantAwareEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false, updatable = false)
    @GeneratedValue(strategy = SEQUENCE, generator = "person_generator")
    @SequenceGenerator(name = "person_generator", sequenceName = "person_id_seq")
    private Long id;

    @Column(nullable = false)
    private String username;
    @Column(nullable = false)
    private String lastName;
    @Column(nullable = false)
    private String firstName;
    private String email;

    @ElementCollection(fetch = EAGER)
    @Enumerated(STRING)
    private Collection<Role> permissions;

    @ElementCollection(fetch = EAGER)
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
        return hasAnyRole(role);
    }

    public boolean hasAnyRole(final Role... role) {
        return Stream.of(role).anyMatch(getPermissions()::contains);
    }

    public boolean isInactive() {
        return hasRole(INACTIVE);
    }

    public boolean isActive() {
        return !isInactive();
    }

    public boolean isPrivileged() {
        return getPermissions().stream()
            .anyMatch(role -> privilegedRoles().contains(role));
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

    public String getInitials() {

        final String niceName = getNiceName();

        final int idxLastWhitespace = niceName.lastIndexOf(' ');
        if (idxLastWhitespace == -1) {
            return niceName.substring(0, 1).toUpperCase();
        }

        return (niceName.charAt(0) + niceName.substring(idxLastWhitespace + 1, idxLastWhitespace + 2)).toUpperCase();
    }

    @Override
    public String toString() {
        return "Person{id='" + getId() + "'}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Person that = (Person) o;
        return null != this.getId() && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
