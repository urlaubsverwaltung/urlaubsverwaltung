package org.synyx.urlaubsverwaltung.restapi.availability;

import org.joda.time.DateMidnight;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.synyx.urlaubsverwaltung.core.period.DayLength;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.settings.FederalState;
import org.synyx.urlaubsverwaltung.core.workingtime.PublicHolidaysService;
import org.synyx.urlaubsverwaltung.core.workingtime.WorkingTimeService;
import org.synyx.urlaubsverwaltung.test.TestDataCreator;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * @author  Timo Eifler - eifler@synyx.de
 */
public class HolidayAbsenceProviderTest {

    private HolidayAbsenceProvider holidayAbsenceProvider;

    private SickDayAbsenceProvider sickDayAbsenceProvider;
    private WorkingTimeService workingTimeService;
    private PublicHolidaysService publicHolidaysService;

    private TimedAbsenceSpans emptyTimedAbsenceSpans;
    private Person testPerson;
    private DateMidnight newYearsDay;
    private DateMidnight standardWorkingDay;

    @Before
    public void setUp() {

        emptyTimedAbsenceSpans = new TimedAbsenceSpans(new ArrayList<>());
        testPerson = TestDataCreator.createPerson();
        newYearsDay = new DateMidnight(2016, 1, 1);
        standardWorkingDay = new DateMidnight(2016, 1, 4);

        sickDayAbsenceProvider = mock(SickDayAbsenceProvider.class);
        setupWorkingTimeServiceMock();
        setupHolidayServiceMock();

        holidayAbsenceProvider = new HolidayAbsenceProvider(sickDayAbsenceProvider, publicHolidaysService,
                workingTimeService);
    }


    private void setupWorkingTimeServiceMock() {

        workingTimeService = mock(WorkingTimeService.class);
        when(workingTimeService.getFederalStateForPerson(Mockito.any(Person.class),
                    Mockito.any(DateMidnight.class)))
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

        verify(sickDayAbsenceProvider, Mockito.times(1))
            .checkForAbsence(emptyTimedAbsenceSpans, testPerson, standardWorkingDay);
    }
}
