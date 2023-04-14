package org.synyx.urlaubsverwaltung.person.settings;

import javax.persistence.Embeddable;

@Embeddable
public class AvatarSettings {

    /**
     * Is gravatar used for avatar images
     */
    private boolean gravatarEnabled = true;

    public boolean isGravatarEnabled() {
        return gravatarEnabled;
    }

    public void setGravatarEnabled(boolean gravatarEnabled) {
        this.gravatarEnabled = gravatarEnabled;
    }
}
