package org.synyx.urlaubsverwaltung.workingtime;

import org.springframework.data.jpa.domain.AbstractPersistable;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.period.WeekDay;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.settings.FederalState;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.OneToOne;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


/**
 * Entity representing the working time of a person.
 */
@Entity
public class WorkingTime extends AbstractPersistable<Integer> {

    @OneToOne
    private Person person;

    @Enumerated(EnumType.STRING)
    private DayLength monday = DayLength.ZERO;

    @Enumerated(EnumType.STRING)
    private DayLength tuesday = DayLength.ZERO;

    @Enumerated(EnumType.STRING)
    private DayLength wednesday = DayLength.ZERO;

    @Enumerated(EnumType.STRING)
    private DayLength thursday = DayLength.ZERO;

    @Enumerated(EnumType.STRING)
    private DayLength friday = DayLength.ZERO;

    @Enumerated(EnumType.STRING)
    private DayLength saturday = DayLength.ZERO;

    @Enumerated(EnumType.STRING)
    private DayLength sunday = DayLength.ZERO;

    private LocalDate validFrom;

    /**
     * If set, override the system-wide FederalState setting for this person. TODO: Maybe we should embed the whole
     * WorkingTimeSettings to allow overriding all of them?
     */
    @Enumerated(EnumType.STRING)
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
        List<WeekDay> workingDays = new ArrayList<>();

        if (monday.equals(DayLength.FULL)) {
            workingDays.add(WeekDay.MONDAY);
        }
        if (tuesday.equals(DayLength.FULL)) {
            workingDays.add(WeekDay.TUESDAY);
        }
        if (wednesday.equals(DayLength.FULL)) {
            workingDays.add(WeekDay.WEDNESDAY);
        }
        if (thursday.equals(DayLength.FULL)) {
            workingDays.add(WeekDay.THURSDAY);
        }
        if (friday.equals(DayLength.FULL)) {
            workingDays.add(WeekDay.FRIDAY);
        }
        if (saturday.equals(DayLength.FULL)) {
            workingDays.add(WeekDay.SATURDAY);
        }
        if (sunday.equals(DayLength.FULL)) {
            workingDays.add(WeekDay.SUNDAY);
        }

        return workingDays;
    }


    public boolean hasWorkingDays(List<Integer> workingDays) {

        for (WeekDay day : WeekDay.values()) {
            int dayOfWeek = day.getDayOfWeek();

            DayLength dayLength = getDayLengthForWeekDay(dayOfWeek);

            if (dayLength == DayLength.FULL) {
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

        this.monday = DayLength.ZERO;
        this.tuesday = DayLength.ZERO;
        this.wednesday = DayLength.ZERO;
        this.thursday = DayLength.ZERO;
        this.friday = DayLength.ZERO;
        this.saturday = DayLength.ZERO;
        this.sunday = DayLength.ZERO;
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
}
