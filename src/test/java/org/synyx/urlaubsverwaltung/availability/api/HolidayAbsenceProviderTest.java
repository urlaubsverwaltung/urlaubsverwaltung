package org.synyx.urlaubsverwaltung.availability.api;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.settings.FederalState;
import org.synyx.urlaubsverwaltung.testdatacreator.TestDataCreator;
import org.synyx.urlaubsverwaltung.workingtime.PublicHolidaysService;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class HolidayAbsenceProviderTest {

    private HolidayAbsenceProvider holidayAbsenceProvider;

    private SickDayAbsenceProvider sickDayAbsenceProvider;
    private WorkingTimeService workingTimeService;
    private PublicHolidaysService publicHolidaysService;

    private TimedAbsenceSpans emptyTimedAbsenceSpans;
    private Person testPerson;
    private LocalDate newYearsDay;
    private LocalDate standardWorkingDay;

    @Before
    public void setUp() {

        emptyTimedAbsenceSpans = new TimedAbsenceSpans(new ArrayList<>());
        testPerson = TestDataCreator.createPerson();
        newYearsDay = LocalDate.of(2016, 1, 1);
        standardWorkingDay = LocalDate.of(2016, 1, 4);

        sickDayAbsenceProvider = mock(SickDayAbsenceProvider.class);
        setupWorkingTimeServiceMock();
        setupHolidayServiceMock();

        holidayAbsenceProvider = new HolidayAbsenceProvider(sickDayAbsenceProvider, publicHolidaysService,
            workingTimeService);
    }


    private void setupWorkingTimeServiceMock() {

        workingTimeService = mock(WorkingTimeService.class);
        when(workingTimeService.getFederalStateForPerson(any(Person.class),
            any(LocalDate.class)))
            .thenReturn(FederalState.BADEN_WUERTTEMBERG);
    }


    private void setupHolidayServiceMock() {

        publicHolidaysService = mock(PublicHolidaysService.class);

        when(publicHolidaysService.getWorkingDurationOfDate(newYearsDay, FederalState.BADEN_WUERTTEMBERG))
            .thenReturn(new BigDecimal(0));

        when(publicHolidaysService.getWorkingDurationOfDate(standardWorkingDay,
            FederalState.BADEN_WUERTTEMBERG))
            .thenReturn(new BigDecimal(1));
    }


    @Test
    public void ensurePersonIsNotAvailableOnHoliDays() {

        TimedAbsenceSpans updatedTimedAbsenceSpans = holidayAbsenceProvider.addAbsence(emptyTimedAbsenceSpans,
            testPerson, newYearsDay);

        List<TimedAbsence> absencesList = updatedTimedAbsenceSpans.getAbsencesList();

        Assert.assertEquals("wrong number of absences in list", 1, absencesList.size());
        Assert.assertEquals("wrong absence type", TimedAbsence.Type.HOLIDAY, absencesList.get(0).getType());
        Assert.assertEquals("wrong part of day set on absence", DayLength.FULL.name(),
            absencesList.get(0).getPartOfDay());
        Assert.assertTrue("wrong absence ratio", BigDecimal.ONE.compareTo(absencesList.get(0).getRatio()) == 0);
    }


    @Test
    public void ensureDoesNotCallNextProviderIfAlreadyAbsentForWholeDay() {

        holidayAbsenceProvider.checkForAbsence(emptyTimedAbsenceSpans, testPerson, newYearsDay);

        Mockito.verifyNoMoreInteractions(sickDayAbsenceProvider);
    }


    @Test
    public void ensureCallsSickDayAbsenceProviderIfNotAbsentForHoliday() {

        holidayAbsenceProvider.checkForAbsence(emptyTimedAbsenceSpans, testPerson, standardWorkingDay);

        verify(sickDayAbsenceProvider, times(1))
            .checkForAbsence(emptyTimedAbsenceSpans, testPerson, standardWorkingDay);
    }
}
