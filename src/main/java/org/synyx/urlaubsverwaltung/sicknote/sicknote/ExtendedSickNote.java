package org.synyx.urlaubsverwaltung.sicknote.sicknote;

import org.springframework.beans.BeanUtils;
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysCountService;

import java.math.BigDecimal;
import java.time.DayOfWeek;

/**
 * Represents an extended {@link SickNote} with information about the number
 * of work days. (depending on working time of the person)
 */
public class ExtendedSickNote extends SickNote {

    private final BigDecimal workDays;

    public ExtendedSickNote(SickNote sickNote, WorkDaysCountService workDaysCountService) {

        // copy all the properties from the given sick note
        BeanUtils.copyProperties(sickNote, this);

        // not copied, must be set explicitly
        setId(sickNote.getId());

        // calculate the work days
        this.workDays = workDaysCountService.getWorkDaysCount(getDayLength(), getStartDate(), getEndDate(), getPerson());
    }

    public BigDecimal getWorkDays() {
        return workDays;
    }

    public DayOfWeek getWeekDayOfStartDate() {
        return getStartDate().getDayOfWeek();
    }

    public DayOfWeek getWeekDayOfEndDate() {
        return getEndDate().getDayOfWeek();
    }
}
