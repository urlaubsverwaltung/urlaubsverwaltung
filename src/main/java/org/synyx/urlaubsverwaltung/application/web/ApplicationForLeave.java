package org.synyx.urlaubsverwaltung.application.web;

import org.springframework.beans.BeanUtils;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.period.WeekDay;
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysService;

import java.math.BigDecimal;
import java.time.temporal.ChronoField;

import static java.time.temporal.ChronoField.DAY_OF_WEEK;


/**
 * Represents an extended {@link org.synyx.urlaubsverwaltung.application.domain.Application} with information about
 * the number of work days. (depending on working time of the person)
 */
public class ApplicationForLeave extends Application {

    private final BigDecimal workDays;

    public ApplicationForLeave(Application application, WorkDaysService calendarService) {

        // copy all the properties from the given application for leave
        BeanUtils.copyProperties(application, this);

        // not copied, must be set explicitly
        setId(application.getId());

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
