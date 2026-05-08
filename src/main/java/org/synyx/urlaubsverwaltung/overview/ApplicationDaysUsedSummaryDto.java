package org.synyx.urlaubsverwaltung.overview;

import org.synyx.urlaubsverwaltung.application.application.Application;
import org.synyx.urlaubsverwaltung.application.application.ApplicationStatus;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysCountService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;

import static java.time.Month.DECEMBER;
import static java.util.Collections.max;
import static java.util.Collections.min;
import static java.util.List.of;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.ALLOWED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.ALLOWED_CANCELLATION_REQUESTED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.TEMPORARY_ALLOWED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.WAITING;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.HOLIDAY;

/**
 * Object to abstract how many days have been used in a year.
 */
public class ApplicationDaysUsedSummaryDto {

    private final int year;

    // used days for vacation type HOLIDAY
    private final ApplicationDaysUsedDto holidayDays;
    private final ApplicationDaysUsedDto holidayDaysAllowed;

    // used days for all the other vacation types except HOLIDAY
    private final ApplicationDaysUsedDto otherDays;
    private final ApplicationDaysUsedDto otherDaysAllowed;

    ApplicationDaysUsedSummaryDto(List<Application> applications, int year, WorkDaysCountService calendarService) {

        this.year = year;
        this.holidayDays = new ApplicationDaysUsedDto(WAITING, TEMPORARY_ALLOWED, ALLOWED, ALLOWED_CANCELLATION_REQUESTED);
        this.holidayDaysAllowed = new ApplicationDaysUsedDto(ALLOWED, ALLOWED_CANCELLATION_REQUESTED);

        this.otherDays = new ApplicationDaysUsedDto(WAITING, TEMPORARY_ALLOWED, ALLOWED, ALLOWED_CANCELLATION_REQUESTED);
        this.otherDaysAllowed = new ApplicationDaysUsedDto(ALLOWED, ALLOWED_CANCELLATION_REQUESTED);

        for (final Application application : applications) {
            if (application.hasStatus(WAITING) || application.hasStatus(TEMPORARY_ALLOWED) || application.hasStatus(ALLOWED) || application.hasStatus(ALLOWED_CANCELLATION_REQUESTED)) {
                final BigDecimal vacationDays = getVacationDays(application, calendarService);
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
    }

    public ApplicationDaysUsedDto getHolidayDays() {
        return holidayDays;
    }

    public ApplicationDaysUsedDto getHolidayDaysAllowed() {
        return holidayDaysAllowed;
    }

    public ApplicationDaysUsedDto getOtherDays() {
        return otherDays;
    }

    public ApplicationDaysUsedDto getOtherDaysAllowed() {
        return otherDaysAllowed;
    }

    private BigDecimal getVacationDays(Application application, WorkDaysCountService calendarService) {

        final DayLength dayLength = application.getDayLength();
        final LocalDate startDate = max(of(application.getStartDate(), Year.of(year).atDay(1)));
        final LocalDate endDate = min(of(application.getEndDate(), Year.of(year).atMonth(DECEMBER).atEndOfMonth()));
        final Person person = application.getPerson();

        return calendarService.getWorkDaysCount(dayLength, startDate, endDate, person);
    }
}
