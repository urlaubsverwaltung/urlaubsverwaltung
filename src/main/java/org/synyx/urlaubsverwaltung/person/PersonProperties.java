package org.synyx.urlaubsverwaltung.person;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("uv.person")
public class PersonProperties {

    /**
     * Indicates if persons can be deleted via the user interface. By default, this is enabled.
     * Disable it when persons are deleted by an external system, e.g. via an event of the extension.
     */
    private boolean uiDeletionEnabled = true;

    public boolean isUiDeletionEnabled() {
        return uiDeletionEnabled;
    }

    public void setUiDeletionEnabled(boolean uiDeletionEnabled) {
        this.uiDeletionEnabled = uiDeletionEnabled;
    }
}
