package org.synyx.urlaubsverwaltung.extension.backup.model;

import org.synyx.urlaubsverwaltung.sicknote.settings.SickNoteSettings;

public record SickNoteSettingsDTO(
    Integer maximumSickPayDays,
    Integer daysBeforeEndOfSickPayNotification,
    boolean userIsAllowedToSubmitSickNotes) {

    public static SickNoteSettingsDTO of(SickNoteSettings sickNoteSettings) {
        return new SickNoteSettingsDTO(
            sickNoteSettings.getMaximumSickPayDays(),
            sickNoteSettings.getDaysBeforeEndOfSickPayNotification(),
            sickNoteSettings.getUserIsAllowedToSubmitSickNotes()
        );
    }

    public SickNoteSettings toSickNoteSettings() {
        SickNoteSettings sickNoteSettings = new SickNoteSettings();
        sickNoteSettings.setMaximumSickPayDays(maximumSickPayDays);
        sickNoteSettings.setDaysBeforeEndOfSickPayNotification(daysBeforeEndOfSickPayNotification);
        sickNoteSettings.setUserIsAllowedToSubmitSickNotes(userIsAllowedToSubmitSickNotes);
        return sickNoteSettings;
    }
}
