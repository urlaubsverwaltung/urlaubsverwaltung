package org.synyx.urlaubsverwaltung.sicknote.web;

import org.springframework.beans.BeanUtils;
import org.synyx.urlaubsverwaltung.period.WeekDay;
import org.synyx.urlaubsverwaltung.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysService;

import java.math.BigDecimal;

import static java.time.temporal.ChronoField.DAY_OF_WEEK;


/**
 * Represents an extended {@link org.synyx.urlaubsverwaltung.sicknote.SickNote} with information about the number
 * of work days. (depending on working time of the person)
 */
public class ExtendedSickNote extends SickNote {

    private final BigDecimal workDays;

    public ExtendedSickNote(SickNote sickNote, WorkDaysService calendarService) {

        // copy all the properties from the given sick note
        BeanUtils.copyProperties(sickNote, this);

        // not copied, must be set explicitly
        setId(sickNote.getId());

        // calculate the work days
        this.workDays = calendarService.getWorkDays(getDayLength(), getStartDate(), getEndDate(), getPerson());
    }

    public BigDecimal getWorkDays() {

        return workDays;
    }


    public WeekDay getWeekDayOfStartDate() {

        return WeekDay.getByDayOfWeek(getStartDate().get(DAY_OF_WEEK));
    }


    public WeekDay getWeekDayOfEndDate() {

        return WeekDay.getByDayOfWeek(getEndDate().get(DAY_OF_WEEK));
    }
}
