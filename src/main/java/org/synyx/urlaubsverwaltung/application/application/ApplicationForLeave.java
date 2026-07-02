package org.synyx.urlaubsverwaltung.application.application;

import org.springframework.beans.BeanUtils;
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysCountService;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Represents an extended {@link Application} with information about
 * the number of work days. (depending on working time of the person)
 */
public class ApplicationForLeave extends Application {

    private final BigDecimal workDays;
    private final SortedMap<Integer, BigDecimal> workDaysByYear;

    public ApplicationForLeave(Application application, WorkDaysCountService workDaysCountService) {

        // copy all the properties from the given application for leave
        BeanUtils.copyProperties(application, this);

        // not copied, must be set explicitly
        setId(application.getId());

        // calculate the work days
        this.workDays = workDaysCountService.getWorkDaysCount(getDayLength(), getStartDate(), getEndDate(), getPerson());
        this.workDaysByYear = workDaysByYear(workDaysCountService);
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

    private SortedMap<Integer, BigDecimal> workDaysByYear(WorkDaysCountService workDaysCountService) {
        if (getStartDate().getYear() == getEndDate().getYear()) {
            final SortedMap<Integer, BigDecimal> singleYear = new TreeMap<>();
            singleYear.put(getStartDate().getYear(), workDays);
            return singleYear;
        }
        return workDaysCountService.getWorkDaysCountByYear(getDayLength(), getStartDate(), getEndDate(), getPerson());
    }

    public DayOfWeek getWeekDayOfStartDate() {
        return getStartDate().getDayOfWeek();
    }

    public DayOfWeek getWeekDayOfEndDate() {
        return getEndDate().getDayOfWeek();
    }
}
