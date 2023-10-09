package org.synyx.urlaubsverwaltung.person.settings;

import jakarta.persistence.Embeddable;

import java.util.Objects;

@Embeddable
public class AvatarSettings {

    /**
     * Is gravatar used for avatar images
     */
    private boolean gravatarEnabled = false;

    public boolean isGravatarEnabled() {
        return gravatarEnabled;
    }

    public void setGravatarEnabled(boolean gravatarEnabled) {
        this.gravatarEnabled = gravatarEnabled;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AvatarSettings that = (AvatarSettings) o;
        return gravatarEnabled == that.gravatarEnabled;
    }

    @Override
    public int hashCode() {
        return Objects.hash(gravatarEnabled);
    }

    @Override
    public String toString() {
        return "AvatarSettings{" +
            "gravatarEnabled=" + gravatarEnabled +
            '}';
    }
}
