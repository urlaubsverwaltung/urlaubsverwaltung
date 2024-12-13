package org.synyx.urlaubsverwaltung.extension.backup.model;

import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.notification.UserNotificationSettingsEntity;

import static org.assertj.core.api.Assertions.assertThat;

class UserNotificationSettingsDTOTest {

    @Test
    void happyPath() {
        UserNotificationSettingsDTO dto = new UserNotificationSettingsDTO(true);
        UserNotificationSettingsEntity entity = dto.toUserNotificationSettingsEntity(1L);

        assertThat(entity.getPersonId()).isEqualTo(1L);
        assertThat(entity.isRestrictToDepartments()).isEqualTo(dto.restrictToDepartments());
    }

}
