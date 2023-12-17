package org.synyx.urlaubsverwaltung.account;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embeddable;
import org.synyx.urlaubsverwaltung.MonthDayDateAttributeConverter;

import java.time.MonthDay;

import static java.time.Month.APRIL;

/**
 * Settings concerning absence of persons because of vacation or sick days.
 */
@Embeddable
public class AccountSettings {

    /**
     * Based on http://www.gesetze-im-internet.de/burlg/__3.html the default is 24 days
     */
    private Integer defaultVacationDays = 24;

    /**
     * Specifies the maximal number of annual vacation days a person can have.
     */
    private Integer maximumAnnualVacationDays = 40;

    /**
     * Specifies the date, when the vacation will expire globally
     */
    @Convert(converter = MonthDayDateAttributeConverter.class)
    @Column(name = "account_expiry_date")
    private MonthDay expiryDate = MonthDay.of(APRIL, 1);

    /**
     * Specifies if remaining vacation days will expire globally
     */
    private boolean doRemainingVacationDaysExpireGlobally = true;

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

    public MonthDay getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(MonthDay expiryDate) {
        this.expiryDate = expiryDate;
    }

    public boolean isDoRemainingVacationDaysExpireGlobally() {
        return doRemainingVacationDaysExpireGlobally;
    }

    public void setDoRemainingVacationDaysExpireGlobally(boolean doRemainingVacationDaysExpireGlobally) {
        this.doRemainingVacationDaysExpireGlobally = doRemainingVacationDaysExpireGlobally;
    }
}
