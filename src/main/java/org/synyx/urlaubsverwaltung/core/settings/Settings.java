package org.synyx.urlaubsverwaltung.core.settings;

import org.springframework.util.Assert;

import org.synyx.urlaubsverwaltung.core.application.domain.DayLength;


/**
 * Represents the settings / business rules for the application.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class Settings {

    /**
     * Specifies the maximal number of annual vacation days a person can have.
     */
    private Integer maximumAnnualVacationDays = 40;

    /**
     * Specifies how many months in advance a person can apply for leave.
     */
    private Integer maximumMonthsToApplyForLeaveInAdvance = 12;

    /**
     * Specifies the maximal period of sick pay in days.
     */
    private Integer maximumSickPayDays = 42;

    /**
     * Specifies when a notification about the end of sick pay should be sent to the affected person and office. (number
     * of days before the end of sick pay)
     */
    private Integer daysBeforeEndOfSickPayNotification = 7;

    /**
     * Defines the working duration for Christmas Eve and New Years Eve.
     *
     * <p>Options: {@link DayLength#FULL} means that the day is fully counted as work day, {@link DayLength#MORNING} and
     * {@link DayLength#NOON} means that only half of the day is counted as work day, {@link DayLength#ZERO} means that
     * the day is fully counted as public holiday</p>
     */
    private DayLength workingDurationForChristmasEve = DayLength.MORNING;
    private DayLength workingDurationForNewYearsEve = DayLength.MORNING;

    public Integer getMaximumAnnualVacationDays() {

        return maximumAnnualVacationDays;
    }


    public Integer getMaximumMonthsToApplyForLeaveInAdvance() {

        return maximumMonthsToApplyForLeaveInAdvance;
    }


    public Integer getMaximumSickPayDays() {

        return maximumSickPayDays;
    }


    public Integer getDaysBeforeEndOfSickPayNotification() {

        return daysBeforeEndOfSickPayNotification;
    }


    public DayLength getWorkingDurationForChristmasEve() {

        return workingDurationForChristmasEve;
    }


    public DayLength getWorkingDurationForNewYearsEve() {

        return workingDurationForNewYearsEve;
    }


    public void setMaximumAnnualVacationDays(Integer maximumAnnualVacationDays) {

        Assert.notNull(maximumAnnualVacationDays, "Maximum annual vacation days must not be null");
        Assert.isTrue(maximumAnnualVacationDays > 0, "Maximum annual vacation days must be greater than 0");
        Assert.isTrue(maximumAnnualVacationDays < 367,
            "Maximum annual vacation days must not be greater than number of days of ");

        this.maximumAnnualVacationDays = maximumAnnualVacationDays;
    }


    public void setMaximumMonthsToApplyForLeaveInAdvance(Integer maximumMonthsToApplyForLeaveInAdvance) {

        Assert.notNull(maximumMonthsToApplyForLeaveInAdvance,
            "Maximum months to apply for leave in advance must not be null");
        Assert.isTrue(maximumMonthsToApplyForLeaveInAdvance > 0,
            "Maximum months to apply for leave in advance must be greater than 0");

        this.maximumMonthsToApplyForLeaveInAdvance = maximumMonthsToApplyForLeaveInAdvance;
    }


    public void setMaximumSickPayDays(Integer maximumSickPayDays) {

        Assert.notNull(maximumSickPayDays, "Maximum sick pay days must not be null");
        Assert.isTrue(maximumSickPayDays > 0, "Maximum sick pay days must be greater than 0");

        this.maximumSickPayDays = maximumSickPayDays;
    }


    public void setDaysBeforeEndOfSickPayNotification(Integer daysBeforeEndOfSickPayNotification) {

        Assert.notNull(daysBeforeEndOfSickPayNotification, "Days before end of sick pay notification must not be null");
        Assert.isTrue(daysBeforeEndOfSickPayNotification > 0,
            "Days before end of sick pay notification must be greater than 0");
        Assert.isTrue(daysBeforeEndOfSickPayNotification < maximumSickPayDays,
            "Days before end of sick pay notification must be less than maximum sick pay days");

        this.daysBeforeEndOfSickPayNotification = daysBeforeEndOfSickPayNotification;
    }


    public void setWorkingDurationForChristmasEve(DayLength workingDurationForChristmasEve) {

        Assert.notNull(workingDurationForChristmasEve, "Working duration for Christmas Eve must not be null");

        this.workingDurationForChristmasEve = workingDurationForChristmasEve;
    }


    public void setWorkingDurationForNewYearsEve(DayLength workingDurationForNewYearsEve) {

        Assert.notNull(workingDurationForNewYearsEve, "Working duration for New Years Eve must not be null");

        this.workingDurationForNewYearsEve = workingDurationForNewYearsEve;
    }
}
