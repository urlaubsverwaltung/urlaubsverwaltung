package org.synyx.urlaubsverwaltung.web.application;

import org.joda.time.DateMidnight;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.core.application.domain.DayLength;
import org.synyx.urlaubsverwaltung.core.application.domain.VacationType;
import org.synyx.urlaubsverwaltung.core.calendar.OwnCalendarService;
import org.synyx.urlaubsverwaltung.core.person.Person;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Unit test for {@link org.synyx.urlaubsverwaltung.web.application.UsedDaysOverview}.
 *
 * @author Aljona Murygina - murygina@synyx.de
 */
public class UsedDaysOverviewTest {

    private OwnCalendarService calendarService;

    @Before
    public void setUp() {

        calendarService = Mockito.mock(OwnCalendarService.class);

    }

    @Test
    public void ensureGeneratesCorrectUsedDaysOverview() {

        Application holiday = new Application();
        holiday.setVacationType(VacationType.HOLIDAY);
        holiday.setStartDate(new DateMidnight(2014, 10, 13));
        holiday.setEndDate(new DateMidnight(2014, 10, 13));
        holiday.setDays(BigDecimal.ONE);
        holiday.setStatus(ApplicationStatus.WAITING);

        Application holidayAllowed = new Application();
        holidayAllowed.setVacationType(VacationType.HOLIDAY);
        holidayAllowed.setStartDate(new DateMidnight(2014, 10, 14));
        holidayAllowed.setEndDate(new DateMidnight(2014, 10, 14));
        holidayAllowed.setDays(BigDecimal.ONE);
        holidayAllowed.setStatus(ApplicationStatus.ALLOWED);

        Application specialLeave = new Application();
        specialLeave.setVacationType(VacationType.SPECIALLEAVE);
        specialLeave.setStartDate(new DateMidnight(2014, 10, 15));
        specialLeave.setEndDate(new DateMidnight(2014, 10, 15));
        specialLeave.setDays(BigDecimal.ONE);
        specialLeave.setStatus(ApplicationStatus.WAITING);

        Application specialLeaveAllowed = new Application();
        specialLeaveAllowed.setVacationType(VacationType.SPECIALLEAVE);
        specialLeaveAllowed.setStartDate(new DateMidnight(2014, 10, 16));
        specialLeaveAllowed.setEndDate(new DateMidnight(2014, 10, 16));
        specialLeaveAllowed.setDays(BigDecimal.ONE);
        specialLeaveAllowed.setStatus(ApplicationStatus.ALLOWED);

        Application unpaidLeave = new Application();
        unpaidLeave.setVacationType(VacationType.UNPAIDLEAVE);
        unpaidLeave.setStartDate(new DateMidnight(2014, 10, 17));
        unpaidLeave.setEndDate(new DateMidnight(2014, 10, 17));
        unpaidLeave.setDays(BigDecimal.ONE);
        unpaidLeave.setStatus(ApplicationStatus.WAITING);

        Application unpaidLeaveAllowed = new Application();
        unpaidLeaveAllowed.setVacationType(VacationType.UNPAIDLEAVE);
        unpaidLeaveAllowed.setStartDate(new DateMidnight(2014, 10, 20));
        unpaidLeaveAllowed.setEndDate(new DateMidnight(2014, 10, 20));
        unpaidLeaveAllowed.setDays(BigDecimal.ONE);
        unpaidLeaveAllowed.setStatus(ApplicationStatus.ALLOWED);

        Application overtimeLeave = new Application();
        overtimeLeave.setVacationType(VacationType.OVERTIME);
        overtimeLeave.setStartDate(new DateMidnight(2014, 10, 21));
        overtimeLeave.setEndDate(new DateMidnight(2014, 10, 21));
        overtimeLeave.setDays(BigDecimal.ONE);
        overtimeLeave.setStatus(ApplicationStatus.WAITING);

        Application overtimeLeaveAllowed = new Application();
        overtimeLeaveAllowed.setVacationType(VacationType.OVERTIME);
        overtimeLeaveAllowed.setStartDate(new DateMidnight(2014, 10, 22));
        overtimeLeaveAllowed.setEndDate(new DateMidnight(2014, 10, 22));
        overtimeLeaveAllowed.setDays(BigDecimal.ONE);
        overtimeLeaveAllowed.setStatus(ApplicationStatus.ALLOWED);

        List<Application> applications = Arrays.asList(holiday, holidayAllowed, specialLeave, specialLeaveAllowed, unpaidLeave, unpaidLeaveAllowed, overtimeLeave, overtimeLeaveAllowed);
        UsedDaysOverview usedDaysOverview = new UsedDaysOverview(applications, 2014, calendarService);

        UsedDays holidayDays = usedDaysOverview.getHolidayDays();
        Assert.assertNotNull("Should not be null", holidayDays.getDays());
        Assert.assertEquals("Wrong number of waiting holiday days", BigDecimal.ONE, holidayDays.getDays().get("WAITING"));
        Assert.assertEquals("Wrong number of allowed holiday days", BigDecimal.ONE, holidayDays.getDays().get("ALLOWED"));

        UsedDays otherDays = usedDaysOverview.getOtherDays();
        Assert.assertNotNull("Should not be null", otherDays.getDays());
        Assert.assertEquals("Wrong number of waiting other days", BigDecimal.valueOf(3), otherDays.getDays().get("WAITING"));
        Assert.assertEquals("Wrong number of allowed other days", BigDecimal.valueOf(3), otherDays.getDays().get("ALLOWED"));

    }

    @Test
    public void ensureCalculatesDaysForGivenYearForApplicationsSpanningTwoYears() {

        Person person = new Person();
        DayLength fullDay = DayLength.FULL;
        DateMidnight startDate = new DateMidnight(2013, 12, 24);
        DateMidnight endDate = new DateMidnight(2014, 1, 6);


        Application holiday = new Application();
        holiday.setVacationType(VacationType.HOLIDAY);
        // 3 days in 2013
        holiday.setStartDate(startDate);
        // 2 days in 2014
        holiday.setEndDate(endDate);
        // sum is 5 days
        holiday.setDays(BigDecimal.valueOf(5));
        holiday.setStatus(ApplicationStatus.WAITING);
        holiday.setPerson(person);
        holiday.setHowLong(fullDay);

        Mockito.when(calendarService.getWorkDays(fullDay, new DateMidnight(2014, 1, 1), endDate, person)).thenReturn(BigDecimal.valueOf(2));

        UsedDaysOverview usedDaysOverview = new UsedDaysOverview(Arrays.asList(holiday), 2014, calendarService);

        UsedDays holidayDays = usedDaysOverview.getHolidayDays();
        Assert.assertNotNull("Should not be null", holidayDays.getDays());
        Assert.assertEquals("Wrong number of waiting holiday days", BigDecimal.valueOf(2), holidayDays.getDays().get("WAITING"));
        Assert.assertEquals("Wrong number of waiting holiday days", BigDecimal.ZERO, holidayDays.getDays().get("ALLOWED"));

        UsedDays otherDays = usedDaysOverview.getOtherDays();
        Assert.assertNotNull("Should not be null", otherDays.getDays());
        Assert.assertEquals("Wrong number of waiting other days", BigDecimal.ZERO, otherDays.getDays().get("WAITING"));
        Assert.assertEquals("Wrong number of allowed other days", BigDecimal.ZERO, otherDays.getDays().get("ALLOWED"));

    }
}