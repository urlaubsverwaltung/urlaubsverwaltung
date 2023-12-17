package org.synyx.urlaubsverwaltung.account;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Enumerated;

import java.time.Month;
import java.time.MonthDay;

import static jakarta.persistence.EnumType.STRING;
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
     * Specifies the day of month of the date, when the vacation will expire globally
     */
    @Column(name = "account_expiry_date_day_of_month")
    private int expiryDateDayOfMonth = 1;

    /**
     * Specifies the month of the date, when the vacation will expire globally
     */
    @Column(name = "account_expiry_date_month")
    @Enumerated(STRING)
    private Month expiryDateMonth = APRIL;

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

    public int getExpiryDateDayOfMonth() {
        return expiryDateDayOfMonth;
    }

    public void setExpiryDateDayOfMonth(int expiryDateDayOfMonth) {
        this.expiryDateDayOfMonth = expiryDateDayOfMonth;
    }

    public Month getExpiryDateMonth() {
        return expiryDateMonth;
    }

    public void setExpiryDateMonth(Month expiryDateMonth) {
        this.expiryDateMonth = expiryDateMonth;
    }

    public MonthDay getExpiryDate() {
        return MonthDay.of(getExpiryDateMonth(), expiryDateDayOfMonth);
    }

    public boolean isDoRemainingVacationDaysExpireGlobally() {
        return doRemainingVacationDaysExpireGlobally;
    }

    public void setDoRemainingVacationDaysExpireGlobally(boolean doRemainingVacationDaysExpireGlobally) {
        this.doRemainingVacationDaysExpireGlobally = doRemainingVacationDaysExpireGlobally;
    }
}
