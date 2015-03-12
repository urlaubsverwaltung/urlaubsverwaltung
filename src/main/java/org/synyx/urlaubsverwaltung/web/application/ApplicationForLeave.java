package org.synyx.urlaubsverwaltung.web.application;

import org.springframework.beans.BeanUtils;

import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.calendar.OwnCalendarService;

import java.math.BigDecimal;


/**
 * Represents an extended {@link org.synyx.urlaubsverwaltung.core.application.domain.Application} with information about
 * the number of work days. (depending on working time of the person)
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class ApplicationForLeave extends Application {

    private BigDecimal workDays = BigDecimal.ZERO;

    public ApplicationForLeave(Application application, OwnCalendarService calendarService) {

        // copy all the properties from the given application for leave
        BeanUtils.copyProperties(application, this);

        // not copied, must be set explicitly
        setId(application.getId());

        // calculate the work days
        this.workDays = calendarService.getWorkDays(getHowLong(), getStartDate(), getEndDate(), getPerson());
    }

    public BigDecimal getWorkDays() {

        return workDays;
    }
}
