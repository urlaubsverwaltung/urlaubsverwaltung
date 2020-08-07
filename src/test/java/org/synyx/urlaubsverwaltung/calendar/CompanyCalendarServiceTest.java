package org.synyx.urlaubsverwaltung.calendar;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.MessageSource;
import org.synyx.urlaubsverwaltung.absence.Absence;
import org.synyx.urlaubsverwaltung.absence.AbsenceService;
import org.synyx.urlaubsverwaltung.absence.AbsenceTimeConfiguration;
import org.synyx.urlaubsverwaltung.api.RestApiDateFormat;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.period.Period;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.settings.CalendarSettings;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.Locale.GERMAN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.USER;
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
    @Mock
    private MessageSource messageSource;

    private static LocalDate toDateTime(String input) {
        return LocalDate.parse(input, ofPattern("yyyy-MM-dd"));
    }

    @Before
    public void setUp() {

        sut = new CompanyCalendarService(absenceService, companyCalendarRepository, iCalService, personService, messageSource);
    }

    @Test
    public void getCalendarForAllForOneFullDay() {

        final List<Absence> absences = List.of(absence(createPerson(),"2019-03-26","2019-03-26", FULL));
        when(absenceService.getOpenAbsences()).thenReturn(absences);

        final Person person = new Person();
        person.setId(10);
        when(personService.getPersonByID(10)).thenReturn(Optional.of(person));

        CompanyCalendar companyCalendar = new CompanyCalendar();
        companyCalendar.setId(1L);
        when(companyCalendarRepository.findBySecretAndPerson("secret", person)).thenReturn(companyCalendar);

        when(messageSource.getMessage(eq("calendar.company.title"), any(), eq(GERMAN))).thenReturn("Abwesenheitskalender der Firma");
        when(iCalService.generateCalendar("Abwesenheitskalender der Firma", absences)).thenReturn("calendar");

        final String calendar = sut.getCalendarForAll(10, "secret", GERMAN);
        assertThat(calendar).isEqualTo("calendar");
    }

    @Test(expected = IllegalArgumentException.class)
    public void getCalendarForAllButNoCompanyCalendarWithSecretFound() {

        final Person person = new Person();
        person.setId(10);
        when(personService.getPersonByID(10)).thenReturn(Optional.of(person));

        when(companyCalendarRepository.findBySecretAndPerson("secret", person)).thenReturn(null);

        sut.getCalendarForAll(10, "secret", GERMAN);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getCalendarForAllSecretIsNull() {

        sut.getCalendarForAll(1, null, GERMAN);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getCalendarForAllSecretIsEmpty() {

        sut.getCalendarForAll(1, "", GERMAN);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getCalendarForAllSecretIsEmptyWithWhitespace() {

        sut.getCalendarForAll(1, "  ", GERMAN);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getCalendarForAllCorrectSecretButPersonIsWrong() {

        when(personService.getPersonByID(1)).thenReturn(Optional.empty());

        sut.getCalendarForAll(1, "secret", GERMAN);
    }

    @Test
    public void deleteCalendarForPerson() {

        final Person person = new Person();
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

        sut.deleteCalendarForPerson(1);

        verify(companyCalendarRepository).deleteByPerson(person);
    }

    @Test
    public void deleteCalendarsForPersonsWithoutOneOfRole() {

        final Person office = createPerson("office", OFFICE);
        final Person user = createPerson("user", USER);

        when(personService.getActivePersons()).thenReturn(List.of(office, user));

        sut.deleteCalendarsForPersonsWithoutOneOfRole(OFFICE);

        verify(companyCalendarRepository).deleteByPerson(user);
        verify(companyCalendarRepository, never()).deleteByPerson(office);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureGetCompanyCalendarThrowsWhenGivenPersonDoesNotExist() {

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

    private Absence absence(Person person, String start, String end, DayLength length) {
        Instant startInstant = Instant.from(DateTimeFormatter.ofPattern(RestApiDateFormat.DATE_PATTERN).parse(start));
        Instant endInstant = Instant.from(DateTimeFormatter.ofPattern(RestApiDateFormat.DATE_PATTERN).parse(end));
        final Period period = new Period(startInstant, endInstant, length);
        final AbsenceTimeConfiguration timeConfig = new AbsenceTimeConfiguration(new CalendarSettings());

        return new Absence(person, period, timeConfig, Clock.systemUTC());
    }
}
