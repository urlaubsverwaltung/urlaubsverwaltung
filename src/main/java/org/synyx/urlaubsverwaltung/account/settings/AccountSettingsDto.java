package org.synyx.urlaubsverwaltung.account.settings;

public class AccountSettingsDto {

    private Long id;

    private Integer defaultVacationDays;

    private Integer maximumAnnualVacationDays;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getDefaultVacationDays() {
        return defaultVacationDays;
    }

    public void setDefaultVacationDays(Integer defaultVacationDays) {
        this.defaultVacationDays = defaultVacationDays;
    }

    public Integer getMaximumAnnualVacationDays() {
        return maximumAnnualVacationDays;
    }

    public void setMaximumAnnualVacationDays(Integer maximumAnnualVacationDays) {
        this.maximumAnnualVacationDays = maximumAnnualVacationDays;
    }
}
