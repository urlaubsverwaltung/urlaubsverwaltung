package org.synyx.urlaubsverwaltung.workingtime;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Enumerated;
import org.synyx.urlaubsverwaltung.period.DayLength;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.EnumType.STRING;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;
import static org.synyx.urlaubsverwaltung.period.DayLength.MORNING;
import static org.synyx.urlaubsverwaltung.period.DayLength.ZERO;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.GERMANY_BADEN_WUERTTEMBERG;

/**
 * Settings concerning working time of persons, like settings for public holidays.
 */
@Embeddable
public class WorkingTimeSettings {

    @Enumerated(STRING)
    private DayLength monday = FULL;

    @Enumerated(STRING)
    private DayLength tuesday = FULL;

    @Enumerated(STRING)
    private DayLength wednesday = FULL;

    @Enumerated(STRING)
    private DayLength thursday = FULL;

    @Enumerated(STRING)
    private DayLength friday = FULL;

    @Enumerated(STRING)
    private DayLength saturday = ZERO;

    @Enumerated(STRING)
    private DayLength sunday = ZERO;

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

    public DayLength getMonday() {
        return monday;
    }

    public void setMonday(DayLength monday) {
        this.monday = monday;
    }

    public DayLength getTuesday() {
        return tuesday;
    }

    public void setTuesday(DayLength tuesday) {
        this.tuesday = tuesday;
    }

    public DayLength getWednesday() {
        return wednesday;
    }

    public void setWednesday(DayLength wednesday) {
        this.wednesday = wednesday;
    }

    public DayLength getThursday() {
        return thursday;
    }

    public void setThursday(DayLength thursday) {
        this.thursday = thursday;
    }

    public DayLength getFriday() {
        return friday;
    }

    public void setFriday(DayLength friday) {
        this.friday = friday;
    }

    public DayLength getSaturday() {
        return saturday;
    }

    public void setSaturday(DayLength saturday) {
        this.saturday = saturday;
    }

    public DayLength getSunday() {
        return sunday;
    }

    public void setSunday(DayLength sunday) {
        this.sunday = sunday;
    }

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

    public void setWorkingDays(List<Integer> workingDays) {

        setAllDayLengthsToZero();

        if (!workingDays.isEmpty()) {
            for (Integer dayOfWeek : workingDays) {
                switch (DayOfWeek.of(dayOfWeek)) {
                    case MONDAY -> this.monday = FULL;
                    case TUESDAY -> this.tuesday = FULL;
                    case WEDNESDAY -> this.wednesday = FULL;
                    case THURSDAY -> this.thursday = FULL;
                    case FRIDAY -> this.friday = FULL;
                    case SATURDAY -> this.saturday = FULL;
                    case SUNDAY -> this.sunday = FULL;
                }
            }
        }
    }

    public List<Integer> getWorkingDays() {
        final List<Integer> workingDays = new ArrayList<>();

        if (monday.equals(FULL)) {
            workingDays.add(1);
        }
        if (tuesday.equals(FULL)) {
            workingDays.add(2);
        }
        if (wednesday.equals(FULL)) {
            workingDays.add(3);
        }
        if (thursday.equals(FULL)) {
            workingDays.add(4);
        }
        if (friday.equals(FULL)) {
            workingDays.add(5);
        }
        if (saturday.equals(FULL)) {
            workingDays.add(6);
        }
        if (sunday.equals(FULL)) {
            workingDays.add(7);
        }

        return workingDays;
    }

    private void setAllDayLengthsToZero() {
        this.monday = ZERO;
        this.tuesday = ZERO;
        this.wednesday = ZERO;
        this.thursday = ZERO;
        this.friday = ZERO;
        this.saturday = ZERO;
        this.sunday = ZERO;
    }
}
