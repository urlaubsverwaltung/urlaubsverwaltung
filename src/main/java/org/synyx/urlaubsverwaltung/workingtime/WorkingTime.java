package org.synyx.urlaubsverwaltung.workingtime;

import org.springframework.data.jpa.domain.AbstractPersistable;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.period.WeekDay;
import org.synyx.urlaubsverwaltung.person.Person;

import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.OneToOne;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static javax.persistence.EnumType.STRING;
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
@Entity
public class WorkingTime extends AbstractPersistable<Integer> {

    @OneToOne
    private Person person;

    @Enumerated(STRING)
    private DayLength monday = ZERO;

    @Enumerated(STRING)
    private DayLength tuesday = ZERO;

    @Enumerated(STRING)
    private DayLength wednesday = ZERO;

    @Enumerated(STRING)
    private DayLength thursday = ZERO;

    @Enumerated(STRING)
    private DayLength friday = ZERO;

    @Enumerated(STRING)
    private DayLength saturday = ZERO;

    @Enumerated(STRING)
    private DayLength sunday = ZERO;

    private LocalDate validFrom;

    /**
     * If set, override the system-wide FederalState setting for this person.
     */
    @Enumerated(STRING)
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
}
