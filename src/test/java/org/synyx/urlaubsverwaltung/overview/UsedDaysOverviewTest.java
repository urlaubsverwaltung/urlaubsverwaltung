package org.synyx.urlaubsverwaltung.overview;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysCountService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.TestDataCreator.anyApplication;
import static org.synyx.urlaubsverwaltung.TestDataCreator.createApplication;
import static org.synyx.urlaubsverwaltung.TestDataCreator.createVacationType;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.ALLOWED;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.ALLOWED_CANCELLATION_REQUESTED;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.TEMPORARY_ALLOWED;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.WAITING;
import static org.synyx.urlaubsverwaltung.application.domain.VacationCategory.HOLIDAY;
import static org.synyx.urlaubsverwaltung.application.domain.VacationCategory.OVERTIME;
import static org.synyx.urlaubsverwaltung.application.domain.VacationCategory.SPECIALLEAVE;
import static org.synyx.urlaubsverwaltung.application.domain.VacationCategory.UNPAIDLEAVE;


@ExtendWith(MockitoExtension.class)
class UsedDaysOverviewTest {

    @Mock
    private WorkDaysCountService workDaysCountService;

    @Test
    void ensureThrowsIfOneOfTheGivenApplicationsDoesNotMatchTheGivenYear() {

        final Application application = new Application();
        application.setVacationType(createVacationType(HOLIDAY));
        application.setStartDate(LocalDate.of(2014, 10, 13));
        application.setEndDate(LocalDate.of(2014, 10, 13));
        application.setStatus(WAITING);

        assertThatIllegalArgumentException()
            .isThrownBy(() -> new UsedDaysOverview(singletonList(application), 2015, workDaysCountService));
    }

    @Test
    void ensureGeneratesCorrectUsedDaysOverview() {

        final Application holiday = anyApplication();
        holiday.setVacationType(createVacationType(HOLIDAY));
        holiday.setStartDate(LocalDate.of(2014, 10, 13));
        holiday.setEndDate(LocalDate.of(2014, 10, 13));
        holiday.setStatus(WAITING);

        final Application holidayAllowed = anyApplication();
        holidayAllowed.setVacationType(createVacationType(HOLIDAY));
        holidayAllowed.setStartDate(LocalDate.of(2014, 10, 14));
        holidayAllowed.setEndDate(LocalDate.of(2014, 10, 14));
        holidayAllowed.setStatus(ALLOWED);

        final Application holidayAllowedRequestCancellation = anyApplication();
        holidayAllowedRequestCancellation.setVacationType(createVacationType(HOLIDAY));
        holidayAllowedRequestCancellation.setStartDate(LocalDate.of(2014, 10, 14));
        holidayAllowedRequestCancellation.setEndDate(LocalDate.of(2014, 10, 14));
        holidayAllowedRequestCancellation.setStatus(ALLOWED_CANCELLATION_REQUESTED);

        final Application specialLeave = anyApplication();
        specialLeave.setVacationType(createVacationType(SPECIALLEAVE));
        specialLeave.setStartDate(LocalDate.of(2014, 10, 15));
        specialLeave.setEndDate(LocalDate.of(2014, 10, 15));
        specialLeave.setStatus(WAITING);

        final Application specialLeaveAllowed = anyApplication();
        specialLeaveAllowed.setVacationType(createVacationType(SPECIALLEAVE));
        specialLeaveAllowed.setStartDate(LocalDate.of(2014, 10, 16));
        specialLeaveAllowed.setEndDate(LocalDate.of(2014, 10, 16));
        specialLeaveAllowed.setStatus(ALLOWED);

        final Application unpaidLeave = anyApplication();
        unpaidLeave.setVacationType(createVacationType(UNPAIDLEAVE));
        unpaidLeave.setStartDate(LocalDate.of(2014, 10, 17));
        unpaidLeave.setEndDate(LocalDate.of(2014, 10, 17));
        unpaidLeave.setStatus(WAITING);

        final Application unpaidLeaveAllowed = anyApplication();
        unpaidLeaveAllowed.setVacationType(createVacationType(UNPAIDLEAVE));
        unpaidLeaveAllowed.setStartDate(LocalDate.of(2014, 10, 20));
        unpaidLeaveAllowed.setEndDate(LocalDate.of(2014, 10, 20));
        unpaidLeaveAllowed.setStatus(ALLOWED);

        final Application overtimeLeave = anyApplication();
        overtimeLeave.setVacationType(createVacationType(OVERTIME));
        overtimeLeave.setStartDate(LocalDate.of(2014, 10, 21));
        overtimeLeave.setEndDate(LocalDate.of(2014, 10, 21));
        overtimeLeave.setStatus(WAITING);

        final Application overtimeLeaveAllowed = anyApplication();
        overtimeLeaveAllowed.setVacationType(createVacationType(OVERTIME));
        overtimeLeaveAllowed.setStartDate(LocalDate.of(2014, 10, 22));
        overtimeLeaveAllowed.setEndDate(LocalDate.of(2014, 10, 22));
        overtimeLeaveAllowed.setStatus(ALLOWED);

        final Application overtimeLeaveRequestCancellation = anyApplication();
        overtimeLeaveRequestCancellation.setVacationType(createVacationType(OVERTIME));
        overtimeLeaveRequestCancellation.setStartDate(LocalDate.of(2014, 10, 22));
        overtimeLeaveRequestCancellation.setEndDate(LocalDate.of(2014, 10, 22));
        overtimeLeaveRequestCancellation.setStatus(ALLOWED_CANCELLATION_REQUESTED);

        final List<Application> applications = Arrays.asList(holiday, holidayAllowed, holidayAllowedRequestCancellation,
            specialLeave, specialLeaveAllowed, unpaidLeave, unpaidLeaveAllowed, overtimeLeave, overtimeLeaveAllowed,
            overtimeLeaveRequestCancellation);

        // just return 1 day for each application for leave
        when(workDaysCountService.getWorkDaysCount(any(DayLength.class), any(LocalDate.class),
            any(LocalDate.class), any(Person.class)))
            .thenReturn(ONE);

        final UsedDaysOverview usedDaysOverview = new UsedDaysOverview(applications, 2014, workDaysCountService);

        final UsedDays holidayDays = usedDaysOverview.getHolidayDays();
        assertThat(holidayDays.getDays())
            .containsEntry("WAITING", ONE)
            .containsEntry("ALLOWED", ONE)
            .containsEntry("ALLOWED_CANCELLATION_REQUESTED", ONE);

        final UsedDays otherDays = usedDaysOverview.getOtherDays();
        assertThat(otherDays.getDays())
            .containsEntry("WAITING", BigDecimal.valueOf(3))
            .containsEntry("ALLOWED", BigDecimal.valueOf(3))
            .containsEntry("ALLOWED_CANCELLATION_REQUESTED", ONE);
    }

