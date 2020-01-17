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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;
import static org.synyx.urlaubsverwaltung.period.DayLength.MORNING;
import static org.synyx.urlaubsverwaltung.period.DayLength.NOON;
import static org.synyx.urlaubsverwaltung.testdatacreator.TestDataCreator.createPerson;


@RunWith(MockitoJUnitRunner.class)
public class PersonCalendarServiceTest {

    private PersonCalendarService sut;

    @Mock
    private AbsenceService absenceService;
    @Mock
    private PersonService personService;
    @Mock
    private PersonCalendarRepository personCalendarRepository;
    @Mock
    private ICalService iCalService;

    @Before
    public void setUp() {

        sut = new PersonCalendarService(absenceService, personService, personCalendarRepository, iCalService);
    }

    @Test
    public void getCalendarForPersonForOneFullDay() {

        final Person person = createPerson();
        person.setId(1);
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

        final PersonCalendar personCalendar = new PersonCalendar();
        personCalendar.setPerson(person);
        when(personCalendarRepository.findBySecret("secret")).thenReturn(personCalendar);

        final List<Absence> fullDayAbsences = List.of(absence(person, toDateTime("2019-03-26"), toDateTime("2019-03-26"), FULL));
        when(absenceService.getOpenAbsences(List.of(person))).thenReturn(fullDayAbsences);

        when(iCalService.generateCalendar("Abwesenheitskalender von Marlene Muster", fullDayAbsences)).thenReturn("calendar");

        final String calendar = sut.getCalendarForPerson(1, "secret");
        assertThat(calendar).isEqualTo("calendar");
    }

    @Test
    public void getCalendarForPersonForHalfDayMorning() {

        final Person person = createPerson();
        person.setId(1);
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

        final PersonCalendar personCalendar = new PersonCalendar();
        personCalendar.setPerson(person);
        when(personCalendarRepository.findBySecret("secret")).thenReturn(personCalendar);

        final List<Absence> morningAbsences = List.of(absence(person, toDateTime("2019-04-26"), toDateTime("2019-04-26"), MORNING));
        when(absenceService.getOpenAbsences(List.of(person))).thenReturn(morningAbsences);

        when(iCalService.generateCalendar("Abwesenheitskalender von Marlene Muster", morningAbsences)).thenReturn("calendar");

        final String calendar = sut.getCalendarForPerson(1, "secret");
        assertThat(calendar).isEqualTo("calendar");
    }

    @Test
    public void getCalendarForPersonForMultipleFullDays() {

        final Person person = createPerson();
        person.setId(1);
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

        final PersonCalendar personCalendar = new PersonCalendar();
        personCalendar.setPerson(person);
        when(personCalendarRepository.findBySecret("secret")).thenReturn(personCalendar);

        final List<Absence> manyFullDayAbsences = List.of(absence(person, toDateTime("2019-03-26"), toDateTime("2019-04-01"), FULL));
        when(absenceService.getOpenAbsences(List.of(person))).thenReturn(manyFullDayAbsences);

        when(iCalService.generateCalendar("Abwesenheitskalender von Marlene Muster", manyFullDayAbsences)).thenReturn("calendar");

        final String iCal = sut.getCalendarForPerson(1, "secret");
        assertThat(iCal).isEqualTo("calendar");
    }

    @Test
    public void getCalendarForPersonForHalfDayNoon() {

        final Person person = createPerson();
        person.setId(1);
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

        final PersonCalendar personCalendar = new PersonCalendar();
        personCalendar.setPerson(person);
        when(personCalendarRepository.findBySecret("secret")).thenReturn(personCalendar);

        final List<Absence> noonAbsences = List.of(absence(person, toDateTime("2019-05-26"), toDateTime("2019-05-26"), NOON));
        when(absenceService.getOpenAbsences(List.of(person))).thenReturn(noonAbsences);

        when(iCalService.generateCalendar("Abwesenheitskalender von Marlene Muster", noonAbsences)).thenReturn("calendar");

        final String calendar = sut.getCalendarForPerson(1, "secret");
        assertThat(calendar).isEqualTo("calendar");
    }

