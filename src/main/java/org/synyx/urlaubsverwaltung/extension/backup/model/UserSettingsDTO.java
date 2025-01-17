package org.synyx.urlaubsverwaltung.extension.backup.model;

import org.synyx.urlaubsverwaltung.user.UserSettingsEntity;

import java.util.Locale;

public record UserSettingsDTO(ThemeDTO theme, Locale locale, Locale browserSpecific,
                              UserNotificationSettingsDTO userNotificationSettings,
                              UserPaginationSettingsDTO userPaginationSettings) {

    public UserSettingsEntity toUserSettingsEntity(Long personId) {
        UserSettingsEntity entity = new UserSettingsEntity();
        entity.setPersonId(personId);
        entity.setTheme(this.theme.toTheme());
        entity.setLocale(this.locale);
        entity.setLocaleBrowserSpecific(this.browserSpecific);
        return entity;
    }
}
