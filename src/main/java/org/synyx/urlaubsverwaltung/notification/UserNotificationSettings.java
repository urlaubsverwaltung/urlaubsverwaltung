package org.synyx.urlaubsverwaltung.notification;

import org.synyx.urlaubsverwaltung.person.PersonId;

/**
 * Info about the notification configuration of a person.
 */
public record UserNotificationSettings(PersonId personId, boolean restrictToDepartments) {

    @Override
    public String toString() {
        return "Notification{" +
            "personId=" + personId +
            ", restrictToDepartments=" + restrictToDepartments +
            '}';
    }
}
