package org.synyx.urlaubsverwaltung.sicknote.statistics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteService;
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysCountService;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Year;
import java.time.ZoneId;
import java.util.List;

import static java.math.BigDecimal.ONE;
import static java.time.temporal.TemporalAdjusters.lastDayOfYear;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.SECOND_STAGE_AUTHORITY;
import static org.synyx.urlaubsverwaltung.person.Role.SICK_NOTE_VIEW;
import static org.synyx.urlaubsverwaltung.person.Role.USER;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteStatus.ACTIVE;

@ExtendWith(MockitoExtension.class)
class SickNoteStatisticsServiceTest {

    private SickNoteStatisticsService sut;

    @Mock
    private SickNoteService sickNoteService;
    @Mock
    private WorkDaysCountService workDaysCountService;
    @Mock
    private DepartmentService departmentService;

    @BeforeEach
    void setUp() {
        sut = new SickNoteStatisticsService(sickNoteService, workDaysCountService, departmentService);
    }

    @Test
    void ensureCreateStatisticsForPersonWithRoleDepartmentHeadOnlyForMembers() {

        final Clock fixedClock = Clock.fixed(Instant.parse("2022-10-17T00:00:00.00Z"), ZoneId.systemDefault());

        final Person departmentHead = new Person();
        departmentHead.setPermissions(List.of(USER, DEPARTMENT_HEAD, SICK_NOTE_VIEW));

        final Person member1 = new Person();
        final Person member2 = new Person();
        final List<Person> members = List.of(member1, member2);
        when(departmentService.getMembersForDepartmentHead(departmentHead)).thenReturn(members);

        final LocalDate firstDayOfYear = Year.now(fixedClock).atDay(1);
        final LocalDate lastDayOfYear = firstDayOfYear.with(lastDayOfYear());
        final List<SickNote> sickNotes = List.of(SickNote.builder()
            .person(member1)
            .startDate(LocalDate.of(2022, 10, 10))
            .endDate(LocalDate.of(2022, 10, 10))
            .build());
        when(sickNoteService.getForStatesAndPerson(List.of(ACTIVE), members, firstDayOfYear, lastDayOfYear)).thenReturn(sickNotes);
        when(workDaysCountService.getWorkDaysCount(any(), any(), any(), any())).thenReturn(ONE);

        final SickNoteStatistics sickNoteStatistics = sut.createStatisticsForPerson(departmentHead, fixedClock);
        assertThat(sickNoteStatistics.getTotalNumberOfSickNotes()).isOne();
        assertThat(sickNoteStatistics.getNumberOfPersonsWithMinimumOneSickNote()).isOne();
    }

    @Test
    void ensureCreateStatisticsForNoPersonWithRoleDepartmentHeadWithoutSickNoteView() {

        final Clock fixedClock = Clock.fixed(Instant.parse("2022-10-17T00:00:00.00Z"), ZoneId.systemDefault());

        final Person departmentHead = new Person();
        departmentHead.setPermissions(List.of(USER, DEPARTMENT_HEAD));

        final SickNoteStatistics sickNoteStatistics = sut.createStatisticsForPerson(departmentHead, fixedClock);
        assertThat(sickNoteStatistics.getTotalNumberOfSickNotes()).isZero();
        assertThat(sickNoteStatistics.getNumberOfPersonsWithMinimumOneSickNote()).isZero();
    }

    @Test
    void ensureCreateStatisticsForPersonWithRoleSecondStageAuthorityOnlyForMembers() {

        final Clock fixedClock = Clock.fixed(Instant.parse("2022-10-17T00:00:00.00Z"), ZoneId.systemDefault());

        final Person ssa = new Person();
        ssa.setPermissions(List.of(USER, SECOND_STAGE_AUTHORITY, SICK_NOTE_VIEW));

        final Person member1 = new Person();
        final Person member2 = new Person();
        final List<Person> members = List.of(member1, member2);
        when(departmentService.getMembersForSecondStageAuthority(ssa)).thenReturn(members);

        final LocalDate firstDayOfYear = Year.now(fixedClock).atDay(1);
        final LocalDate lastDayOfYear = firstDayOfYear.with(lastDayOfYear());
        final SickNote sickNote = SickNote.builder()
            .person(member1)
            .startDate(LocalDate.of(2022, 10, 10))
            .endDate(LocalDate.of(2022, 10, 10))
            .build();
        final List<SickNote> sickNotes = List.of(sickNote);
        when(sickNoteService.getForStatesAndPerson(List.of(ACTIVE), members, firstDayOfYear, lastDayOfYear)).thenReturn(sickNotes);
        when(workDaysCountService.getWorkDaysCount(any(), any(), any(), any())).thenReturn(ONE);

        final SickNoteStatistics sickNoteStatistics = sut.createStatisticsForPerson(ssa, fixedClock);
        assertThat(sickNoteStatistics.getTotalNumberOfSickNotes()).isOne();
        assertThat(sickNoteStatistics.getNumberOfPersonsWithMinimumOneSickNote()).isOne();
    }

