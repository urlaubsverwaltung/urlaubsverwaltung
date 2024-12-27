package org.synyx.urlaubsverwaltung.workingtime;

import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.time.DayOfWeek.FRIDAY;
import static java.time.DayOfWeek.MONDAY;
import static java.time.DayOfWeek.SATURDAY;
import static java.time.DayOfWeek.SUNDAY;
import static java.time.DayOfWeek.THURSDAY;
import static java.time.DayOfWeek.TUESDAY;
import static java.time.DayOfWeek.WEDNESDAY;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;
import static org.synyx.urlaubsverwaltung.period.DayLength.ZERO;


/**
 * Entity representing the working time of a person.
 */
public class WorkingTime {

    private final Person person;
    private final LocalDate validFrom;
    private final FederalState federalState;
    private final boolean isDefaultFederalState;
    private DayLength monday = ZERO;
    private DayLength tuesday = ZERO;
    private DayLength wednesday = ZERO;
    private DayLength thursday = ZERO;
    private DayLength friday = ZERO;
    private DayLength saturday = ZERO;
    private DayLength sunday = ZERO;

    public WorkingTime(Person person, LocalDate validFrom, FederalState federalState, boolean isDefaultFederalState) {
        this.person = person;
        this.validFrom = validFrom;
        this.federalState = federalState;
        this.isDefaultFederalState = isDefaultFederalState;
    }

    public Person getPerson() {
        return person;
    }

    public LocalDate getValidFrom() {
        return this.validFrom;
    }

    public FederalState getFederalState() {
        return federalState;
    }

    public boolean isDefaultFederalState() {
        return isDefaultFederalState;
    }

    public DayLength getMonday() {
        return monday;
    }

    public DayLength getTuesday() {
        return tuesday;
    }

    public DayLength getWednesday() {
        return wednesday;
    }

    public DayLength getThursday() {
        return thursday;
    }

    public DayLength getFriday() {
        return friday;
    }

    public DayLength getSaturday() {
        return saturday;
    }

    public DayLength getSunday() {
        return sunday;
    }

    public void setWorkingDays(List<DayOfWeek> workingDays, DayLength dayLength) {

        setAllDayLengthsToZero();

        if (!workingDays.isEmpty()) {
            for (DayOfWeek dayOfWeek : workingDays) {
                setDayLengthForWeekDay(dayOfWeek, dayLength);
            }
        }
    }

    public List<DayOfWeek> getWorkingDays() {
        final List<DayOfWeek> workingDays = new ArrayList<>();

        if (monday.equals(FULL)) {
            workingDays.add(MONDAY);
        }
        if (tuesday.equals(FULL)) {
            workingDays.add(TUESDAY);
        }
        if (wednesday.equals(FULL)) {
            workingDays.add(WEDNESDAY);
        }
        if (thursday.equals(FULL)) {
            workingDays.add(THURSDAY);
        }
        if (friday.equals(FULL)) {
            workingDays.add(FRIDAY);
        }
        if (saturday.equals(FULL)) {
            workingDays.add(SATURDAY);
        }
        if (sunday.equals(FULL)) {
            workingDays.add(SUNDAY);
        }

        return workingDays;
    }

    /**
     * checks if the given day of week is a working day or not.
     * A working day can be FULL or MORNING or NOON, doesn't matter.
     *
     * @param dayOfWeek the day of week to check
     * @return <code>false</code> when given day of week matches DayLength ZERO, <code>true</code> otherwise.
     */
    public boolean isWorkingDay(DayOfWeek dayOfWeek) {
        return switch (dayOfWeek) {
            case MONDAY -> this.monday != ZERO;
            case TUESDAY -> this.tuesday != ZERO;
            case WEDNESDAY -> this.wednesday != ZERO;
            case THURSDAY -> this.thursday != ZERO;
            case FRIDAY -> this.friday != ZERO;
            case SATURDAY -> this.saturday != ZERO;
            case SUNDAY -> this.sunday != ZERO;
        };
    }

    public DayLength getDayLengthForWeekDay(DayOfWeek dayOfWeek) {
        return switch (dayOfWeek) {
            case MONDAY -> this.monday;
            case TUESDAY -> this.tuesday;
            case WEDNESDAY -> this.wednesday;
            case THURSDAY -> this.thursday;
            case FRIDAY -> this.friday;
            case SATURDAY -> this.saturday;
            case SUNDAY -> this.sunday;
        };
    }

    public void setDayLengthForWeekDay(DayOfWeek dayOfWeek, DayLength dayLength) {
        switch (dayOfWeek) {
            case MONDAY -> this.monday = dayLength;
            case TUESDAY -> this.tuesday = dayLength;
            case WEDNESDAY -> this.wednesday = dayLength;
            case THURSDAY -> this.thursday = dayLength;
            case FRIDAY -> this.friday = dayLength;
            case SATURDAY -> this.saturday = dayLength;
            case SUNDAY -> this.sunday = dayLength;
        }
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

    @Override
    public String toString() {
        return "WorkingTime{" +
            "person=" + person +
            ", monday=" + monday +
            ", tuesday=" + tuesday +
            ", wednesday=" + wednesday +
            ", thursday=" + thursday +
            ", friday=" + friday +
            ", saturday=" + saturday +
            ", sunday=" + sunday +
            ", validFrom=" + validFrom +
            ", federalState=" + federalState +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WorkingTime that = (WorkingTime) o;
        return Objects.equals(person, that.person) && Objects.equals(validFrom, that.validFrom);
    }

    @Override
    public int hashCode() {
        return Objects.hash(person, validFrom);
    }
}
