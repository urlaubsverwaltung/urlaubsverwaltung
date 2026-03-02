package org.synyx.urlaubsverwaltung.overtime;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties("uv.overtime")
@Validated
public class OvertimeProperties {

    /**
     * Indicates if the overtime of the users should be synchronized with the zeiterfassung. By default, this is disabled.
     */
    private boolean syncActive = false;

    /**
     * The URL to the zeiterfassung lock settings.
     */
    private String zeiterfassungLockSettingsUrl = "https://urlaubsverwaltung.cloud/hilfe/zeiterfassung/zeiteintraege/#koennen-zeiteintraege-festgeschrieben-werden";

    public boolean isSyncActive() {
        return syncActive;
    }

    public void setSyncActive(boolean syncActive) {
        this.syncActive = syncActive;
    }

    public String getZeiterfassungLockSettingsUrl() {
        return zeiterfassungLockSettingsUrl;
    }

    public void setZeiterfassungLockSettingsUrl(String zeiterfassungLockSettingsUrl) {
        this.zeiterfassungLockSettingsUrl = zeiterfassungLockSettingsUrl;
    }
}
