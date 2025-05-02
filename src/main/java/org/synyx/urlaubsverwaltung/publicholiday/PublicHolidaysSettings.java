package org.synyx.urlaubsverwaltung.publicholiday;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Enumerated;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.workingtime.FederalState;

import java.io.Serializable;
import java.util.Objects;

import static jakarta.persistence.EnumType.STRING;
import static org.synyx.urlaubsverwaltung.period.DayLength.MORNING;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.GERMANY_BADEN_WUERTTEMBERG;

/**
 * Settings concerning working time of persons, like settings for public holidays.
 */
@Embeddable
public class PublicHolidaysSettings implements Serializable {

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
    private FederalState federalState = GERMANY_BADEN_WUERTTEMBERG;

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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        PublicHolidaysSettings that = (PublicHolidaysSettings) o;
        return workingDurationForChristmasEve == that.workingDurationForChristmasEve
            && workingDurationForNewYearsEve == that.workingDurationForNewYearsEve
            && federalState == that.federalState;
    }

    @Override
    public int hashCode() {
        return Objects.hash(workingDurationForChristmasEve, workingDurationForNewYearsEve, federalState);
    }
}
