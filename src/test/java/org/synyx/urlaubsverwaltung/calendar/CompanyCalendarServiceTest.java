package org.synyx.urlaubsverwaltung.calendar;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.core.io.ByteArrayResource;
import org.synyx.urlaubsverwaltung.absence.AbsenceTimeConfiguration;
import org.synyx.urlaubsverwaltung.absence.TimeSettings;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.period.Period;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonDeletedEvent;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.Locale.GERMAN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.TestDataCreator.createPerson;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.USER;


@ExtendWith(MockitoExtension.class)
class CompanyCalendarServiceTest {

    private CompanyCalendarService sut;

    @Mock
    private CalendarAbsenceService calendarAbsenceService;
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

    @BeforeEach
    void setUp() {

        sut = new CompanyCalendarService(calendarAbsenceService, companyCalendarRepository, iCalService, personService, messageSource, Clock.systemUTC());
    }

    @Test
    void getCalendarForAllForOneFullDay() {

        final List<CalendarAbsence> absences = List.of(absence(new Person("muster", "Muster", "Marlene", "muster@example.org"), toDateTime("2019-03-26"), toDateTime("2019-03-26"), FULL));
        when(calendarAbsenceService.getOpenAbsencesSince(any(LocalDate.class))).thenReturn(absences);

        final Person person = new Person();
        person.setId(10L);
        when(personService.getPersonByID(10L)).thenReturn(Optional.of(person));

        CompanyCalendar companyCalendar = new CompanyCalendar(person);
        companyCalendar.setId(1L);
        companyCalendar.setCalendarPeriod(java.time.Period.parse("P1Y"));
        when(companyCalendarRepository.findBySecretAndPerson("secret", person)).thenReturn(Optional.of(companyCalendar));

        when(messageSource.getMessage(eq("calendar.company.title"), any(), eq(GERMAN))).thenReturn("Abwesenheitskalender der Firma");
        final ByteArrayResource iCal = new ByteArrayResource(new byte[]{}, "calendar.ics");
        when(iCalService.getCalendar("Abwesenheitskalender der Firma", absences, person)).thenReturn(iCal);

        final ByteArrayResource calendar = sut.getCalendarForAll(10L, "secret", GERMAN);
        assertThat(calendar).isEqualTo(iCal);
    }

    @Test
    void getCalendarForAllButNoCompanyCalendarWithSecretFound() {

        final Person person = new Person();
        person.setId(10L);
        when(personService.getPersonByID(10L)).thenReturn(Optional.of(person));

        when(companyCalendarRepository.findBySecretAndPerson("secret", person)).thenReturn(Optional.empty());

        assertThatIllegalArgumentException()
            .isThrownBy(() -> sut.getCalendarForAll(10L, "secret", GERMAN));
    }

    @Test
    void getCalendarForAllSecretIsNull() {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> sut.getCalendarForAll(1L, null, GERMAN));
    }

    @Test
    void getCalendarForAllSecretIsEmpty() {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> sut.getCalendarForAll(1L, "", GERMAN));
    }

    @Test
    void getCalendarForAllSecretIsEmptyWithWhitespace() {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> sut.getCalendarForAll(1L, "  ", GERMAN));
    }

    @Test
    void getCalendarForAllCorrectSecretButPersonIsWrong() {
        when(personService.getPersonByID(1L)).thenReturn(Optional.empty());

        assertThatIllegalArgumentException()
            .isThrownBy(() -> sut.getCalendarForAll(1L, "secret", GERMAN));
    }

    @Test
    void deleteCalendarForPerson() {

        final Person person = new Person();
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));

        sut.deleteCalendarForPerson(1);

        verify(companyCalendarRepository).deleteByPerson(person);
    }

    @Test
    void deleteOnPersonDeletionEvent() {
        final Person person = new Person();

        sut.deleteCalendarForPerson(new PersonDeletedEvent(person));

        verify(companyCalendarRepository).deleteByPerson(person);
    }

    @Test
    void deleteCalendarsForPersonsWithoutOneOfRole() {

        final Person office = createPerson("office", OFFICE);
        final Person user = createPerson("user", USER);

        when(personService.getActivePersons()).thenReturn(List.of(office, user));

        sut.deleteCalendarsForPersonsWithoutOneOfRole(OFFICE);

        verify(companyCalendarRepository).deleteByPerson(user);
        verify(companyCalendarRepository, never()).deleteByPerson(office);
    }

    @Test
    void ensureGetCompanyCalendarThrowsWhenGivenPersonDoesNotExist() {
        when(personService.getPersonByID(1L)).thenReturn(Optional.empty());

        assertThatIllegalArgumentException()
            .isThrownBy(() -> sut.getCompanyCalendar(1));
    }

    @Test
    void ensureGetCompanyCalendarReturnsCalendar() {

        final CompanyCalendar expectedCalendar = new CompanyCalendar();

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));
        when(companyCalendarRepository.findByPerson(person)).thenReturn(Optional.of(expectedCalendar));

        final Optional<CompanyCalendar> actualCompanyCalendar = sut.getCompanyCalendar(1);

        assertThat(actualCompanyCalendar).hasValue(expectedCalendar);
    }

    @Test
    void ensureGetCompanyCalendarReturnsEmptyOptional() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));
        when(companyCalendarRepository.findByPerson(person)).thenReturn(Optional.empty());

        final Optional<CompanyCalendar> actualCompanyCalendar = sut.getCompanyCalendar(1);

        assertThat(actualCompanyCalendar).isEmpty();
    }

    @Test
    void ensureCreateCalendarForPersonThrowsWhenGivenPersonDoesNotExist() {
        when(personService.getPersonByID(1L)).thenReturn(Optional.empty());

        assertThatIllegalArgumentException()
            .isThrownBy(() -> sut.createCalendarForPerson(1, java.time.Period.parse("P12M")));
    }

    @Test
    void ensureCreateCalendarForPerson() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));

        when(companyCalendarRepository.findByPerson(person)).thenReturn(Optional.empty());
        when(companyCalendarRepository.save(any(CompanyCalendar.class))).thenAnswer(returnsFirstArg());

        final CompanyCalendar actualCalendarForPerson = sut.createCalendarForPerson(1, java.time.Period.parse("P12M"));

        assertThat(actualCalendarForPerson.getPerson()).isEqualTo(person);
        assertThat(actualCalendarForPerson.getSecret()).isNotBlank();
    }

    @Test
    void ensureCreateCalendarForPersonUpdatesExistingCalendar() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));

        final CompanyCalendar expectedCompanyCalendar = new CompanyCalendar();
        final String secretBeforeUpdate = expectedCompanyCalendar.getSecret();

        when(companyCalendarRepository.findByPerson(person)).thenReturn(Optional.of(expectedCompanyCalendar));
        when(companyCalendarRepository.save(any(CompanyCalendar.class))).thenAnswer(returnsFirstArg());

        final CompanyCalendar actualCalendarForPerson = sut.createCalendarForPerson(1, java.time.Period.parse("P12M"));

        assertThat(actualCalendarForPerson.getPerson()).isEqualTo(person);
        assertThat(actualCalendarForPerson.getSecret()).isNotBlank();
        assertThat(actualCalendarForPerson.getSecret()).isNotEqualTo(secretBeforeUpdate);
    }

    private CalendarAbsence absence(Person person, LocalDate start, LocalDate end, DayLength length) {
        final Period period = new Period(start, end, length);
        final AbsenceTimeConfiguration timeConfig = new AbsenceTimeConfiguration(new TimeSettings());

        return new CalendarAbsence(person, period, timeConfig);
    }
}
