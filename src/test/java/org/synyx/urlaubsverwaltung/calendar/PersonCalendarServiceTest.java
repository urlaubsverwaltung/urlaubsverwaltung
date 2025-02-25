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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;
import static org.synyx.urlaubsverwaltung.period.DayLength.MORNING;
import static org.synyx.urlaubsverwaltung.period.DayLength.NOON;


@ExtendWith(MockitoExtension.class)
class PersonCalendarServiceTest {

    private PersonCalendarService sut;

    @Mock
    private CalendarAbsenceService calendarAbsenceService;
    @Mock
    private PersonService personService;
    @Mock
    private PersonCalendarRepository personCalendarRepository;
    @Mock
    private ICalService iCalService;
    @Mock
    private MessageSource messageSource;

    private static LocalDate toDateTime(String input) {
        return LocalDate.parse(input, ofPattern("yyyy-MM-dd"));
    }

    @BeforeEach
    void setUp() {

        sut = new PersonCalendarService(calendarAbsenceService, personService, personCalendarRepository, iCalService, messageSource, Clock.systemUTC());
    }

    @Test
    void getCalendarForPersonForOneFullDay() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setId(1L);
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));

        final PersonCalendar personCalendar = new PersonCalendar();
        personCalendar.setPerson(person);
        personCalendar.setCalendarPeriod(java.time.Period.parse("P12M"));
        when(personCalendarRepository.findBySecret("secret")).thenReturn(Optional.of(personCalendar));

        final List<CalendarAbsence> fullDayAbsences = List.of(absence(person, toDateTime("2019-03-26"), toDateTime("2019-03-26"), FULL));
        when(calendarAbsenceService.getOpenAbsencesSince(eq(List.of(person)), any(LocalDate.class))).thenReturn(fullDayAbsences);

        when(messageSource.getMessage(eq("calendar.person.title"), any(), eq(GERMAN))).thenReturn("Abwesenheitskalender von Marlene Muster");
        final ByteArrayResource iCal = new ByteArrayResource(new byte[]{}, "calendar.ics");
        when(iCalService.getCalendar("Abwesenheitskalender von Marlene Muster", fullDayAbsences, person)).thenReturn(iCal);

        final ByteArrayResource calendar = sut.getCalendarForPerson(1L, "secret", GERMAN);
        assertThat(calendar).isEqualTo(iCal);
    }

    @Test
    void getCalendarForPersonForHalfDayMorning() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setId(1L);
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));

        final PersonCalendar personCalendar = new PersonCalendar();
        personCalendar.setPerson(person);
        personCalendar.setCalendarPeriod(java.time.Period.parse("P12M"));
        when(personCalendarRepository.findBySecret("secret")).thenReturn(Optional.of(personCalendar));

        final List<CalendarAbsence> morningAbsences = List.of(absence(person, toDateTime("2019-04-26"), toDateTime("2019-04-26"), MORNING));
        when(calendarAbsenceService.getOpenAbsencesSince(eq(List.of(person)), any(LocalDate.class))).thenReturn(morningAbsences);

        when(messageSource.getMessage(eq("calendar.person.title"), any(), eq(GERMAN))).thenReturn("Abwesenheitskalender von Marlene Muster");
        final ByteArrayResource iCal = new ByteArrayResource(new byte[]{}, "calendar.ics");
        when(iCalService.getCalendar("Abwesenheitskalender von Marlene Muster", morningAbsences, person)).thenReturn(iCal);

        final ByteArrayResource calendar = sut.getCalendarForPerson(1L, "secret", GERMAN);
        assertThat(calendar).isEqualTo(iCal);
    }

    @Test
    void getCalendarForPersonForMultipleFullDays() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setId(1L);
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));

        final PersonCalendar personCalendar = new PersonCalendar();
        personCalendar.setPerson(person);
        personCalendar.setCalendarPeriod(java.time.Period.parse("P12M"));
        when(personCalendarRepository.findBySecret("secret")).thenReturn(Optional.of(personCalendar));

        final List<CalendarAbsence> manyFullDayAbsences = List.of(absence(person, toDateTime("2019-03-26"), toDateTime("2019-04-01"), FULL));
        when(calendarAbsenceService.getOpenAbsencesSince(eq(List.of(person)), any(LocalDate.class))).thenReturn(manyFullDayAbsences);

        when(messageSource.getMessage(eq("calendar.person.title"), any(), eq(GERMAN))).thenReturn("Abwesenheitskalender von Marlene Muster");
        final ByteArrayResource iCal = new ByteArrayResource(new byte[]{}, "calendar.ics");
        when(iCalService.getCalendar("Abwesenheitskalender von Marlene Muster", manyFullDayAbsences, person)).thenReturn(iCal);

        final ByteArrayResource calendar = sut.getCalendarForPerson(1L, "secret", GERMAN);
        assertThat(calendar).isEqualTo(iCal);
    }

    @Test
    void getCalendarForPersonForHalfDayNoon() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setId(1L);
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));

        final PersonCalendar personCalendar = new PersonCalendar();
        personCalendar.setPerson(person);
        personCalendar.setCalendarPeriod(java.time.Period.parse("P12M"));
        when(personCalendarRepository.findBySecret("secret")).thenReturn(Optional.of(personCalendar));

        final List<CalendarAbsence> noonAbsences = List.of(absence(person, toDateTime("2019-05-26"), toDateTime("2019-05-26"), NOON));
        when(calendarAbsenceService.getOpenAbsencesSince(eq(List.of(person)), any(LocalDate.class))).thenReturn(noonAbsences);

        when(messageSource.getMessage(eq("calendar.person.title"), any(), eq(GERMAN))).thenReturn("Abwesenheitskalender von Marlene Muster");
        final ByteArrayResource iCal = new ByteArrayResource(new byte[]{}, "calendar.ics");
        when(iCalService.getCalendar("Abwesenheitskalender von Marlene Muster", noonAbsences, person)).thenReturn(iCal);

        final ByteArrayResource calendar = sut.getCalendarForPerson(1L, "secret", GERMAN);
        assertThat(calendar).isEqualTo(iCal);
    }

    @Test
    void getCalendarForPersonButPersonNotFound() {

        when(personService.getPersonByID(1L)).thenReturn(Optional.ofNullable(null));

        final PersonCalendar personCalendar = new PersonCalendar();
        personCalendar.setPerson(new Person("muster", "Muster", "Marlene", "muster@example.org"));
        when(personCalendarRepository.findBySecret("secret")).thenReturn(Optional.of(personCalendar));

        assertThatIllegalArgumentException()
            .isThrownBy(() -> sut.getCalendarForPerson(1L, "secret", GERMAN));
    }

    @Test
    void getCalendarForPersonButSecretIsNull() {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> sut.getCalendarForPerson(1L, null, GERMAN));
    }

    @Test
    void getCalendarForPersonButSecretIsEmpty() {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> sut.getCalendarForPerson(1L, "", GERMAN));
    }

    @Test
    void getCalendarForPersonButSecretIsEmptyWithWhitespace() {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> sut.getCalendarForPerson(1L, "  ", GERMAN));
    }

    @Test
    void getCalendarForPersonButSecretDoesNotExist() {
        when(personCalendarRepository.findBySecret("secret")).thenReturn(Optional.empty());
        assertThatIllegalArgumentException()
            .isThrownBy(() -> sut.getCalendarForPerson(1L, "secret", GERMAN));
    }

    @Test
    void getCalendarForPersonButSecretDoesNotMatchTheGivenPerson() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setId(1L);
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));

        final Person notMatchingPerson = new Person("muster", "Muster", "Marlene", "muster@example.org");
        notMatchingPerson.setId(1337L);
        PersonCalendar personCalendar = new PersonCalendar();
        personCalendar.setPerson(notMatchingPerson);
        when(personCalendarRepository.findBySecret("secret")).thenReturn(Optional.of(personCalendar));

        assertThatIllegalArgumentException()
            .isThrownBy(() -> sut.getCalendarForPerson(1L, "secret", GERMAN));
    }

    @Test
    void getPersonCalendar() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setId(1L);
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));

        final PersonCalendar receivedPersonCalendar = new PersonCalendar(person);
        when(personCalendarRepository.findByPerson(person)).thenReturn(Optional.of(receivedPersonCalendar));

        final Optional<PersonCalendar> personCalendar = sut.getPersonCalendar(1L);
        assertThat(personCalendar).contains(receivedPersonCalendar);
    }

    @Test
    void getPersonCalendarNoPerson() {
        when(personService.getPersonByID(1L)).thenReturn(Optional.empty());

        assertThatIllegalArgumentException()
            .isThrownBy(() -> sut.getPersonCalendar(1L));
    }

    @Test
    void getPersonCalendarNoPersonCalendarFound() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setId(1L);
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));

        when(personCalendarRepository.findByPerson(person)).thenReturn(Optional.empty());

        final Optional<PersonCalendar> personCalendar = sut.getPersonCalendar(1L);
        assertThat(personCalendar).isEmpty();
    }

    @Test
    void createCalendarForPerson() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setId(1L);
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));

        final PersonCalendar receivedPersonCalendar = new PersonCalendar(person);
        when(personCalendarRepository.findByPerson(person)).thenReturn(Optional.of(receivedPersonCalendar));

        when(personCalendarRepository.save(receivedPersonCalendar)).thenReturn(receivedPersonCalendar);

        final PersonCalendar calendarForPerson = sut.createCalendarForPerson(1L, java.time.Period.parse("P12M"));
        assertThat(calendarForPerson.getPerson()).isEqualTo(person);
        assertThat(calendarForPerson.getSecret()).isNotBlank();
        assertThat(calendarForPerson.getCalendarPeriod()).isEqualTo(java.time.Period.parse("P12M"));

    }

    @Test
    void createCalendarForPersonNoPersonFound() {
        when(personService.getPersonByID(1L)).thenReturn(Optional.empty());
        assertThatIllegalArgumentException()
            .isThrownBy(() -> sut.createCalendarForPerson(1L, java.time.Period.parse("P12M")));
    }

    @Test
    void createCalendarForPersonNoPersonCalendarFound() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setId(1L);
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));

        when(personCalendarRepository.findByPerson(person)).thenReturn(Optional.empty());

        when(personCalendarRepository.save(any(PersonCalendar.class))).thenAnswer(returnsFirstArg());

        final PersonCalendar calendarForPerson = sut.createCalendarForPerson(1L, java.time.Period.parse("P12M"));
        assertThat(calendarForPerson.getPerson()).isEqualTo(person);
        assertThat(calendarForPerson.getSecret()).isNotBlank();
        assertThat(calendarForPerson.getCalendarPeriod()).isEqualTo(java.time.Period.parse("P12M"));

        verify(personCalendarRepository).save(any(PersonCalendar.class));
    }

    @Test
    void deletePersonalCalendarForPerson() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setId(1L);
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));

        sut.deletePersonalCalendarForPerson(1);

        verify(personCalendarRepository).deleteByPerson(person);
    }

    @Test
    void deleteOnPersonDeletionEvent() {
        final Person person = new Person();

        sut.deletePersonalCalendar(new PersonDeletedEvent(person));

        verify(personCalendarRepository).deleteByPerson(person);
    }

    private CalendarAbsence absence(Person person, LocalDate start, LocalDate end, DayLength length) {
        final Period period = new Period(start, end, length);
        final AbsenceTimeConfiguration timeConfig = new AbsenceTimeConfiguration(new TimeSettings());

        return new CalendarAbsence(person, period, timeConfig);
    }
}
