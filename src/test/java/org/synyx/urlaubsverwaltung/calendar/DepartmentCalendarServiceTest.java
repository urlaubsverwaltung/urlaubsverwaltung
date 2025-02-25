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
import org.synyx.urlaubsverwaltung.department.Department;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.period.Period;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonDeletedEvent;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static java.time.LocalDate.parse;
import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.Locale.GERMAN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.TestDataCreator.createDepartment;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;


@ExtendWith(MockitoExtension.class)
class DepartmentCalendarServiceTest {

    private DepartmentCalendarService sut;

    @Mock
    private CalendarAbsenceService calendarAbsenceService;
    @Mock
    private DepartmentService departmentService;
    @Mock
    private PersonService personService;
    @Mock
    private DepartmentCalendarRepository departmentCalendarRepository;
    @Mock
    private ICalService iCalService;
    @Mock
    private MessageSource messageSource;

    @BeforeEach
    void setUp() {

        sut = new DepartmentCalendarService(calendarAbsenceService, departmentService, personService,
            departmentCalendarRepository, iCalService, messageSource, Clock.systemUTC());
    }

    @Test
    void deleteCalendarForDepartmentAndPerson() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setId(10L);
        when(personService.getPersonByID(10L)).thenReturn(Optional.of(person));

        sut.deleteCalendarForDepartmentAndPerson(1, 10);

