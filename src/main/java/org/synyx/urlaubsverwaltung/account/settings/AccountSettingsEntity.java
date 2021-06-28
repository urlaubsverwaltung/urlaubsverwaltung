package org.synyx.urlaubsverwaltung.account.settings;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity(name = "account_settings")
public class AccountSettingsEntity {

    @Id
    @GeneratedValue
    private Long id;

    /**
     * Based on http://www.gesetze-im-internet.de/burlg/__3.html the default is 24 days
     */
    private Integer defaultVacationDays = 24;

    /**
     * Specifies the maximal number of annual vacation days a person can have.
     */
    private Integer maximumAnnualVacationDays = 40;

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
