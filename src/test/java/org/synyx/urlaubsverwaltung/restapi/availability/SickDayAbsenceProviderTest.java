package org.synyx.urlaubsverwaltung.restapi.availability;

import org.joda.time.DateMidnight;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;

import org.synyx.urlaubsverwaltung.core.period.DayLength;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNoteService;
import org.synyx.urlaubsverwaltung.test.TestDataCreator;

import java.math.BigDecimal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * @author  Timo Eifler - eifler@synyx.de
 */
public class SickDayAbsenceProviderTest {

    private SickDayAbsenceProvider sickDayAbsenceProvider;

    private SickNoteService sickNoteService;
    private VacationAbsenceProvider vacationAbsenceProvider;

    private TimedAbsenceSpans emptyTimedAbsenceSpans;
    private Person testPerson;
    private SickNote sickNote;
    private DateMidnight sickDay;
    private DateMidnight standardWorkingDay;

    @Before
    public void setUp() {

        emptyTimedAbsenceSpans = new TimedAbsenceSpans(new ArrayList<>());
        testPerson = TestDataCreator.createPerson();
        sickNote = TestDataCreator.createSickNote(testPerson, sickDay, sickDay, DayLength.FULL);
        sickDay = new DateMidnight(2016, 1, 4);
        standardWorkingDay = new DateMidnight(2016, 1, 5);

        setupSickNoteServiceMock();
        vacationAbsenceProvider = Mockito.mock(VacationAbsenceProvider.class);

        sickDayAbsenceProvider = new SickDayAbsenceProvider(vacationAbsenceProvider, sickNoteService);
    }


    private void setupSickNoteServiceMock() {

        sickNoteService = Mockito.mock(SickNoteService.class);

        Mockito.when(sickNoteService.getByPersonAndPeriod(testPerson, sickDay, sickDay))
            .thenReturn(Collections.singletonList(sickNote));

        Mockito.when(sickNoteService.getByPersonAndPeriod(testPerson, standardWorkingDay, standardWorkingDay))
            .thenReturn(Collections.emptyList());
    }


    @Test
    public void ensurePersonIsNotAvailableOnSickDay() {

        TimedAbsenceSpans updatedTimedAbsenceSpans = sickDayAbsenceProvider.checkForAbsence(emptyTimedAbsenceSpans,
                testPerson, sickDay);

        List<TimedAbsence> absencesList = updatedTimedAbsenceSpans.getAbsencesList();

        Assert.assertEquals("wrong number of absences in list", 1, absencesList.size());
        Assert.assertEquals("wrong absence type", TimedAbsence.Type.SICK_NOTE, absencesList.get(0).getType());
        Assert.assertEquals("wrong part of day set on absence", DayLength.FULL.name(),
            absencesList.get(0).getPartOfDay());
        Assert.assertTrue("wrong absence ratio", BigDecimal.ONE.compareTo(absencesList.get(0).getRatio()) == 0);
    }


    @Test
    public void ensureDoesNotCallNextProviderIfAlreadyAbsentForWholeDay() {

        sickDayAbsenceProvider.checkForAbsence(emptyTimedAbsenceSpans, testPerson, sickDay);

        Mockito.verifyNoMoreInteractions(vacationAbsenceProvider);
    }


    @Test
    public void ensureCallsVacationAbsenceProviderIfNotAbsentForSickDay() {

        sickDayAbsenceProvider.checkForAbsence(emptyTimedAbsenceSpans, testPerson, standardWorkingDay);

        Mockito.verify(vacationAbsenceProvider, Mockito.times(1))
            .checkForAbsence(emptyTimedAbsenceSpans, testPerson, standardWorkingDay);
    }
}
