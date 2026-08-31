package org.synyx.urlaubsverwaltung.absence.statistics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonActivePeriod;
import org.synyx.urlaubsverwaltung.person.PersonActivePeriodService;
import org.synyx.urlaubsverwaltung.person.PersonId;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.Role;

import java.time.Instant;
import java.time.Year;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.SECOND_STAGE_AUTHORITY;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

@ExtendWith(MockitoExtension.class)
class AbsenceStatisticsPersonsTest {

    private AbsenceStatisticsPersons sut;

    @Mock
    private PersonService personService;
    @Mock
    private DepartmentService departmentService;
    @Mock
    private PersonActivePeriodService personActivePeriodService;

    @Captor
    private ArgumentCaptor<Collection<PersonId>> personIdsCaptor;

    @BeforeEach
    void setUp() {
        sut = new AbsenceStatisticsPersons(personService, departmentService, personActivePeriodService);
    }

    private static Person person(long id, Role... roles) {
        final Person person = new Person();
        person.setId(id);
        person.setPermissions(List.of(roles));
        return person;
    }

    private void givenAllActive(Person... persons) {
        final Map<PersonId, List<PersonActivePeriod>> activePeriods = new HashMap<>();
        for (Person person : persons) {
            activePeriods.put(person.getIdAsPersonId(), List.of(new PersonActivePeriod(person.getIdAsPersonId(), Instant.EPOCH)));
        }
        when(personActivePeriodService.getActivePeriodsOverlapping(any(), any(), any())).thenReturn(activePeriods);
    }

    @Nested
    class RoleBasedCandidates {

        @Test
        void officeGetsEveryPersonOfTheSystem() {

            final Person signedInUser = person(1, OFFICE);
            final Person person1 = person(2);
            final Person person2 = person(3);

            when(personService.getAllPersons()).thenReturn(List.of(person1, person2));
            givenAllActive(person1, person2);

            final List<Person> actual = sut.relevantPersons(signedInUser, Year.of(2024));

            assertThat(actual).containsExactly(person1, person2);
        }

        @Test
        void bossGetsEveryPersonOfTheSystem() {

            final Person signedInUser = person(1, BOSS);
            final Person person1 = person(2);

            when(personService.getAllPersons()).thenReturn(List.of(person1));
            givenAllActive(person1);

            final List<Person> actual = sut.relevantPersons(signedInUser, Year.of(2024));

            assertThat(actual).containsExactly(person1);
        }

        @Test
        void departmentHeadGetsOnlyManagedMembersNotTheWholeWorkforce() {

            final Person signedInUser = person(1, DEPARTMENT_HEAD);
            final Person managedMember = person(2);
            final Year year = Year.of(2024);

            when(departmentService.getManagedMembersOfPerson(signedInUser, year)).thenReturn(List.of(managedMember));
            givenAllActive(managedMember);

            final List<Person> actual = sut.relevantPersons(signedInUser, year);

            assertThat(actual).containsExactly(managedMember);
        }

        @Test
        void secondStageAuthorityGetsOnlyManagedMembers() {

            final Person signedInUser = person(1, SECOND_STAGE_AUTHORITY);
            final Person managedMember = person(2);
            final Year year = Year.of(2024);

            when(departmentService.getManagedMembersOfPerson(signedInUser, year)).thenReturn(List.of(managedMember));
            givenAllActive(managedMember);

            final List<Person> actual = sut.relevantPersons(signedInUser, year);

            assertThat(actual).containsExactly(managedMember);
        }

        @Test
        void personWithoutEligibleRoleGetsEmptyListWithoutAnyLookup() {

            final Person signedInUser = person(1, USER);

            final List<Person> actual = sut.relevantPersons(signedInUser, Year.of(2024));

            assertThat(actual).isEmpty();
            verifyNoInteractions(personService, departmentService, personActivePeriodService);
        }
    }

