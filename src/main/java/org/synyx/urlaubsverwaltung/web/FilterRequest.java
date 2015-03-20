package org.synyx.urlaubsverwaltung.web;

import org.joda.time.DateMidnight;
import org.joda.time.DateTimeConstants;

import org.synyx.urlaubsverwaltung.core.util.DateUtil;


/**
 * Represents a request to filter something by {@link FilterRequest.Period}.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class FilterRequest {

    public enum Period {

        YEAR,
        QUARTER,
        MONTH
    }

    private Period period;

    public FilterRequest() {

        this(Period.YEAR);
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
        } else if (Period.QUARTER.equals(period)) {
            return getStartDateOfQuarter(DateMidnight.now());
        }

        throw new IllegalStateException("Filter request has no valid period!");
    }


    public DateMidnight getEndDate() {

        int currentYear = DateMidnight.now().getYear();

        if (Period.YEAR.equals(period)) {
            return DateUtil.getLastDayOfYear(DateMidnight.now().getYear());
        } else if (Period.MONTH.equals(period)) {
            return DateUtil.getLastDayOfMonth(currentYear, DateMidnight.now().getMonthOfYear());
        } else if (Period.QUARTER.equals(period)) {
            return getEndDateOfQuarter(DateMidnight.now());
        }

        throw new IllegalStateException("Filter request has no valid period!");
    }


    /**
     * Get the start date of the quarter where the given date lies in.
     *
     * @param  referenceDate  to determine the start of the quarter by
     *
     * @return  the start date of the quarter where the given date lies in
     */
    DateMidnight getStartDateOfQuarter(DateMidnight referenceDate) {

        int month = referenceDate.getMonthOfYear();
        int year = referenceDate.getYear();

        if (month >= DateTimeConstants.JANUARY && month <= DateTimeConstants.MARCH) {
            return DateUtil.getFirstDayOfMonth(year, DateTimeConstants.JANUARY);
        }

        if (month >= DateTimeConstants.APRIL && month <= DateTimeConstants.JUNE) {
            return DateUtil.getFirstDayOfMonth(year, DateTimeConstants.APRIL);
        }

        if (month >= DateTimeConstants.JULY && month <= DateTimeConstants.SEPTEMBER) {
            return DateUtil.getFirstDayOfMonth(year, DateTimeConstants.JULY);
        }

        if (month >= DateTimeConstants.OCTOBER && month <= DateTimeConstants.DECEMBER) {
            return DateUtil.getFirstDayOfMonth(year, DateTimeConstants.OCTOBER);
        }

        throw new IllegalStateException("The given date has no valid month!");
    }


    /**
     * Get the end date of the quarter where the given date lies in.
     *
     * @param  referenceDate  to determine the end of the quarter by
     *
     * @return  the end date of the quarter where the given date lies in
     */
    DateMidnight getEndDateOfQuarter(DateMidnight referenceDate) {

        int month = referenceDate.getMonthOfYear();
        int year = referenceDate.getYear();

        if (month >= DateTimeConstants.JANUARY && month <= DateTimeConstants.MARCH) {
            return DateUtil.getLastDayOfMonth(year, DateTimeConstants.MARCH);
        }

        if (month >= DateTimeConstants.APRIL && month <= DateTimeConstants.JUNE) {
            return DateUtil.getLastDayOfMonth(year, DateTimeConstants.JUNE);
        }

        if (month >= DateTimeConstants.JULY && month <= DateTimeConstants.SEPTEMBER) {
            return DateUtil.getLastDayOfMonth(year, DateTimeConstants.SEPTEMBER);
        }

        if (month >= DateTimeConstants.OCTOBER && month <= DateTimeConstants.DECEMBER) {
            return DateUtil.getLastDayOfMonth(year, DateTimeConstants.DECEMBER);
        }

        throw new IllegalStateException("The given date has no valid month!");
    }
}
