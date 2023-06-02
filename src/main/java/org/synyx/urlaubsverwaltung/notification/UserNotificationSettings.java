package org.synyx.urlaubsverwaltung.notification;

import org.synyx.urlaubsverwaltung.person.PersonId;

import java.util.Objects;

/**
 * Info about the notification configuration of a person.
 */
public final class UserNotificationSettings {

    private final PersonId personId;
    private final boolean restrictToDepartments;

    public UserNotificationSettings(PersonId personId, boolean restrictToDepartments) {
        this.personId = personId;
        this.restrictToDepartments = restrictToDepartments;
    }

    public PersonId getPersonId() {
        return personId;
    }

    public boolean isRestrictToDepartments() {
        return restrictToDepartments;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserNotificationSettings that = (UserNotificationSettings) o;
        return restrictToDepartments == that.restrictToDepartments && Objects.equals(personId, that.personId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(personId, restrictToDepartments);
    }

    @Override
    public String toString() {
        return "Notification{" +
            "personId=" + personId +
            ", restrictToDepartments=" + restrictToDepartments +
            '}';
    }
}
