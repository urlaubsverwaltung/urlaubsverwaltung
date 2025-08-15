package org.synyx.urlaubsverwaltung.sicknote.statistics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteCategory;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteService;
import org.synyx.urlaubsverwaltung.sicknote.sicknotetype.SickNoteType;

import java.time.Clock;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;

import static java.time.temporal.TemporalAdjusters.lastDayOfYear;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.SECOND_STAGE_AUTHORITY;
import static org.synyx.urlaubsverwaltung.person.Role.SICK_NOTE_VIEW;
import static org.synyx.urlaubsverwaltung.person.Role.USER;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteStatus.ACTIVE;
import static org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendarFactory.workingTimeCalendarMondayToSunday;

@ExtendWith(MockitoExtension.class)
class SickNoteStatisticsServiceTest {

    private SickNoteStatisticsService sut;

    @Mock
    private SickNoteService sickNoteService;
    @Mock
    private DepartmentService departmentService;
    @Mock
    private PersonService personService;

    private static final Clock clock = Clock.systemDefaultZone();

    @BeforeEach
    void setUp() {
        sut = new SickNoteStatisticsService(sickNoteService, departmentService, personService, clock);
    }

    @Test
    void ensureCreateStatisticsForPersonWithRoleDepartmentHeadOnlyForMembers() {

        final Year year = Year.of(2022);

        final Person departmentHead = new Person();
        departmentHead.setId(1L);
        departmentHead.setPermissions(List.of(USER, DEPARTMENT_HEAD, SICK_NOTE_VIEW));

        final Person member1 = new Person();
        member1.setId(2L);

        final Person member2 = new Person();
        member2.setId(3L);

        final List<Person> members = List.of(member1, member2);
        when(departmentService.getManagedMembersOfPerson(departmentHead, year)).thenReturn(members);

        final LocalDate date = LocalDate.of(year.getValue(), 10, 10);

        final LocalDate firstDayOfYear = year.atDay(1);
        final LocalDate lastDayOfYear = firstDayOfYear.with(lastDayOfYear());
        final List<SickNote> sickNotes = List.of(SickNote.builder()
            .person(member1)
            .sickNoteType(sickNoteType(SickNoteCategory.SICK_NOTE))
            .startDate(date)
            .endDate(date)
            .dayLength(FULL)
            .workingTimeCalendar(workingTimeCalendarMondayToSunday(date, date))
            .build());
        when(sickNoteService.getForStatesAndPerson(List.of(ACTIVE), members, firstDayOfYear, lastDayOfYear)).thenReturn(sickNotes);

        final SickNoteStatistics sickNoteStatistics = sut.createStatisticsForPerson(year, departmentHead);
        assertThat(sickNoteStatistics.getTotalNumberOfSickNotes()).isOne();
        assertThat(sickNoteStatistics.getNumberOfPersonsWithMinimumOneSickNote()).isOne();

        verifyNoMoreInteractions(personService);
    }

    @Test
    void ensureCreateStatisticsForNoPersonWithRoleDepartmentHeadWithoutSickNoteView() {

        final Year year = Year.of(2022);

        final Person departmentHead = new Person();
        departmentHead.setPermissions(List.of(USER, DEPARTMENT_HEAD));

        final SickNoteStatistics sickNoteStatistics = sut.createStatisticsForPerson(year, departmentHead);
        assertThat(sickNoteStatistics.getTotalNumberOfSickNotes()).isZero();
        assertThat(sickNoteStatistics.getNumberOfPersonsWithMinimumOneSickNote()).isZero();

        verifyNoMoreInteractions(personService);
        verifyNoMoreInteractions(departmentService);
        verifyNoMoreInteractions(sickNoteService);
    }

