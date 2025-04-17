package org.synyx.urlaubsverwaltung.extension.backup.model;

import org.synyx.urlaubsverwaltung.settings.Settings;

public record SettingsDTO(
    Long id, ApplicationSettingsDTO applicationSettings, AccountSettingsDTO accountSettings,
    WorkingTimeSettingsDTO workingTimeSettings, OverTimeSettingsDTO overTimeSettings,
    TimeSettingsDTO timeSettings, SickNoteSettingsDTO sickNoteSettings,
    AvatarSettingsDTO avatarSettings, PublicHolidaysSettingsDTO publicHolidaysSettings
) {

    public Settings toSettings() {
        final Settings settings = new Settings();
        settings.setApplicationSettings(applicationSettings().toApplicationSettings());
        settings.setAccountSettings(accountSettings().toAccountSettings());
        settings.setWorkingTimeSettings(workingTimeSettings().toWorkingTimeSettings());
        settings.setOvertimeSettings(overTimeSettings().toOverTimeSettings());
        settings.setTimeSettings(timeSettings().toTimeSettings());
        settings.setSickNoteSettings(sickNoteSettings().toSickNoteSettings());
        settings.setAvatarSettings(avatarSettings().toAvatarSettings());
        settings.setPublicHolidaysSettings(publicHolidaysSettings().toPublicHolidaysSettings());
        return settings;
    }
}
