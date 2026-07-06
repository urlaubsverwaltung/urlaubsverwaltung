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

    /**
     * Creates an {@link ApplicationForLeave} with the already calculated number of work days.
     * <p>
     * Prefer this constructor together with {@link WorkDaysCountService#getWorkDaysCountForApplications(java.util.Collection)}
     * when creating an {@link ApplicationForLeave} for each element of a collection, to avoid one working-time query per
     * application.
     *
     * @param application the application to extend
     * @param workDays    the number of work days of the application
     */
    public ApplicationForLeave(Application application, BigDecimal workDays) {

        // copy all the properties from the given application for leave
        BeanUtils.copyProperties(application, this);

        // not copied, must be set explicitly
        setId(application.getId());

        this.workDays = workDays;
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
