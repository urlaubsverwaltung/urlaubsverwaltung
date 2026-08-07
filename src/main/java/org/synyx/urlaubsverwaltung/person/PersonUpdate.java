package org.synyx.urlaubsverwaltung.person;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

import static java.util.Optional.ofNullable;

/**
 * Describes the data of a {@linkplain Person} to change. Every aspect that is not given stays untouched, which
 * makes it impossible to change data unintentionally - especially data that is not meant to be changed at all,
 * like the creation timestamp of a {@linkplain Person}.
 */
public final class PersonUpdate {

    private final PersonalData personalData;
    private final Collection<Role> permissions;
    private final Collection<MailNotification> notifications;

    private PersonUpdate(PersonalData personalData, Collection<Role> permissions, Collection<MailNotification> notifications) {
        this.personalData = personalData;
        this.permissions = permissions;
        this.notifications = notifications;
    }

    public static PersonUpdate ofPersonalData(String username, String firstName, String lastName, String email) {
        return new PersonUpdate(new PersonalData(username, firstName, lastName, email), null, null);
    }

    public static PersonUpdate ofPermissions(Collection<Role> permissions) {
        return new PersonUpdate(null, permissions, null);
    }

    public static PersonUpdate ofNotifications(Collection<MailNotification> notifications) {
        return new PersonUpdate(null, null, notifications);
    }

    public PersonUpdate withPersonalData(String username, String firstName, String lastName, String email) {
        return new PersonUpdate(new PersonalData(username, firstName, lastName, email), permissions, notifications);
    }

    public PersonUpdate withPermissions(Collection<Role> permissions) {
        return new PersonUpdate(personalData, permissions, notifications);
    }

    public PersonUpdate withNotifications(Collection<MailNotification> notifications) {
        return new PersonUpdate(personalData, permissions, notifications);
    }

    public Optional<PersonalData> personalData() {
        return ofNullable(personalData);
    }

    public Optional<Collection<Role>> permissions() {
        return ofNullable(permissions);
    }

    public Optional<Collection<MailNotification>> notifications() {
        return ofNullable(notifications);
    }

    public record PersonalData(String username, String firstName, String lastName, String email) {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final PersonUpdate that = (PersonUpdate) o;
        return Objects.equals(personalData, that.personalData)
            && Objects.equals(permissions, that.permissions)
            && Objects.equals(notifications, that.notifications);
    }

    @Override
    public int hashCode() {
        return Objects.hash(personalData, permissions, notifications);
    }

    @Override
    public String toString() {
        return "PersonUpdate{" +
            "personalData=" + personalData +
            ", permissions=" + permissions +
            ", notifications=" + notifications +
            '}';
    }
}
