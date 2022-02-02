package org.synyx.urlaubsverwaltung.user;

import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.person.Person;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

@Service
class UserSettingsService {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final UserSettingsRepository userSettingsRepository;

    UserSettingsService(UserSettingsRepository userSettingsRepository) {
        this.userSettingsRepository = userSettingsRepository;
    }

    UserSettings getUserSettingsForPerson(Person person) {
        final UserSettingsEntity entity = findForPersonOrGetDefault(person);
        return toUserSettings(entity);
    }

    UserSettings updateUserThemePreference(Person person, Theme theme) {
        final UserSettingsEntity entity = findForPersonOrGetDefault(person);
        entity.setPersonId(person.getId());
        entity.setPerson(null);
        entity.setTheme(theme);

        final UserSettingsEntity persistedEntity = userSettingsRepository.save(entity);

        return toUserSettings(persistedEntity);
    }

    private UserSettingsEntity findForPersonOrGetDefault(Person person) {
        return userSettingsRepository.findById(person.getId()).orElseGet(() -> defaultUserSettingsEntity(person));
    }

    private UserSettingsEntity defaultUserSettingsEntity(Person person) {
        final UserSettingsEntity userSettingsEntity = new UserSettingsEntity();
        userSettingsEntity.setPerson(person);
        userSettingsEntity.setTheme(Theme.SYSTEM);

        LOG.info("created (not persisted) default userSettingsEntity={}", userSettingsEntity);

        return userSettingsEntity;
    }

    private static UserSettings toUserSettings(UserSettingsEntity userSettingsEntity) {
        return new UserSettings(userSettingsEntity.getTheme());
    }
}