    @Test
    void ensureCreateStatisticsForPersonWithRoleSecondStageAuthorityOnlyForMembers() {

        final Year year = Year.of(2022);

        final Person secondStageAuthPerson = new Person();
        secondStageAuthPerson.setId(1L);
        secondStageAuthPerson.setPermissions(List.of(USER, SECOND_STAGE_AUTHORITY, SICK_NOTE_VIEW));

        final Person member1 = new Person();
        member1.setId(2L);

        final Person member2 = new Person();
        member2.setId(3L);

        final List<Person> members = List.of(member1, member2);
        when(departmentService.getManagedMembersOfPerson(secondStageAuthPerson, year)).thenReturn(members);

        final LocalDate date = LocalDate.of(year.getValue(), 10, 10);

        final LocalDate firstDayOfYear = year.atDay(1);
        final LocalDate lastDayOfYear = firstDayOfYear.with(lastDayOfYear());
        final SickNote sickNote = SickNote.builder()
            .person(member1)
            .sickNoteType(sickNoteType(SickNoteCategory.SICK_NOTE))
            .startDate(date)
            .endDate(date)
            .dayLength(FULL)
            .workingTimeCalendar(workingTimeCalendarMondayToSunday(date, date))
            .build();
        final List<SickNote> sickNotes = List.of(sickNote);
        when(sickNoteService.getForStatesAndPerson(List.of(ACTIVE), members, firstDayOfYear, lastDayOfYear)).thenReturn(sickNotes);

        final SickNoteStatistics sickNoteStatistics = sut.createStatisticsForPerson(year, secondStageAuthPerson);
        assertThat(sickNoteStatistics.getTotalNumberOfSickNotes()).isOne();
        assertThat(sickNoteStatistics.getNumberOfPersonsWithMinimumOneSickNote()).isOne();

        verifyNoMoreInteractions(personService);
    }

    @Test
    void ensureCreateNoStatisticsForPersonWithRoleSecondStageAuthorityWithoutSickNoteView() {

        final Year year = Year.of(2022);

        final Person ssa = new Person();
        ssa.setPermissions(List.of(USER, SECOND_STAGE_AUTHORITY));

        final SickNoteStatistics sickNoteStatistics = sut.createStatisticsForPerson(year, ssa);
        assertThat(sickNoteStatistics.getTotalNumberOfSickNotes()).isZero();
        assertThat(sickNoteStatistics.getNumberOfPersonsWithMinimumOneSickNote()).isZero();

        verifyNoMoreInteractions(personService);
        verifyNoMoreInteractions(departmentService);
        verifyNoMoreInteractions(sickNoteService);
    }

    @Test
    void ensureCreateStatisticsForPersonWithRoleBossAndSickNoteView() {

        final Year year = Year.of(2022);

        final Person personWithRole = new Person();
        personWithRole.setId(1L);
        personWithRole.setPermissions(List.of(USER, BOSS, SICK_NOTE_VIEW));

        final Person person = new Person();
        person.setId(2L);
        person.setPermissions(List.of(USER));

        when(personService.getAllPersonsHavingAccountInYear(year)).thenReturn(List.of(personWithRole, person));

        final LocalDate from = LocalDate.of(year.getValue(), 1, 1);
        final LocalDate to = LocalDate.of(year.getValue(), 12, 31);
        final LocalDate date = LocalDate.of(year.getValue(), 10, 10);

        final SickNote sickNote = SickNote.builder()
            .person(person)
            .sickNoteType(sickNoteType(SickNoteCategory.SICK_NOTE))
            .startDate(date)
            .endDate(date)
            .dayLength(FULL)
            .workingTimeCalendar(workingTimeCalendarMondayToSunday(date, date))
            .build();
        when(sickNoteService.getForStatesAndPerson(List.of(ACTIVE), List.of(personWithRole, person), from, to)).thenReturn(List.of(sickNote));

        final SickNoteStatistics sickNoteStatistics = sut.createStatisticsForPerson(year, personWithRole);
        assertThat(sickNoteStatistics.getTotalNumberOfSickNotes()).isOne();
        assertThat(sickNoteStatistics.getNumberOfPersonsWithMinimumOneSickNote()).isOne();

        verifyNoMoreInteractions(departmentService);
    }

    @Test
    void ensureCreateNoStatisticsForPersonWithRoleBossWithoutSickNoteView() {

        final Year year = Year.of(2022);

        final Person personWithRole = new Person();
        personWithRole.setId(1L);
        personWithRole.setPermissions(List.of(USER, BOSS));

        final SickNoteStatistics sickNoteStatistics = sut.createStatisticsForPerson(year, personWithRole);
        assertThat(sickNoteStatistics.getTotalNumberOfSickNotes()).isZero();
        assertThat(sickNoteStatistics.getNumberOfPersonsWithMinimumOneSickNote()).isZero();

        verifyNoMoreInteractions(personService);
        verifyNoMoreInteractions(departmentService);
        verifyNoMoreInteractions(sickNoteService);
    }

