package org.synyx.urlaubsverwaltung.restapi;

import org.joda.time.DateMidnight;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;

import org.synyx.urlaubsverwaltung.core.application.service.ApplicationService;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.settings.FederalState;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNoteService;
import org.synyx.urlaubsverwaltung.core.workingtime.PublicHolidaysService;
import org.synyx.urlaubsverwaltung.core.workingtime.WorkingTime;
import org.synyx.urlaubsverwaltung.core.workingtime.WorkingTimeService;
import org.synyx.urlaubsverwaltung.test.TestDataCreator;

import java.math.BigDecimal;

import java.util.Optional;


/**
 * @author  Timo Eifler - eifler@synyx.de
 */
public class AvailabilityServiceTest {

    private AvailabilityService availabilityService;
    private ApplicationService applicationService;
    private SickNoteService sickNoteService;
    private PublicHolidaysService publicHolidaysService;
    private WorkingTimeService workingTimeService;

    private Person testPerson;
    private DateMidnight testDateRangeStart;
    private DateMidnight testDateRangeEnd;

    @Before
    public void setUp() {

        applicationService = Mockito.mock(ApplicationService.class);
        sickNoteService = Mockito.mock(SickNoteService.class);
        setupDefaultHolidaysService();
        setupDefaultWorkingTimeService();

        testPerson = TestDataCreator.createPerson();
        testDateRangeStart = DateMidnight.now().withDayOfYear(1);
        testDateRangeEnd = DateMidnight.now().withDayOfYear(5);

        availabilityService = new AvailabilityService(applicationService, this.sickNoteService, publicHolidaysService,
                workingTimeService);
    }


    private void setupDefaultHolidaysService() {

        publicHolidaysService = Mockito.mock(PublicHolidaysService.class);
        Mockito.when(publicHolidaysService.getWorkingDurationOfDate(Mockito.any(DateMidnight.class),
                    Mockito.any(FederalState.class)))
            .thenReturn(new BigDecimal(8));
    }


    private void setupDefaultWorkingTimeService() {

        WorkingTime workingTime = TestDataCreator.createWorkingTime();
        Optional<WorkingTime> defaultServiceReturn = Optional.of(workingTime);
        workingTimeService = Mockito.mock(WorkingTimeService.class);
        Mockito.when(workingTimeService.getByPersonAndValidityDateEqualsOrMinorDate(Mockito.any(Person.class),
                    Mockito.any(DateMidnight.class)))
            .thenReturn(defaultServiceReturn);
        Mockito.when(workingTimeService.getFederalStateForPerson(Mockito.any(Person.class),
                    Mockito.any(DateMidnight.class)))
            .thenReturn(FederalState.BADEN_WUERTTEMBERG);
    }


    @Test
    public void ensureFetchesAvailabilityListForEachDayInDateRange() {

        AvailabilityList personsAvailabilities = availabilityService.getPersonsAvailabilities(1, testDateRangeStart,
                testDateRangeEnd, testPerson);

        Assert.assertEquals(5, personsAvailabilities.getAvailabilities().size());
    }


    @Test
    public void ensurePersonIsNotAvailableOnHolidays() {

        Mockito.when(publicHolidaysService.getWorkingDurationOfDate(testDateRangeStart,
                    FederalState.BADEN_WUERTTEMBERG))
            .thenReturn(new BigDecimal(0));

        AvailabilityList personsAvailabilities = availabilityService.getPersonsAvailabilities(1, testDateRangeStart,
                testDateRangeEnd, testPerson);
        DayAvailability availabilityOnFirstDayOfTheYear = personsAvailabilities.getAvailabilities().get(0);

        BigDecimal hoursAvailableOnFirstDayOfTheYear = availabilityOnFirstDayOfTheYear.getHoursAvailable();
        Assert.assertEquals("Wrong number of hours available", new BigDecimal(0), hoursAvailableOnFirstDayOfTheYear);

        DayAvailability.Absence.Type absenceType = availabilityOnFirstDayOfTheYear.getAbsenceSpans().get(0).getType();
        Assert.assertEquals("Wrong absence type", DayAvailability.Absence.Type.HOLIDAY, absenceType);
    }
}
