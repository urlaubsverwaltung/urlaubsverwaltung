package org.synyx.urlaubsverwaltung.core.settings;

import org.synyx.urlaubsverwaltung.core.period.DayLength;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;


/**
 * Settings concerning working time of persons, like settings for public holidays.
 *
 * @author  Aljona Murygina - murygina@synyx.de
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
     * Defines the maximum number of overtime a person can have.
     *
     * @since  2.13.0
     */
    @Column(name = "overtime_maximum")
    private Integer maximumOvertime = 100;

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


    public Integer getMaximumOvertime() {

        return maximumOvertime;
    }


    public void setMaximumOvertime(Integer maximumOvertime) {

        this.maximumOvertime = maximumOvertime;
    }
}
