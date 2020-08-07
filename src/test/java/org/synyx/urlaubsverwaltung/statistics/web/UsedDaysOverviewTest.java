package org.synyx.urlaubsverwaltung.statistics.web;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.application.domain.VacationCategory;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.testdatacreator.TestDataCreator;
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysService;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.application.domain.VacationCategory.HOLIDAY;
import static org.synyx.urlaubsverwaltung.testdatacreator.TestDataCreator.createApplication;
import static org.synyx.urlaubsverwaltung.testdatacreator.TestDataCreator.createVacationType;


@RunWith(MockitoJUnitRunner.class)
public class UsedDaysOverviewTest {

    @Mock
    private WorkDaysService calendarService;

    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsIfOneOfTheGivenApplicationsDoesNotMatchTheGivenYear() {

        Application application = new Application();
        application.setVacationType(createVacationType(HOLIDAY));
        application.setStartDate(Instant.from(LocalDate.of(2014, 10, 13)));
        application.setEndDate(Instant.from(LocalDate.of(2014, 10, 13)));
        application.setStatus(ApplicationStatus.WAITING);

        new UsedDaysOverview(singletonList(application), 2015, calendarService);
    }


    @Test
    public void ensureGeneratesCorrectUsedDaysOverview() {

        Application holiday = TestDataCreator.anyApplication();
        holiday.setVacationType(createVacationType(HOLIDAY));
        holiday.setStartDate(Instant.from(LocalDate.of(2014, 10, 13)));
        holiday.setEndDate(Instant.from(LocalDate.of(2014, 10, 13)));
        holiday.setStatus(ApplicationStatus.WAITING);

        Application holidayAllowed = TestDataCreator.anyApplication();
        holidayAllowed.setVacationType(createVacationType(HOLIDAY));
        holidayAllowed.setStartDate(Instant.from(LocalDate.of(2014, 10, 14)));
        holidayAllowed.setEndDate(Instant.from(LocalDate.of(2014, 10, 14)));
        holidayAllowed.setStatus(ApplicationStatus.ALLOWED);

        Application specialLeave = TestDataCreator.anyApplication();
        specialLeave.setVacationType(createVacationType(VacationCategory.SPECIALLEAVE));
        specialLeave.setStartDate(Instant.from(LocalDate.of(2014, 10, 15)));
        specialLeave.setEndDate(Instant.from(LocalDate.of(2014, 10, 15)));
        specialLeave.setStatus(ApplicationStatus.WAITING);

        Application specialLeaveAllowed = TestDataCreator.anyApplication();
        specialLeaveAllowed.setVacationType(createVacationType(VacationCategory.SPECIALLEAVE));
        specialLeaveAllowed.setStartDate(Instant.from(LocalDate.of(2014, 10, 16)));
        specialLeaveAllowed.setEndDate(Instant.from(LocalDate.of(2014, 10, 16)));
        specialLeaveAllowed.setStatus(ApplicationStatus.ALLOWED);

        Application unpaidLeave = TestDataCreator.anyApplication();
        unpaidLeave.setVacationType(createVacationType(VacationCategory.UNPAIDLEAVE));
        unpaidLeave.setStartDate(Instant.from(LocalDate.of(2014, 10, 17)));
        unpaidLeave.setEndDate(Instant.from(LocalDate.of(2014, 10, 17)));
        unpaidLeave.setStatus(ApplicationStatus.WAITING);

        Application unpaidLeaveAllowed = TestDataCreator.anyApplication();
        unpaidLeaveAllowed.setVacationType(createVacationType(VacationCategory.UNPAIDLEAVE));
        unpaidLeaveAllowed.setStartDate(Instant.from(LocalDate.of(2014, 10, 20)));
        unpaidLeaveAllowed.setEndDate(Instant.from(LocalDate.of(2014, 10, 20)));
        unpaidLeaveAllowed.setStatus(ApplicationStatus.ALLOWED);

        Application overtimeLeave = TestDataCreator.anyApplication();
        overtimeLeave.setVacationType(createVacationType(VacationCategory.OVERTIME));
        overtimeLeave.setStartDate(Instant.from(LocalDate.of(2014, 10, 21)));
        overtimeLeave.setEndDate(Instant.from(LocalDate.of(2014, 10, 21)));
        overtimeLeave.setStatus(ApplicationStatus.WAITING);

        Application overtimeLeaveAllowed = TestDataCreator.anyApplication();
        overtimeLeaveAllowed.setVacationType(createVacationType(VacationCategory.OVERTIME));
        overtimeLeaveAllowed.setStartDate(Instant.from(LocalDate.of(2014, 10, 22)));
        overtimeLeaveAllowed.setEndDate(Instant.from(LocalDate.of(2014, 10, 22)));
        overtimeLeaveAllowed.setStatus(ApplicationStatus.ALLOWED);

        List<Application> applications = Arrays.asList(holiday, holidayAllowed, specialLeave, specialLeaveAllowed,
            unpaidLeave, unpaidLeaveAllowed, overtimeLeave, overtimeLeaveAllowed);

        // just return 1 day for each application for leave
        when(calendarService.getWorkDays(any(DayLength.class), any(Instant.class),
            any(Instant.class), any(Person.class)))
            .thenReturn(ONE);

        UsedDaysOverview usedDaysOverview = new UsedDaysOverview(applications, 2014, calendarService);

        UsedDays holidayDays = usedDaysOverview.getHolidayDays();
        assertThat(holidayDays.getDays().get("WAITING")).isEqualTo(ONE);
        assertThat(holidayDays.getDays().get("ALLOWED")).isEqualTo(ONE);

        UsedDays otherDays = usedDaysOverview.getOtherDays();
        assertThat(otherDays.getDays().get("WAITING")).isEqualTo(BigDecimal.valueOf(3));
        assertThat(otherDays.getDays().get("ALLOWED")).isEqualTo(BigDecimal.valueOf(3));
    }


