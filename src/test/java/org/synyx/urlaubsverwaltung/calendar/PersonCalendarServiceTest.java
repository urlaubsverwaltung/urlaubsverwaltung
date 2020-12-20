package org.synyx.urlaubsverwaltung.calendar;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.synyx.urlaubsverwaltung.absence.Absence;
import org.synyx.urlaubsverwaltung.absence.AbsenceService;
import org.synyx.urlaubsverwaltung.absence.AbsenceTimeConfiguration;
import org.synyx.urlaubsverwaltung.absence.TimeSettings;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.period.Period;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.io.File;
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
    private AbsenceService absenceService;
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

        sut = new PersonCalendarService(absenceService, personService, personCalendarRepository, iCalService, messageSource, Clock.systemUTC());
    }

    @Test
    void getCalendarForPersonForOneFullDay() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setId(1);
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

        final PersonCalendar personCalendar = new PersonCalendar();
        personCalendar.setPerson(person);
        personCalendar.setFetchSinceMonths(12);
        when(personCalendarRepository.findBySecret("secret")).thenReturn(Optional.of(personCalendar));

        final List<Absence> fullDayAbsences = List.of(absence(person, toDateTime("2019-03-26"), toDateTime("2019-03-26"), FULL));
        when(absenceService.getOpenAbsencesSince(eq(List.of(person)), any(LocalDate.class))).thenReturn(fullDayAbsences);

        when(messageSource.getMessage(eq("calendar.person.title"), any(), eq(GERMAN))).thenReturn("Abwesenheitskalender von Marlene Muster");
        when(iCalService.getCalendar("Abwesenheitskalender von Marlene Muster", fullDayAbsences)).thenReturn(new File("calendar.ics"));

        final File calendar = sut.getCalendarForPerson(1, "secret", GERMAN);
        assertThat(calendar).hasName("calendar.ics");
    }

    @Test
    void getCalendarForPersonForHalfDayMorning() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setId(1);
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

        final PersonCalendar personCalendar = new PersonCalendar();
        personCalendar.setPerson(person);
        personCalendar.setFetchSinceMonths(12);
        when(personCalendarRepository.findBySecret("secret")).thenReturn(Optional.of(personCalendar));

        final List<Absence> morningAbsences = List.of(absence(person, toDateTime("2019-04-26"), toDateTime("2019-04-26"), MORNING));
        when(absenceService.getOpenAbsencesSince(eq(List.of(person)), any(LocalDate.class))).thenReturn(morningAbsences);

        when(messageSource.getMessage(eq("calendar.person.title"), any(), eq(GERMAN))).thenReturn("Abwesenheitskalender von Marlene Muster");
        final File iCal = new File("calendar.ics");
        iCal.deleteOnExit();
        when(iCalService.getCalendar("Abwesenheitskalender von Marlene Muster", morningAbsences)).thenReturn(iCal);

        final File calendar = sut.getCalendarForPerson(1, "secret", GERMAN);
        assertThat(calendar).hasName("calendar.ics");
    }

    @Test
    void getCalendarForPersonForMultipleFullDays() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setId(1);
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

        final PersonCalendar personCalendar = new PersonCalendar();
        personCalendar.setPerson(person);
        personCalendar.setFetchSinceMonths(12);
        when(personCalendarRepository.findBySecret("secret")).thenReturn(Optional.of(personCalendar));

        final List<Absence> manyFullDayAbsences = List.of(absence(person, toDateTime("2019-03-26"), toDateTime("2019-04-01"), FULL));
        when(absenceService.getOpenAbsencesSince(eq(List.of(person)), any(LocalDate.class))).thenReturn(manyFullDayAbsences);

        when(messageSource.getMessage(eq("calendar.person.title"), any(), eq(GERMAN))).thenReturn("Abwesenheitskalender von Marlene Muster");
        when(iCalService.getCalendar("Abwesenheitskalender von Marlene Muster", manyFullDayAbsences)).thenReturn(new File("calendar.ics"));

        final File iCal = sut.getCalendarForPerson(1, "secret", GERMAN);
        assertThat(iCal).hasName("calendar.ics");
    }

    @Test
    void getCalendarForPersonForHalfDayNoon() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setId(1);
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

        final PersonCalendar personCalendar = new PersonCalendar();
        personCalendar.setPerson(person);
        personCalendar.setFetchSinceMonths(12);
        when(personCalendarRepository.findBySecret("secret")).thenReturn(Optional.of(personCalendar));

        final List<Absence> noonAbsences = List.of(absence(person, toDateTime("2019-05-26"), toDateTime("2019-05-26"), NOON));
        when(absenceService.getOpenAbsencesSince(eq(List.of(person)), any(LocalDate.class))).thenReturn(noonAbsences);

        when(messageSource.getMessage(eq("calendar.person.title"), any(), eq(GERMAN))).thenReturn("Abwesenheitskalender von Marlene Muster");
        when(iCalService.getCalendar("Abwesenheitskalender von Marlene Muster", noonAbsences)).thenReturn(new File("calendar.ics"));

        final File calendar = sut.getCalendarForPerson(1, "secret", GERMAN);
        assertThat(calendar).hasName("calendar.ics");
    }

    @Test
    void getCalendarForPersonButPersonNotFound() {

        when(personService.getPersonByID(1)).thenReturn(Optional.ofNullable(null));

        final PersonCalendar personCalendar = new PersonCalendar();
        personCalendar.setPerson(new Person("muster", "Muster", "Marlene", "muster@example.org"));
        when(personCalendarRepository.findBySecret("secret")).thenReturn(Optional.of(personCalendar));

        assertThatIllegalArgumentException()
            .isThrownBy(() -> sut.getCalendarForPerson(1, "secret", GERMAN));
    }

    @Test
    void getCalendarForPersonButSecretIsNull() {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> sut.getCalendarForPerson(1, null, GERMAN));
    }

    @Test
    void getCalendarForPersonButSecretIsEmpty() {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> sut.getCalendarForPerson(1, "", GERMAN));
    }

    @Test
    void getCalendarForPersonButSecretIsEmptyWithWhitespace() {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> sut.getCalendarForPerson(1, "  ", GERMAN));
    }

    @Test
    void getCalendarForPersonButSecretDoesNotExist() {
        when(personCalendarRepository.findBySecret("secret")).thenReturn(Optional.empty());
        assertThatIllegalArgumentException()
            .isThrownBy(() -> sut.getCalendarForPerson(1, "secret", GERMAN));
    }

    @Test
    void getCalendarForPersonButSecretDoesNotMatchTheGivenPerson() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setId(1);
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

        final Person notMatchingPerson = new Person("muster", "Muster", "Marlene", "muster@example.org");
        notMatchingPerson.setId(1337);
        PersonCalendar personCalendar = new PersonCalendar();
        personCalendar.setPerson(notMatchingPerson);
        when(personCalendarRepository.findBySecret("secret")).thenReturn(Optional.of(personCalendar));

        assertThatIllegalArgumentException()
            .isThrownBy(() -> sut.getCalendarForPerson(1, "secret", GERMAN));
    }

    @Test
    void getPersonCalendar() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setId(1);
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

        final PersonCalendar receivedPersonCalendar = new PersonCalendar(person);
        when(personCalendarRepository.findByPerson(person)).thenReturn(Optional.of(receivedPersonCalendar));

        final Optional<PersonCalendar> personCalendar = sut.getPersonCalendar(1);
        assertThat(personCalendar).contains(receivedPersonCalendar);
    }

    @Test
    void getPersonCalendarNoPerson() {
        when(personService.getPersonByID(1)).thenReturn(Optional.empty());

        assertThatIllegalArgumentException()
            .isThrownBy(() -> sut.getPersonCalendar(1));
    }

    @Test
    void getPersonCalendarNoPersonCalendarFound() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setId(1);
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

        when(personCalendarRepository.findByPerson(person)).thenReturn(Optional.empty());

        final Optional<PersonCalendar> personCalendar = sut.getPersonCalendar(1);
        assertThat(personCalendar).isEmpty();
    }

    @Test
    void createCalendarForPerson() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setId(1);
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

        final PersonCalendar receivedPersonCalendar = new PersonCalendar(person);
        when(personCalendarRepository.findByPerson(person)).thenReturn(Optional.of(receivedPersonCalendar));

        when(personCalendarRepository.save(receivedPersonCalendar)).thenReturn(receivedPersonCalendar);

        final PersonCalendar calendarForPerson = sut.createCalendarForPerson(1, 12);
        assertThat(calendarForPerson.getPerson()).isEqualTo(person);
        assertThat(calendarForPerson.getSecret()).isNotBlank();
        assertThat(calendarForPerson.getFetchSinceMonths()).isEqualTo(12);
    }

    @Test
    void createCalendarForPersonNoPersonFound() {
        when(personService.getPersonByID(1)).thenReturn(Optional.empty());
        assertThatIllegalArgumentException()
            .isThrownBy(() -> sut.createCalendarForPerson(1, 12));
    }

    @Test
    void createCalendarForPersonNoPersonCalendarFound() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setId(1);
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

        when(personCalendarRepository.findByPerson(person)).thenReturn(Optional.empty());

        when(personCalendarRepository.save(any(PersonCalendar.class))).thenAnswer(returnsFirstArg());

        final PersonCalendar calendarForPerson = sut.createCalendarForPerson(1, 12);
        assertThat(calendarForPerson.getPerson()).isEqualTo(person);
        assertThat(calendarForPerson.getSecret()).isNotBlank();
        assertThat(calendarForPerson.getFetchSinceMonths()).isEqualTo(12);

        verify(personCalendarRepository).save(any(PersonCalendar.class));
    }

    @Test
    void deletePersonalCalendarForPerson() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setId(1);
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

        sut.deletePersonalCalendarForPerson(1);

        verify(personCalendarRepository).deleteByPerson(person);
    }

    private Absence absence(Person person, LocalDate start, LocalDate end, DayLength length) {
        final Period period = new Period(start, end, length);
        final AbsenceTimeConfiguration timeConfig = new AbsenceTimeConfiguration(new TimeSettings());

        return new Absence(person, period, timeConfig);
    }
}
