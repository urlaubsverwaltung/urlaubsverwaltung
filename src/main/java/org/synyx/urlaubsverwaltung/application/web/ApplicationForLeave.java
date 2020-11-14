package org.synyx.urlaubsverwaltung.application.web;

import org.springframework.beans.BeanUtils;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.period.WeekDay;
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysCountService;

import java.math.BigDecimal;

/**
 * Represents an extended {@link org.synyx.urlaubsverwaltung.application.domain.Application} with information about
 * the number of work days. (depending on working time of the person)
 */
public class ApplicationForLeave extends Application {

    private final BigDecimal workDays;

    public ApplicationForLeave(Application application, WorkDaysCountService calendarService) {

        // copy all the properties from the given application for leave
        BeanUtils.copyProperties(application, this);

        // not copied, must be set explicitly
        setId(application.getId());

        // calculate the work days
        this.workDays = calendarService.getWorkDaysCount(getDayLength(), getStartDate(), getEndDate(), getPerson());
    }

    public BigDecimal getWorkDays() {
        return workDays;
    }

    public WeekDay getWeekDayOfStartDate() {
        return WeekDay.getByDayOfWeek(getStartDate().getDayOfWeek().getValue());
    }

    public WeekDay getWeekDayOfEndDate() {
        return WeekDay.getByDayOfWeek(getEndDate().getDayOfWeek().getValue());
    }
}