    @Test
    public void ensureCalculatesDaysForGivenYearForApplicationsSpanningTwoYears() {

        Person person = TestDataCreator.createPerson();
        Instant startDate = Instant.from(LocalDate.of(2013, 12, 24));
        Instant endDate = Instant.from(LocalDate.of(2014, 1, 6));

        // 3 days in 2013, 2 days in 2014
        Application holiday = createApplication(person, createVacationType(HOLIDAY), startDate, endDate, DayLength.FULL);

        when(calendarService.getWorkDays(DayLength.FULL, Instant.from(LocalDate.of(2014, 1, 1)), endDate, person))
            .thenReturn(BigDecimal.valueOf(2));

        UsedDaysOverview usedDaysOverview = new UsedDaysOverview(singletonList(holiday), 2014, calendarService);

        UsedDays holidayDays = usedDaysOverview.getHolidayDays();
        assertThat(holidayDays.getDays().get("WAITING")).isEqualTo(BigDecimal.valueOf(2));
        assertThat(holidayDays.getDays().get("ALLOWED")).isEqualTo(ZERO);

        UsedDays otherDays = usedDaysOverview.getOtherDays();
        assertThat(otherDays.getDays().get("WAITING")).isEqualTo(ZERO);
        assertThat(otherDays.getDays().get("ALLOWED")).isEqualTo(ZERO);
    }


    @Test
    public void ensureGeneratesCorrectUsedDaysOverviewConsideringTemporaryAllowedApplicationsForLeave() {

        Application holiday = TestDataCreator.anyApplication();
        holiday.setVacationType(createVacationType(HOLIDAY));
        holiday.setStartDate(Instant.from(LocalDate.of(2014, 10, 13)));
        holiday.setEndDate(Instant.from(LocalDate.of(2014, 10, 13)));
        holiday.setStatus(ApplicationStatus.WAITING);

        Application holidayAllowed = TestDataCreator.anyApplication();
        holidayAllowed.setVacationType(createVacationType(HOLIDAY));
        holidayAllowed.setStartDate(Instant.from(LocalDate.of(2014, 10, 14)));
        holidayAllowed.setEndDate(Instant.from(LocalDate.of(2014, 10, 14)));
        holidayAllowed.setStatus(ApplicationStatus.ALLOWED);

        Application holidayTemporaryAllowed = TestDataCreator.anyApplication();
        holidayTemporaryAllowed.setVacationType(createVacationType(HOLIDAY));
        holidayTemporaryAllowed.setStartDate(Instant.from(LocalDate.of(2014, 10, 15)));
        holidayTemporaryAllowed.setEndDate(Instant.from(LocalDate.of(2014, 10, 15)));
        holidayTemporaryAllowed.setStatus(ApplicationStatus.TEMPORARY_ALLOWED);

        List<Application> applications = Arrays.asList(holiday, holidayTemporaryAllowed, holidayAllowed);

        // just return 1 day for each application for leave
        when(calendarService.getWorkDays(any(DayLength.class), any(Instant.class),
            any(Instant.class), any(Person.class))).thenReturn(ONE);

        UsedDaysOverview usedDaysOverview = new UsedDaysOverview(applications, 2014, calendarService);
        assertThat(usedDaysOverview.getHolidayDays().getDays().get("WAITING")).isEqualTo(ONE);
        assertThat(usedDaysOverview.getHolidayDays().getDays().get("TEMPORARY_ALLOWED")).isEqualTo(ONE);
        assertThat(usedDaysOverview.getHolidayDays().getDays().get("ALLOWED")).isEqualTo(ONE);
    }
}
