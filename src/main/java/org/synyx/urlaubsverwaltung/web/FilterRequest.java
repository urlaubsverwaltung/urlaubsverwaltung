package org.synyx.urlaubsverwaltung.web;

import org.joda.time.DateMidnight;

import org.synyx.urlaubsverwaltung.core.util.DateUtil;


/**
 * Represents a request to filter something by {@link FilterRequest.Period}.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class FilterRequest {

    public enum Period {

        YEAR,
        QUARTAL,
        MONTH
    }

    private Period period;

    public FilterRequest() {

        this.period = Period.YEAR;
    }


    public FilterRequest(Period period) {

        this.period = period;
    }

    public Period getPeriod() {

        return period;
    }


    public void setPeriod(Period period) {

        this.period = period;
    }


    public DateMidnight getStartDate() {

        int currentYear = DateMidnight.now().getYear();

        if (Period.YEAR.equals(period)) {
            return DateUtil.getFirstDayOfYear(currentYear);
        } else if (Period.MONTH.equals(period)) {
            return DateUtil.getFirstDayOfMonth(currentYear, DateMidnight.now().getMonthOfYear());
        } else if (Period.QUARTAL.equals(period)) {
            DateMidnight firstDayOfQuarter = DateMidnight.now().minusMonths(2).dayOfMonth().withMinimumValue();

            if (firstDayOfQuarter.getYear() != currentYear) {
                return DateMidnight.now().dayOfYear().withMinimumValue();
            }

            return firstDayOfQuarter;
        }

        throw new IllegalStateException("Filter request has no valid period!");
    }


    public DateMidnight getEndDate() {

        int currentYear = DateMidnight.now().getYear();

        if (Period.YEAR.equals(period)) {
            return DateUtil.getLastDayOfYear(DateMidnight.now().getYear());
        } else if (Period.MONTH.equals(period)) {
            return DateUtil.getLastDayOfMonth(currentYear, DateMidnight.now().getMonthOfYear());
        } else if (Period.QUARTAL.equals(period)) {
            return DateMidnight.now().dayOfMonth().withMaximumValue();
        }

        throw new IllegalStateException("Filter request has no valid period!");
    }
}
