package org.synyx.urlaubsverwaltung.application.web;

import org.joda.time.DateMidnight;

import org.springframework.util.Assert;

import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.calendar.OwnCalendarService;

import java.math.BigDecimal;

import java.util.List;


/**
 * Object to abstract how many days have been used in a year.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class UsedDaysOverview {

    private int year;
    private BigDecimal numberOfHolidayDays = BigDecimal.ZERO;
    private BigDecimal numberOfSpecialLeaveDays = BigDecimal.ZERO;
    private BigDecimal numberOfUnpaidLeaveDays = BigDecimal.ZERO;
    private BigDecimal numberOfOvertimeDays = BigDecimal.ZERO;

    public UsedDaysOverview(List<Application> applications, int year, OwnCalendarService calendarService) {

        this.year = year;

        for (Application application : applications) {
            ApplicationStatus status = application.getStatus();

            if (status == ApplicationStatus.ALLOWED || status == ApplicationStatus.WAITING) {
                BigDecimal days;

                int yearOfStartDate = application.getStartDate().getYear();
                int yearOfEndDate = application.getEndDate().getYear();

                Assert.isTrue(yearOfStartDate == this.year || yearOfEndDate == this.year,
                    "Either start date or end date must be in the given year.");

                if (yearOfStartDate != yearOfEndDate) {
                    DateMidnight startDate = getStartDateForCalculation(application);
                    DateMidnight endDate = getEndDateForCalculation(application);

                    days = calendarService.getWorkDays(application.getHowLong(), startDate, endDate);
                } else {
                    days = application.getDays();
                }

                addDays(application, days);
            }
        }
    }

    public BigDecimal getNumberOfHolidayDays() {

        return numberOfHolidayDays;
    }


    public BigDecimal getNumberOfSpecialLeaveDays() {

        return numberOfSpecialLeaveDays;
    }


    public BigDecimal getNumberOfUnpaidLeaveDays() {

        return numberOfUnpaidLeaveDays;
    }


    public BigDecimal getNumberOfOvertimeDays() {

        return numberOfOvertimeDays;
    }


    private void addDays(Application application, BigDecimal days) {

        switch (application.getVacationType()) {
            case HOLIDAY:
                numberOfHolidayDays = numberOfHolidayDays.add(days);
                break;

            case SPECIALLEAVE:
                numberOfSpecialLeaveDays = numberOfSpecialLeaveDays.add(days);
                break;

            case UNPAIDLEAVE:
                numberOfUnpaidLeaveDays = numberOfUnpaidLeaveDays.add(days);
                break;

            case OVERTIME:
                numberOfOvertimeDays = numberOfOvertimeDays.add(days);
                break;
        }
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
