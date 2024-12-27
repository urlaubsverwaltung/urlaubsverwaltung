package org.synyx.urlaubsverwaltung.settings;

import org.synyx.urlaubsverwaltung.person.settings.AvatarSettings;

import java.util.Objects;

public class SettingsAvatarDto {

    private Long id;
    private AvatarSettings avatarSettings;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AvatarSettings getAvatarSettings() {
        return avatarSettings;
    }

    public void setAvatarSettings(AvatarSettings avatarSettings) {
        this.avatarSettings = avatarSettings;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SettingsAvatarDto that = (SettingsAvatarDto) o;
        return Objects.equals(id, that.id) && Objects.equals(avatarSettings, that.avatarSettings);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, avatarSettings);
    }

    @Override
    public String toString() {
        return "SettingsAvatarDto{" +
            "id=" + id +
            ", avatarSettings=" + avatarSettings +
            '}';
    }
}
