package org.synyx.urlaubsverwaltung.sicknote.statistics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.Role;
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
import static org.synyx.urlaubsverwaltung.person.Role.DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
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
        departmentHead.setPermissions(List.of(USER, DEPARTMENT_HEAD));

        final Person member1 = new Person();
        final Person member2 = new Person();
        final List<Person> members = List.of(member1, member2);
        when(departmentService.getMembersForDepartmentHead(departmentHead)).thenReturn(members);

        final LocalDate firstDayOfYear = Year.now(fixedClock).atDay(1);
        final LocalDate lastDayOfYear = firstDayOfYear.with(lastDayOfYear());
        final SickNote sickNote = new SickNote();
        sickNote.setPerson(member1);
        sickNote.setStartDate(LocalDate.of(2022, 10, 10));
        sickNote.setEndDate(LocalDate.of(2022, 10, 10));
        final List<SickNote> sickNotes = List.of(sickNote);
        when(sickNoteService.getForStatesAndPerson(List.of(ACTIVE), members, firstDayOfYear, lastDayOfYear)).thenReturn(sickNotes);
        when(workDaysCountService.getWorkDaysCount(any(), any(), any(), any())).thenReturn(ONE);

        final SickNoteStatistics sickNoteStatistics = sut.createStatisticsForPerson(departmentHead, fixedClock);
        assertThat(sickNoteStatistics.getTotalNumberOfSickNotes()).isOne();
        assertThat(sickNoteStatistics.getNumberOfPersonsWithMinimumOneSickNote()).isOne();
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"OFFICE", "BOSS"})
    void ensureCreateStatisticsForPersonWithRole(Role role) {

        final Clock fixedClock = Clock.fixed(Instant.parse("2022-10-17T00:00:00.00Z"), ZoneId.systemDefault());

        final Person personWithRole = new Person();
        personWithRole.setPermissions(List.of(USER, role));

        final Person person = new Person();
        final SickNote sickNote = new SickNote();
        sickNote.setPerson(person);
        sickNote.setStartDate(LocalDate.of(2022, 10, 10));
        sickNote.setEndDate(LocalDate.of(2022, 10, 10));
        final List<SickNote> sickNotes = List.of(sickNote);
        when(sickNoteService.getAllActiveByYear(Year.now(fixedClock).getValue())).thenReturn(sickNotes);
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
