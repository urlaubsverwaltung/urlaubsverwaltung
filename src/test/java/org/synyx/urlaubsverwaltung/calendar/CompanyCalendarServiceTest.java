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
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.settings.CalendarSettings;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static java.time.format.DateTimeFormatter.ofPattern;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
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
    @Mock
    private PersonService personService;

    @Before
    public void setUp() {

        sut = new CompanyCalendarService(absenceService, companyCalendarRepository, iCalService, personService);
    }

    @Test
    public void getCalendarForAllForOneFullDay() {

        final List<Absence> absences = List.of(absence(createPerson(), toDateTime("2019-03-26"), toDateTime("2019-03-26"), FULL));
        when(absenceService.getOpenAbsences()).thenReturn(absences);

        final Person person = new Person();
        person.setId(10);
        when(personService.getPersonByID(10)).thenReturn(Optional.of(person));

        CompanyCalendar companyCalendar = new CompanyCalendar();
        companyCalendar.setId(1L);
        when(companyCalendarRepository.findBySecretAndPerson("secret", person)).thenReturn(companyCalendar);

        when(iCalService.generateCalendar("Abwesenheitskalender der Firma", absences)).thenReturn("calendar");

        final String calendar = sut.getCalendarForAll(10, "secret");
        assertThat(calendar).isEqualTo("calendar");
    }

    @Test(expected = IllegalArgumentException.class)
    public void getCalendarForAllButNoCompanyCalendarWithSecretFound() {

        final Person person = new Person();
        person.setId(10);
        when(personService.getPersonByID(10)).thenReturn(Optional.of(person));

        when(companyCalendarRepository.findBySecretAndPerson("secret", person)).thenReturn(null);

        sut.getCalendarForAll(10, "secret");
    }

    @Test(expected = IllegalArgumentException.class)
    public void getCalendarForAllSecretIsNull() {

        sut.getCalendarForAll(1, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getCalendarForAllSecretIsEmpty() {

        sut.getCalendarForAll(1, "");
    }

    @Test(expected = IllegalArgumentException.class)
    public void getCalendarForAllSecretIsEmptyWithWhitespace() {

        sut.getCalendarForAll(1, "  ");
    }

    @Test(expected = IllegalArgumentException.class)
    public void getCalendarForAllCorrectSecretButPersonIsWrong() {

        when(personService.getPersonByID(1)).thenReturn(Optional.empty());

        sut.getCalendarForAll(1, "secret");
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureGetCompanyCalendarThrowsWhenGIvenPersonDoesNotExist() {

        when(personService.getPersonByID(1)).thenReturn(Optional.empty());

        sut.getCompanyCalendar(1);
    }

    @Test
    public void ensureGetCompanyCalendarReturnsCalendar() {

        final CompanyCalendar expectedCalendar = mock(CompanyCalendar.class);

        final Person person = createPerson();
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));
        when(companyCalendarRepository.findByPerson(person)).thenReturn(expectedCalendar);

        final Optional<CompanyCalendar> actualCompanyCalendar = sut.getCompanyCalendar(1);

        assertThat(actualCompanyCalendar).hasValue(expectedCalendar);
    }

    @Test
    public void ensureGetCompanyCalendarReturnsEmptyOptional() {

        final Person person = createPerson();
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));
        when(companyCalendarRepository.findByPerson(person)).thenReturn(null);

        final Optional<CompanyCalendar> actualCompanyCalendar = sut.getCompanyCalendar(1);

        assertThat(actualCompanyCalendar).isEmpty();
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureCreateCalendarForPersonThrowsWhenGivenPersonDoesNotExist() {

        when(personService.getPersonByID(1)).thenReturn(Optional.empty());

        sut.createCalendarForPerson(1);
    }

    @Test
    public void ensureCreateCalendarForPerson() {

        final Person person = createPerson();
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

        when(companyCalendarRepository.findByPerson(person)).thenReturn(null);
        when(companyCalendarRepository.save(any(CompanyCalendar.class))).thenAnswer(returnsFirstArg());

        final CompanyCalendar actualCalendarForPerson = sut.createCalendarForPerson(1);

        assertThat(actualCalendarForPerson.getPerson()).isEqualTo(person);
        assertThat(actualCalendarForPerson.getSecret()).isNotBlank();
    }

    @Test
    public void ensureCreateCalendarForPersonUpdatesExistingCalendar() {

        final Person person = createPerson();
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

        final CompanyCalendar expectedCompanyCalendar = new CompanyCalendar();
        final String secretBeforeUpdate = expectedCompanyCalendar.getSecret();

        when(companyCalendarRepository.findByPerson(person)).thenReturn(expectedCompanyCalendar);
        when(companyCalendarRepository.save(any(CompanyCalendar.class))).thenAnswer(returnsFirstArg());

        final CompanyCalendar actualCalendarForPerson = sut.createCalendarForPerson(1);

        assertThat(actualCalendarForPerson.getPerson()).isEqualTo(person);
        assertThat(actualCalendarForPerson.getSecret()).isNotBlank();
        assertThat(actualCalendarForPerson.getSecret()).isNotEqualTo(secretBeforeUpdate);
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
