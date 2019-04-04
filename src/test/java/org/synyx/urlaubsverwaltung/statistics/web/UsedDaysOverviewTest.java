package org.synyx.urlaubsverwaltung.statistics.web;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.application.domain.VacationCategory;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysService;
import org.synyx.urlaubsverwaltung.testdatacreator.TestDataCreator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * Unit test for {@link org.synyx.urlaubsverwaltung.statistics.web.UsedDaysOverview}.
 */
public class UsedDaysOverviewTest {

    private WorkDaysService calendarService;

    @Before
    public void setUp() {

        calendarService = mock(WorkDaysService.class);
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsIfOneOfTheGivenApplicationsDoesNotMatchTheGivenYear() {

        Application application = new Application();
        application.setVacationType(TestDataCreator.createVacationType(VacationCategory.HOLIDAY));
        application.setStartDate(LocalDate.of(2014, 10, 13));
        application.setEndDate(LocalDate.of(2014, 10, 13));
        application.setStatus(ApplicationStatus.WAITING);

        new UsedDaysOverview(Collections.singletonList(application), 2015, calendarService);
    }


    @Test
    public void ensureGeneratesCorrectUsedDaysOverview() {

        Application holiday = TestDataCreator.anyApplication();
        holiday.setVacationType(TestDataCreator.createVacationType(VacationCategory.HOLIDAY));
        holiday.setStartDate(LocalDate.of(2014, 10, 13));
        holiday.setEndDate(LocalDate.of(2014, 10, 13));
        holiday.setStatus(ApplicationStatus.WAITING);

        Application holidayAllowed = TestDataCreator.anyApplication();
        holidayAllowed.setVacationType(TestDataCreator.createVacationType(VacationCategory.HOLIDAY));
        holidayAllowed.setStartDate(LocalDate.of(2014, 10, 14));
        holidayAllowed.setEndDate(LocalDate.of(2014, 10, 14));
        holidayAllowed.setStatus(ApplicationStatus.ALLOWED);

        Application specialLeave = TestDataCreator.anyApplication();
        specialLeave.setVacationType(TestDataCreator.createVacationType(VacationCategory.SPECIALLEAVE));
        specialLeave.setStartDate(LocalDate.of(2014, 10, 15));
        specialLeave.setEndDate(LocalDate.of(2014, 10, 15));
        specialLeave.setStatus(ApplicationStatus.WAITING);

        Application specialLeaveAllowed = TestDataCreator.anyApplication();
        specialLeaveAllowed.setVacationType(TestDataCreator.createVacationType(VacationCategory.SPECIALLEAVE));
        specialLeaveAllowed.setStartDate(LocalDate.of(2014, 10, 16));
        specialLeaveAllowed.setEndDate(LocalDate.of(2014, 10, 16));
        specialLeaveAllowed.setStatus(ApplicationStatus.ALLOWED);

        Application unpaidLeave = TestDataCreator.anyApplication();
        unpaidLeave.setVacationType(TestDataCreator.createVacationType(VacationCategory.UNPAIDLEAVE));
        unpaidLeave.setStartDate(LocalDate.of(2014, 10, 17));
        unpaidLeave.setEndDate(LocalDate.of(2014, 10, 17));
        unpaidLeave.setStatus(ApplicationStatus.WAITING);

        Application unpaidLeaveAllowed = TestDataCreator.anyApplication();
        unpaidLeaveAllowed.setVacationType(TestDataCreator.createVacationType(VacationCategory.UNPAIDLEAVE));
        unpaidLeaveAllowed.setStartDate(LocalDate.of(2014, 10, 20));
        unpaidLeaveAllowed.setEndDate(LocalDate.of(2014, 10, 20));
        unpaidLeaveAllowed.setStatus(ApplicationStatus.ALLOWED);

        Application overtimeLeave = TestDataCreator.anyApplication();
        overtimeLeave.setVacationType(TestDataCreator.createVacationType(VacationCategory.OVERTIME));
        overtimeLeave.setStartDate(LocalDate.of(2014, 10, 21));
        overtimeLeave.setEndDate(LocalDate.of(2014, 10, 21));
        overtimeLeave.setStatus(ApplicationStatus.WAITING);

        Application overtimeLeaveAllowed = TestDataCreator.anyApplication();
        overtimeLeaveAllowed.setVacationType(TestDataCreator.createVacationType(VacationCategory.OVERTIME));
        overtimeLeaveAllowed.setStartDate(LocalDate.of(2014, 10, 22));
        overtimeLeaveAllowed.setEndDate(LocalDate.of(2014, 10, 22));
        overtimeLeaveAllowed.setStatus(ApplicationStatus.ALLOWED);

        List<Application> applications = Arrays.asList(holiday, holidayAllowed, specialLeave, specialLeaveAllowed,
            unpaidLeave, unpaidLeaveAllowed, overtimeLeave, overtimeLeaveAllowed);

        // just return 1 day for each application for leave
        when(calendarService.getWorkDays(any(DayLength.class), any(LocalDate.class),
            any(LocalDate.class), any(Person.class)))
            .thenReturn(BigDecimal.ONE);

        UsedDaysOverview usedDaysOverview = new UsedDaysOverview(applications, 2014, calendarService);

        UsedDays holidayDays = usedDaysOverview.getHolidayDays();
        Assert.assertNotNull("Should not be null", holidayDays.getDays());
        Assert.assertEquals("Wrong number of waiting holiday days", BigDecimal.ONE,
            holidayDays.getDays().get("WAITING"));
        Assert.assertEquals("Wrong number of allowed holiday days", BigDecimal.ONE,
            holidayDays.getDays().get("ALLOWED"));

        UsedDays otherDays = usedDaysOverview.getOtherDays();
        Assert.assertNotNull("Should not be null", otherDays.getDays());
        Assert.assertEquals("Wrong number of waiting other days", BigDecimal.valueOf(3),
            otherDays.getDays().get("WAITING"));
        Assert.assertEquals("Wrong number of allowed other days", BigDecimal.valueOf(3),
            otherDays.getDays().get("ALLOWED"));
    }


    @Test
    public void ensureCalculatesDaysForGivenYearForApplicationsSpanningTwoYears() {

        Person person = TestDataCreator.createPerson();
        DayLength fullDay = DayLength.FULL;
        LocalDate startDate = LocalDate.of(2013, 12, 24);
        LocalDate endDate = LocalDate.of(2014, 1, 6);

        // 3 days in 2013, 2 days in 2014
        Application holiday = TestDataCreator.createApplication(person,
            TestDataCreator.createVacationType(VacationCategory.HOLIDAY), startDate, endDate, fullDay);

        when(calendarService.getWorkDays(fullDay, LocalDate.of(2014, 1, 1), endDate, person))
            .thenReturn(BigDecimal.valueOf(2));

        UsedDaysOverview usedDaysOverview = new UsedDaysOverview(Collections.singletonList(holiday), 2014,
            calendarService);

        UsedDays holidayDays = usedDaysOverview.getHolidayDays();
        Assert.assertNotNull("Should not be null", holidayDays.getDays());
        Assert.assertEquals("Wrong number of waiting holiday days", BigDecimal.valueOf(2),
            holidayDays.getDays().get("WAITING"));
        Assert.assertEquals("Wrong number of allowed holiday days", BigDecimal.ZERO,
            holidayDays.getDays().get("ALLOWED"));

        UsedDays otherDays = usedDaysOverview.getOtherDays();
        Assert.assertNotNull("Should not be null", otherDays.getDays());
        Assert.assertEquals("Wrong number of waiting other days", BigDecimal.ZERO, otherDays.getDays().get("WAITING"));
        Assert.assertEquals("Wrong number of allowed other days", BigDecimal.ZERO, otherDays.getDays().get("ALLOWED"));
    }


    @Test
    public void ensureGeneratesCorrectUsedDaysOverviewConsideringTemporaryAllowedApplicationsForLeave() {

        Application holiday = TestDataCreator.anyApplication();
        holiday.setVacationType(TestDataCreator.createVacationType(VacationCategory.HOLIDAY));
        holiday.setStartDate(LocalDate.of(2014, 10, 13));
        holiday.setEndDate(LocalDate.of(2014, 10, 13));
        holiday.setStatus(ApplicationStatus.WAITING);

        Application holidayAllowed = TestDataCreator.anyApplication();
        holidayAllowed.setVacationType(TestDataCreator.createVacationType(VacationCategory.HOLIDAY));
        holidayAllowed.setStartDate(LocalDate.of(2014, 10, 14));
        holidayAllowed.setEndDate(LocalDate.of(2014, 10, 14));
        holidayAllowed.setStatus(ApplicationStatus.ALLOWED);

        Application holidayTemporaryAllowed = TestDataCreator.anyApplication();
        holidayTemporaryAllowed.setVacationType(TestDataCreator.createVacationType(VacationCategory.HOLIDAY));
        holidayTemporaryAllowed.setStartDate(LocalDate.of(2014, 10, 15));
        holidayTemporaryAllowed.setEndDate(LocalDate.of(2014, 10, 15));
        holidayTemporaryAllowed.setStatus(ApplicationStatus.TEMPORARY_ALLOWED);

        List<Application> applications = Arrays.asList(holiday, holidayTemporaryAllowed, holidayAllowed);

        // just return 1 day for each application for leave
        when(calendarService.getWorkDays(any(DayLength.class), any(LocalDate.class),
            any(LocalDate.class), any(Person.class)))
            .thenReturn(BigDecimal.ONE);

        UsedDaysOverview usedDaysOverview = new UsedDaysOverview(applications, 2014, calendarService);

        UsedDays holidayDays = usedDaysOverview.getHolidayDays();
        Assert.assertNotNull("Should not be null", holidayDays.getDays());
        Assert.assertEquals("Wrong number of waiting holiday days", BigDecimal.ONE,
            holidayDays.getDays().get("WAITING"));
        Assert.assertEquals("Wrong number of waiting holiday days", BigDecimal.ONE,
            holidayDays.getDays().get("TEMPORARY_ALLOWED"));
        Assert.assertEquals("Wrong number of allowed holiday days", BigDecimal.ONE,
            holidayDays.getDays().get("ALLOWED"));
    }

}
