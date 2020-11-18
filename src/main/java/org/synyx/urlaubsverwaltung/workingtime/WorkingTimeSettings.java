package org.synyx.urlaubsverwaltung.workingtime;

import org.synyx.urlaubsverwaltung.period.DayLength;

import javax.persistence.Embeddable;
import javax.persistence.Enumerated;

import static javax.persistence.EnumType.STRING;
import static org.synyx.urlaubsverwaltung.period.DayLength.MORNING;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.BADEN_WUERTTEMBERG;

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
    @Enumerated(STRING)
    private DayLength workingDurationForChristmasEve = MORNING;

    @Enumerated(STRING)
    private DayLength workingDurationForNewYearsEve = MORNING;

    /**
     * Defines the federal state of Germany to be able to check correctly if a day is a public holiday or not.
     */
    @Enumerated(STRING)
    private FederalState federalState = BADEN_WUERTTEMBERG;

    public DayLength getWorkingDurationForChristmasEve() {
        return workingDurationForChristmasEve;
    }

    public void setWorkingDurationForChristmasEve(DayLength workingDurationForChristmasEve) {
        this.workingDurationForChristmasEve = workingDurationForChristmasEve;
    }

    public DayLength getWorkingDurationForNewYearsEve() {
        return workingDurationForNewYearsEve;
    }

    public void setWorkingDurationForNewYearsEve(DayLength workingDurationForNewYearsEve) {
        this.workingDurationForNewYearsEve = workingDurationForNewYearsEve;
    }

    public FederalState getFederalState() {
        return federalState;
    }

    public void setFederalState(FederalState federalState) {
        this.federalState = federalState;
    }
}
