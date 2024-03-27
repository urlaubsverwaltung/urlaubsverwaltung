package org.synyx.urlaubsverwaltung.account;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.time.Month;
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
     * Specifies the day of month of the date, when the vacation will expire globally
     */
    @Column(name = "account_expiry_date_day_of_month")
    private int expiryDateDayOfMonth = 1;

    /**
     * Specifies the month of the date, when the vacation will expire globally
     */
    @Column(name = "account_expiry_date_month")
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

    /**
     * setter used by the view model to set the {@linkplain Month} of the expiry date.
     *
     * @param expiryDateMonthOrdinal numeric value of the month. starting with january=1
     */
    public void setExpiryDateMonth(int expiryDateMonthOrdinal) {
        this.expiryDateMonth = Month.of(expiryDateMonthOrdinal);
    }

    public MonthDay getExpiryDate() {
        return MonthDay.of(expiryDateMonth, expiryDateDayOfMonth);
    }

    public boolean isDoRemainingVacationDaysExpireGlobally() {
        return doRemainingVacationDaysExpireGlobally;
    }

    public void setDoRemainingVacationDaysExpireGlobally(boolean doRemainingVacationDaysExpireGlobally) {
        this.doRemainingVacationDaysExpireGlobally = doRemainingVacationDaysExpireGlobally;
    }
}
