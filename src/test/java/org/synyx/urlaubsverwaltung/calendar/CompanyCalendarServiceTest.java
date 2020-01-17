package org.synyx.urlaubsverwaltung.calendar;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.synyx.urlaubsverwaltung.absence.AbsenceService;
import org.synyx.urlaubsverwaltung.calendarintegration.absence.Absence;
import org.synyx.urlaubsverwaltung.calendarintegration.absence.AbsenceTimeConfiguration;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.period.Period;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.settings.CalendarSettings;

import java.time.LocalDate;
import java.util.List;

import static java.time.format.DateTimeFormatter.ofPattern;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;
import static org.synyx.urlaubsverwaltung.testdatacreator.TestDataCreator.createPerson;


@RunWith(MockitoJUnitRunner.class)
public class CompanyCalendarServiceTest {

    private CompanyCalendarService sut;

    @Mock
    private AbsenceService absenceService;
    @Mock
    private CompanyCalendarRepository companyCalendarRepository;
    @Mock
    private ICalService iCalService;

    @Before
    public void setUp() {

        sut = new CompanyCalendarService(absenceService, companyCalendarRepository, iCalService);
    }

    @Test
    public void getCalendarForAllForOneFullDay() {

        final List<Absence> absences = List.of(absence(createPerson(), toDateTime("2019-03-26"), toDateTime("2019-03-26"), FULL));
        when(absenceService.getOpenAbsences()).thenReturn(absences);

        CompanyCalendar companyCalendar = new CompanyCalendar();
        companyCalendar.setId(1L);
        when(companyCalendarRepository.findBySecret("secret")).thenReturn(companyCalendar);

        when(iCalService.generateCalendar("Abwesenheitskalender der Firma", absences)).thenReturn("calendar");

        final String calendar = sut.getCalendarForAll("secret");
        assertThat(calendar).isEqualTo("calendar");
    }

    @Test(expected = IllegalArgumentException.class)
    public void getCalendarForAllButNoCompanyCalendarWithSecretFound() {

        when(companyCalendarRepository.findBySecret("secret")).thenReturn(null);

        sut.getCalendarForAll("secret");
    }

    @Test(expected = IllegalArgumentException.class)
    public void getCalendarForAllSecretIsNull() {

        sut.getCalendarForAll(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getCalendarForAllSecretIsEmpty() {

        sut.getCalendarForAll("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void getCalendarForAllSecretIsEmptyWithWhitespace() {

        sut.getCalendarForAll("  ");
    }

    private Absence absence(Person person, LocalDate start, LocalDate end, DayLength length) {
        final Period period = new Period(start, end, length);
        final AbsenceTimeConfiguration timeConfig = new AbsenceTimeConfiguration(new CalendarSettings());

        return new Absence(person, period, timeConfig);
    }

    private static LocalDate toDateTime(String input) {
        return LocalDate.parse(input, ofPattern("yyyy-MM-dd"));
    }
}
