package org.synyx.urlaubsverwaltung.user;

import org.slf4j.Logger;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.person.Person;

import java.util.Locale;
import java.util.Optional;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

@Service
class UserSettingsService {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final UserSettingsRepository userSettingsRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    UserSettingsService(UserSettingsRepository userSettingsRepository, ApplicationEventPublisher applicationEventPublisher) {
        this.userSettingsRepository = userSettingsRepository;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    UserSettings getUserSettingsForPerson(Person person) {
        final UserSettingsEntity entity = findForPersonOrGetDefault(person);
        return toUserSettings(entity);
    }

    Optional<Theme> findThemeForUsername(String username) {
        return userSettingsRepository.findByPersonUsername(username).map(UserSettingsEntity::getTheme);
    }

    Optional<Locale> findLocaleForUsername(String username) {
        return userSettingsRepository.findByPersonUsername(username).map(UserSettingsEntity::getLocale);
    }

    /**
     * @param person the person to update the {@link UserSettings} for.
     * @param theme  the {@link Theme} for the person.
     * @param locale the locale to set for the person. must be a {@link SupportedLocale} or {@code null} to use the user-agent as fallback.
     * @return the updated {@link UserSettings}
     */
    UserSettings updateUserThemePreference(Person person, Theme theme, @Nullable Locale locale) {
        final UserSettingsEntity entity = findForPersonOrGetDefault(person);
        entity.setPersonId(person.getId());
        entity.setPerson(null);
        entity.setTheme(theme);
        entity.setLocale(locale);

        final UserSettingsEntity persistedEntity = userSettingsRepository.save(entity);
        applicationEventPublisher.publishEvent(new UserLocaleChangedEvent(persistedEntity.getLocale()));

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
        return new UserSettings(userSettingsEntity.getTheme(), userSettingsEntity.getLocale());
    }
}
