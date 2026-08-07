package org.synyx.urlaubsverwaltung.sicknote.statistics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNotePermissionEvaluator;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;

import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.person.Role.DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.person.Role.INACTIVE;
import static org.synyx.urlaubsverwaltung.person.Role.SECOND_STAGE_AUTHORITY;
import static org.synyx.urlaubsverwaltung.person.Role.SICK_NOTE_VIEW;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

@ExtendWith(MockitoExtension.class)
class SickNoteRelevantPersonsServiceTest {

    private SickNoteRelevantPersonsService sut;

    @Mock
    private SickNotePermissionEvaluator sickNotePermissionEvaluator;
    @Mock
    private PersonService personService;
    @Mock
    private DepartmentService departmentService;

    private static final Clock fixedClock = Clock.fixed(Instant.parse("2026-08-07T00:00:00.00Z"), UTC);

    @BeforeEach
    void setUp() {
        sut = new SickNoteRelevantPersonsService(sickNotePermissionEvaluator, personService, departmentService, fixedClock);
    }

    @Test
    void ensureGetStatisticRelevantPersonsForPersonAllowedToViewAllPersons() {

        final Person person = new Person();
        person.setPermissions(List.of(USER));

        when(sickNotePermissionEvaluator.isAllowedToViewSickNotesOfAllPersons(person)).thenReturn(true);

        final Person personOne = new Person();
        personOne.setId(1L);
        final Person personTwo = new Person();
        personTwo.setId(2L);

        when(personService.getAllPersonsHavingAccountInYear(Year.of(2026))).thenReturn(List.of(personOne, personTwo));

        final LocalDate from = LocalDate.of(2026, 1, 1);
        final LocalDate to = LocalDate.of(2026, 12, 31);

        final List<Person> actual = sut.getStatisticRelevantPersons(from, to, person);
        assertThat(actual).containsExactly(personOne, personTwo);

        verifyNoMoreInteractions(departmentService);
    }

    @Test
    void ensureGetStatisticRelevantPersonsForPersonAllowedToViewAllPersonsAcrossMultipleYears() {

        final Person person = new Person();
        person.setPermissions(List.of(USER));

        when(sickNotePermissionEvaluator.isAllowedToViewSickNotesOfAllPersons(person)).thenReturn(true);

        final Person personOne = new Person();
        personOne.setId(1L);
        final Person personTwo = new Person();
        personTwo.setId(2L);

        when(personService.getAllPersonsHavingAccountInYear(Year.of(2025))).thenReturn(List.of(personOne));
        when(personService.getAllPersonsHavingAccountInYear(Year.of(2026))).thenReturn(List.of(personOne, personTwo));

        final LocalDate from = LocalDate.of(2025, 12, 1);
        final LocalDate to = LocalDate.of(2026, 1, 31);

        final List<Person> actual = sut.getStatisticRelevantPersons(from, to, person);
        assertThat(actual).containsExactly(personOne, personTwo);

        verifyNoMoreInteractions(departmentService);
    }

    @Test
    void ensureGetStatisticRelevantPersonsForDepartmentHeadReturnsOnlyActiveMembersInCurrentYear() {

        final Person departmentHead = new Person();
        departmentHead.setId(1L);
        departmentHead.setPermissions(List.of(USER, DEPARTMENT_HEAD, SICK_NOTE_VIEW));

        when(sickNotePermissionEvaluator.isAllowedToViewSickNotesOfAllPersons(departmentHead)).thenReturn(false);

        final Person activeMember = new Person();
        activeMember.setId(2L);
        activeMember.setPermissions(List.of(USER));

        final Person inactiveMember = new Person();
        inactiveMember.setId(3L);
        inactiveMember.setPermissions(List.of(INACTIVE));

        when(departmentService.getManagedMembersOfPerson(departmentHead, Year.of(2026))).thenReturn(List.of(activeMember, inactiveMember));

        final LocalDate from = LocalDate.of(2026, 1, 1);
        final LocalDate to = LocalDate.of(2026, 12, 31);

        final List<Person> actual = sut.getStatisticRelevantPersons(from, to, departmentHead);
        assertThat(actual).containsExactly(activeMember);

        verifyNoMoreInteractions(personService);
    }