    @Test
    void ensureCreateNoStatisticsForPersonWithRoleSecondStageAuthorityWithoutSickNoteView() {

        final Clock fixedClock = Clock.fixed(Instant.parse("2022-10-17T00:00:00.00Z"), ZoneId.systemDefault());

        final Person ssa = new Person();
        ssa.setPermissions(List.of(USER, SECOND_STAGE_AUTHORITY));

        final SickNoteStatistics sickNoteStatistics = sut.createStatisticsForPerson(ssa, fixedClock);
        assertThat(sickNoteStatistics.getTotalNumberOfSickNotes()).isZero();
        assertThat(sickNoteStatistics.getNumberOfPersonsWithMinimumOneSickNote()).isZero();
    }

    @Test
    void ensureCreateStatisticsForPersonWithRoleBossAndSickNoteView() {

        final Clock fixedClock = Clock.fixed(Instant.parse("2022-10-17T00:00:00.00Z"), ZoneId.systemDefault());

        final Person personWithRole = new Person();
        personWithRole.setPermissions(List.of(USER, BOSS, SICK_NOTE_VIEW));

        final Person person = new Person();
        final LocalDate from = LocalDate.of(2022, 1, 1);
        final LocalDate to = LocalDate.of(2022, 12, 31);

        final SickNote sickNote = SickNote.builder()
            .person(person)
            .startDate(LocalDate.of(2022, 10, 10))
            .endDate(LocalDate.of(2022, 10, 10))
            .build();
        final List<SickNote> sickNotes = List.of(sickNote);
        when(sickNoteService.getAllActiveByPeriod(from, to)).thenReturn(sickNotes);
        when(workDaysCountService.getWorkDaysCount(any(), any(), any(), any())).thenReturn(ONE);

        final SickNoteStatistics sickNoteStatistics = sut.createStatisticsForPerson(personWithRole, fixedClock);
        assertThat(sickNoteStatistics.getTotalNumberOfSickNotes()).isOne();
        assertThat(sickNoteStatistics.getNumberOfPersonsWithMinimumOneSickNote()).isOne();
    }

    @Test
    void ensureCreateNoStatisticsForPersonWithRoleBossWithoutSickNoteView() {

        final Clock fixedClock = Clock.fixed(Instant.parse("2022-10-17T00:00:00.00Z"), ZoneId.systemDefault());

        final Person personWithRole = new Person();
        personWithRole.setPermissions(List.of(USER, BOSS));

        final SickNoteStatistics sickNoteStatistics = sut.createStatisticsForPerson(personWithRole, fixedClock);
        assertThat(sickNoteStatistics.getTotalNumberOfSickNotes()).isZero();
        assertThat(sickNoteStatistics.getNumberOfPersonsWithMinimumOneSickNote()).isZero();
    }

    @Test
    void ensureCreateStatisticsForPersonWithRoleOffice() {

        final Clock fixedClock = Clock.fixed(Instant.parse("2022-10-17T00:00:00.00Z"), ZoneId.systemDefault());

        final Person personWithRole = new Person();
        personWithRole.setPermissions(List.of(USER, OFFICE));

        final Person person = new Person();
        final LocalDate from = LocalDate.of(2022, 1, 1);
        final LocalDate to = LocalDate.of(2022, 12, 31);

        final SickNote sickNote = SickNote.builder()
            .person(person)
            .startDate(LocalDate.of(2022, 10, 10))
            .endDate(LocalDate.of(2022, 10, 10))
            .build();
        final List<SickNote> sickNotes = List.of(sickNote);
        when(sickNoteService.getAllActiveByPeriod(from, to)).thenReturn(sickNotes);
        when(workDaysCountService.getWorkDaysCount(any(), any(), any(), any())).thenReturn(ONE);

        final SickNoteStatistics sickNoteStatistics = sut.createStatisticsForPerson(personWithRole, fixedClock);
        assertThat(sickNoteStatistics.getTotalNumberOfSickNotes()).isOne();
        assertThat(sickNoteStatistics.getNumberOfPersonsWithMinimumOneSickNote()).isOne();
    }

    @Test
    void ensureCreateStatisticsForPersonWithoutPrivilegedRole() {

        final Clock fixedClock = Clock.fixed(Instant.parse("2022-10-17T00:00:00.00Z"), ZoneId.systemDefault());

        final Person person = new Person();
        person.setPermissions(List.of(USER));

        final SickNoteStatistics sickNoteStatistics = sut.createStatisticsForPerson(person, fixedClock);
        assertThat(sickNoteStatistics.getTotalNumberOfSickNotes()).isZero();
        assertThat(sickNoteStatistics.getNumberOfPersonsWithMinimumOneSickNote()).isZero();
    }
}
