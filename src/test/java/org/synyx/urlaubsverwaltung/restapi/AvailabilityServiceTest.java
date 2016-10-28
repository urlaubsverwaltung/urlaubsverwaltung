package org.synyx.urlaubsverwaltung.restapi;

import org.joda.time.DateMidnight;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;

import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.application.service.ApplicationService;
import org.synyx.urlaubsverwaltung.core.period.DayLength;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.settings.FederalState;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNoteService;
import org.synyx.urlaubsverwaltung.core.workingtime.PublicHolidaysService;
import org.synyx.urlaubsverwaltung.core.workingtime.WorkingTime;
import org.synyx.urlaubsverwaltung.core.workingtime.WorkingTimeService;
import org.synyx.urlaubsverwaltung.test.TestDataCreator;

import java.math.BigDecimal;

import java.util.Collections;
import java.util.Optional;


/**
 * @author  Timo Eifler - eifler@synyx.de
 */
public class AvailabilityServiceTest {

    private static final int DAYS_IN_TEST_DATE_RANGE = 8;

    private AvailabilityService availabilityService;
    private ApplicationService applicationService;
    private SickNoteService sickNoteService;
    private PublicHolidaysService publicHolidaysService;
    private WorkingTimeService workingTimeService;

    private Person testPerson;
    private WorkingTime testWorkingTime;
    private DateMidnight testDateRangeStart;
    private DateMidnight testDateRangeEnd;

    @Before
    public void setUp() {

        testPerson = TestDataCreator.createPerson();
        testWorkingTime = TestDataCreator.createWorkingTime();
        testDateRangeStart = new DateMidnight(2016, 1, 1);
        testDateRangeEnd = new DateMidnight(2016, 1, DAYS_IN_TEST_DATE_RANGE);

        applicationService = Mockito.mock(ApplicationService.class);
        sickNoteService = Mockito.mock(SickNoteService.class);
        setupDefaultHolidaysService();
        setupDefaultWorkingTimeService();

        availabilityService = new AvailabilityService(applicationService, this.sickNoteService, publicHolidaysService,
                workingTimeService);
    }


    private void setupDefaultHolidaysService() {

        publicHolidaysService = Mockito.mock(PublicHolidaysService.class);
        Mockito.when(publicHolidaysService.getWorkingDurationOfDate(Mockito.any(DateMidnight.class),
                    Mockito.any(FederalState.class)))
            .thenReturn(new BigDecimal(1));
    }


    private void setupDefaultWorkingTimeService() {

        workingTimeService = Mockito.mock(WorkingTimeService.class);
        Mockito.when(workingTimeService.getByPersonAndValidityDateEqualsOrMinorDate(Mockito.any(Person.class),
                    Mockito.any(DateMidnight.class)))
            .thenReturn(Optional.of(testWorkingTime));
        Mockito.when(workingTimeService.getFederalStateForPerson(Mockito.any(Person.class),
                    Mockito.any(DateMidnight.class)))
            .thenReturn(FederalState.BADEN_WUERTTEMBERG);
    }


    @Test
    public void ensureFetchesAvailabilityListForEachDayInDateRange() {

        AvailabilityList personsAvailabilities = availabilityService.getPersonsAvailabilities(1, testDateRangeStart,
                testDateRangeEnd, testPerson);

        Assert.assertEquals(8, personsAvailabilities.getAvailabilities().size());
    }


    @Test
    public void ensurePersonIsNotAvailableOnFreeDays() {

        DateMidnight firstSundayIn2016 = new DateMidnight(2016, 1, 3);
        AvailabilityList personsAvailabilities = availabilityService.getPersonsAvailabilities(1, firstSundayIn2016,
                firstSundayIn2016, testPerson);
        DayAvailability availabilityOnSunday = personsAvailabilities.getAvailabilities().get(0);

        BigDecimal ratioAvailableOnSunday = availabilityOnSunday.getAvailabilityRatio();
        Assert.assertTrue("Wrong ratio of availability", BigDecimal.ZERO.compareTo(ratioAvailableOnSunday) == 0);

        DayAvailability.TimedAbsence.Type absenceType = availabilityOnSunday.getAbsenceSpans().get(0).getType();
        Assert.assertEquals("Wrong absence type", DayAvailability.TimedAbsence.Type.FREETIME, absenceType);
    }


