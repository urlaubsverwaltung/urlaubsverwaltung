package org.synyx.urlaubsverwaltung.core.settings;

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

        this.maximumAnnualVacationDays = maximumAnnualVacationDays;
    }


    public void setMaximumMonthsToApplyForLeaveInAdvance(Integer maximumMonthsToApplyForLeaveInAdvance) {

        this.maximumMonthsToApplyForLeaveInAdvance = maximumMonthsToApplyForLeaveInAdvance;
    }


    public void setMaximumSickPayDays(Integer maximumSickPayDays) {

        this.maximumSickPayDays = maximumSickPayDays;
    }


    public void setDaysBeforeEndOfSickPayNotification(Integer daysBeforeEndOfSickPayNotification) {

        this.daysBeforeEndOfSickPayNotification = daysBeforeEndOfSickPayNotification;
    }


    public void setWorkingDurationForChristmasEve(DayLength workingDurationForChristmasEve) {

        this.workingDurationForChristmasEve = workingDurationForChristmasEve;
    }


    public void setWorkingDurationForNewYearsEve(DayLength workingDurationForNewYearsEve) {

        this.workingDurationForNewYearsEve = workingDurationForNewYearsEve;
    }
}
