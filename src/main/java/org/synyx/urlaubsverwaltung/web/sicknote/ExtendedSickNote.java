package org.synyx.urlaubsverwaltung.web.sicknote;

import org.springframework.beans.BeanUtils;

import org.synyx.urlaubsverwaltung.core.application.domain.DayLength;
import org.synyx.urlaubsverwaltung.core.calendar.WorkDaysService;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNote;

import java.math.BigDecimal;


/**
 * Represents an extended {@link org.synyx.urlaubsverwaltung.core.sicknote.SickNote} with information about the number
 * of work days. (depending on working time of the person)
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class ExtendedSickNote extends SickNote {

    private BigDecimal workDays = BigDecimal.ZERO;

    public ExtendedSickNote(SickNote sickNote, WorkDaysService calendarService) {

        // copy all the properties from the given sick note
        BeanUtils.copyProperties(sickNote, this);

        // not copied, must be set explicitly
        setId(sickNote.getId());

        // calculate the work days
        this.workDays = calendarService.getWorkDays(DayLength.FULL, getStartDate(), getEndDate(), getPerson());
    }

    public BigDecimal getWorkDays() {

        return workDays;
    }
}
