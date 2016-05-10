package org.synyx.urlaubsverwaltung.core.calendar.workingtime;

import org.apache.commons.collections.CollectionUtils;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;

import org.springframework.data.jpa.domain.AbstractPersistable;

import org.synyx.urlaubsverwaltung.core.period.DayLength;
import org.synyx.urlaubsverwaltung.core.period.WeekDay;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.settings.FederalState;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;


/**
 * Entity representing the working time of a person.
 *
 * @author  Aljona Murygina - murygina@synyx.de
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

    @Temporal(javax.persistence.TemporalType.DATE)
    private Date validFrom;

    /**
     * If set, override the system-wide FederalState setting for this person.
     * TODO: Maybe we should embed the whole WorkingTimeSettings to allow overriding all of them?
     */
    @Enumerated(EnumType.STRING)
    private FederalState federalStateOverride;


    public void setWorkingDays(List<Integer> workingDays, DayLength dayLength) {

        setAllDayLengthsToZero();

        if (!CollectionUtils.isEmpty(workingDays)) {
            for (Integer dayOfWeek : workingDays) {
                setDayLengthForWeekDay(dayOfWeek, dayLength);
            }
        }
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

        switch (weekDay) {
            case DateTimeConstants.MONDAY:
                return this.monday;

            case DateTimeConstants.TUESDAY:
                return this.tuesday;

            case DateTimeConstants.WEDNESDAY:
                return this.wednesday;

            case DateTimeConstants.THURSDAY:
                return this.thursday;

            case DateTimeConstants.FRIDAY:
                return this.friday;

            case DateTimeConstants.SATURDAY:
                return this.saturday;

            case DateTimeConstants.SUNDAY:
                return this.sunday;

            default:
                return null;
        }
    }


    public void setDayLengthForWeekDay(int weekDay, DayLength dayLength) {

        switch (weekDay) {
            case DateTimeConstants.MONDAY:
                this.monday = dayLength;
                break;

            case DateTimeConstants.TUESDAY:
                this.tuesday = dayLength;
                break;

            case DateTimeConstants.WEDNESDAY:
                this.wednesday = dayLength;
                break;

            case DateTimeConstants.THURSDAY:
                this.thursday = dayLength;
                break;

            case DateTimeConstants.FRIDAY:
                this.friday = dayLength;
                break;

            case DateTimeConstants.SATURDAY:
                this.saturday = dayLength;
                break;

            case DateTimeConstants.SUNDAY:
                this.sunday = dayLength;
                break;

            default:
                break;
        }
    }


    public DateMidnight getValidFrom() {

        if (this.validFrom == null) {
            return null;
        }

        return new DateTime(this.validFrom).toDateMidnight();
    }


    public void setValidFrom(DateMidnight validFrom) {

        if (validFrom == null) {
            this.validFrom = null;
        } else {
            this.validFrom = validFrom.toDate();
        }
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

    public Optional<FederalState> getFederalStateOverride(){

        return Optional.ofNullable(federalStateOverride);
    }
}
