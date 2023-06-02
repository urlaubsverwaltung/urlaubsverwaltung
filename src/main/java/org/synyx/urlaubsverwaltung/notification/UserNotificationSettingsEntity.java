package org.synyx.urlaubsverwaltung.notification;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Objects;

@Entity
@Table(name = "user_notification_settings")
class UserNotificationSettingsEntity {

    @Id
    private Integer personId;
    private boolean restrictToDepartments;

    public Integer getPersonId() {
        return personId;
    }

    public void setPersonId(Integer personId) {
        this.personId = personId;
    }

    public boolean isRestrictToDepartments() {
        return restrictToDepartments;
    }

    public void setRestrictToDepartments(boolean restrictToDepartments) {
        this.restrictToDepartments = restrictToDepartments;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserNotificationSettingsEntity that = (UserNotificationSettingsEntity) o;
        return Objects.equals(personId, that.personId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(personId);
    }

    @Override
    public String toString() {
        return "NotificationEntity{" +
            "personId=" + personId +
            ", restrictToDepartments=" + restrictToDepartments +
            '}';
    }
}
