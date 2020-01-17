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
public class ICalServiceTest {

    private ICalService sut;

    @Mock
    private AbsenceService absenceService;
    @Mock
    private PersonService personService;
    @Mock
    private DepartmentService departmentService;
    @Mock
    private PersonCalendarRepository personCalendarRepository;
    @Mock
    private DepartmentCalendarRepository departmentCalendarRepository;
    @Mock
    private CompanyCalendarRepository companyCalendarRepository;


    @Before
    public void setUp() {

        sut = new ICalService(absenceService, personService, departmentService, personCalendarRepository, departmentCalendarRepository, companyCalendarRepository);
    }

    @Test(expected = CalendarException.class)
    public void getCalendarForPersonAndNoAbsenceFound() {

        final Person person = createPerson();
        person.setId(1);
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

        final PersonCalendar personCalendar = new PersonCalendar();
        personCalendar.setPerson(person);
        when(personCalendarRepository.findBySecret("secret")).thenReturn(personCalendar);

        when(absenceService.getOpenAbsences(List.of(person))).thenReturn(List.of());

        sut.getCalendarForPerson(1, "secret");
    }

    @Test
    public void getCalendarForPersonForOneFullDay() {

        final Person person = createPerson();
        person.setId(1);
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

        final PersonCalendar personCalendar = new PersonCalendar();
        personCalendar.setPerson(person);
        when(personCalendarRepository.findBySecret("secret")).thenReturn(personCalendar);

        final Absence fullDayAbsence = absence(person, toDateTime("2019-03-26"), toDateTime("2019-03-26"), FULL);
        when(absenceService.getOpenAbsences(List.of(person))).thenReturn(List.of(fullDayAbsence));

        final String iCal = sut.getCalendarForPerson(1, "secret");

        assertThat(iCal).contains("VERSION:2.0");
        assertThat(iCal).contains("CALSCALE:GREGORIAN");
        assertThat(iCal).contains("PRODID:-//Urlaubsverwaltung//iCal4j 1.0//DE");
        assertThat(iCal).contains("X-WR-CALNAME:Abwesenheitskalender von Marlene Muster");

        assertThat(iCal).contains("SUMMARY:Marlene Muster abwesend");
        assertThat(iCal).contains("DTSTART;VALUE=DATE:20190326");
    }

    @Test
    public void getCalendarForPersonForHalfDayMorning() {

        final Person person = createPerson();
        person.setId(1);
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

        final PersonCalendar personCalendar = new PersonCalendar();
        personCalendar.setPerson(person);
        when(personCalendarRepository.findBySecret("secret")).thenReturn(personCalendar);

        final Absence morningAbsence = absence(person, toDateTime("2019-04-26"), toDateTime("2019-04-26"), MORNING);
        when(absenceService.getOpenAbsences(List.of(person))).thenReturn(List.of(morningAbsence));

        final String iCal = sut.getCalendarForPerson(1, "secret");

        assertThat(iCal).contains("VERSION:2.0");
        assertThat(iCal).contains("CALSCALE:GREGORIAN");
        assertThat(iCal).contains("PRODID:-//Urlaubsverwaltung//iCal4j 1.0//DE");
        assertThat(iCal).contains("X-WR-CALNAME:Abwesenheitskalender von Marlene Muster");

        assertThat(iCal).contains("SUMMARY:Marlene Muster abwesend");
        assertThat(iCal).contains("DTSTART;TZID=Etc/UTC:20190426T080000");
        assertThat(iCal).contains("DTEND;TZID=Etc/UTC:20190426T120000");
    }

    @Test
    public void getCalendarForPersonForMultipleFullDays() {

        final Person person = createPerson();
        person.setId(1);
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

        final PersonCalendar personCalendar = new PersonCalendar();
        personCalendar.setPerson(person);
        when(personCalendarRepository.findBySecret("secret")).thenReturn(personCalendar);

        final Absence manyFullDayAbsence = absence(person, toDateTime("2019-03-26"), toDateTime("2019-04-01"), FULL);
        when(absenceService.getOpenAbsences(List.of(person))).thenReturn(List.of(manyFullDayAbsence));

        final String iCal = sut.getCalendarForPerson(1, "secret");

        assertThat(iCal).contains("VERSION:2.0");
        assertThat(iCal).contains("CALSCALE:GREGORIAN");
        assertThat(iCal).contains("PRODID:-//Urlaubsverwaltung//iCal4j 1.0//DE");
        assertThat(iCal).contains("X-WR-CALNAME:Abwesenheitskalender von Marlene Muster");

        assertThat(iCal).contains("SUMMARY:Marlene Muster abwesend");
        assertThat(iCal).contains("DTSTART;TZID=Etc/UTC:20190326T000000");
        assertThat(iCal).contains("DTEND;TZID=Etc/UTC:20190401T000000");
    }

