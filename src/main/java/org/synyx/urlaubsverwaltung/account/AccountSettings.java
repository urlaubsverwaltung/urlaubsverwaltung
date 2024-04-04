package org.synyx.urlaubsverwaltung.account;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Enumerated;
import org.slf4j.Logger;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;

import static jakarta.persistence.EnumType.STRING;
import static java.lang.invoke.MethodHandles.lookup;
import static java.time.Month.APRIL;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Settings concerning absence of persons because of vacation or sick days.
 */
@Embeddable
public class AccountSettings {

    private static final Logger LOG = getLogger(lookup().lookupClass());

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

    /**
     * This method constructs an expiry date of type {@link LocalDate} and handles leap years for instance (february 28 vs 29).
     *
     * @param year the year to get the expiry date for
     * @return expiry date for the given year
     */
    public LocalDate getExpiryDateForYear(Year year) {

        // 31. February is (or could be) a valid input in the global settings.
        // Therefore, just subtract one day until a valid localDate can be parsed.
        int subtract = 0;

        LocalDate date = null;

        while (date == null) {
            final Month month = getExpiryDateMonth();
            final int dayOfMonth = getExpiryDateDayOfMonth() - subtract;
            try {
                date = LocalDate.of(year.getValue(), month, dayOfMonth);
            } catch (DateTimeException e) {
                LOG.debug("could not create expiry date for month={} dayOfMonth={}", month, dayOfMonth);
            } finally {
                subtract++;
            }
        }

        return date;
    }

    public boolean isDoRemainingVacationDaysExpireGlobally() {
        return doRemainingVacationDaysExpireGlobally;
    }

    public void setDoRemainingVacationDaysExpireGlobally(boolean doRemainingVacationDaysExpireGlobally) {
        this.doRemainingVacationDaysExpireGlobally = doRemainingVacationDaysExpireGlobally;
    }
}
