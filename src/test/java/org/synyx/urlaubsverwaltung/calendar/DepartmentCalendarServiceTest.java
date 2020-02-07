package org.synyx.urlaubsverwaltung.calendar;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.synyx.urlaubsverwaltung.absence.AbsenceService;
import org.synyx.urlaubsverwaltung.calendarintegration.absence.Absence;
import org.synyx.urlaubsverwaltung.calendarintegration.absence.AbsenceTimeConfiguration;
import org.synyx.urlaubsverwaltung.department.Department;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
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
import static org.synyx.urlaubsverwaltung.testdatacreator.TestDataCreator.createDepartment;
import static org.synyx.urlaubsverwaltung.testdatacreator.TestDataCreator.createPerson;


@RunWith(MockitoJUnitRunner.class)
public class DepartmentCalendarServiceTest {

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


    @Before
    public void setUp() {

        sut = new DepartmentCalendarService(absenceService, departmentService, personService, departmentCalendarRepository, iCalService);
    }

    @Test
    public void getCalendarForDepartmentForOneFullDay() {

        final Department department = createDepartment("DepartmentName");
        department.setId(1);
        when(departmentService.getDepartmentById(1)).thenReturn(Optional.of(department));

        final Person person = createPerson();
        person.setId(10);
        when(personService.getPersonByID(10)).thenReturn(Optional.of(person));

        department.setMembers(List.of(person));

        final DepartmentCalendar departmentCalendar = new DepartmentCalendar();
        departmentCalendar.setId(1L);
        departmentCalendar.setDepartment(department);
        when(departmentCalendarRepository.findBySecretAndPerson("secret", person)).thenReturn(departmentCalendar);

        final List<Absence> fullDayAbsences = List.of(absence(person, toDateTime("2019-03-26"), toDateTime("2019-03-26"), FULL));
        when(absenceService.getOpenAbsences(List.of(person))).thenReturn(fullDayAbsences);

        when(iCalService.generateCalendar("Abwesenheitskalender der Abteilung DepartmentName", fullDayAbsences)).thenReturn("calendar");

        final String calendar = sut.getCalendarForDepartment(1, 10, "secret");
        assertThat(calendar).isEqualTo("calendar");
    }

    @Test(expected = IllegalArgumentException.class)
    public void getCalendarForDepartmentButDepartmentNotFound() {

        when(departmentService.getDepartmentById(1)).thenReturn(Optional.ofNullable(null));

        final Person person = new Person();
        person.setId(10);
        when(personService.getPersonByID(10)).thenReturn(Optional.of(person));

        when(departmentCalendarRepository.findBySecretAndPerson("secret", person)).thenReturn(new DepartmentCalendar());

        sut.getCalendarForDepartment(1, 10, "secret");
    }

    @Test(expected = IllegalArgumentException.class)
    public void getCalendarForDepartmentSecretIsNull() {

        sut.getCalendarForDepartment(1, 10, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getCalendarForDepartmentSecretIsEmpty() {

        sut.getCalendarForDepartment(1, 10, "");
    }

    @Test(expected = IllegalArgumentException.class)
    public void getCalendarForDepartmentSecretIsEmptyWithWhitespace() {

        sut.getCalendarForDepartment(1, 10, "  ");
    }

    @Test(expected = IllegalArgumentException.class)
    public void getCalendarForDepartmentButSecretDoesNotExist() {

        final Person person = new Person();
        when(personService.getPersonByID(10)).thenReturn(Optional.of(person));
        when(departmentCalendarRepository.findBySecretAndPerson("secret", person)).thenReturn(null);

        sut.getCalendarForDepartment(1, 10, "secret");
    }

    @Test(expected = IllegalArgumentException.class)
    public void getCalendarForDepartmentButSecretDoesNotMatchTheGivenPerson() {

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

        sut.getCalendarForDepartment(1, 10, "secret");
    }

    @Test
    public void createCalendarForDepartmentAndPerson() {

        final Person person = createPerson();
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

    @Test(expected = IllegalArgumentException.class)
    public void createCalendarForDepartmentAndPersonNoPersonFound() {

        when(personService.getPersonByID(1)).thenReturn(Optional.empty());

        sut.createCalendarForDepartmentAndPerson(42, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createCalendarForDepartmentAndPersonNoDepartmentFound() {

        final Person person = createPerson();
        person.setId(1);
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

        when(departmentService.getDepartmentById(42)).thenReturn(Optional.empty());

        sut.createCalendarForDepartmentAndPerson(42, 1);
    }

    @Test
    public void createCalendarForDepartmentAndPersonNoCalendarFound() {

        final Person person = createPerson();
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

    private Absence absence(Person person, LocalDate start, LocalDate end, DayLength length) {
        final Period period = new Period(start, end, length);
        final AbsenceTimeConfiguration timeConfig = new AbsenceTimeConfiguration(new CalendarSettings());

        return new Absence(person, period, timeConfig);
    }

    private static LocalDate toDateTime(String input) {
        return LocalDate.parse(input, ofPattern("yyyy-MM-dd"));
    }
}
