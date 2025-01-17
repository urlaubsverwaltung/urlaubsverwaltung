package org.synyx.urlaubsverwaltung.notification;

import org.springframework.stereotype.Service;

@Service
public class UserNotificationSettingsImportService {

    private final UserNotificationSettingsRepository userNotificationSettingsRepository;

    UserNotificationSettingsImportService(UserNotificationSettingsRepository userNotificationSettingsRepository) {
        this.userNotificationSettingsRepository = userNotificationSettingsRepository;
    }

    public void deleteAll() {
        userNotificationSettingsRepository.deleteAll();
    }

    public void importUserNotificationSettings(UserNotificationSettingsEntity userNotificationSettingsEntity) {
        userNotificationSettingsRepository.save(userNotificationSettingsEntity);
    }
}
