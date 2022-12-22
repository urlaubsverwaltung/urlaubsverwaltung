package org.synyx.urlaubsverwaltung.notification;

import org.springframework.data.jpa.repository.JpaRepository;

interface UserNotificationSettingsRepository extends JpaRepository<UserNotificationSettingsEntity, Long> {
}