    @Test
    void ensureCalculatesDaysForGivenYearForApplicationsSpanningTwoYears() {

        Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        LocalDate startDate = LocalDate.of(2013, 12, 24);
        LocalDate endDate = LocalDate.of(2014, 1, 6);

        // 3 days in 2013, 2 days in 2014
        Application holiday = createApplication(person, createVacationType(HOLIDAY), startDate, endDate, DayLength.FULL);

        when(workDaysCountService.getWorkDaysCount(DayLength.FULL, LocalDate.of(2014, 1, 1), endDate, person))
            .thenReturn(BigDecimal.valueOf(2));

        final UsedDaysOverview usedDaysOverview = new UsedDaysOverview(singletonList(holiday), 2014, workDaysCountService);

        final UsedDays holidayDays = usedDaysOverview.getHolidayDays();
        assertThat(holidayDays.getDays())
            .containsEntry("WAITING", BigDecimal.valueOf(2))
            .containsEntry("ALLOWED", ZERO);

        final UsedDays otherDays = usedDaysOverview.getOtherDays();
        assertThat(otherDays.getDays())
            .containsEntry("WAITING", ZERO)
            .containsEntry("ALLOWED", ZERO);
    }

    @Test
    void ensureGeneratesCorrectUsedDaysOverviewConsideringTemporaryAllowedApplicationsForLeave() {

        final Application holiday = anyApplication();
        holiday.setVacationType(createVacationType(HOLIDAY));
        holiday.setStartDate(LocalDate.of(2014, 10, 13));
        holiday.setEndDate(LocalDate.of(2014, 10, 13));
        holiday.setStatus(WAITING);

        final Application holidayAllowed = anyApplication();
        holidayAllowed.setVacationType(createVacationType(HOLIDAY));
        holidayAllowed.setStartDate(LocalDate.of(2014, 10, 14));
        holidayAllowed.setEndDate(LocalDate.of(2014, 10, 14));
        holidayAllowed.setStatus(ALLOWED);

        final Application holidayTemporaryAllowed = anyApplication();
        holidayTemporaryAllowed.setVacationType(createVacationType(HOLIDAY));
        holidayTemporaryAllowed.setStartDate(LocalDate.of(2014, 10, 15));
        holidayTemporaryAllowed.setEndDate(LocalDate.of(2014, 10, 15));
        holidayTemporaryAllowed.setStatus(TEMPORARY_ALLOWED);

        List<Application> applications = Arrays.asList(holiday, holidayTemporaryAllowed, holidayAllowed);

        // just return 1 day for each application for leave
        when(workDaysCountService.getWorkDaysCount(any(DayLength.class), any(LocalDate.class),
            any(LocalDate.class), any(Person.class))).thenReturn(ONE);

        final UsedDaysOverview usedDaysOverview = new UsedDaysOverview(applications, 2014, workDaysCountService);
        assertThat(usedDaysOverview.getHolidayDays().getDays())
            .containsEntry("WAITING", ONE)
            .containsEntry("TEMPORARY_ALLOWED", ONE)
            .containsEntry("ALLOWED", ONE);
    }
}