    @Test
    void ensureGetStatisticRelevantPersonsForSecondStageAuthorityReturnsOnlyActiveMembersInCurrentYear() {

        final Person secondStageAuthority = new Person();
        secondStageAuthority.setId(1L);
        secondStageAuthority.setPermissions(List.of(USER, SECOND_STAGE_AUTHORITY, SICK_NOTE_VIEW));

        when(sickNotePermissionEvaluator.isAllowedToViewSickNotesOfAllPersons(secondStageAuthority)).thenReturn(false);

        final Person activeMember = new Person();
        activeMember.setId(2L);
        activeMember.setPermissions(List.of(USER));

        final Person inactiveMember = new Person();
        inactiveMember.setId(3L);
        inactiveMember.setPermissions(List.of(INACTIVE));

        when(departmentService.getManagedMembersOfPerson(secondStageAuthority, Year.of(2026))).thenReturn(List.of(activeMember, inactiveMember));

        final LocalDate from = LocalDate.of(2026, 1, 1);
        final LocalDate to = LocalDate.of(2026, 12, 31);

        final List<Person> actual = sut.getStatisticRelevantPersons(from, to, secondStageAuthority);
        assertThat(actual).containsExactly(activeMember);

        verifyNoMoreInteractions(personService);
    }

    @Test
    void ensureGetStatisticRelevantPersonsForDepartmentHeadReturnsAllMembersRegardlessOfActiveStateForPastYear() {

        final Person departmentHead = new Person();
        departmentHead.setId(1L);
        departmentHead.setPermissions(List.of(USER, DEPARTMENT_HEAD, SICK_NOTE_VIEW));

        when(sickNotePermissionEvaluator.isAllowedToViewSickNotesOfAllPersons(departmentHead)).thenReturn(false);

        final Person activeMember = new Person();
        activeMember.setId(2L);
        activeMember.setPermissions(List.of(USER));

        final Person inactiveMember = new Person();
        inactiveMember.setId(3L);
        inactiveMember.setPermissions(List.of(INACTIVE));

        when(departmentService.getManagedMembersOfPerson(departmentHead, Year.of(2025))).thenReturn(List.of(activeMember, inactiveMember));

        final LocalDate from = LocalDate.of(2025, 1, 1);
        final LocalDate to = LocalDate.of(2025, 12, 31);

        final List<Person> actual = sut.getStatisticRelevantPersons(from, to, departmentHead);
        assertThat(actual).containsExactly(activeMember, inactiveMember);

        verifyNoMoreInteractions(personService);
    }

    @Test
    void ensureGetStatisticRelevantPersonsForPersonWithSickNoteViewOnlyReturnsThemself() {

        final Person person = new Person();
        person.setId(1L);
        person.setPermissions(List.of(USER, SICK_NOTE_VIEW));

        when(sickNotePermissionEvaluator.isAllowedToViewSickNotesOfAllPersons(person)).thenReturn(false);

        final LocalDate from = LocalDate.of(2026, 1, 1);
        final LocalDate to = LocalDate.of(2026, 12, 31);

        final List<Person> actual = sut.getStatisticRelevantPersons(from, to, person);
        assertThat(actual).containsExactly(person);

        verifyNoMoreInteractions(personService);
        verifyNoMoreInteractions(departmentService);
    }

    @Test
    void ensureGetStatisticRelevantPersonsForPersonWithoutAnyRelevantRoleReturnsEmptyList() {

        final Person person = new Person();
        person.setId(1L);
        person.setPermissions(List.of(USER));

        when(sickNotePermissionEvaluator.isAllowedToViewSickNotesOfAllPersons(person)).thenReturn(false);

        final LocalDate from = LocalDate.of(2026, 1, 1);
        final LocalDate to = LocalDate.of(2026, 12, 31);

        final List<Person> actual = sut.getStatisticRelevantPersons(from, to, person);
        assertThat(actual).isEmpty();

        verifyNoMoreInteractions(personService);
        verifyNoMoreInteractions(departmentService);
    }
}
