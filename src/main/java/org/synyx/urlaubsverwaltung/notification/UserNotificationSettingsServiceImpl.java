package org.synyx.urlaubsverwaltung.notification;

import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.person.PersonId;

@Service
class UserNotificationSettingsServiceImpl implements UserNotificationSettingsService {

    private final UserNotificationSettingsRepository repository;

    UserNotificationSettingsServiceImpl(UserNotificationSettingsRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserNotificationSettings findNotificationSettings(PersonId personId) {
        return repository.findById(personId.getValue()).map(UserNotificationSettingsServiceImpl::toNotification)
            .orElseGet(() -> defaultNotificationSettings(personId));
    }

    @Override
    public UserNotificationSettings updateNotificationSettings(PersonId personId, boolean restrictToDepartments) {

        final UserNotificationSettingsEntity entity = new UserNotificationSettingsEntity();
        entity.setPersonId(personId.getValue());
        entity.setRestrictToDepartments(restrictToDepartments);

        return toNotification(repository.save(entity));
    }

    private static UserNotificationSettings defaultNotificationSettings(PersonId personId) {
        return new UserNotificationSettings(personId, false);
    }

    private static UserNotificationSettings toNotification(UserNotificationSettingsEntity entity) {
        return new UserNotificationSettings(new PersonId(entity.getPersonId()), entity.isRestrictToDepartments());
    }
}
