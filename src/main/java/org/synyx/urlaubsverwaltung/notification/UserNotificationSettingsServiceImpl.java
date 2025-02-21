package org.synyx.urlaubsverwaltung.notification;

import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.person.PersonId;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.function.Function.identity;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toMap;

@Service
class UserNotificationSettingsServiceImpl implements UserNotificationSettingsService {

    private final UserNotificationSettingsRepository repository;

    UserNotificationSettingsServiceImpl(UserNotificationSettingsRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserNotificationSettings findNotificationSettings(PersonId personId) {
        return repository.findById(personId.value()).map(UserNotificationSettingsServiceImpl::toNotification)
            .orElseGet(() -> defaultNotificationSettings(personId));
    }

    @Override
    public Map<PersonId, UserNotificationSettings> findNotificationSettings(Collection<PersonId> personIds) {

        final List<Long> personIdValues = personIds.stream().map(PersonId::value).toList();

        final Map<PersonId, UserNotificationSettings> notificationsByPerson = repository.findAllById(personIdValues).stream()
            .map(UserNotificationSettingsServiceImpl::toNotification)
            .collect(toMap(UserNotificationSettings::personId, identity(), (userNotificationSettings, userNotificationSettings2) -> userNotificationSettings));

        final Stream<UserNotificationSettings> defaultNotificationSettings = personIds.stream()
            .filter(not(notificationsByPerson::containsKey))
            .map(UserNotificationSettingsServiceImpl::defaultNotificationSettings);

        return Stream.concat(notificationsByPerson.values().stream(), defaultNotificationSettings)
            .collect(toMap(UserNotificationSettings::personId, identity(), (userNotificationSettings, userNotificationSettings2) -> userNotificationSettings));
    }

    @Override
    public UserNotificationSettings updateNotificationSettings(PersonId personId, boolean restrictToDepartments) {

        final UserNotificationSettingsEntity entity = new UserNotificationSettingsEntity();
        entity.setPersonId(personId.value());
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
