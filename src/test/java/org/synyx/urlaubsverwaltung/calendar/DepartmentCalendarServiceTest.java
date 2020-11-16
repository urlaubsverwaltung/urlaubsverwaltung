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
import org.synyx.urlaubsverwaltung.department.Department;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.period.Period;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.absence.TimeSettings;

import java.io.File;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static java.time.LocalDate.parse;
import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.Locale.GERMAN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
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
    private AbsenceService absenceService;
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

        sut = new DepartmentCalendarService(absenceService, departmentService, personService, departmentCalendarRepository, iCalService, messageSource);
    }

    @Test
    void deleteCalendarForDepartmentAndPerson() {

        final Department department = createDepartment("DepartmentName");
        department.setId(1);
        when(departmentService.getDepartmentById(1)).thenReturn(Optional.of(department));

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setId(10);
        when(personService.getPersonByID(10)).thenReturn(Optional.of(person));

        sut.deleteCalendarForDepartmentAndPerson(1, 10);

        verify(departmentCalendarRepository).deleteByDepartmentAndPerson(department, person);
    }

    @Test
    void getCalendarForDepartment() {

        final Department department = createDepartment("DepartmentName");
        department.setId(1);
        when(departmentService.getDepartmentById(1)).thenReturn(Optional.of(department));

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setId(10);
        when(personService.getPersonByID(10)).thenReturn(Optional.of(person));

        final DepartmentCalendar departmentCalendar = new DepartmentCalendar();
        when(departmentCalendarRepository.findByDepartmentAndPerson(department, person)).thenReturn(departmentCalendar);

        final Optional<DepartmentCalendar> calendarForDepartment = sut.getCalendarForDepartment(1, 10);
        assertThat(calendarForDepartment).contains(departmentCalendar);
    }

    @Test
    void getCalendarForDepartmentForOneFullDay() {

        final Department department = createDepartment("DepartmentName");
        department.setId(1);
        when(departmentService.getDepartmentById(1)).thenReturn(Optional.of(department));

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setId(10);
        when(personService.getPersonByID(10)).thenReturn(Optional.of(person));

        department.setMembers(List.of(person));

        final DepartmentCalendar departmentCalendar = new DepartmentCalendar();
        departmentCalendar.setId(1L);
        departmentCalendar.setDepartment(department);
        when(departmentCalendarRepository.findBySecretAndPerson("secret", person)).thenReturn(departmentCalendar);

        final List<Absence> fullDayAbsences = List.of(absence(person, parse("2019-03-26", ofPattern("yyyy-MM-dd")), parse("2019-03-26", ofPattern("yyyy-MM-dd")), FULL));
        when(absenceService.getOpenAbsences(List.of(person))).thenReturn(fullDayAbsences);

        when(messageSource.getMessage(eq("calendar.department.title"), any(), eq(GERMAN))).thenReturn("Abwesenheitskalender der Abteilung DepartmentName");
        final File iCal = new File("calendar.ics");
        iCal.deleteOnExit();
        when(iCalService.getCalendar("Abwesenheitskalender der Abteilung DepartmentName", fullDayAbsences)).thenReturn(iCal);

        final File calendar = sut.getCalendarForDepartment(1, 10, "secret", GERMAN);
        assertThat(calendar).hasName("calendar.ics");
    }

    @Test
    void getCalendarForDepartmentButDepartmentNotFound() {

        when(departmentService.getDepartmentById(1)).thenReturn(Optional.empty());

        final Person person = new Person();
        person.setId(10);
        when(personService.getPersonByID(10)).thenReturn(Optional.of(person));

        when(departmentCalendarRepository.findBySecretAndPerson("secret", person)).thenReturn(new DepartmentCalendar());

        assertThatIllegalArgumentException()
            .isThrownBy(() -> sut.getCalendarForDepartment(1, 10, "secret", GERMAN));
    }

    @Test
    void getCalendarForDepartmentSecretIsNull() {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> sut.getCalendarForDepartment(1, 10, null, GERMAN));
    }

    @Test
    void getCalendarForDepartmentSecretIsEmpty() {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> sut.getCalendarForDepartment(1, 10, "", GERMAN));
    }

    @Test
    void getCalendarForDepartmentSecretIsEmptyWithWhitespace() {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> sut.getCalendarForDepartment(1, 10, "  ", GERMAN));
    }

    @Test
    void getCalendarForDepartmentButSecretDoesNotExist() {

        final Person person = new Person();
        when(personService.getPersonByID(10)).thenReturn(Optional.of(person));
        when(departmentCalendarRepository.findBySecretAndPerson("secret", person)).thenReturn(null);

        assertThatIllegalArgumentException()
            .isThrownBy(() -> sut.getCalendarForDepartment(1, 10, "secret", GERMAN));
    }

    @Test
    void getCalendarForDepartmentButSecretDoesNotMatchTheGivenPerson() {

        final Department department = createDepartment();
        department.setId(1);
        when(departmentService.getDepartmentById(1)).thenReturn(Optional.of(department));

        final Person person = new Person();
        when(personService.getPersonByID(10)).thenReturn(Optional.of(person));

        final Department notMatchingDepartment = createDepartment();
        notMatchingDepartment.setId(1337);
        DepartmentCalendar calendar = new DepartmentCalendar();
        calendar.setDepartment(notMatchingDepartment);
        when(departmentCalendarRepository.findBySecretAndPerson("secret", person)).thenReturn(calendar);

        assertThatIllegalArgumentException()
            .isThrownBy(() -> sut.getCalendarForDepartment(1, 10, "secret", GERMAN));
    }

    @Test
    void createCalendarForDepartmentAndPerson() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setId(1);
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

        final Department department = createDepartment();
        department.setId(42);
        when(departmentService.getDepartmentById(42)).thenReturn(Optional.of(department));

        DepartmentCalendar receivedDepartmentCalendar = new DepartmentCalendar();
        when(departmentCalendarRepository.findByDepartmentAndPerson(department, person)).thenReturn(receivedDepartmentCalendar);

        when(departmentCalendarRepository.save(receivedDepartmentCalendar)).thenReturn(receivedDepartmentCalendar);

        final DepartmentCalendar actualDepartmentCalendar = sut.createCalendarForDepartmentAndPerson(42, 1);
        assertThat(actualDepartmentCalendar.getDepartment()).isEqualTo(department);
        assertThat(actualDepartmentCalendar.getPerson()).isEqualTo(person);
        assertThat(actualDepartmentCalendar.getSecret()).isNotBlank();
    }

    @Test
    void createCalendarForDepartmentAndPersonNoPersonFound() {
        when(personService.getPersonByID(1)).thenReturn(Optional.empty());

        assertThatIllegalArgumentException()
            .isThrownBy(() -> sut.createCalendarForDepartmentAndPerson(42, 1));
    }

    @Test
    void createCalendarForDepartmentAndPersonNoDepartmentFound() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setId(1);
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

        when(departmentService.getDepartmentById(42)).thenReturn(Optional.empty());

        assertThatIllegalArgumentException()
            .isThrownBy(() -> sut.createCalendarForDepartmentAndPerson(42, 1));
    }

    @Test
    void createCalendarForDepartmentAndPersonNoCalendarFound() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setId(1);
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

        final Department department = createDepartment();
        department.setId(42);
        when(departmentService.getDepartmentById(42)).thenReturn(Optional.of(department));

        when(departmentCalendarRepository.findByDepartmentAndPerson(department, person)).thenReturn(null);

        when(departmentCalendarRepository.save(any(DepartmentCalendar.class))).thenAnswer(returnsFirstArg());

        final DepartmentCalendar actualDepartmentCalendar = sut.createCalendarForDepartmentAndPerson(42, 1);
        assertThat(actualDepartmentCalendar.getDepartment()).isEqualTo(department);
        assertThat(actualDepartmentCalendar.getPerson()).isEqualTo(person);
        assertThat(actualDepartmentCalendar.getSecret()).isNotBlank();

        verify(departmentCalendarRepository).save(any(DepartmentCalendar.class));
    }

    @Test
    void deleteDepartmentsCalendarsForPerson() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

        sut.deleteDepartmentsCalendarsForPerson(1);

        verify(departmentCalendarRepository).deleteByPerson(person);
    }

    private Absence absence(Person person, LocalDate start, LocalDate end, DayLength length) {
        final Period period = new Period(start, end, length);
        final AbsenceTimeConfiguration timeConfig = new AbsenceTimeConfiguration(new TimeSettings());

        return new Absence(person, period, timeConfig);
    }
}
