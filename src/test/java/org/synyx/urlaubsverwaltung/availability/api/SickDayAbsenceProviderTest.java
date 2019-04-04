package org.synyx.urlaubsverwaltung.availability.api;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteService;
import org.synyx.urlaubsverwaltung.testdatacreator.TestDataCreator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class SickDayAbsenceProviderTest {

    private SickDayAbsenceProvider sickDayAbsenceProvider;

    private SickNoteService sickNoteService;
    private VacationAbsenceProvider vacationAbsenceProvider;

    private TimedAbsenceSpans emptyTimedAbsenceSpans;
    private Person testPerson;
    private SickNote sickNote;
    private LocalDate sickDay;
    private LocalDate standardWorkingDay;

    @Before
    public void setUp() {

        emptyTimedAbsenceSpans = new TimedAbsenceSpans(new ArrayList<>());
        testPerson = TestDataCreator.createPerson();
        sickNote = TestDataCreator.createSickNote(testPerson, sickDay, sickDay, DayLength.FULL);
        sickDay = LocalDate.of(2016, 1, 4);
        standardWorkingDay = LocalDate.of(2016, 1, 5);

        setupSickNoteServiceMock();
        vacationAbsenceProvider = mock(VacationAbsenceProvider.class);

        sickDayAbsenceProvider = new SickDayAbsenceProvider(vacationAbsenceProvider, sickNoteService);
    }


    private void setupSickNoteServiceMock() {

        sickNoteService = mock(SickNoteService.class);

        when(sickNoteService.getByPersonAndPeriod(testPerson, sickDay, sickDay))
            .thenReturn(Collections.singletonList(sickNote));

        when(sickNoteService.getByPersonAndPeriod(testPerson, standardWorkingDay, standardWorkingDay))
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

        verify(vacationAbsenceProvider, times(1))
            .checkForAbsence(emptyTimedAbsenceSpans, testPerson, standardWorkingDay);
    }
}
