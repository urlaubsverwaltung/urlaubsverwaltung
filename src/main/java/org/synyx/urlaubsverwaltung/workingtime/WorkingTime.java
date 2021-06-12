package org.synyx.urlaubsverwaltung.workingtime;

import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.period.WeekDay;
import org.synyx.urlaubsverwaltung.person.Person;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;
import static org.synyx.urlaubsverwaltung.period.DayLength.ZERO;
import static org.synyx.urlaubsverwaltung.period.WeekDay.FRIDAY;
import static org.synyx.urlaubsverwaltung.period.WeekDay.MONDAY;
import static org.synyx.urlaubsverwaltung.period.WeekDay.SATURDAY;
import static org.synyx.urlaubsverwaltung.period.WeekDay.SUNDAY;
import static org.synyx.urlaubsverwaltung.period.WeekDay.THURSDAY;
import static org.synyx.urlaubsverwaltung.period.WeekDay.TUESDAY;
import static org.synyx.urlaubsverwaltung.period.WeekDay.WEDNESDAY;


/**
 * Entity representing the working time of a person.
 */
public class WorkingTime {

    private Person person;
    private DayLength monday = ZERO;
    private DayLength tuesday = ZERO;
    private DayLength wednesday = ZERO;
    private DayLength thursday = ZERO;
    private DayLength friday = ZERO;
    private DayLength saturday = ZERO;
    private DayLength sunday = ZERO;
    private LocalDate validFrom;

    /**
     * If set, override the system-wide FederalState setting for this person.
     */
    private FederalState federalStateOverride;

    public void setWorkingDays(List<Integer> workingDays, DayLength dayLength) {

        setAllDayLengthsToZero();

        if (!workingDays.isEmpty()) {
            for (Integer dayOfWeek : workingDays) {
                setDayLengthForWeekDay(dayOfWeek, dayLength);
            }
        }
    }

    public List<WeekDay> getWorkingDays() {
        final List<WeekDay> workingDays = new ArrayList<>();

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

    public boolean hasWorkingDays(List<Integer> workingDays) {

        for (WeekDay day : WeekDay.values()) {
            int dayOfWeek = day.getDayOfWeek();

            final DayLength dayLength = getDayLengthForWeekDay(dayOfWeek);

            if (dayLength == FULL) {
                // has to be in the given list
                if (!workingDays.contains(dayOfWeek)) {
                    return false;
                }
            } else {
                // must not be in the given list
                if (workingDays.contains(dayOfWeek)) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * checks if the given day of week is a working day or not.
     * A working day can be FULL or MORNING or NOON, doesn't matter.
     *
     * @param dayOfWeek the day of week to check
     * @return <code>false</code> when given day of week matches DayLength ZERO, <code>true</code> otherwise.
     */
    public boolean isWorkingDay(DayOfWeek dayOfWeek) {
        switch (dayOfWeek) {
            case MONDAY:
                return this.monday != ZERO;
            case TUESDAY:
                return this.tuesday != ZERO;
            case WEDNESDAY:
                return this.wednesday != ZERO;
            case THURSDAY:
                return this.thursday != ZERO;
            case FRIDAY:
                return this.friday != ZERO;
            case SATURDAY:
                return this.saturday != ZERO;
            case SUNDAY:
                return this.sunday != ZERO;
        }
        return false;
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

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public DayLength getDayLengthForWeekDay(int weekDay) {
        switch (DayOfWeek.of(weekDay)) {
            case MONDAY:
                return this.monday;
            case TUESDAY:
                return this.tuesday;
            case WEDNESDAY:
                return this.wednesday;
            case THURSDAY:
                return this.thursday;
            case FRIDAY:
                return this.friday;
            case SATURDAY:
                return this.saturday;
            case SUNDAY:
                return this.sunday;
            default:
                return null;
        }
    }

    public void setDayLengthForWeekDay(int weekDay, DayLength dayLength) {
        switch (DayOfWeek.of(weekDay)) {
            case MONDAY:
                this.monday = dayLength;
                break;
            case TUESDAY:
                this.tuesday = dayLength;
                break;
            case WEDNESDAY:
                this.wednesday = dayLength;
                break;
            case THURSDAY:
                this.thursday = dayLength;
                break;
            case FRIDAY:
                this.friday = dayLength;
                break;
            case SATURDAY:
                this.saturday = dayLength;
                break;
            case SUNDAY:
                this.sunday = dayLength;
                break;
            default:
                break;
        }
    }

    public LocalDate getValidFrom() {
        if (this.validFrom == null) {
            return null;
        }

        return this.validFrom;
    }

    public void setValidFrom(LocalDate validFrom) {
        this.validFrom = validFrom;
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

    public Optional<FederalState> getFederalStateOverride() {
        return Optional.ofNullable(federalStateOverride);
    }

    public void setFederalStateOverride(FederalState federalState) {
        this.federalStateOverride = federalState;
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
            ", federalStateOverride=" + federalStateOverride +
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
