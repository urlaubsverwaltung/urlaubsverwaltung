package org.synyx.urlaubsverwaltung.application.application;

import org.springframework.beans.BeanUtils;
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysCountService;

import java.math.BigDecimal;
import java.time.DayOfWeek;

/**
 * Represents an extended {@link Application} with information about
 * the number of work days. (depending on working time of the person)
 */
public class ApplicationForLeave extends Application {

    private final BigDecimal workDays;

    public ApplicationForLeave(Application application, WorkDaysCountService workDaysCountService) {

        // copy all the properties from the given application for leave
        BeanUtils.copyProperties(application, this);

        // not copied, must be set explicitly
        setId(application.getId());

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