    @Test
    public void getCalendarForPersonForHalfDayNoon() {

        final Person person = createPerson();
        person.setId(1);
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

        final PersonCalendar personCalendar = new PersonCalendar();
        personCalendar.setPerson(person);
        when(personCalendarRepository.findBySecret("secret")).thenReturn(personCalendar);

        final Absence noonAbsence = absence(person, toDateTime("2019-05-26"), toDateTime("2019-05-26"), NOON);
        when(absenceService.getOpenAbsences(List.of(person))).thenReturn(List.of(noonAbsence));

        final String iCal = sut.getCalendarForPerson(1, "secret");

        assertThat(iCal).contains("VERSION:2.0");
        assertThat(iCal).contains("CALSCALE:GREGORIAN");
        assertThat(iCal).contains("PRODID:-//Urlaubsverwaltung//iCal4j 1.0//DE");
        assertThat(iCal).contains("X-WR-CALNAME:Abwesenheitskalender von Marlene Muster");

        assertThat(iCal).contains("SUMMARY:Marlene Muster abwesend");
        assertThat(iCal).contains("DTSTART;TZID=Etc/UTC:20190526T120000");
        assertThat(iCal).contains("DTEND;TZID=Etc/UTC:20190526T160000");
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
    public void getCalendarForDepartmentForOneFullDay() {

        final Department department = createDepartment("DepartmentName");
        department.setId(1);
        when(departmentService.getDepartmentById(1)).thenReturn(Optional.of(department));

        final DepartmentCalendar calendar = new DepartmentCalendar();
        calendar.setId(1L);
        calendar.setDepartment(department);
        when(departmentCalendarRepository.findBySecret("secret")).thenReturn(calendar);

        final Person person = createPerson();
        department.setMembers(List.of(person));

        final Absence fullDayAbsence = absence(person, toDateTime("2019-03-26"), toDateTime("2019-03-26"), FULL);
        when(absenceService.getOpenAbsences(List.of(person))).thenReturn(List.of(fullDayAbsence));

        final String iCal = sut.getCalendarForDepartment(1, "secret");

        assertThat(iCal).contains("VERSION:2.0");
        assertThat(iCal).contains("CALSCALE:GREGORIAN");
        assertThat(iCal).contains("PRODID:-//Urlaubsverwaltung//iCal4j 1.0//DE");
        assertThat(iCal).contains("X-WR-CALNAME:Abwesenheitskalender der Abteilung DepartmentName");

        assertThat(iCal).contains("SUMMARY:Marlene Muster abwesend");
        assertThat(iCal).contains("DTSTART;VALUE=DATE:20190326");
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

    @Test
    public void getCalendarForAllForOneFullDay() {

        final Person person = createPerson();
        final Absence fullDayAbsence = absence(person, toDateTime("2019-03-26"), toDateTime("2019-03-26"), FULL);
        when(absenceService.getOpenAbsences()).thenReturn(List.of(fullDayAbsence));

        CompanyCalendar calendar = new CompanyCalendar();
        calendar.setId(1L);
        when(companyCalendarRepository.findBySecret("secret")).thenReturn(calendar);

        final String iCal = sut.getCalendarForAll("secret");

        assertThat(iCal).contains("VERSION:2.0");
        assertThat(iCal).contains("CALSCALE:GREGORIAN");
        assertThat(iCal).contains("PRODID:-//Urlaubsverwaltung//iCal4j 1.0//DE");
        assertThat(iCal).contains("X-WR-CALNAME:Abwesenheitskalender der Firma");

        assertThat(iCal).contains("SUMMARY:Marlene Muster abwesend");
        assertThat(iCal).contains("DTSTART;VALUE=DATE:20190326");
    }

    @Test(expected = IllegalArgumentException.class)
    public void getCalendarForAllSecretIsNull() {

        sut.getCalendarForAll(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getCalendarForAllSecretIsEmpty() {

        sut.getCalendarForAll("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void getCalendarForAllSecretIsEmptyWithWhitespace() {

        sut.getCalendarForAll("  ");
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
