package org.synyx.urlaubsverwaltung.statistics.web;

import org.springframework.util.Assert;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.application.domain.VacationCategory;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.util.DateUtil;
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;


/**
 * Object to abstract how many days have been used in a year.
 */
public class UsedDaysOverview {

    private final int year;

    // used days for vacation type HOLIDAY
    private final UsedDays holidayDays;

    // used days for all the other vacation types except HOLIDAY
    private final UsedDays otherDays;

    public UsedDaysOverview(List<Application> applications, int year, WorkDaysService calendarService) {

        this.year = year;
        this.holidayDays = new UsedDays(ApplicationStatus.WAITING, ApplicationStatus.ALLOWED,
            ApplicationStatus.TEMPORARY_ALLOWED);
        this.otherDays = new UsedDays(ApplicationStatus.WAITING, ApplicationStatus.ALLOWED,
            ApplicationStatus.TEMPORARY_ALLOWED);

        for (Application application : applications) {
            ApplicationStatus status = application.getStatus();

            if (application.hasStatus(ApplicationStatus.WAITING) || application.hasStatus(ApplicationStatus.ALLOWED)
                || application.hasStatus(ApplicationStatus.TEMPORARY_ALLOWED)) {
                BigDecimal days = getVacationDays(application, calendarService);

                if (application.getVacationType().isOfCategory(VacationCategory.HOLIDAY)) {
                    this.holidayDays.addDays(status, days);
                } else {
                    this.otherDays.addDays(status, days);
                }
            }
        }
    }

    public UsedDays getHolidayDays() {

        return holidayDays;
    }


    public UsedDays getOtherDays() {

        return otherDays;
    }


    private BigDecimal getVacationDays(Application application, WorkDaysService calendarService) {

        int yearOfStartDate = application.getStartDate().getYear();
        int yearOfEndDate = application.getEndDate().getYear();

        Assert.isTrue(yearOfStartDate == this.year || yearOfEndDate == this.year,
            "Either start date or end date must be in the given year.");

        DayLength dayLength = application.getDayLength();
        Person person = application.getPerson();

        if (yearOfStartDate != yearOfEndDate) {
            LocalDate startDate = getStartDateForCalculation(application);
            LocalDate endDate = getEndDateForCalculation(application);

            return calendarService.getWorkDays(dayLength, startDate, endDate, person);
        }

        return calendarService.getWorkDays(dayLength, application.getStartDate(), application.getEndDate(), person);
    }


    private LocalDate getStartDateForCalculation(Application application) {

        if (application.getStartDate().getYear() != this.year) {
            return DateUtil.getFirstDayOfYear(application.getEndDate().getYear());
        }

        return application.getStartDate();
    }


    private LocalDate getEndDateForCalculation(Application application) {

        if (application.getEndDate().getYear() != this.year) {
            return DateUtil.getLastDayOfYear(application.getStartDate().getYear());
        }

        return application.getEndDate();
    }
}
