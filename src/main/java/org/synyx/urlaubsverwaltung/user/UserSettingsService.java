package org.synyx.urlaubsverwaltung.user;

import org.slf4j.Logger;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.person.Person;

import java.util.Locale;
import java.util.Optional;

import static java.lang.invoke.MethodHandles.lookup;
import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;
import static org.slf4j.LoggerFactory.getLogger;

@Service
class UserSettingsService {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final UserSettingsRepository userSettingsRepository;

    UserSettingsService(UserSettingsRepository userSettingsRepository) {
        this.userSettingsRepository = userSettingsRepository;
    }

    UserSettings getUserSettingsForPerson(Person person, Locale defaultLocale) {
        final UserSettingsEntity entity = findForPersonOrGetDefault(person);
        return toUserSettings(entity, defaultLocale);
    }

    Optional<Theme> findThemeForUsername(String username) {
        return userSettingsRepository.findByPersonUsername(username).map(UserSettingsEntity::getTheme);
    }

    Optional<Locale> findLocaleForUsername(String username) {
        return userSettingsRepository.findByPersonUsername(username).map(UserSettingsEntity::getLocale);
    }

    UserSettings updateUserThemePreference(Person person, Theme theme, Locale locale) {
        final UserSettingsEntity entity = findForPersonOrGetDefault(person);
        entity.setPersonId(person.getId());
        entity.setPerson(null);
        entity.setTheme(theme);
        entity.setLocale(locale);

        final UserSettingsEntity persistedEntity = userSettingsRepository.save(entity);
        LocaleContextHolder.setLocale(locale);

        return toUserSettings(persistedEntity);
    }

    private UserSettingsEntity findForPersonOrGetDefault(Person person) {
        return userSettingsRepository.findById(person.getId()).orElseGet(() -> defaultUserSettingsEntity(person));
    }

    private UserSettingsEntity defaultUserSettingsEntity(Person person) {
        final UserSettingsEntity userSettingsEntity = new UserSettingsEntity();
        userSettingsEntity.setPerson(person);
        userSettingsEntity.setTheme(Theme.SYSTEM);

        LOG.debug("created (not persisted) default userSettingsEntity={}", userSettingsEntity);

        return userSettingsEntity;
    }

    private static UserSettings toUserSettings(UserSettingsEntity userSettingsEntity) {
        return new UserSettings(userSettingsEntity.getTheme(), requireNonNull(userSettingsEntity.getLocale()));
    }

    private static UserSettings toUserSettings(UserSettingsEntity userSettingsEntity, Locale defaultLocale) {
        return new UserSettings(userSettingsEntity.getTheme(), requireNonNullElse(userSettingsEntity.getLocale(), defaultLocale));
    }
}
