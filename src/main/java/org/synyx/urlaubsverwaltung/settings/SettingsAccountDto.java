package org.synyx.urlaubsverwaltung.settings;

import org.synyx.urlaubsverwaltung.account.AccountSettings;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeSettings;

import java.util.Objects;

public class SettingsAccountDto {

    private Long id;
    private WorkingTimeSettings workingTimeSettings;
    private AccountSettings accountSettings;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public WorkingTimeSettings getWorkingTimeSettings() {
        return workingTimeSettings;
    }

    public void setWorkingTimeSettings(WorkingTimeSettings workingTimeSettings) {
        this.workingTimeSettings = workingTimeSettings;
    }

    public AccountSettings getAccountSettings() {
        return accountSettings;
    }

    public void setAccountSettings(AccountSettings accountSettings) {
        this.accountSettings = accountSettings;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SettingsAccountDto that = (SettingsAccountDto) o;
        return Objects.equals(id, that.id)
            && Objects.equals(workingTimeSettings, that.workingTimeSettings)
            && Objects.equals(accountSettings, that.accountSettings);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, workingTimeSettings, accountSettings);
    }
}