    @Test(expected = IllegalArgumentException.class)
    public void getCalendarForPersonButPersonNotFound() {

        when(personService.getPersonByID(1)).thenReturn(Optional.ofNullable(null));

        final PersonCalendar personCalendar = new PersonCalendar();
        personCalendar.setPerson(createPerson());
        when(personCalendarRepository.findBySecret("secret")).thenReturn(personCalendar);

        sut.getCalendarForPerson(1, "secret");
    }

    @Test(expected = IllegalArgumentException.class)
    public void getCalendarForPersonButSecretIsNull() {

        sut.getCalendarForPerson(1, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getCalendarForPersonButSecretIsEmpty() {

        sut.getCalendarForPerson(1, "");
    }

    @Test(expected = IllegalArgumentException.class)
    public void getCalendarForPersonButSecretIsEmptyWithWhitespace() {

        sut.getCalendarForPerson(1, "  ");
    }

    @Test(expected = IllegalArgumentException.class)
    public void getCalendarForPersonButSecretDoesNotExist() {

        when(personCalendarRepository.findBySecret("secret")).thenReturn(null);

        sut.getCalendarForPerson(1, "secret");
    }

    @Test(expected = IllegalArgumentException.class)
    public void getCalendarForPersonButSecretDoesNotMatchTheGivenPerson() {

        final Person person = createPerson();
        person.setId(1);
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

        final Person notMatchingPerson = createPerson();
        notMatchingPerson.setId(1337);
        PersonCalendar personCalendar = new PersonCalendar();
        personCalendar.setPerson(notMatchingPerson);
        when(personCalendarRepository.findBySecret("secret")).thenReturn(personCalendar);

        sut.getCalendarForPerson(1, "secret");
    }

    @Test
    public void getPersonCalendar() {

        final Person person = createPerson();
        person.setId(1);
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

        final PersonCalendar receivedPersonCalendar = new PersonCalendar(person);
        when(personCalendarRepository.findByPerson(person)).thenReturn(receivedPersonCalendar);

        final Optional<PersonCalendar> personCalendar = sut.getPersonCalendar(1);
        assertThat(personCalendar.get()).isEqualTo(receivedPersonCalendar);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getPersonCalendarNoPerson() {

        when(personService.getPersonByID(1)).thenReturn(Optional.empty());

        sut.getPersonCalendar(1);
    }

    @Test
    public void getPersonCalendarNoPersonCalendarFound() {

        final Person person = createPerson();
        person.setId(1);
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

        when(personCalendarRepository.findByPerson(person)).thenReturn(null);

        final Optional<PersonCalendar> personCalendar = sut.getPersonCalendar(1);
        assertThat(personCalendar.isEmpty()).isTrue();
    }

    @Test
    public void createCalendarForPerson() {

        final Person person = createPerson();
        person.setId(1);
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

        final PersonCalendar receivedPersonCalendar = new PersonCalendar(person);
        when(personCalendarRepository.findByPerson(person)).thenReturn(receivedPersonCalendar);

        when(personCalendarRepository.save(receivedPersonCalendar)).thenReturn(receivedPersonCalendar);

        final PersonCalendar calendarForPerson = sut.createCalendarForPerson(1);
        assertThat(calendarForPerson.getPerson()).isEqualTo(person);
        assertThat(calendarForPerson.getSecret()).isNotBlank();
    }

    @Test(expected = IllegalArgumentException.class)
    public void createCalendarForPersonNoPersonFound() {

        when(personService.getPersonByID(1)).thenReturn(Optional.empty());

        sut.createCalendarForPerson(1);
    }

    @Test
    public void createCalendarForPersonNoPersonCalendarFound() {

        final Person person = createPerson();
        person.setId(1);
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

        when(personCalendarRepository.findByPerson(person)).thenReturn(null);

        when(personCalendarRepository.save(any(PersonCalendar.class))).thenAnswer(returnsFirstArg());

        final PersonCalendar calendarForPerson = sut.createCalendarForPerson(1);
        assertThat(calendarForPerson.getPerson()).isEqualTo(person);
        assertThat(calendarForPerson.getSecret()).isNotBlank();

        verify(personCalendarRepository).save(any(PersonCalendar.class));
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
