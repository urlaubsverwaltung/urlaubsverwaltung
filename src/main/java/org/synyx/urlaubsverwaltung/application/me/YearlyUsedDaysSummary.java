package org.synyx.urlaubsverwaltung.application.me;

import org.synyx.urlaubsverwaltung.absence.DateRange;
import org.synyx.urlaubsverwaltung.application.application.Application;
import org.synyx.urlaubsverwaltung.application.application.ApplicationStatus;
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysCountService;

import java.math.BigDecimal;
import java.time.Year;
import java.util.List;
import java.util.Map;

import static java.time.Month.DECEMBER;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.ALLOWED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.ALLOWED_CANCELLATION_REQUESTED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.TEMPORARY_ALLOWED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.WAITING;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.HOLIDAY;

/**
 * Object to abstract how many days have been used in a year.
 */
public class YearlyUsedDaysSummary {

    private final int year;

    // used days for vacation type HOLIDAY
    private final UsedDays holidayDays;
    private final UsedDays holidayDaysAllowed;

    // used days for all the other vacation types except HOLIDAY
    private final UsedDays otherDays;
    private final UsedDays otherDaysAllowed;

    YearlyUsedDaysSummary(List<Application> applications, int year, WorkDaysCountService calendarService) {

        this.year = year;
        this.holidayDays = new UsedDays(WAITING, TEMPORARY_ALLOWED, ALLOWED, ALLOWED_CANCELLATION_REQUESTED);
        this.holidayDaysAllowed = new UsedDays(ALLOWED, ALLOWED_CANCELLATION_REQUESTED);

        this.otherDays = new UsedDays(WAITING, TEMPORARY_ALLOWED, ALLOWED, ALLOWED_CANCELLATION_REQUESTED);
        this.otherDaysAllowed = new UsedDays(ALLOWED, ALLOWED_CANCELLATION_REQUESTED);

        final List<Application> relevantApplications = applications.stream()
            .filter(application -> application.hasStatus(WAITING) || application.hasStatus(TEMPORARY_ALLOWED) || application.hasStatus(ALLOWED) || application.hasStatus(ALLOWED_CANCELLATION_REQUESTED))
            .toList();

        final DateRange yearDateRange = new DateRange(Year.of(year).atDay(1), Year.of(year).atMonth(DECEMBER).atEndOfMonth());
        final Map<Application, BigDecimal> vacationDaysByApplication = relevantApplications.isEmpty()
            ? Map.of()
            : calendarService.getWorkDaysCountForApplications(relevantApplications, yearDateRange);

        for (final Application application : relevantApplications) {
            final BigDecimal vacationDays = vacationDaysByApplication.get(application);
            final ApplicationStatus status = application.getStatus();

            if (application.getVacationType().isOfCategory(HOLIDAY)) {
                this.holidayDays.addDays(status, vacationDays);
            } else {
                this.otherDays.addDays(status, vacationDays);
            }

            if (application.hasStatus(ALLOWED) || application.hasStatus(ALLOWED_CANCELLATION_REQUESTED)) {
                if (application.getVacationType().isOfCategory(HOLIDAY)) {
                    this.holidayDaysAllowed.addDays(status, vacationDays);
                } else {
                    this.otherDaysAllowed.addDays(status, vacationDays);
                }
            }
        }
    }

    public UsedDays getHolidayDays() {
        return holidayDays;
    }

    public UsedDays getHolidayDaysAllowed() {
        return holidayDaysAllowed;
    }

    public UsedDays getOtherDays() {
        return otherDays;
    }

    public UsedDays getOtherDaysAllowed() {
        return otherDaysAllowed;
    }
}
