package org.synyx.urlaubsverwaltung.restapi.availability;

import org.joda.time.DateMidnight;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;

import org.synyx.urlaubsverwaltung.core.period.DayLength;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.settings.FederalState;
import org.synyx.urlaubsverwaltung.core.workingtime.WorkingTime;
import org.synyx.urlaubsverwaltung.core.workingtime.WorkingTimeService;
import org.synyx.urlaubsverwaltung.test.TestDataCreator;

import java.math.BigDecimal;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


/**
 * @author  Timo Eifler - eifler@synyx.de
 */
public class FreeTimeAbsenceProviderTest {

    private FreeTimeAbsenceProvider freeTimeAbsenceProvider;

    private HolidayAbsenceProvider holidayAbsenceProvider;
    private WorkingTimeService workingTimeService;
    private TimedAbsenceSpans emptyTimedAbsenceSpans;
    private Person testPerson;
    private WorkingTime testWorkingTime;

    @Before
    public void setUp() {

        holidayAbsenceProvider = Mockito.mock(HolidayAbsenceProvider.class);
        setupDefaultWorkingTimeService();

        emptyTimedAbsenceSpans = new TimedAbsenceSpans(new ArrayList<>());
        testPerson = TestDataCreator.createPerson();

        freeTimeAbsenceProvider = new FreeTimeAbsenceProvider(holidayAbsenceProvider, workingTimeService);
    }


    private void setupDefaultWorkingTimeService() {

        testWorkingTime = TestDataCreator.createWorkingTime();
        workingTimeService = Mockito.mock(WorkingTimeService.class);
        Mockito.when(workingTimeService.getByPersonAndValidityDateEqualsOrMinorDate(Mockito.any(Person.class),
                    Mockito.any(DateMidnight.class)))
            .thenReturn(Optional.of(testWorkingTime));
        Mockito.when(workingTimeService.getFederalStateForPerson(Mockito.any(Person.class),
                    Mockito.any(DateMidnight.class)))
            .thenReturn(FederalState.BADEN_WUERTTEMBERG);
    }


    @Test
    public void ensurePersonIsNotAvailableOnFreeDays() {

        DateMidnight firstSundayIn2016 = new DateMidnight(2016, 1, 3);

        TimedAbsenceSpans updatedTimedAbsenceSpans = freeTimeAbsenceProvider.addAbsence(emptyTimedAbsenceSpans,
                testPerson, firstSundayIn2016);

        List<TimedAbsence> absencesList = updatedTimedAbsenceSpans.getAbsencesList();

        Assert.assertEquals("wrong number of absences in list", 1, absencesList.size());
        Assert.assertEquals("wrong absence type", TimedAbsence.Type.FREETIME, absencesList.get(0).getType());
        Assert.assertEquals("wrong part of day set on absence", DayLength.FULL.name(),
            absencesList.get(0).getPartOfDay());
        Assert.assertTrue("wrong absence ratio", BigDecimal.ONE.compareTo(absencesList.get(0).getRatio()) == 0);
    }


    @Test
    public void ensureDoesNotCallNextProviderIfAlreadyAbsentForWholeDay() {

        DateMidnight firstSundayIn2016 = new DateMidnight(2016, 1, 3);

        freeTimeAbsenceProvider.checkForAbsence(emptyTimedAbsenceSpans, testPerson, firstSundayIn2016);

        Mockito.verifyNoMoreInteractions(holidayAbsenceProvider);
    }


    @Test
    public void ensureCallsHolidayAbsenceProviderIfNotAbsentForFreeTime() {

        DateMidnight standardWorkingDay = new DateMidnight(2016, 1, 4);

        freeTimeAbsenceProvider.checkForAbsence(emptyTimedAbsenceSpans, testPerson, standardWorkingDay);

        Mockito.verify(holidayAbsenceProvider, Mockito.times(1))
            .checkForAbsence(emptyTimedAbsenceSpans, testPerson, standardWorkingDay);
    }
}
