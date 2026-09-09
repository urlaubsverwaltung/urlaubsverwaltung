package org.synyx.urlaubsverwaltung.overtime.statistics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.Role;

import java.time.Year;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.person.Role.DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

/**
 * Truth table of "whose overtime is part of the figures of this signed-in person?".
 */
@ExtendWith(MockitoExtension.class)
class OvertimeStatisticsPersonsTest {

    private static final Year YEAR = Year.of(2026);

    private OvertimeStatisticsPersons sut;

    @Mock
    private PersonService personService;
    @Mock
    private DepartmentService departmentService;

    @BeforeEach
    void setUp() {
        sut = new OvertimeStatisticsPersons(personService, departmentService);
    }

    @Nested
    class RelevantPersonsOfYear {

        @ParameterizedTest
        @EnumSource(value = Role.class, names = {"OFFICE", "BOSS"})
        void ensureOfficeAndBossGetTheWholeCohortOfTheYear(Role role) {

            final Person signedInUser = person(1L, USER, role);
            final Person marie = person(2L, USER);
            final Person klaus = person(3L, USER);

            when(personService.getAllPersonsHavingAccountInYear(YEAR)).thenReturn(List.of(marie, klaus));

            assertThat(sut.relevantPersonsOfYear(signedInUser, YEAR)).containsExactly(marie, klaus);
            verifyNoInteractions(departmentService);
        }

        @ParameterizedTest
        @EnumSource(value = Role.class, names = {"DEPARTMENT_HEAD", "SECOND_STAGE_AUTHORITY"})
        void ensureManagerGetsTheManagedMembersOfThatYear(Role role) {

            final Person signedInUser = person(1L, USER, role);
            final Person marie = person(2L, USER);

            when(departmentService.getManagedMembersOfPerson(signedInUser, YEAR)).thenReturn(List.of(marie));

            assertThat(sut.relevantPersonsOfYear(signedInUser, YEAR)).containsExactly(marie);
            verifyNoInteractions(personService);
        }

        @ParameterizedTest
        @EnumSource(value = Role.class, names = {"USER", "INACTIVE"})
        void ensureNobodyElseGetsAnyPerson(Role role) {

            assertThat(sut.relevantPersonsOfYear(person(1L, role), YEAR)).isEmpty();

            verifyNoInteractions(personService);
            verifyNoInteractions(departmentService);
        }

        @Test
        void ensureManagerWithoutManagedMembersGetsAnEmptyList() {

            final Person signedInUser = person(1L, USER, DEPARTMENT_HEAD);

            when(departmentService.getManagedMembersOfPerson(signedInUser, YEAR)).thenReturn(List.of());

            assertThat(sut.relevantPersonsOfYear(signedInUser, YEAR)).isEmpty();
        }

        @Test
        void ensureOfficeWinsOverAManagementRole() {

            final Person signedInUser = person(1L, USER, OFFICE, DEPARTMENT_HEAD);
            final Person marie = person(2L, USER);

            when(personService.getAllPersonsHavingAccountInYear(YEAR)).thenReturn(List.of(marie));

            assertThat(sut.relevantPersonsOfYear(signedInUser, YEAR)).containsExactly(marie);
            verifyNoInteractions(departmentService);
        }
    }

    @Nested
    class RelevantPersonsOfWholeHistory {

        @ParameterizedTest
        @EnumSource(value = Role.class, names = {"OFFICE", "BOSS"})
        void ensureOfficeAndBossGetEveryActivePerson(Role role) {

            final Person signedInUser = person(1L, USER, role);
            final Person marie = person(2L, USER);
            final Person klaus = person(3L, USER);

            when(personService.getActivePersons()).thenReturn(List.of(marie, klaus));

            assertThat(sut.relevantPersonsOfWholeHistory(signedInUser)).containsExactly(marie, klaus);
            verifyNoInteractions(departmentService);
        }

        @ParameterizedTest
        @EnumSource(value = Role.class, names = {"DEPARTMENT_HEAD", "SECOND_STAGE_AUTHORITY"})
        void ensureManagerGetsTheManagedActiveMembers(Role role) {

            final Person signedInUser = person(1L, USER, role);
            final Person marie = person(2L, USER);

            when(departmentService.getManagedActiveMembersOfPerson(signedInUser)).thenReturn(List.of(marie));

            assertThat(sut.relevantPersonsOfWholeHistory(signedInUser)).containsExactly(marie);
            verifyNoInteractions(personService);
        }

        @ParameterizedTest
        @EnumSource(value = Role.class, names = {"USER", "INACTIVE"})
        void ensureNobodyElseGetsAnyPerson(Role role) {

            assertThat(sut.relevantPersonsOfWholeHistory(person(1L, role))).isEmpty();

            verifyNoInteractions(personService);
            verifyNoInteractions(departmentService);
        }
    }

    private static Person person(long id, Role... roles) {
        final Person person = new Person("user-" + id, "Reichenbach", "Marie", "person%d@example.org".formatted(id));
        person.setId(id);
        person.setPermissions(List.of(roles));
        return person;
    }
}
