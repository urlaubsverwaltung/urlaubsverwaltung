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
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;
import static org.synyx.urlaubsverwaltung.period.DayLength.MORNING;
import static org.synyx.urlaubsverwaltung.period.DayLength.NOON;
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
    private DepartmentCalendarRepository departmentCalendarRepository;
    @Mock
    private ICalService iCalService;


    @Before
    public void setUp() {

        sut = new DepartmentCalendarService(absenceService, departmentService, departmentCalendarRepository, iCalService);
    }

    @Test
    public void getCalendarForDepartmentForOneFullDay() {

        final Department department = createDepartment("DepartmentName");
        department.setId(1);
        when(departmentService.getDepartmentById(1)).thenReturn(Optional.of(department));

        final DepartmentCalendar departmentCalendar = new DepartmentCalendar();
        departmentCalendar.setId(1L);
        departmentCalendar.setDepartment(department);
        when(departmentCalendarRepository.findBySecret("secret")).thenReturn(departmentCalendar);

        final Person person = createPerson();
        department.setMembers(List.of(person));

        final List<Absence> fullDayAbsences = List.of(absence(person, toDateTime("2019-03-26"), toDateTime("2019-03-26"), FULL));
        when(absenceService.getOpenAbsences(List.of(person))).thenReturn(fullDayAbsences);

        when(iCalService.generateCalendar("Abwesenheitskalender der Abteilung DepartmentName", fullDayAbsences)).thenReturn("calendar");

        final String calendar = sut.getCalendarForDepartment(1, "secret");
        assertThat(calendar).isEqualTo("calendar");
    }

    @Test(expected = IllegalArgumentException.class)
    public void getCalendarForDepartmentButDepartmentNotFound() {

        when(departmentService.getDepartmentById(1)).thenReturn(Optional.ofNullable(null));

        when(departmentCalendarRepository.findBySecret("secret")).thenReturn(new DepartmentCalendar());

        sut.getCalendarForDepartment(1, "secret");
    }

    @Test(expected = IllegalArgumentException.class)
    public void getCalendarForDepartmentSecretIsNull() {

        sut.getCalendarForDepartment(1, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getCalendarForDepartmentSecretIsEmpty() {

        sut.getCalendarForDepartment(1, "");
    }

    @Test(expected = IllegalArgumentException.class)
    public void getCalendarForDepartmentSecretIsEmptyWithWhitespace() {

        sut.getCalendarForDepartment(1, "  ");
    }

    @Test(expected = IllegalArgumentException.class)
    public void getCalendarForDepartmentButSecretDoesNotExist() {

        when(departmentCalendarRepository.findBySecret("secret")).thenReturn(null);

        sut.getCalendarForDepartment(1, "secret");
    }

    @Test(expected = IllegalArgumentException.class)
    public void getCalendarForDepartmentButSecretDoesNotMatchTheGivenPerson() {

        final Department department = createDepartment();
        department.setId(1);
        when(departmentService.getDepartmentById(1)).thenReturn(Optional.of(department));

        final Department notMatchingDepartment = createDepartment();
        notMatchingDepartment.setId(1337);
        DepartmentCalendar calendar = new DepartmentCalendar();
        calendar.setDepartment(notMatchingDepartment);
        when(departmentCalendarRepository.findBySecret("secret")).thenReturn(calendar);

        sut.getCalendarForDepartment(1, "secret");
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