    @Test
    void ensureCreateStatisticsForPersonWithRoleOffice() {

        final Year year = Year.of(2022);

        final Person office = new Person();
        office.setId(1L);
        office.setPermissions(List.of(USER, OFFICE));

        final Person person = new Person();
        person.setId(2L);
        person.setPermissions(List.of(USER));

        when(personService.getAllPersonsHavingAccountInYear(year)).thenReturn(List.of(office, person));

        final LocalDate from = LocalDate.of(2022, 1, 1);
        final LocalDate to = LocalDate.of(2022, 12, 31);

        final SickNote sickNote = SickNote.builder()
            .person(person)
            .sickNoteType(sickNoteType(SickNoteCategory.SICK_NOTE))
            .startDate(LocalDate.of(year.getValue(), 10, 10))
            .endDate(LocalDate.of(year.getValue(), 10, 10))
            .dayLength(FULL)
            .workingTimeCalendar(workingTimeCalendarMondayToSunday(from, to))
            .build();
        when(sickNoteService.getForStatesAndPerson(List.of(ACTIVE), List.of(office, person), from, to)).thenReturn(List.of(sickNote));

        final SickNoteStatistics sickNoteStatistics = sut.createStatisticsForPerson(year, office);
        assertThat(sickNoteStatistics.getTotalNumberOfSickNotes()).isOne();
        assertThat(sickNoteStatistics.getNumberOfPersonsWithMinimumOneSickNote()).isOne();

        verifyNoMoreInteractions(departmentService);
    }

    private static SickNoteType sickNoteType(SickNoteCategory category) {
        final SickNoteType sickNoteType = new SickNoteType();
        sickNoteType.setId(1L);
        sickNoteType.setCategory(category);
        return sickNoteType;
    }

    @Test
    void ensureCreateStatisticsForUser() {

        final Year year = Year.of(2022);

        final Person user = new Person();
        user.setId(1L);
        user.setPermissions(List.of(USER, SICK_NOTE_VIEW));

        final LocalDate from = LocalDate.of(2022, 1, 1);
        final LocalDate to = LocalDate.of(2022, 12, 31);

        final SickNote sickNote = SickNote.builder()
            .person(user)
            .sickNoteType(sickNoteType(SickNoteCategory.SICK_NOTE))
            .startDate(LocalDate.of(year.getValue(), 10, 10))
            .endDate(LocalDate.of(year.getValue(), 10, 10))
            .dayLength(FULL)
            .workingTimeCalendar(workingTimeCalendarMondayToSunday(from, to))
            .build();
        when(sickNoteService.getForStatesAndPerson(List.of(ACTIVE), List.of(user), from, to)).thenReturn(List.of(sickNote));

        final SickNoteStatistics sickNoteStatistics = sut.createStatisticsForPerson(year, user);
        assertThat(sickNoteStatistics.getTotalNumberOfSickNotes()).isOne();
        assertThat(sickNoteStatistics.getNumberOfPersonsWithMinimumOneSickNote()).isOne();

        verifyNoMoreInteractions(personService);
        verifyNoMoreInteractions(departmentService);
    }

    @Test
    void ensureCreateStatisticsForPersonWithoutPrivilegedRole() {

        final Year year = Year.of(2022);

        final Person person = new Person();
        person.setId(1L);
        person.setPermissions(List.of(USER));

        final SickNoteStatistics sickNoteStatistics = sut.createStatisticsForPerson(year, person);
        assertThat(sickNoteStatistics.getTotalNumberOfSickNotes()).isZero();
        assertThat(sickNoteStatistics.getNumberOfPersonsWithMinimumOneSickNote()).isZero();

        verifyNoMoreInteractions(personService);
        verifyNoMoreInteractions(departmentService);
        verifyNoMoreInteractions(sickNoteService);
    }
}
