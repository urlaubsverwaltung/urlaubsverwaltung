package org.synyx.urlaubsverwaltung.web;

import org.joda.time.DateMidnight;
import org.joda.time.DateTimeConstants;

import org.springframework.util.Assert;

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

        Assert.notNull(period, "Period must be set!");

        int currentYear = DateMidnight.now().getYear();

        switch (period) {
            case YEAR:
                return DateUtil.getFirstDayOfYear(currentYear);

            case MONTH:
                return DateUtil.getFirstDayOfMonth(currentYear, DateMidnight.now().getMonthOfYear());

            case QUARTER:
                return getStartDateOfQuarter(DateMidnight.now());
        }

        throw new IllegalStateException("Filter request has no valid period!");
    }


    public DateMidnight getEndDate() {

        Assert.notNull(period, "Period must be set!");

        int currentYear = DateMidnight.now().getYear();

        switch (period) {
            case YEAR:
                return DateUtil.getLastDayOfYear(DateMidnight.now().getYear());

            case MONTH:
                return DateUtil.getLastDayOfMonth(currentYear, DateMidnight.now().getMonthOfYear());

            case QUARTER:
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

        int year = referenceDate.getYear();

        if (isInFirstQuarter(referenceDate)) {
            return DateUtil.getFirstDayOfMonth(year, DateTimeConstants.JANUARY);
        }

        if (isInSecondQuarter(referenceDate)) {
            return DateUtil.getFirstDayOfMonth(year, DateTimeConstants.APRIL);
        }

        if (isInThirdQuarter(referenceDate)) {
            return DateUtil.getFirstDayOfMonth(year, DateTimeConstants.JULY);
        }

        if (isInFourthQuarter(referenceDate)) {
            return DateUtil.getFirstDayOfMonth(year, DateTimeConstants.OCTOBER);
        }

        throw new IllegalStateException("The given date has no valid month!");
    }


    private boolean isInFirstQuarter(DateMidnight date) {

        int month = date.getMonthOfYear();

        return month >= DateTimeConstants.JANUARY && month <= DateTimeConstants.MARCH;
    }


    private boolean isInSecondQuarter(DateMidnight date) {

        int month = date.getMonthOfYear();

        return month >= DateTimeConstants.APRIL && month <= DateTimeConstants.JUNE;
    }


    private boolean isInThirdQuarter(DateMidnight date) {

        int month = date.getMonthOfYear();

        return month >= DateTimeConstants.JULY && month <= DateTimeConstants.SEPTEMBER;
    }


    private boolean isInFourthQuarter(DateMidnight date) {

        int month = date.getMonthOfYear();

        return month >= DateTimeConstants.OCTOBER && month <= DateTimeConstants.DECEMBER;
    }


    /**
     * Get the end date of the quarter where the given date lies in.
     *
     * @param  referenceDate  to determine the end of the quarter by
     *
     * @return  the end date of the quarter where the given date lies in
     */
    DateMidnight getEndDateOfQuarter(DateMidnight referenceDate) {

        int year = referenceDate.getYear();

        if (isInFirstQuarter(referenceDate)) {
            return DateUtil.getLastDayOfMonth(year, DateTimeConstants.MARCH);
        }

        if (isInSecondQuarter(referenceDate)) {
            return DateUtil.getLastDayOfMonth(year, DateTimeConstants.JUNE);
        }

        if (isInThirdQuarter(referenceDate)) {
            return DateUtil.getLastDayOfMonth(year, DateTimeConstants.SEPTEMBER);
        }

        if (isInFourthQuarter(referenceDate)) {
            return DateUtil.getLastDayOfMonth(year, DateTimeConstants.DECEMBER);
        }

        throw new IllegalStateException("The given date has no valid month!");
    }
}
