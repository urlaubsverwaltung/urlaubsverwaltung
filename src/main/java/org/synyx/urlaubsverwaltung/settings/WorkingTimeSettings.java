package org.synyx.urlaubsverwaltung.settings;

import org.synyx.urlaubsverwaltung.period.DayLength;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;


/**
 * Settings concerning working time of persons, like settings for public holidays.
 */
@Embeddable
public class WorkingTimeSettings {

    /**
     * Defines the working duration for Christmas Eve and New Years Eve.
     *
     * <p>Options: {@link DayLength#FULL} means that the day is fully counted as work day, {@link DayLength#MORNING} and
     * {@link DayLength#NOON} means that only half of the day is counted as work day, {@link DayLength#ZERO} means that
     * the day is fully counted as public holiday</p>
     */
    @Enumerated(EnumType.STRING)
    private DayLength workingDurationForChristmasEve = DayLength.MORNING;

    @Enumerated(EnumType.STRING)
    private DayLength workingDurationForNewYearsEve = DayLength.MORNING;

    /**
     * Defines the federal state of Germany to be able to check correctly if a day is a public holiday or not.
     */
    @Enumerated(EnumType.STRING)
    private FederalState federalState = FederalState.BADEN_WUERTTEMBERG;

    /**
     * Activates or deactivates overtime settings.
     *
     * @since 2.15.0
     */
    @Column(name = "overtime_active")
    private boolean overtimeActive = false;

    /**
     * Defines the maximum number of overtime a person can have.
     *
     * @since 2.13.0
     */
    @Column(name = "overtime_maximum")
    private Integer maximumOvertime = 100; // NOSONAR

    /**
     * Defines the minimum number of overtime a person can have. Minimum overtime means missing hours (equates to
     * negative)
     *
     * @since 2.15.0
     */
    @Column(name = "overtime_minimum")
    private Integer minimumOvertime = 5; // NOSONAR

    public DayLength getWorkingDurationForChristmasEve() {

        return workingDurationForChristmasEve;
    }


    public DayLength getWorkingDurationForNewYearsEve() {

        return workingDurationForNewYearsEve;
    }


    public FederalState getFederalState() {

        return federalState;
    }


    public void setWorkingDurationForChristmasEve(DayLength workingDurationForChristmasEve) {

        this.workingDurationForChristmasEve = workingDurationForChristmasEve;
    }


    public void setWorkingDurationForNewYearsEve(DayLength workingDurationForNewYearsEve) {

        this.workingDurationForNewYearsEve = workingDurationForNewYearsEve;
    }


    public void setFederalState(FederalState federalState) {

        this.federalState = federalState;
    }


    public boolean isOvertimeActive() {

        return overtimeActive;
    }


    public void setOvertimeActive(boolean overtimeActive) {

        this.overtimeActive = overtimeActive;
    }


    public Integer getMaximumOvertime() {

        return maximumOvertime;
    }


    public void setMaximumOvertime(Integer maximumOvertime) {

        this.maximumOvertime = maximumOvertime;
    }


    public Integer getMinimumOvertime() {

        return minimumOvertime;
    }


    public void setMinimumOvertime(Integer minimumOvertime) {

        this.minimumOvertime = minimumOvertime;
    }
}
