package org.synyx.urlaubsverwaltung.person;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("uv.person")
public class PersonProperties {

    /**
     * Indicates if persons can be deactivated via the user interface. By default, this is enabled.
     * Disable it when persons are deactivated by an external system, e.g. via an event of the extension.
     * Already inactive persons can still be reactivated via the user interface.
     */
    private boolean uiDeactivationEnabled = true;

    public boolean isUiDeactivationEnabled() {
        return uiDeactivationEnabled;
    }

    public void setUiDeactivationEnabled(boolean uiDeactivationEnabled) {
        this.uiDeactivationEnabled = uiDeactivationEnabled;
    }
}
