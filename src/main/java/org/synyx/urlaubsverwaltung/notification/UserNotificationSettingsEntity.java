package org.synyx.urlaubsverwaltung.notification;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.synyx.urlaubsverwaltung.tenancy.tenant.AbstractTenantAwareEntity;

import java.util.Objects;

@Entity
@Table(name = "user_notification_settings")
public class UserNotificationSettingsEntity extends AbstractTenantAwareEntity {

    @Id
    private Long personId;
    private boolean restrictToDepartments;

    public Long getPersonId() {
        return personId;
    }

    public void setPersonId(Long personId) {
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
