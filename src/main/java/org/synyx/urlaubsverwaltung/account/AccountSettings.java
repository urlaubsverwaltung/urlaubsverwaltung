package org.synyx.urlaubsverwaltung.account;

import javax.persistence.Embeddable;

/**
 * Settings concerning absence of persons because of vacation or sick days.
 */
@Embeddable
public class AccountSettings {

    /**
     * Specifies the maximal number of annual vacation days a person can have.
     */
    private Integer maximumAnnualVacationDays = 40;

    public Integer getMaximumAnnualVacationDays() {
        return maximumAnnualVacationDays;
    }

    public void setMaximumAnnualVacationDays(Integer maximumAnnualVacationDays) {
        this.maximumAnnualVacationDays = maximumAnnualVacationDays;
    }
}