        verify(departmentCalendarRepository).deleteByDepartmentIdAndPerson(1L, person);
    }

    @Test
    void getCalendarForDepartment() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setId(10L);
        when(personService.getPersonByID(10L)).thenReturn(Optional.of(person));

        final DepartmentCalendar departmentCalendar = new DepartmentCalendar();
        when(departmentCalendarRepository.findByDepartmentIdAndPerson(1L, person)).thenReturn(Optional.of(departmentCalendar));

        final Optional<DepartmentCalendar> calendarForDepartment = sut.getCalendarForDepartment(1L, 10L);
        assertThat(calendarForDepartment).contains(departmentCalendar);
    }

    @Test
    void getCalendarForDepartmentForOneFullDay() {

        final Department department = createDepartment("DepartmentName");
        department.setId(1L);
        department.setCreatedAt(LocalDate.of(2018, 1, 1));
        when(departmentService.getDepartmentById(1L)).thenReturn(Optional.of(department));

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setId(10L);
        when(personService.getPersonByID(10L)).thenReturn(Optional.of(person));

        department.setMembers(List.of(person));

        final DepartmentCalendar departmentCalendar = new DepartmentCalendar();
        departmentCalendar.setId(1L);
        departmentCalendar.setDepartmentId(1L);
        departmentCalendar.setCalendarPeriod(java.time.Period.parse("P12M"));
        when(departmentCalendarRepository.findBySecretAndPerson("secret", person)).thenReturn(Optional.of(departmentCalendar));

        final List<CalendarAbsence> fullDayAbsences = List.of(absence(person, parse("2019-03-26", ofPattern("yyyy-MM-dd")), parse("2019-03-26", ofPattern("yyyy-MM-dd")), FULL));
        when(calendarAbsenceService.getOpenAbsencesSince(eq(List.of(person)), any(LocalDate.class))).thenReturn(fullDayAbsences);

        when(messageSource.getMessage(eq("calendar.department.title"), any(), eq(GERMAN))).thenReturn("Abwesenheitskalender der Abteilung DepartmentName");
        final ByteArrayResource iCal = new ByteArrayResource(new byte[]{}, "calendar");
        when(iCalService.getCalendar("Abwesenheitskalender der Abteilung DepartmentName", fullDayAbsences, person)).thenReturn(iCal);

        final ByteArrayResource calendar = sut.getCalendarForDepartment(1L, 10L, "secret", GERMAN);
        assertThat(calendar).isEqualTo(iCal);
    }

    @Test
    void getCalendarForDepartmentButDepartmentNotFound() {

        when(departmentService.getDepartmentById(1L)).thenReturn(Optional.empty());

        final Person person = new Person();
        person.setId(10L);
        when(personService.getPersonByID(10L)).thenReturn(Optional.of(person));

        when(departmentCalendarRepository.findBySecretAndPerson("secret", person)).thenReturn(Optional.of(new DepartmentCalendar()));

        assertThatIllegalArgumentException()
            .isThrownBy(() -> sut.getCalendarForDepartment(1L, 10L, "secret", GERMAN));
    }

    @Test
    void getCalendarForDepartmentSecretIsNull() {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> sut.getCalendarForDepartment(1L, 10L, null, GERMAN));
    }

    @Test
    void getCalendarForDepartmentSecretIsEmpty() {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> sut.getCalendarForDepartment(1L, 10L, "", GERMAN));
    }

    @Test
    void getCalendarForDepartmentSecretIsEmptyWithWhitespace() {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> sut.getCalendarForDepartment(1L, 10L, "  ", GERMAN));
    }

    @Test
    void getCalendarForDepartmentButSecretDoesNotExist() {

        final Person person = new Person();
        when(personService.getPersonByID(10L)).thenReturn(Optional.of(person));
        when(departmentCalendarRepository.findBySecretAndPerson("secret", person)).thenReturn(Optional.empty());

        assertThatIllegalArgumentException()
            .isThrownBy(() -> sut.getCalendarForDepartment(1L, 10L, "secret", GERMAN));
    }

    @Test
    void getCalendarForDepartmentButSecretDoesNotMatchTheGivenPerson() {

        when(departmentService.getDepartmentById(1L)).thenReturn(Optional.of(new Department()));

        final Person person = new Person();
        when(personService.getPersonByID(10L)).thenReturn(Optional.of(person));

        final DepartmentCalendar calendar = new DepartmentCalendar();
        calendar.setDepartmentId(1337L);
        when(departmentCalendarRepository.findBySecretAndPerson("secret", person)).thenReturn(Optional.of(calendar));

        assertThatIllegalArgumentException()
            .isThrownBy(() -> sut.getCalendarForDepartment(1L, 10L, "secret", GERMAN));
    }

    @Test
    void createCalendarForDepartmentAndPerson() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setId(1L);
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));

        when(departmentService.departmentExists(42L)).thenReturn(true);

        DepartmentCalendar receivedDepartmentCalendar = new DepartmentCalendar();
        when(departmentCalendarRepository.findByDepartmentIdAndPerson(42L, person)).thenReturn(Optional.of(receivedDepartmentCalendar));

        when(departmentCalendarRepository.save(receivedDepartmentCalendar)).thenReturn(receivedDepartmentCalendar);

        final DepartmentCalendar actualDepartmentCalendar = sut.createCalendarForDepartmentAndPerson(42, 1, java.time.Period.parse("P12M"));
        assertThat(actualDepartmentCalendar.getDepartmentId()).isEqualTo(42);
        assertThat(actualDepartmentCalendar.getPerson()).isEqualTo(person);
        assertThat(actualDepartmentCalendar.getSecret()).isNotBlank();
    }

    @Test
    void createCalendarForDepartmentAndPersonNoPersonFound() {
        when(personService.getPersonByID(1L)).thenReturn(Optional.empty());

        assertThatIllegalArgumentException()
            .isThrownBy(() -> sut.createCalendarForDepartmentAndPerson(42, 1, java.time.Period.parse("P12M")));
    }

    @Test
    void createCalendarForDepartmentAndPersonNoDepartmentFound() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setId(1L);
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));

        when(departmentService.departmentExists(42L)).thenReturn(false);

        assertThatIllegalStateException()
            .isThrownBy(() -> sut.createCalendarForDepartmentAndPerson(42, 1, java.time.Period.parse("P12M")));
    }

    @Test
    void createCalendarForDepartmentAndPersonNoCalendarFound() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setId(1L);
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));

        when(departmentService.departmentExists(42L)).thenReturn(true);

        when(departmentCalendarRepository.findByDepartmentIdAndPerson(42L, person)).thenReturn(Optional.empty());

        when(departmentCalendarRepository.save(any(DepartmentCalendar.class))).thenAnswer(returnsFirstArg());

        final DepartmentCalendar actualDepartmentCalendar = sut.createCalendarForDepartmentAndPerson(42, 1, java.time.Period.parse("P12M"));
        assertThat(actualDepartmentCalendar.getDepartmentId()).isEqualTo(42);
        assertThat(actualDepartmentCalendar.getPerson()).isEqualTo(person);
        assertThat(actualDepartmentCalendar.getSecret()).isNotBlank();

        verify(departmentCalendarRepository).save(any(DepartmentCalendar.class));
    }

    @Test
    void deleteDepartmentsCalendarsForPerson() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setId(1L);
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));

        sut.deleteDepartmentsCalendarsForPerson(1);

        verify(departmentCalendarRepository).deleteByPerson(person);
    }

    @Test
    void deleteOnPersonDeletionEvent() {
        final Person person = new Person();

        sut.deleteCalendarForPerson(new PersonDeletedEvent(person));

        verify(departmentCalendarRepository).deleteByPerson(person);
    }


    @Test
    void getCalendarForDepartmentAndCreatedAtIsAfterChosenPeriodSinceDate() {

        final Clock clock = Clock.fixed(Instant.parse("2019-04-15T10:00:00.00Z"), ZoneId.of("UTC"));
        final DepartmentCalendarService departmentCalendarService = new DepartmentCalendarService(calendarAbsenceService, departmentService, personService,
            departmentCalendarRepository, iCalService, messageSource, clock);

        final Department department = createDepartment("DepartmentName");
        department.setId(1L);
        final LocalDate createdAt = LocalDate.of(2018, 5, 1);
        department.setCreatedAt(createdAt);
        when(departmentService.getDepartmentById(1L)).thenReturn(Optional.of(department));

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setId(10L);
        when(personService.getPersonByID(10L)).thenReturn(Optional.of(person));

        department.setMembers(List.of(person));

        final DepartmentCalendar departmentCalendar = new DepartmentCalendar();
        departmentCalendar.setId(1L);
        departmentCalendar.setDepartmentId(1L);
        departmentCalendar.setCalendarPeriod(java.time.Period.parse("P12M"));
        when(departmentCalendarRepository.findBySecretAndPerson("secret", person)).thenReturn(Optional.of(departmentCalendar));

        final List<CalendarAbsence> fullDayAbsences = List.of(absence(person, parse("2018-03-26", ofPattern("yyyy-MM-dd")), parse("2018-03-26", ofPattern("yyyy-MM-dd")), FULL));
        when(calendarAbsenceService.getOpenAbsencesSince(List.of(person), createdAt)).thenReturn(fullDayAbsences);

        departmentCalendarService.getCalendarForDepartment(1L, 10L, "secret", GERMAN);
        verify(calendarAbsenceService).getOpenAbsencesSince(List.of(person), createdAt);
    }

    @Test
    void getCalendarForDepartmentAndCreatedAtIsBeforeChosenPeriodSinceDate() {

        final Clock clock = Clock.fixed(Instant.parse("2019-06-15T10:00:00.00Z"), ZoneId.of("UTC"));
        final DepartmentCalendarService departmentCalendarService = new DepartmentCalendarService(calendarAbsenceService, departmentService, personService,
            departmentCalendarRepository, iCalService, messageSource, clock);

        final Department department = createDepartment("DepartmentName");
        department.setId(1L);
        final LocalDate createdAt = LocalDate.of(2018, 5, 1);
        department.setCreatedAt(createdAt);
        when(departmentService.getDepartmentById(1L)).thenReturn(Optional.of(department));

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setId(10L);
        when(personService.getPersonByID(10L)).thenReturn(Optional.of(person));

        department.setMembers(List.of(person));

        final DepartmentCalendar departmentCalendar = new DepartmentCalendar();
        departmentCalendar.setId(1L);
        departmentCalendar.setDepartmentId(1L);
        departmentCalendar.setCalendarPeriod(java.time.Period.parse("P12M"));
        when(departmentCalendarRepository.findBySecretAndPerson("secret", person)).thenReturn(Optional.of(departmentCalendar));

        final List<CalendarAbsence> fullDayAbsences = List.of(absence(person, parse("2018-03-26", ofPattern("yyyy-MM-dd")), parse("2018-03-26", ofPattern("yyyy-MM-dd")), FULL));
        when(calendarAbsenceService.getOpenAbsencesSince(List.of(person), LocalDate.of(2018, 6, 15))).thenReturn(fullDayAbsences);

        departmentCalendarService.getCalendarForDepartment(1L, 10L, "secret", GERMAN);
        verify(calendarAbsenceService).getOpenAbsencesSince(List.of(person), LocalDate.of(2018, 6, 15));
    }

    @Test
    void getCalendarsForPersonReturnsCalendarsForGivenPersonId() {
        Long personId = 1L;

        final DepartmentCalendar departmentCalendar = new DepartmentCalendar();

        when(departmentCalendarRepository.findByPersonId(personId)).thenReturn(List.of(departmentCalendar));

        List<DepartmentCalendar> result = sut.getCalendarsForPerson(personId);

        assertThat(result).hasSize(1).containsExactly(departmentCalendar);
    }

    private CalendarAbsence absence(Person person, LocalDate start, LocalDate end, DayLength length) {
        final Period period = new Period(start, end, length);
        final AbsenceTimeConfiguration timeConfig = new AbsenceTimeConfiguration(new TimeSettings());

        return new CalendarAbsence(person, period, timeConfig);
    }
}