    @Nested
    class ActivePeriodFiltering {

        @Test
        void queriesActivePeriodOverlapForTheWholeRequestedYear() {

            final Person signedInUser = person(1, OFFICE);
            final Person person1 = person(2);
            final Year year = Year.of(2024);

            when(personService.getAllPersons()).thenReturn(List.of(person1));
            givenAllActive(person1);

            sut.relevantPersons(signedInUser, year);

            verify(personActivePeriodService).getActivePeriodsOverlapping(personIdsCaptor.capture(),
                eq(Instant.parse("2024-01-01T00:00:00Z")), eq(Instant.parse("2025-01-01T00:00:00Z")));
            assertThat(personIdsCaptor.getValue()).containsExactly(person1.getIdAsPersonId());
        }

        @Test
        void personEnteringMidYearIsIncluded() {

            final Person signedInUser = person(1, OFFICE);
            final Person joinedMidYear = person(2);
            final Year year = Year.of(2024);

            when(personService.getAllPersons()).thenReturn(List.of(joinedMidYear));
            when(personActivePeriodService.getActivePeriodsOverlapping(any(), any(), any())).thenReturn(Map.of(
                joinedMidYear.getIdAsPersonId(), List.of(new PersonActivePeriod(joinedMidYear.getIdAsPersonId(), Instant.parse("2024-07-01T00:00:00Z")))
            ));

            final List<Person> actual = sut.relevantPersons(signedInUser, year);

            assertThat(actual).containsExactly(joinedMidYear);
        }

        @Test
        void personLeavingMidYearIsIncluded() {

            final Person signedInUser = person(1, OFFICE);
            final Person leftMidYear = person(2);
            final Year year = Year.of(2024);

            when(personService.getAllPersons()).thenReturn(List.of(leftMidYear));
            when(personActivePeriodService.getActivePeriodsOverlapping(any(), any(), any())).thenReturn(Map.of(
                leftMidYear.getIdAsPersonId(), List.of(new PersonActivePeriod(leftMidYear.getIdAsPersonId(),
                    Instant.parse("2020-01-01T00:00:00Z"), Optional.of(Instant.parse("2024-07-01T00:00:00Z"))))
            ));

            final List<Person> actual = sut.relevantPersons(signedInUser, year);

            assertThat(actual).containsExactly(leftMidYear);
        }

        @Test
        void personJoiningOnlyNextYearIsNotIncluded() {

            final Person signedInUser = person(1, OFFICE);
            final Person joinsNextYear = person(2);
            final Year year = Year.of(2024);

            when(personService.getAllPersons()).thenReturn(List.of(joinsNextYear));
            when(personActivePeriodService.getActivePeriodsOverlapping(any(), any(), any())).thenReturn(Map.of(
                joinsNextYear.getIdAsPersonId(), emptyList()
            ));

            final List<Person> actual = sut.relevantPersons(signedInUser, year);

            assertThat(actual).isEmpty();
        }

        @Test
        void personDeactivatedInTheMeantimeStillCountsForAPastYearTheyWereActiveIn() {

            final Person signedInUser = person(1, OFFICE);
            final Person meanwhileDeactivated = person(2);
            final Year pastYear = Year.of(2020);

            when(personService.getAllPersons()).thenReturn(List.of(meanwhileDeactivated));
            when(personActivePeriodService.getActivePeriodsOverlapping(any(), any(), any())).thenReturn(Map.of(
                meanwhileDeactivated.getIdAsPersonId(), List.of(new PersonActivePeriod(meanwhileDeactivated.getIdAsPersonId(),
                    Instant.parse("2019-01-01T00:00:00Z"), Optional.of(Instant.parse("2023-01-01T00:00:00Z"))))
            ));

            final List<Person> actual = sut.relevantPersons(signedInUser, pastYear);

            assertThat(actual).containsExactly(meanwhileDeactivated);
        }
    }
}
