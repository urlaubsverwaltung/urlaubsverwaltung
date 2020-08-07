package org.synyx.urlaubsverwaltung.availability.api;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.settings.FederalState;
import org.synyx.urlaubsverwaltung.testdatacreator.TestDataCreator;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTime;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeService;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class FreeTimeAbsenceProviderTest {

    private FreeTimeAbsenceProvider freeTimeAbsenceProvider;

    private PublicHolidayAbsenceProvider publicHolidayAbsenceProvider;
    private WorkingTimeService workingTimeService;
    private TimedAbsenceSpans emptyTimedAbsenceSpans;
    private Person testPerson;

    @Before
    public void setUp() {

        publicHolidayAbsenceProvider = mock(PublicHolidayAbsenceProvider.class);
        setupDefaultWorkingTimeService();

        emptyTimedAbsenceSpans = new TimedAbsenceSpans(new ArrayList<>());
        testPerson = TestDataCreator.createPerson();

        freeTimeAbsenceProvider = new FreeTimeAbsenceProvider(publicHolidayAbsenceProvider, workingTimeService);
    }


    private void setupDefaultWorkingTimeService() {

        WorkingTime testWorkingTime = TestDataCreator.createWorkingTime();
        workingTimeService = mock(WorkingTimeService.class);
        when(workingTimeService.getByPersonAndValidityDateEqualsOrMinorDate(any(Person.class),
            any(Instant.class)))
            .thenReturn(Optional.of(testWorkingTime));
        when(workingTimeService.getFederalStateForPerson(any(Person.class),
            any(Instant.class)))
            .thenReturn(FederalState.BADEN_WUERTTEMBERG);
    }


    @Test
    public void ensurePersonIsNotAvailableOnFreeDays() {

        Instant firstSundayIn2016 = Instant.from(LocalDate.of(2016, 1, 3));

        TimedAbsenceSpans updatedTimedAbsenceSpans = freeTimeAbsenceProvider.addAbsence(emptyTimedAbsenceSpans,
            testPerson, firstSundayIn2016);

        List<TimedAbsence> absencesList = updatedTimedAbsenceSpans.getAbsencesList();

        Assert.assertEquals("wrong number of absences in list", 1, absencesList.size());
        Assert.assertEquals("wrong absence type", TimedAbsence.Type.FREETIME, absencesList.get(0).getType());
        Assert.assertEquals("wrong part of day set on absence", DayLength.FULL.name(),
            absencesList.get(0).getPartOfDay());
        Assert.assertTrue("wrong absence ratio", BigDecimal.ONE.compareTo(absencesList.get(0).getRatio()) == 0);
    }

    @Test(expected = FreeTimeAbsenceException.class)
    public void ensureExceptionWhenPersonWorkingTimeIsNotAvailable() {

        Instant firstSundayIn2016 = Instant.from(LocalDate.of(2016, 1, 3));

        when(workingTimeService.getByPersonAndValidityDateEqualsOrMinorDate(eq(testPerson),
            eq(firstSundayIn2016)))
            .thenReturn(Optional.empty());

        TimedAbsenceSpans updatedTimedAbsenceSpans = freeTimeAbsenceProvider.addAbsence(emptyTimedAbsenceSpans,
            testPerson, firstSundayIn2016);
    }

    @Test
    public void ensureDoesNotCallNextProviderIfAlreadyAbsentForWholeDay() {

        Instant firstSundayIn2016 = Instant.from(LocalDate.of(2016, 1, 3));

        freeTimeAbsenceProvider.checkForAbsence(emptyTimedAbsenceSpans, testPerson, firstSundayIn2016);

        Mockito.verifyNoMoreInteractions(publicHolidayAbsenceProvider);
    }


    @Test
    public void ensureCallsHolidayAbsenceProviderIfNotAbsentForFreeTime() {

        Instant standardWorkingDay = Instant.from(LocalDate.of(2016, 1, 4));

        freeTimeAbsenceProvider.checkForAbsence(emptyTimedAbsenceSpans, testPerson, standardWorkingDay);

        verify(publicHolidayAbsenceProvider, times(1))
            .checkForAbsence(emptyTimedAbsenceSpans, testPerson, standardWorkingDay);
    }
}
