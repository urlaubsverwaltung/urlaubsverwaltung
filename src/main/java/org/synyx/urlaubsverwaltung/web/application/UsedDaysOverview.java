package org.synyx.urlaubsverwaltung.web.application;

import org.joda.time.DateMidnight;

import org.springframework.util.Assert;

import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.core.application.domain.DayLength;
import org.synyx.urlaubsverwaltung.core.application.domain.VacationType;
import org.synyx.urlaubsverwaltung.core.calendar.OwnCalendarService;
import org.synyx.urlaubsverwaltung.core.person.Person;

import java.math.BigDecimal;

import java.util.List;


/**
 * Object to abstract how many days have been used in a year.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class UsedDaysOverview {

    private final int year;

    // used days for vacation type HOLIDAY
    private final UsedDays holidayDays;

    // used days for all the other vacation types except HOLIDAY
    private final UsedDays otherDays;

    public UsedDaysOverview(List<Application> applications, int year, OwnCalendarService calendarService) {

        this.year = year;
        this.holidayDays = new UsedDays(ApplicationStatus.WAITING, ApplicationStatus.ALLOWED);
        this.otherDays = new UsedDays(ApplicationStatus.WAITING, ApplicationStatus.ALLOWED);

        for (Application application : applications) {
            ApplicationStatus status = application.getStatus();

            if (ApplicationStatus.WAITING.equals(status) || ApplicationStatus.ALLOWED.equals(status)) {
                BigDecimal days;

                int yearOfStartDate = application.getStartDate().getYear();
                int yearOfEndDate = application.getEndDate().getYear();

                Assert.isTrue(yearOfStartDate == this.year || yearOfEndDate == this.year,
                    "Either start date or end date must be in the given year.");

                if (yearOfStartDate != yearOfEndDate) {
                    DateMidnight startDate = getStartDateForCalculation(application);
                    DateMidnight endDate = getEndDateForCalculation(application);
                    DayLength dayLength = application.getHowLong();
                    Person person = application.getPerson();

                    days = calendarService.getWorkDays(dayLength, startDate, endDate, person);
                } else {
                    days = application.getDays();
                }

                if (VacationType.HOLIDAY.equals(application.getVacationType())) {
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


    private DateMidnight getStartDateForCalculation(Application application) {

        if (application.getStartDate().getYear() == this.year) {
            return application.getStartDate();
        } else {
            return application.getEndDate().dayOfYear().withMinimumValue(); // 1st January
        }
    }


    private DateMidnight getEndDateForCalculation(Application application) {

        if (application.getEndDate().getYear() == this.year) {
            return application.getEndDate();
        } else {
            return application.getStartDate().dayOfYear().withMaximumValue(); // 31st December
        }
    }
}
