package org.synyx.urlaubsverwaltung.extension.backup.model;

import org.synyx.urlaubsverwaltung.notification.UserNotificationSettingsEntity;

public record UserNotificationSettingsDTO(boolean restrictToDepartments) {
    public UserNotificationSettingsEntity toUserNotificationSettingsEntity(Long personId) {
        final UserNotificationSettingsEntity entity = new UserNotificationSettingsEntity();
        entity.setPersonId(personId);
        entity.setRestrictToDepartments(this.restrictToDepartments);
        return entity;
    }
}