    @Test
    public void ensurePersonIsNotAvailableOnHolidays() {

        Mockito.when(publicHolidaysService.getWorkingDurationOfDate(testDateRangeStart,
                    FederalState.BADEN_WUERTTEMBERG))
            .thenReturn(new BigDecimal(0));

        AvailabilityList personsAvailabilities = availabilityService.getPersonsAvailabilities(1, testDateRangeStart,
                testDateRangeEnd, testPerson);
        DayAvailability availabilityOnFirstDayOfTheYear = personsAvailabilities.getAvailabilities().get(0);

        BigDecimal hoursAvailableOnFirstDayOfTheYear = availabilityOnFirstDayOfTheYear.getAvailabilityRatio();
        Assert.assertTrue("Wrong ratio of availability",
            BigDecimal.ZERO.compareTo(hoursAvailableOnFirstDayOfTheYear) == 0);

        DayAvailability.TimedAbsence.Type absenceType = availabilityOnFirstDayOfTheYear.getAbsenceSpans()
                .get(0)
                .getType();
        Assert.assertEquals("Wrong absence type", DayAvailability.TimedAbsence.Type.HOLIDAY, absenceType);
    }


    @Test
    public void ensurePersonIsNotAvailableOnVacation() {

        Application application = TestDataCreator.createApplication(testPerson, new DateMidnight(2016, 1, 4),
                new DateMidnight(2016, 1, 5), DayLength.FULL);

        Mockito.when(applicationService.getApplicationsForACertainPeriodAndPerson(Mockito.any(DateMidnight.class),
                    Mockito.any(DateMidnight.class), Mockito.any(Person.class)))
            .thenReturn(Collections.singletonList(application));

        AvailabilityList personsAvailabilities = availabilityService.getPersonsAvailabilities(1, testDateRangeStart,
                testDateRangeEnd, testPerson);

        DayAvailability availabilityOnSecondDayOfVacation = personsAvailabilities.getAvailabilities().get(4);

        BigDecimal hoursAvailableOnFirstDayOfTheYear = availabilityOnSecondDayOfVacation.getAvailabilityRatio();
        Assert.assertTrue("Wrong ratio of availability",
            BigDecimal.ZERO.compareTo(hoursAvailableOnFirstDayOfTheYear) == 0);

        DayAvailability.TimedAbsence.Type absenceType = availabilityOnSecondDayOfVacation.getAbsenceSpans()
                .get(0)
                .getType();
        Assert.assertEquals("Wrong absence type", DayAvailability.TimedAbsence.Type.VACATION, absenceType);
    }


    @Test
    public void ensurePersonIsNotAvailableOnSickDay() {

        SickNote sickNote = TestDataCreator.createSickNote(testPerson, new DateMidnight(2016, 1, 4),
                new DateMidnight(2016, 1, 5), DayLength.FULL);

        Mockito.when(sickNoteService.getByPersonAndPeriod(Mockito.any(Person.class), Mockito.any(DateMidnight.class),
                    Mockito.any(DateMidnight.class)))
            .thenReturn(Collections.singletonList(sickNote));

        AvailabilityList personsAvailabilities = availabilityService.getPersonsAvailabilities(1, testDateRangeStart,
                testDateRangeEnd, testPerson);

        DayAvailability availabilityOnSecondDayOfVacation = personsAvailabilities.getAvailabilities().get(4);

        BigDecimal hoursAvailableOnFirstDayOfTheYear = availabilityOnSecondDayOfVacation.getAvailabilityRatio();
        Assert.assertTrue("Wrong ratio of availability",
            BigDecimal.ZERO.compareTo(hoursAvailableOnFirstDayOfTheYear) == 0);

        DayAvailability.TimedAbsence.Type absenceType = availabilityOnSecondDayOfVacation.getAbsenceSpans()
                .get(0)
                .getType();
        Assert.assertEquals("Wrong absence type", DayAvailability.TimedAbsence.Type.SICK_NOTE, absenceType);
    }
}
