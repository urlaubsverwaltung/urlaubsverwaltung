package org.synyx.urlaubsverwaltung.sicknote.statistics;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.demodatacreator.DemoDataCreator;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteService;
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static java.time.Month.DECEMBER;
import static java.time.Month.JANUARY;
import static java.time.Month.OCTOBER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.Mockito.when;


/**
 * Unit test for {@link SickNoteStatistics}.
 */
@ExtendWith(MockitoExtension.class)
class SickNoteStatisticsTest {

    private SickNoteStatistics sut;

    @Mock
    private WorkDaysService calendarService;
    @Mock
    private SickNoteService sickNoteDAO;

    private List<SickNote> sickNotes;

    @BeforeEach
    void setUp() {

        sickNotes = new ArrayList<>();

        Person person = DemoDataCreator.createPerson();

        SickNote sickNote1 = DemoDataCreator.createSickNote(person,
            LocalDate.of(2013, OCTOBER, 7),
            LocalDate.of(2013, OCTOBER, 11), DayLength.FULL);

        SickNote sickNote2 = DemoDataCreator.createSickNote(person,
            LocalDate.of(2013, DECEMBER, 18),
            LocalDate.of(2014, JANUARY, 3), DayLength.FULL);

        sickNotes.add(sickNote1);
        sickNotes.add(sickNote2);

        when(sickNoteDAO.getNumberOfPersonsWithMinimumOneSickNote(2013)).thenReturn(7L);
        when(sickNoteDAO.getAllActiveByYear(2013)).thenReturn(sickNotes);

        when(calendarService.getWorkDays(DayLength.FULL, LocalDate.of(2013, OCTOBER, 7),
            LocalDate.of(2013, OCTOBER, 11), person))
            .thenReturn(new BigDecimal("5"));

        when(calendarService.getWorkDays(DayLength.FULL, LocalDate.of(2013, DECEMBER, 18),
            LocalDate.of(2013, DECEMBER, 31), person))
            .thenReturn(new BigDecimal("9"));

        sut = new SickNoteStatistics(2013, sickNoteDAO, calendarService);
    }

    @Test
    void testGetTotalNumberOfSickNotes() {
        Assert.assertEquals(2, sut.getTotalNumberOfSickNotes());
    }

    @Test
    void testGetTotalNumberOfSickDays() {
        Assert.assertEquals(new BigDecimal("14"), sut.getTotalNumberOfSickDays());
    }

    @Test
    void testGetAverageDurationOfDiseasePerPerson() {

        // 2 sick notes: 1st with 5 workdays and 2nd with 9 workdays --> sum = 14 workdays
        // 14 workdays / 7 persons = 2 workdays per person
        sut = new SickNoteStatistics(2013, sickNoteDAO, calendarService);

        final BigDecimal averageDurationOfDiseasePerPerson = sut.getAverageDurationOfDiseasePerPerson();
        assertThat(averageDurationOfDiseasePerPerson).isEqualByComparingTo(BigDecimal.valueOf(2));
    }

    @Test
    void testGetAverageDurationOfDiseasePerPersonDivisionByZero() {

        when(sickNoteDAO.getNumberOfPersonsWithMinimumOneSickNote(2013)).thenReturn(0L);

        sut = new SickNoteStatistics(2013, sickNoteDAO, calendarService);

        final BigDecimal averageDurationOfDiseasePerPerson = sut.getAverageDurationOfDiseasePerPerson();
        assertThat(averageDurationOfDiseasePerPerson).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void testGetTotalNumberOfSickDaysInvalidDateRange() {

        when(sickNoteDAO.getAllActiveByYear(2015)).thenReturn(sickNotes);
        assertThatIllegalArgumentException().isThrownBy(() -> new SickNoteStatistics(2015, sickNoteDAO, calendarService));
    }
}
