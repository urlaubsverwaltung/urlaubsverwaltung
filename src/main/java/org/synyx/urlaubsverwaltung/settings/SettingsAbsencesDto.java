package org.synyx.urlaubsverwaltung.settings;

import org.synyx.urlaubsverwaltung.account.AccountSettings;
import org.synyx.urlaubsverwaltung.application.settings.ApplicationSettings;
import org.synyx.urlaubsverwaltung.sicknote.settings.SickNoteSettings;

import java.util.Objects;

public class SettingsAbsencesDto {

    private Long id;
    private AccountSettings accountSettings;
    private ApplicationSettings applicationSettings;
    private SickNoteSettings sickNoteSettings;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AccountSettings getAccountSettings() {
        return accountSettings;
    }

    public void setAccountSettings(AccountSettings accountSettings) {
        this.accountSettings = accountSettings;
    }

    public ApplicationSettings getApplicationSettings() {
        return applicationSettings;
    }

    public void setApplicationSettings(ApplicationSettings applicationSettings) {
        this.applicationSettings = applicationSettings;
    }

    public SickNoteSettings getSickNoteSettings() {
        return sickNoteSettings;
    }

    public void setSickNoteSettings(SickNoteSettings sickNoteSettings) {
        this.sickNoteSettings = sickNoteSettings;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SettingsAbsencesDto that = (SettingsAbsencesDto) o;
        return Objects.equals(id, that.id)
            && Objects.equals(accountSettings, that.accountSettings)
            && Objects.equals(applicationSettings, that.applicationSettings)
            && Objects.equals(sickNoteSettings, that.sickNoteSettings);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, accountSettings, applicationSettings, sickNoteSettings);
    }
}
