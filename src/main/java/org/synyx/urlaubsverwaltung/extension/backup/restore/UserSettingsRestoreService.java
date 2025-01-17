package org.synyx.urlaubsverwaltung.extension.backup.restore;

import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.extension.backup.model.UserSettingsDTO;
import org.synyx.urlaubsverwaltung.notification.UserNotificationSettingsImportService;
import org.synyx.urlaubsverwaltung.user.UserSettingsImportService;
import org.synyx.urlaubsverwaltung.user.pagination.UserPaginationSettingsImportService;

@Service
@ConditionalOnBackupRestoreEnabled
class UserSettingsRestoreService {

    private final UserSettingsImportService userSettingsImportService;
    private final UserPaginationSettingsImportService userPaginationSettingsImportService;
    private final UserNotificationSettingsImportService userNotificationSettingsImportService;

    UserSettingsRestoreService(UserSettingsImportService userSettingsImportService, UserPaginationSettingsImportService userPaginationSettingsImportService, UserNotificationSettingsImportService userNotificationSettingsImportService) {
        this.userSettingsImportService = userSettingsImportService;
        this.userPaginationSettingsImportService = userPaginationSettingsImportService;
        this.userNotificationSettingsImportService = userNotificationSettingsImportService;
    }

    void importUserSettings(Long personId, UserSettingsDTO userSettingsDTO) {
        userSettingsImportService.importUserSettings(userSettingsDTO.toUserSettingsEntity(personId));
        userPaginationSettingsImportService.importUserPaginationSettings(userSettingsDTO.userPaginationSettings().toUserPaginationSettingsEntity(personId));
        userNotificationSettingsImportService.importUserNotificationSettings(userSettingsDTO.userNotificationSettings().toUserNotificationSettingsEntity(personId));
    }
}
