package org.synyx.urlaubsverwaltung.core.calendar.workingtime;

import lombok.Data;
import org.apache.commons.collections.CollectionUtils;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.springframework.data.jpa.domain.AbstractPersistable;
import org.synyx.urlaubsverwaltung.core.application.domain.DayLength;
import org.synyx.urlaubsverwaltung.core.calendar.Day;
import org.synyx.urlaubsverwaltung.core.person.Person;

import javax.persistence.*;
import java.util.Date;
import java.util.List;


/**
 * Entity representing the working time of a person.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Entity
@Data
public class WorkingTime extends AbstractPersistable<Integer> {

    @OneToOne
    private Person person;

    @Enumerated(EnumType.STRING)
    private DayLength monday;

    @Enumerated(EnumType.STRING)
    private DayLength tuesday;

    @Enumerated(EnumType.STRING)
    private DayLength wednesday;

    @Enumerated(EnumType.STRING)
    private DayLength thursday;

    @Enumerated(EnumType.STRING)
    private DayLength friday;

    @Enumerated(EnumType.STRING)
    private DayLength saturday;

    @Enumerated(EnumType.STRING)
    private DayLength sunday;

    @Temporal(javax.persistence.TemporalType.DATE)
    private Date validFrom;

    public WorkingTime() {

        setAllDayLengthsToZero();
    }

    public void setWorkingDays(List<Integer> workingDays, DayLength dayLength) {

        setAllDayLengthsToZero();

        if (!CollectionUtils.isEmpty(workingDays)) {
            for (Integer dayOfWeek : workingDays) {
                setDayLengthForWeekDay(dayOfWeek, dayLength);
            }
        }
    }


    public boolean hasWorkingDays(List<Integer> workingDays) {

        for (Day day : Day.values()) {
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
}
