package org.synyx.urlaubsverwaltung.extension.backup.model;

import org.synyx.urlaubsverwaltung.person.settings.AvatarSettings;

public record AvatarSettingsDTO(boolean gravatarEnabled) {

    public static AvatarSettingsDTO of(AvatarSettings avatarSettings) {
        return new AvatarSettingsDTO(avatarSettings.isGravatarEnabled());
    }

    public AvatarSettings toAvatarSettings() {
        AvatarSettings avatarSettings = new AvatarSettings();
        avatarSettings.setGravatarEnabled(gravatarEnabled);
        return avatarSettings;
    }
}
