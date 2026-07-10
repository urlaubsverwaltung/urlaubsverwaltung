package org.synyx.urlaubsverwaltung.application.application;

import org.springframework.beans.BeanUtils;
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysCountService;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.util.SortedMap;

/**
 * Represents an extended {@link Application} with information about
 * the number of work days. (depending on working time of the person)
 */
public class ApplicationForLeave extends Application {

    private final BigDecimal workDays;
    private final SortedMap<Integer, BigDecimal> workDaysByYear;

    /**
     * Creates an {@link ApplicationForLeave} with the already calculated work days, split by the years the
     * application spans.
     * <p>
     * Prefer this constructor together with {@link WorkDaysCountService#getWorkDaysCountByYearForApplications(java.util.Collection)}
     * when creating an {@link ApplicationForLeave} for each element of a collection, to avoid one working-time query per
     * application.
     *
     * @param application    the application to extend
     * @param workDaysByYear the number of work days of the application, split by year and sorted by year
     */
    public ApplicationForLeave(Application application, SortedMap<Integer, BigDecimal> workDaysByYear) {

        // copy all the properties from the given application for leave
        BeanUtils.copyProperties(application, this);

        // not copied, must be set explicitly
        setId(application.getId());

        this.workDaysByYear = workDaysByYear;
        this.workDays = workDaysByYear.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getWorkDays() {
        return workDays;
    }

    /**
     * @return the work days split into the years the application spans, sorted by year
     */
    public SortedMap<Integer, BigDecimal> getWorkDaysByYear() {
        return workDaysByYear;
    }

    public DayOfWeek getWeekDayOfStartDate() {
        return getStartDate().getDayOfWeek();
    }

    public DayOfWeek getWeekDayOfEndDate() {
        return getEndDate().getDayOfWeek();
    }
}
