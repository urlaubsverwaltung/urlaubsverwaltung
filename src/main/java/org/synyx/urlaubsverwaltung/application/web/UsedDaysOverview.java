package org.synyx.urlaubsverwaltung.application.web;

import org.joda.time.DateMidnight;

import org.springframework.util.Assert;

import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.application.domain.VacationType;
import org.synyx.urlaubsverwaltung.calendar.OwnCalendarService;

import java.math.BigDecimal;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;


/**
 * Object to abstract how many days have been used in a year.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class UsedDaysOverview {

    private int year;
    private Map<VacationType, UsedDays> usedDays;
    private final List<ApplicationStatus> usedStates = Arrays.asList(ApplicationStatus.WAITING,
            ApplicationStatus.ALLOWED);

    public UsedDaysOverview(List<Application> applications, int year, OwnCalendarService calendarService) {

        this.year = year;
        this.usedDays = initializeUsedDays();

        for (Application application : applications) {
            ApplicationStatus status = application.getStatus();

            if (this.usedStates.contains(status)) {
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

                this.usedDays.get(application.getVacationType()).addDays(status, days);
            }
        }
    }

    public Map<VacationType, UsedDays> getUsedDays() {

        return usedDays;
    }


    private Map<VacationType, UsedDays> initializeUsedDays() {

        SortedMap<VacationType, UsedDays> usedDays = new TreeMap<VacationType, UsedDays>();

        for (VacationType type : VacationType.values()) {
            UsedDays usedDaysElement = new UsedDays(type);

            for (ApplicationStatus status : this.usedStates) {
                usedDaysElement.addDays(status, BigDecimal.ZERO);
            }

            usedDays.put(type, usedDaysElement);
        }

        return usedDays;
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
