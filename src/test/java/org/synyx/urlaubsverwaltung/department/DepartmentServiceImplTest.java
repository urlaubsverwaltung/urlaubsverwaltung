package org.synyx.urlaubsverwaltung.department;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.synyx.urlaubsverwaltung.application.application.Application;
import org.synyx.urlaubsverwaltung.application.application.ApplicationService;
import org.synyx.urlaubsverwaltung.application.application.ApplicationStatus;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonId;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.Role;
import org.synyx.urlaubsverwaltung.search.PageableSearchQuery;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;

import static java.time.Month.DECEMBER;
import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.junit.jupiter.params.provider.EnumSource.Mode.INCLUDE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.person.Role.INACTIVE;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.SECOND_STAGE_AUTHORITY;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

@ExtendWith(MockitoExtension.class)
class DepartmentServiceImplTest {

    private DepartmentServiceImpl sut;

    @Mock
    private DepartmentRepository departmentRepository;
    @Mock
    private DepartmentMembershipService departmentMembershipService;
    @Mock
    private PersonService personService;
    @Mock
    private ApplicationService applicationService;
    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    private final Clock clock = Clock.fixed(Instant.now(), UTC);

    @BeforeEach
    void setUp() {
        sut = new DepartmentServiceImpl(departmentRepository, departmentMembershipService, personService, applicationService, applicationEventPublisher, clock);
    }

    @Nested
    class GetManagedMembersOfPersonYear {

        @Test
        void ensureGetManagedMembersOfPersonYearForUser() {

            final Person person = new Person();
            person.setPermissions(List.of(USER));

            final List<Person> actual = sut.getManagedMembersOfPerson(person, Year.now(clock));
            assertThat(actual).isEmpty();
        }

        @ParameterizedTest
        @EnumSource(value = Role.class, names = {"BOSS", "OFFICE"}, mode = INCLUDE)
        void ensureGetManagedMembersOfPersonYearFor(Role role) {

            final PersonId personId = new PersonId(1L);
            final Person bossOrOfficePerson = new Person();
            bossOrOfficePerson.setId(personId.value());
            bossOrOfficePerson.setPermissions(List.of(USER, role));

            final PersonId personId2 = new PersonId(2L);
            final Person person2 = new Person();
            person2.setId(personId2.value());

            final PersonId personId3 = new PersonId(3L);
            final Person person3 = new Person();
            person3.setId(personId3.value());

            final PersonId personId4 = new PersonId(4L);
            final Person person4 = new Person();
            person4.setId(personId4.value());

            final Year lastYear = Year.now(clock).minusYears(1);
            final Instant joinedLastYear = lastYear.atDay(42).atStartOfDay().toInstant(UTC);

            when(departmentMembershipService.getActiveMembershipsOfYear(lastYear))
                .thenReturn(Map.of(
                    personId2, List.of(
                        new DepartmentMembership(personId2, 1L, DepartmentMembershipKind.MEMBER, joinedLastYear)
                    ),
                    personId3, List.of(
                        new DepartmentMembership(personId3, 1L, DepartmentMembershipKind.MEMBER, joinedLastYear),
                        new DepartmentMembership(personId3, 2L, DepartmentMembershipKind.MEMBER, joinedLastYear)
                    ),
                    personId4, List.of(
                        new DepartmentMembership(personId4, 1L, DepartmentMembershipKind.MEMBER, joinedLastYear)
                    )
                ));

            when(personService.getAllPersonsByIds(Set.of(personId2, personId3, personId4)))
                .thenReturn(List.of(person2, person3, person4));

            final List<Person> actual = sut.getManagedMembersOfPerson(bossOrOfficePerson, lastYear);
            assertThat(actual).containsExactlyInAnyOrder(person2, person3, person4);
        }

        @Test
        void ensureGetManagedMembersOfPersonYearForDepartmentHead() {

            final PersonId departmentHeadId = new PersonId(1L);
            final Person departmentHead = new Person();
            departmentHead.setId(departmentHeadId.value());
            departmentHead.setPermissions(List.of(USER, DEPARTMENT_HEAD));

            final PersonId personId2 = new PersonId(2L);
            final Person person2 = new Person();
            person2.setId(personId2.value());

            final PersonId personId3 = new PersonId(3L);
            final Person person3 = new Person();
            person3.setId(personId3.value());

            final PersonId personId4 = new PersonId(4L);
            final Person person4 = new Person();
            person4.setId(personId4.value());

            final Year lastYear = Year.now(clock).minusYears(1);
            final Instant joinedLastYear = lastYear.atDay(42).atStartOfDay().toInstant(UTC);

            when(departmentMembershipService.getActiveMembershipsOfYear(lastYear))
                .thenReturn(Map.of(
                    departmentHeadId, List.of(
                        new DepartmentMembership(departmentHeadId, 1L, DepartmentMembershipKind.DEPARTMENT_HEAD, joinedLastYear)
                    ),
                    personId2, List.of(
                        new DepartmentMembership(personId2, 1L, DepartmentMembershipKind.MEMBER, joinedLastYear)
                    ),
                    personId3, List.of(
                        new DepartmentMembership(personId3, 2L, DepartmentMembershipKind.MEMBER, joinedLastYear)
                    ),
                    personId4, List.of(
                        new DepartmentMembership(personId4, 1L, DepartmentMembershipKind.MEMBER, joinedLastYear)
                    )
                ));

            when(personService.getAllPersonsByIds(Set.of(personId2, personId4)))
                .thenReturn(List.of(person2, person4));

            final List<Person> actual = sut.getManagedMembersOfPerson(departmentHead, lastYear);
            assertThat(actual).containsExactlyInAnyOrder(person2, person4);
        }

        @Test
        void ensureGetManagedMembersOfPersonYearForSecondStageAuthority() {

            final PersonId secondStageId = new PersonId(1L);
            final Person secondStageAuthority = new Person();
            secondStageAuthority.setId(secondStageId.value());
            secondStageAuthority.setPermissions(List.of(USER, SECOND_STAGE_AUTHORITY));

            final PersonId personId2 = new PersonId(2L);
            final Person person2 = new Person();
            person2.setId(personId2.value());

            final PersonId personId3 = new PersonId(3L);
            final Person person3 = new Person();
            person3.setId(personId3.value());

            final PersonId personId4 = new PersonId(4L);
            final Person person4 = new Person();
            person4.setId(personId4.value());

            final Year lastYear = Year.now(clock).minusYears(1);
            final Instant joinedLastYear = lastYear.atDay(42).atStartOfDay().toInstant(UTC);

            when(departmentMembershipService.getActiveMembershipsOfYear(lastYear))
                .thenReturn(Map.of(
                    secondStageId, List.of(
                        new DepartmentMembership(secondStageId, 1L, DepartmentMembershipKind.SECOND_STAGE_AUTHORITY, joinedLastYear)
                    ),
                    personId2, List.of(
                        new DepartmentMembership(personId2, 1L, DepartmentMembershipKind.MEMBER, joinedLastYear)
                    ),
                    personId3, List.of(
                        new DepartmentMembership(personId3, 2L, DepartmentMembershipKind.MEMBER, joinedLastYear)
                    ),
                    personId4, List.of(
                        new DepartmentMembership(personId4, 1L, DepartmentMembershipKind.MEMBER, joinedLastYear)
                    )
                ));

            when(personService.getAllPersonsByIds(Set.of(personId2, personId4)))
                .thenReturn(List.of(person2, person4));

            final List<Person> actual = sut.getManagedMembersOfPerson(secondStageAuthority, lastYear);
            assertThat(actual).containsExactlyInAnyOrder(person2, person4);
        }

        @Test
        void ensureGetManagedMembersOfPersonYearForDepartmentHeadAndSecondStageAuthority() {

            final PersonId managerId = new PersonId(1L);
            final Person manager = new Person();
            manager.setId(managerId.value());
            manager.setPermissions(List.of(USER, DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY));

            final PersonId personId2 = new PersonId(2L);
            final Person person2 = new Person();
            person2.setId(personId2.value());

            final PersonId personId3 = new PersonId(3L);
            final Person person3 = new Person();
            person3.setId(personId3.value());

            final PersonId personId4 = new PersonId(4L);
            final Person person4 = new Person();
            person4.setId(personId4.value());

            final Year lastYear = Year.now(clock).minusYears(1);
            final Instant joinedLastYear = lastYear.atDay(42).atStartOfDay().toInstant(UTC);

            when(departmentMembershipService.getActiveMembershipsOfYear(lastYear))
                .thenReturn(Map.of(
                    managerId, List.of(
                        new DepartmentMembership(managerId, 1L, DepartmentMembershipKind.DEPARTMENT_HEAD, joinedLastYear),
                        new DepartmentMembership(managerId, 2L, DepartmentMembershipKind.SECOND_STAGE_AUTHORITY, joinedLastYear)
                    ),
                    personId2, List.of(
                        new DepartmentMembership(personId2, 1L, DepartmentMembershipKind.MEMBER, joinedLastYear)
                    ),
                    personId3, List.of(
                        new DepartmentMembership(personId3, 3L, DepartmentMembershipKind.MEMBER, joinedLastYear)
                    ),
                    personId4, List.of(
                        new DepartmentMembership(personId4, 2L, DepartmentMembershipKind.MEMBER, joinedLastYear)
                    )
                ));

            when(personService.getAllPersonsByIds(Set.of(personId2, personId4)))
                .thenReturn(List.of(person2, person4));

            final List<Person> actual = sut.getManagedMembersOfPerson(manager, lastYear);
            assertThat(actual).containsExactlyInAnyOrder(person2, person4);
        }
    }

    @Test
    void ensureGetManagedMembersOfPersonReturnsPageOfDistinctActivePersonsForDepartmentHeadAndSecondStageAuthority() {

        final PersonId personId = new PersonId(1L);
        final Person person = new Person();
        person.setId(personId.value());
        person.setPermissions(List.of(DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY));

        final PersonId maxId = new PersonId(2L);
        final Person max = new Person();
        max.setId(maxId.value());
        max.setFirstName("Max");
        max.setLastName("Mustermann");

        final PersonId janeId = new PersonId(3L);
        final Person jane = new Person();
        jane.setId(janeId.value());
        jane.setFirstName("Jane");
        jane.setLastName("Doe");

        final PersonId inactivePersonId = new PersonId(4L);
        final Person inactivePerson = new Person();
        inactivePerson.setId(inactivePersonId.value());
        inactivePerson.setFirstName("Inactive");
        inactivePerson.setLastName("Person");
        inactivePerson.setPermissions(List.of(INACTIVE));

        final PersonId otherDepartmentHeadId = new PersonId(5L);
        final Person otherDepartmentHead = new Person();
        otherDepartmentHead.setId(otherDepartmentHeadId.value());
        otherDepartmentHead.setPermissions(List.of(DEPARTMENT_HEAD));

        final PersonId otherSecondStageId = new PersonId(6L);
        final Person otherSecondStage = new Person();
        otherSecondStage.setId(otherSecondStageId.value());
        otherSecondStage.setPermissions(List.of(SECOND_STAGE_AUTHORITY));

        // department 1
        final DepartmentMembership personMembership1 = new DepartmentMembership(personId, 1L, DepartmentMembershipKind.DEPARTMENT_HEAD, Instant.now(clock));
        final DepartmentMembership otherDepartmentHeadMembership1 = new DepartmentMembership(otherDepartmentHeadId, 1L, DepartmentMembershipKind.DEPARTMENT_HEAD, Instant.now(clock));
        final DepartmentMembership maxMembership1 = new DepartmentMembership(maxId, 1L, DepartmentMembershipKind.MEMBER, Instant.now(clock));
        final DepartmentMembership janeMembership1 = new DepartmentMembership(janeId, 1L, DepartmentMembershipKind.MEMBER, Instant.now(clock));
        // department 2
        final DepartmentMembership personMembership2 = new DepartmentMembership(personId, 2L, DepartmentMembershipKind.SECOND_STAGE_AUTHORITY, Instant.now(clock));
        final DepartmentMembership otherSecondStageMembership2 = new DepartmentMembership(otherSecondStageId, 2L, DepartmentMembershipKind.SECOND_STAGE_AUTHORITY, Instant.now(clock));
        final DepartmentMembership janeMembership2 = new DepartmentMembership(janeId, 2L, DepartmentMembershipKind.MEMBER, Instant.now(clock));
        final DepartmentMembership inactiveMembership2 = new DepartmentMembership(inactivePersonId, 2L, DepartmentMembershipKind.MEMBER, Instant.now(clock));

        when(departmentMembershipService.getActiveMemberships(personId))
            .thenReturn(List.of(personMembership1, personMembership2));

        final DepartmentStaff staff1 = new DepartmentStaff(1L, List.of(maxMembership1, janeMembership1), List.of(personMembership1, otherDepartmentHeadMembership1), List.of());
        final DepartmentStaff staff2 = new DepartmentStaff(2L, List.of(janeMembership2, inactiveMembership2), List.of(), List.of(personMembership2, otherSecondStageMembership2));

        when(departmentMembershipService.getDepartmentStaff(Set.of(1L, 2L)))
            .thenReturn(Map.of(1L, staff1, 2L, staff2));

        when(personService.getAllPersonsByIds(Set.of(janeId, maxId, inactivePersonId)))
            .thenReturn(List.of(jane, max, inactivePerson));

        final Page<Person> actual = sut.getManagedMembersOfPerson(person, defaultPersonSearchQuery());
        assertThat(actual.getContent()).containsExactly(jane, max);
    }

    @Test
    void ensureGetManagedMembersOfPersonReturnsPageOfDistinctActivePersonsForDepartmentHead() {

        final PersonId personId = new PersonId(1L);
        final Person person = new Person();
        person.setId(personId.value());
        person.setPermissions(List.of(DEPARTMENT_HEAD));

        final PersonId maxId = new PersonId(2L);
        final Person max = new Person();
        max.setId(maxId.value());
        max.setFirstName("Max");
        max.setLastName("Mustermann");

        final PersonId janeId = new PersonId(3L);
        final Person jane = new Person();
        jane.setId(janeId.value());
        jane.setFirstName("Jane");
        jane.setLastName("Doe");

        final PersonId inactivePersonId = new PersonId(4L);
        final Person inactivePerson = new Person();
        inactivePerson.setId(inactivePersonId.value());
        inactivePerson.setFirstName("Inactive");
        inactivePerson.setLastName("Person");
        inactivePerson.setPermissions(List.of(INACTIVE));

        final PersonId otherDepartmentHeadId = new PersonId(5L);
        final Person otherDepartmentHead = new Person();
        otherDepartmentHead.setId(otherDepartmentHeadId.value());
        otherDepartmentHead.setPermissions(List.of(DEPARTMENT_HEAD));

        final PersonId otherSecondStageId = new PersonId(6L);
        final Person otherSecondStage = new Person();
        otherSecondStage.setId(otherSecondStageId.value());
        otherSecondStage.setPermissions(List.of(SECOND_STAGE_AUTHORITY));

        // department 1
        final DepartmentMembership personMembership1 = new DepartmentMembership(personId, 1L, DepartmentMembershipKind.DEPARTMENT_HEAD, Instant.now(clock));
        final DepartmentMembership otherDepartmentHeadMembership1 = new DepartmentMembership(otherDepartmentHeadId, 1L, DepartmentMembershipKind.DEPARTMENT_HEAD, Instant.now(clock));
        final DepartmentMembership maxMembership1 = new DepartmentMembership(maxId, 1L, DepartmentMembershipKind.MEMBER, Instant.now(clock));
        final DepartmentMembership janeMembership1 = new DepartmentMembership(janeId, 1L, DepartmentMembershipKind.MEMBER, Instant.now(clock));
        // department 2
        final DepartmentMembership personMembership2 = new DepartmentMembership(personId, 2L, DepartmentMembershipKind.DEPARTMENT_HEAD, Instant.now(clock));
        final DepartmentMembership otherSecondStageMembership2 = new DepartmentMembership(otherSecondStageId, 2L, DepartmentMembershipKind.SECOND_STAGE_AUTHORITY, Instant.now(clock));
        final DepartmentMembership janeMembership2 = new DepartmentMembership(janeId, 2L, DepartmentMembershipKind.MEMBER, Instant.now(clock));
        final DepartmentMembership inactiveMembership2 = new DepartmentMembership(inactivePersonId, 2L, DepartmentMembershipKind.MEMBER, Instant.now(clock));

        when(departmentMembershipService.getActiveMemberships(personId))
            .thenReturn(List.of(personMembership1, personMembership2));

        final DepartmentStaff staff1 = new DepartmentStaff(1L, List.of(maxMembership1, janeMembership1), List.of(personMembership1, otherDepartmentHeadMembership1), List.of());
        final DepartmentStaff staff2 = new DepartmentStaff(2L, List.of(janeMembership2, inactiveMembership2), List.of(personMembership2), List.of(otherSecondStageMembership2));

        when(departmentMembershipService.getDepartmentStaff(Set.of(1L, 2L)))
            .thenReturn(Map.of(1L, staff1, 2L, staff2));

        when(personService.getAllPersonsByIds(Set.of(janeId, maxId, inactivePersonId)))
            .thenReturn(List.of(jane, max, inactivePerson));

        final Page<Person> actual = sut.getManagedMembersOfPerson(person, defaultPersonSearchQuery());
        assertThat(actual.getContent()).containsExactly(jane, max);
    }

    @Test
    void ensureGetManagedMembersOfPersonReturnsPageOfDistinctActivePersonsForSecondStageAuthority() {

        final PersonId personId = new PersonId(1L);
        final Person person = new Person();
        person.setId(personId.value());
        person.setPermissions(List.of(SECOND_STAGE_AUTHORITY));

        final PersonId maxId = new PersonId(2L);
        final Person max = new Person();
        max.setId(maxId.value());
        max.setFirstName("Max");
        max.setLastName("Mustermann");

        final PersonId janeId = new PersonId(3L);
        final Person jane = new Person();
        jane.setId(janeId.value());
        jane.setFirstName("Jane");
        jane.setLastName("Doe");

        final PersonId inactivePersonId = new PersonId(4L);
        final Person inactivePerson = new Person();
        inactivePerson.setId(inactivePersonId.value());
        inactivePerson.setFirstName("Inactive");
        inactivePerson.setLastName("Person");
        inactivePerson.setPermissions(List.of(INACTIVE));

        final PersonId otherDepartmentHeadId = new PersonId(5L);
        final Person otherDepartmentHead = new Person();
        otherDepartmentHead.setId(otherDepartmentHeadId.value());
        otherDepartmentHead.setPermissions(List.of(DEPARTMENT_HEAD));

        final PersonId otherSecondStageId = new PersonId(6L);
        final Person otherSecondStage = new Person();
        otherSecondStage.setId(otherSecondStageId.value());
        otherSecondStage.setPermissions(List.of(SECOND_STAGE_AUTHORITY));

        // department 1
        final DepartmentMembership personMembership1 = new DepartmentMembership(personId, 1L, DepartmentMembershipKind.SECOND_STAGE_AUTHORITY, Instant.now(clock));
        final DepartmentMembership otherDepartmentHeadMembership1 = new DepartmentMembership(otherDepartmentHeadId, 1L, DepartmentMembershipKind.DEPARTMENT_HEAD, Instant.now(clock));
        final DepartmentMembership maxMembership1 = new DepartmentMembership(maxId, 1L, DepartmentMembershipKind.MEMBER, Instant.now(clock));
        final DepartmentMembership janeMembership1 = new DepartmentMembership(janeId, 1L, DepartmentMembershipKind.MEMBER, Instant.now(clock));
        // department 2
        final DepartmentMembership personMembership2 = new DepartmentMembership(personId, 2L, DepartmentMembershipKind.SECOND_STAGE_AUTHORITY, Instant.now(clock));
        final DepartmentMembership otherSecondStageMembership2 = new DepartmentMembership(otherSecondStageId, 2L, DepartmentMembershipKind.SECOND_STAGE_AUTHORITY, Instant.now(clock));
        final DepartmentMembership janeMembership2 = new DepartmentMembership(janeId, 2L, DepartmentMembershipKind.MEMBER, Instant.now(clock));
        final DepartmentMembership inactiveMembership2 = new DepartmentMembership(inactivePersonId, 2L, DepartmentMembershipKind.MEMBER, Instant.now(clock));

        when(departmentMembershipService.getActiveMemberships(personId))
            .thenReturn(List.of(personMembership1, personMembership2));

        final DepartmentStaff staff1 = new DepartmentStaff(1L, List.of(maxMembership1, janeMembership1), List.of(otherDepartmentHeadMembership1), List.of(personMembership1));
        final DepartmentStaff staff2 = new DepartmentStaff(2L, List.of(janeMembership2, inactiveMembership2), List.of(), List.of(personMembership2, otherSecondStageMembership2));

        when(departmentMembershipService.getDepartmentStaff(Set.of(1L, 2L)))
            .thenReturn(Map.of(1L, staff1, 2L, staff2));

        when(personService.getAllPersonsByIds(Set.of(janeId, maxId, inactivePersonId)))
            .thenReturn(List.of(jane, max, inactivePerson));

        final Page<Person> actual = sut.getManagedMembersOfPerson(person, defaultPersonSearchQuery());
        assertThat(actual.getContent()).containsExactly(jane, max);
    }

    @Test
    void ensureGetManagedMembersOfPersonReturnsPageOfEmptyList() {

        final Person person = new Person();
        person.setId(1L);
        person.setPermissions(List.of());

        final Page<Person> actual = sut.getManagedMembersOfPerson(person, defaultPersonSearchQuery());

        assertThat(actual.getContent()).isEmpty();
        verifyNoInteractions(departmentRepository);
    }

    @Test
    void ensureGetManagedActiveMembersOfPersonReturnsDistinctActivePersonsForDepartmentHeadAndSecondStageAuthority() {

        final PersonId personId = new PersonId(1L);
        final Person person = new Person();
        person.setId(personId.value());
        person.setPermissions(List.of(DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY));

        final PersonId maxId = new PersonId(2L);
        final Person max = new Person();
        max.setId(maxId.value());
        max.setFirstName("Max");
        max.setLastName("Mustermann");

        final PersonId janeId = new PersonId(3L);
        final Person jane = new Person();
        jane.setId(janeId.value());
        jane.setFirstName("Jane");
        jane.setLastName("Doe");

        final PersonId inactivePersonId = new PersonId(4L);
        final Person inactivePerson = new Person();
        inactivePerson.setId(inactivePersonId.value());
        inactivePerson.setPermissions(List.of(INACTIVE));

        final PersonId otherDepartmentHeadId = new PersonId(5L);
        final Person otherDepartmentHead = new Person();
        otherDepartmentHead.setId(otherDepartmentHeadId.value());
        otherDepartmentHead.setPermissions(List.of(DEPARTMENT_HEAD));

        final PersonId otherSecondStageId = new PersonId(6L);
        final Person otherSecondStage = new Person();
        otherSecondStage.setId(otherSecondStageId.value());
        otherSecondStage.setPermissions(List.of(SECOND_STAGE_AUTHORITY));

        // department 1
        final DepartmentMembership personMembership1 = new DepartmentMembership(personId, 1L, DepartmentMembershipKind.DEPARTMENT_HEAD, Instant.now(clock));
        final DepartmentMembership otherDepartmentHeadMembership1 = new DepartmentMembership(otherDepartmentHeadId, 1L, DepartmentMembershipKind.DEPARTMENT_HEAD, Instant.now(clock));
        final DepartmentMembership maxMembership1 = new DepartmentMembership(maxId, 1L, DepartmentMembershipKind.MEMBER, Instant.now(clock));
        final DepartmentMembership janeMembership1 = new DepartmentMembership(janeId, 1L, DepartmentMembershipKind.MEMBER, Instant.now(clock));
        // department 2
        final DepartmentMembership personMembership2 = new DepartmentMembership(personId, 2L, DepartmentMembershipKind.SECOND_STAGE_AUTHORITY, Instant.now(clock));
        final DepartmentMembership otherSecondStageMembership2 = new DepartmentMembership(otherSecondStageId, 2L, DepartmentMembershipKind.SECOND_STAGE_AUTHORITY, Instant.now(clock));
        final DepartmentMembership janeMembership2 = new DepartmentMembership(janeId, 2L, DepartmentMembershipKind.MEMBER, Instant.now(clock));
        final DepartmentMembership inactiveMembership2 = new DepartmentMembership(inactivePersonId, 2L, DepartmentMembershipKind.MEMBER, Instant.now(clock));

        when(departmentMembershipService.getActiveMemberships(personId))
            .thenReturn(List.of(personMembership1, personMembership2));

        final DepartmentStaff staff1 = new DepartmentStaff(1L, List.of(maxMembership1, janeMembership1), List.of(otherDepartmentHeadMembership1), List.of(personMembership1));
        final DepartmentStaff staff2 = new DepartmentStaff(2L, List.of(janeMembership2, inactiveMembership2), List.of(), List.of(personMembership2, otherSecondStageMembership2));

        when(departmentMembershipService.getDepartmentStaff(Set.of(1L, 2L)))
            .thenReturn(Map.of(1L, staff1, 2L, staff2));

        when(personService.getAllPersonsByIds(Set.of(janeId, maxId, inactivePersonId)))
            .thenReturn(List.of(jane, max, inactivePerson));

        final List<Person> actual = sut.getManagedActiveMembersOfPerson(person);
        assertThat(actual).containsExactlyInAnyOrder(jane, max);
    }

    @Test
    void ensureGetManagedActiveMembersOfPersonReturnsDistinctActivePersonsForDepartmentHead() {

        final PersonId personId = new PersonId(1L);
        final Person person = new Person();
        person.setId(personId.value());
        person.setPermissions(List.of(DEPARTMENT_HEAD));

        final PersonId maxId = new PersonId(2L);
        final Person max = new Person();
        max.setId(maxId.value());
        max.setFirstName("Max");
        max.setLastName("Mustermann");

        final PersonId janeId = new PersonId(3L);
        final Person jane = new Person();
        jane.setId(janeId.value());
        jane.setFirstName("Jane");
        jane.setLastName("Doe");

        final PersonId inactivePersonId = new PersonId(4L);
        final Person inactivePerson = new Person();
        inactivePerson.setId(inactivePersonId.value());
        inactivePerson.setPermissions(List.of(INACTIVE));

        final PersonId otherDepartmentHeadId = new PersonId(5L);
        final Person otherDepartmentHead = new Person();
        otherDepartmentHead.setId(otherDepartmentHeadId.value());
        otherDepartmentHead.setPermissions(List.of(DEPARTMENT_HEAD));

        final PersonId otherSecondStageId = new PersonId(6L);
        final Person otherSecondStage = new Person();
        otherSecondStage.setId(otherSecondStageId.value());
        otherSecondStage.setPermissions(List.of(SECOND_STAGE_AUTHORITY));

        // department 1
        final DepartmentMembership personMembership1 = new DepartmentMembership(personId, 1L, DepartmentMembershipKind.DEPARTMENT_HEAD, Instant.now(clock));
        final DepartmentMembership otherDepartmentHeadMembership1 = new DepartmentMembership(otherDepartmentHeadId, 1L, DepartmentMembershipKind.DEPARTMENT_HEAD, Instant.now(clock));
        final DepartmentMembership maxMembership1 = new DepartmentMembership(maxId, 1L, DepartmentMembershipKind.MEMBER, Instant.now(clock));
        final DepartmentMembership janeMembership1 = new DepartmentMembership(janeId, 1L, DepartmentMembershipKind.MEMBER, Instant.now(clock));
        // department 2
        final DepartmentMembership personMembership2 = new DepartmentMembership(personId, 2L, DepartmentMembershipKind.DEPARTMENT_HEAD, Instant.now(clock));
        final DepartmentMembership otherSecondStageMembership2 = new DepartmentMembership(otherSecondStageId, 2L, DepartmentMembershipKind.SECOND_STAGE_AUTHORITY, Instant.now(clock));
        final DepartmentMembership janeMembership2 = new DepartmentMembership(janeId, 2L, DepartmentMembershipKind.MEMBER, Instant.now(clock));
        final DepartmentMembership inactiveMembership2 = new DepartmentMembership(inactivePersonId, 2L, DepartmentMembershipKind.MEMBER, Instant.now(clock));

        when(departmentMembershipService.getActiveMemberships(personId))
            .thenReturn(List.of(personMembership1, personMembership2));

        final DepartmentStaff staff1 = new DepartmentStaff(1L, List.of(maxMembership1, janeMembership1), List.of(personMembership1, otherDepartmentHeadMembership1), List.of());
        final DepartmentStaff staff2 = new DepartmentStaff(2L, List.of(janeMembership2, inactiveMembership2), List.of(personMembership2), List.of(otherSecondStageMembership2));

        when(departmentMembershipService.getDepartmentStaff(Set.of(1L, 2L)))
            .thenReturn(Map.of(1L, staff1, 2L, staff2));

        when(personService.getAllPersonsByIds(Set.of(janeId, maxId, inactivePersonId)))
            .thenReturn(List.of(jane, max, inactivePerson));

        final List<Person> actual = sut.getManagedActiveMembersOfPerson(person);
        assertThat(actual).containsExactlyInAnyOrder(jane, max);
    }

    @Test
    void ensureGetManagedActiveMembersOfPersonReturnsDistinctActivePersonsForSecondStageAuthority() {

        final PersonId personId = new PersonId(1L);
        final Person person = new Person();
        person.setId(personId.value());
        person.setPermissions(List.of(USER, SECOND_STAGE_AUTHORITY));

        final PersonId maxId = new PersonId(2L);
        final Person max = new Person();
        max.setId(maxId.value());

        final PersonId janeId = new PersonId(3L);
        final Person jane = new Person();
        jane.setId(janeId.value());

        final PersonId inactivePersonId = new PersonId(4L);
        final Person inactivePerson = new Person();
        inactivePerson.setId(inactivePersonId.value());
        inactivePerson.setPermissions(List.of(INACTIVE));

        final PersonId otherDepartmentHeadId = new PersonId(5L);
        final Person otherDepartmentHead = new Person();
        otherDepartmentHead.setId(otherDepartmentHeadId.value());
        otherDepartmentHead.setPermissions(List.of(DEPARTMENT_HEAD));

        final PersonId otherSecondStageId = new PersonId(6L);
        final Person otherSecondStage = new Person();
        otherSecondStage.setId(otherSecondStageId.value());
        otherSecondStage.setPermissions(List.of(SECOND_STAGE_AUTHORITY));

        // department 1
        final DepartmentMembership personMembership1 = new DepartmentMembership(personId, 1L, DepartmentMembershipKind.SECOND_STAGE_AUTHORITY, Instant.now(clock));
        final DepartmentMembership otherDepartmentHeadMembership1 = new DepartmentMembership(otherDepartmentHeadId, 1L, DepartmentMembershipKind.DEPARTMENT_HEAD, Instant.now(clock));
        final DepartmentMembership maxMembership1 = new DepartmentMembership(maxId, 1L, DepartmentMembershipKind.MEMBER, Instant.now(clock));
        final DepartmentMembership janeMembership1 = new DepartmentMembership(janeId, 1L, DepartmentMembershipKind.MEMBER, Instant.now(clock));
        // department 2
        final DepartmentMembership personMembership2 = new DepartmentMembership(personId, 2L, DepartmentMembershipKind.SECOND_STAGE_AUTHORITY, Instant.now(clock));
        final DepartmentMembership otherSecondStageMembership2 = new DepartmentMembership(otherSecondStageId, 2L, DepartmentMembershipKind.SECOND_STAGE_AUTHORITY, Instant.now(clock));
        final DepartmentMembership janeMembership2 = new DepartmentMembership(janeId, 2L, DepartmentMembershipKind.MEMBER, Instant.now(clock));
        final DepartmentMembership inactiveMembership2 = new DepartmentMembership(inactivePersonId, 2L, DepartmentMembershipKind.MEMBER, Instant.now(clock));

        when(departmentMembershipService.getActiveMemberships(personId))
            .thenReturn(List.of(personMembership1, personMembership2));

        final DepartmentStaff staff1 = new DepartmentStaff(1L, List.of(maxMembership1, janeMembership1), List.of(otherDepartmentHeadMembership1), List.of(personMembership1));
        final DepartmentStaff staff2 = new DepartmentStaff(2L, List.of(janeMembership2, inactiveMembership2), List.of(), List.of(personMembership2, otherSecondStageMembership2));

        when(departmentMembershipService.getDepartmentStaff(Set.of(1L, 2L)))
            .thenReturn(Map.of(1L, staff1, 2L, staff2));

        when(personService.getAllPersonsByIds(Set.of(janeId, maxId, inactivePersonId)))
            .thenReturn(List.of(jane, max, inactivePerson));

        final List<Person> actual = sut.getManagedActiveMembersOfPerson(person);
        assertThat(actual).containsExactlyInAnyOrder(jane, max);
    }

    @Test
    void ensureGetManagedActiveMembersOfPersonReturnsEmptyList() {

        final Person person = new Person();
        person.setId(1L);
        person.setPermissions(List.of());

        final List<Person> actual = sut.getManagedActiveMembersOfPerson(person);

        assertThat(actual).isEmpty();
        verifyNoInteractions(departmentRepository);
    }

    @Test
    void ensureGetManagedInactiveMembersOfPersonReturnsDistinctInactivePersonsForDepartmentHeadAndSecondStageAuthority() {

        final PersonId personId = new PersonId(1L);
        final Person person = new Person();
        person.setId(personId.value());
        person.setPermissions(List.of(DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY));

        final PersonId maxId = new PersonId(2L);
        final Person max = new Person();
        max.setId(maxId.value());
        max.setFirstName("Max");
        max.setLastName("Mustermann");

        final PersonId janeId = new PersonId(3L);
        final Person jane = new Person();
        jane.setId(janeId.value());
        jane.setFirstName("Jane");
        jane.setLastName("Doe");

        final PersonId inactivePersonId = new PersonId(4L);
        final Person inactivePerson = new Person();
        inactivePerson.setId(inactivePersonId.value());
        inactivePerson.setPermissions(List.of(INACTIVE));

        final PersonId otherDepartmentHeadId = new PersonId(5L);
        final Person otherDepartmentHead = new Person();
        otherDepartmentHead.setId(otherDepartmentHeadId.value());
        otherDepartmentHead.setPermissions(List.of(DEPARTMENT_HEAD));

        final PersonId otherSecondStageId = new PersonId(6L);
        final Person otherSecondStage = new Person();
        otherSecondStage.setId(otherSecondStageId.value());
        otherSecondStage.setPermissions(List.of(SECOND_STAGE_AUTHORITY));

        // department 1
        final DepartmentMembership personMembership1 = new DepartmentMembership(personId, 1L, DepartmentMembershipKind.DEPARTMENT_HEAD, Instant.now(clock));
        final DepartmentMembership otherDepartmentHeadMembership1 = new DepartmentMembership(otherDepartmentHeadId, 1L, DepartmentMembershipKind.DEPARTMENT_HEAD, Instant.now(clock));
        final DepartmentMembership maxMembership1 = new DepartmentMembership(maxId, 1L, DepartmentMembershipKind.MEMBER, Instant.now(clock));
        final DepartmentMembership janeMembership1 = new DepartmentMembership(janeId, 1L, DepartmentMembershipKind.MEMBER, Instant.now(clock));
        // department 2
        final DepartmentMembership personMembership2 = new DepartmentMembership(personId, 2L, DepartmentMembershipKind.SECOND_STAGE_AUTHORITY, Instant.now(clock));
        final DepartmentMembership otherSecondStageMembership2 = new DepartmentMembership(otherSecondStageId, 2L, DepartmentMembershipKind.SECOND_STAGE_AUTHORITY, Instant.now(clock));
        final DepartmentMembership janeMembership2 = new DepartmentMembership(janeId, 2L, DepartmentMembershipKind.MEMBER, Instant.now(clock));
        final DepartmentMembership inactiveMembership2 = new DepartmentMembership(inactivePersonId, 2L, DepartmentMembershipKind.MEMBER, Instant.now(clock));

        when(departmentMembershipService.getActiveMemberships(personId))
            .thenReturn(List.of(personMembership1, personMembership2));

        final DepartmentStaff staff1 = new DepartmentStaff(1L, List.of(maxMembership1, janeMembership1), List.of(otherDepartmentHeadMembership1), List.of(personMembership1));
        final DepartmentStaff staff2 = new DepartmentStaff(2L, List.of(janeMembership2, inactiveMembership2), List.of(), List.of(personMembership2, otherSecondStageMembership2));

        when(departmentMembershipService.getDepartmentStaff(Set.of(1L, 2L)))
            .thenReturn(Map.of(1L, staff1, 2L, staff2));

        when(personService.getAllPersonsByIds(Set.of(janeId, maxId, inactivePersonId)))
            .thenReturn(List.of(jane, max, inactivePerson));

        final Page<Person> actual = sut.getManagedInactiveMembersOfPerson(person, defaultPersonSearchQuery());
        assertThat(actual.getContent()).containsExactly(inactivePerson);
    }

    @Test
    void ensureGetManagedInactiveMembersOfPersonReturnsDistinctInactivePersonsForDepartmentHead() {

        final PersonId personId = new PersonId(1L);
        final Person person = new Person();
        person.setId(personId.value());
        person.setPermissions(List.of(DEPARTMENT_HEAD));

        final PersonId maxId = new PersonId(2L);
        final Person max = new Person();
        max.setId(maxId.value());
        max.setFirstName("Max");
        max.setLastName("Mustermann");

        final PersonId janeId = new PersonId(3L);
        final Person jane = new Person();
        jane.setId(janeId.value());
        jane.setFirstName("Jane");
        jane.setLastName("Doe");

        final PersonId inactivePersonId = new PersonId(4L);
        final Person inactivePerson = new Person();
        inactivePerson.setId(inactivePersonId.value());
        inactivePerson.setFirstName("Inactive");
        inactivePerson.setLastName("Person");
        inactivePerson.setPermissions(List.of(INACTIVE));

        final PersonId otherDepartmentHeadId = new PersonId(5L);
        final Person otherDepartmentHead = new Person();
        otherDepartmentHead.setId(otherDepartmentHeadId.value());
        otherDepartmentHead.setPermissions(List.of(DEPARTMENT_HEAD));

        final PersonId otherSecondStageId = new PersonId(6L);
        final Person otherSecondStage = new Person();
        otherSecondStage.setId(otherSecondStageId.value());
        otherSecondStage.setPermissions(List.of(SECOND_STAGE_AUTHORITY));

        // department 1
        final DepartmentMembership personMembership1 = new DepartmentMembership(personId, 1L, DepartmentMembershipKind.DEPARTMENT_HEAD, Instant.now(clock));
        final DepartmentMembership otherDepartmentHeadMembership1 = new DepartmentMembership(otherDepartmentHeadId, 1L, DepartmentMembershipKind.DEPARTMENT_HEAD, Instant.now(clock));
        final DepartmentMembership maxMembership1 = new DepartmentMembership(maxId, 1L, DepartmentMembershipKind.MEMBER, Instant.now(clock));
        final DepartmentMembership janeMembership1 = new DepartmentMembership(janeId, 1L, DepartmentMembershipKind.MEMBER, Instant.now(clock));
        // department 2
        final DepartmentMembership personMembership2 = new DepartmentMembership(personId, 2L, DepartmentMembershipKind.DEPARTMENT_HEAD, Instant.now(clock));
        final DepartmentMembership otherSecondStageMembership2 = new DepartmentMembership(otherSecondStageId, 2L, DepartmentMembershipKind.SECOND_STAGE_AUTHORITY, Instant.now(clock));
        final DepartmentMembership janeMembership2 = new DepartmentMembership(janeId, 2L, DepartmentMembershipKind.MEMBER, Instant.now(clock));
        final DepartmentMembership inactiveMembership2 = new DepartmentMembership(inactivePersonId, 2L, DepartmentMembershipKind.MEMBER, Instant.now(clock));

        when(departmentMembershipService.getActiveMemberships(personId))
            .thenReturn(List.of(personMembership1, personMembership2));

        final DepartmentStaff staff1 = new DepartmentStaff(1L, List.of(maxMembership1, janeMembership1), List.of(personMembership1, otherDepartmentHeadMembership1), List.of());
        final DepartmentStaff staff2 = new DepartmentStaff(2L, List.of(janeMembership2, inactiveMembership2), List.of(personMembership2), List.of(otherSecondStageMembership2));

        when(departmentMembershipService.getDepartmentStaff(Set.of(1L, 2L)))
            .thenReturn(Map.of(1L, staff1, 2L, staff2));

        when(personService.getAllPersonsByIds(Set.of(janeId, maxId, inactivePersonId)))
            .thenReturn(List.of(jane, max, inactivePerson));

        final Page<Person> actual = sut.getManagedInactiveMembersOfPerson(person, defaultPersonSearchQuery());
        assertThat(actual.getContent()).containsExactly(inactivePerson);
    }

    @Test
    void ensureGetManagedInactiveMembersOfPersonReturnsDistinctInactivePersonsForSecondStageAuthority() {

        final PersonId personId = new PersonId(1L);
        final Person person = new Person();
        person.setId(personId.value());
        person.setPermissions(List.of(USER, SECOND_STAGE_AUTHORITY));

        final PersonId maxId = new PersonId(2L);
        final Person max = new Person();
        max.setId(maxId.value());

        final PersonId janeId = new PersonId(3L);
        final Person jane = new Person();
        jane.setId(janeId.value());

        final PersonId inactivePersonId = new PersonId(4L);
        final Person inactivePerson = new Person();
        inactivePerson.setId(inactivePersonId.value());
        inactivePerson.setPermissions(List.of(INACTIVE));

        final PersonId otherDepartmentHeadId = new PersonId(5L);
        final Person otherDepartmentHead = new Person();
        otherDepartmentHead.setId(otherDepartmentHeadId.value());
        otherDepartmentHead.setPermissions(List.of(DEPARTMENT_HEAD));

        final PersonId otherSecondStageId = new PersonId(6L);
        final Person otherSecondStage = new Person();
        otherSecondStage.setId(otherSecondStageId.value());
        otherSecondStage.setPermissions(List.of(SECOND_STAGE_AUTHORITY));

        // department 1
        final DepartmentMembership personMembership1 = new DepartmentMembership(personId, 1L, DepartmentMembershipKind.SECOND_STAGE_AUTHORITY, Instant.now(clock));
        final DepartmentMembership otherDepartmentHeadMembership1 = new DepartmentMembership(otherDepartmentHeadId, 1L, DepartmentMembershipKind.DEPARTMENT_HEAD, Instant.now(clock));
        final DepartmentMembership maxMembership1 = new DepartmentMembership(maxId, 1L, DepartmentMembershipKind.MEMBER, Instant.now(clock));
        final DepartmentMembership janeMembership1 = new DepartmentMembership(janeId, 1L, DepartmentMembershipKind.MEMBER, Instant.now(clock));
        // department 2
        final DepartmentMembership personMembership2 = new DepartmentMembership(personId, 2L, DepartmentMembershipKind.SECOND_STAGE_AUTHORITY, Instant.now(clock));
        final DepartmentMembership otherSecondStageMembership2 = new DepartmentMembership(otherSecondStageId, 2L, DepartmentMembershipKind.SECOND_STAGE_AUTHORITY, Instant.now(clock));
        final DepartmentMembership janeMembership2 = new DepartmentMembership(janeId, 2L, DepartmentMembershipKind.MEMBER, Instant.now(clock));
        final DepartmentMembership inactiveMembership2 = new DepartmentMembership(inactivePersonId, 2L, DepartmentMembershipKind.MEMBER, Instant.now(clock));

        when(departmentMembershipService.getActiveMemberships(personId))
            .thenReturn(List.of(personMembership1, personMembership2));

        final DepartmentStaff staff1 = new DepartmentStaff(1L, List.of(maxMembership1, janeMembership1), List.of(otherDepartmentHeadMembership1), List.of(personMembership1));
        final DepartmentStaff staff2 = new DepartmentStaff(2L, List.of(janeMembership2, inactiveMembership2), List.of(), List.of(personMembership2, otherSecondStageMembership2));

        when(departmentMembershipService.getDepartmentStaff(Set.of(1L, 2L)))
            .thenReturn(Map.of(1L, staff1, 2L, staff2));

        when(personService.getAllPersonsByIds(Set.of(janeId, maxId, inactivePersonId)))
            .thenReturn(List.of(jane, max, inactivePerson));

        final Page<Person> actual = sut.getManagedInactiveMembersOfPerson(person, defaultPersonSearchQuery());
        assertThat(actual.getContent()).containsExactly(inactivePerson);
    }

    @Test
    void ensureGetManagedInactiveMembersOfPersonReturnsEmptyList() {

        final PersonId personId = new PersonId(1L);
        final Person person = new Person();
        person.setId(personId.value());
        person.setPermissions(List.of(USER));

        final DepartmentMembership membership = new DepartmentMembership(personId, 1L, DepartmentMembershipKind.MEMBER, Instant.now(clock));

        when(departmentMembershipService.getActiveMemberships(personId))
            .thenReturn(List.of(membership));

        when(departmentMembershipService.getDepartmentStaff(Set.of()))
            .thenReturn(Map.of());

        final Page<Person> actual = sut.getManagedInactiveMembersOfPerson(person, defaultPersonSearchQuery());
        assertThat(actual.getContent()).isEmpty();
    }

    @Test
    void ensureGetManagedActiveMembersOfPersonReturnsPageSecond() {

        final PersonId personId = new PersonId(1L);
        final Person person = new Person();
        person.setId(personId.value());
        person.setPermissions(List.of(USER, DEPARTMENT_HEAD));

        final PersonId maxId = new PersonId(2L);
        final Person max = new Person();
        max.setId(maxId.value());
        max.setFirstName("Max");
        max.setLastName("Mustermann");

        final PersonId janeId = new PersonId(3L);
        final Person jane = new Person();
        jane.setId(janeId.value());
        jane.setFirstName("Jane");
        jane.setLastName("Doe");

        final PersonId juleId = new PersonId(4L);
        final Person jule = new Person();
        jule.setId(juleId.value());
        jule.setFirstName("Jule");
        jule.setLastName("Doe");

        final PersonId inactiveId = new PersonId(5L);
        final Person inactive = new Person();
        inactive.setId(inactiveId.value());
        inactive.setPermissions(List.of(INACTIVE));

        final DepartmentMembership personMembership = new DepartmentMembership(personId, 1L, DepartmentMembershipKind.DEPARTMENT_HEAD, Instant.now(clock));
        final DepartmentMembership maxMembership = new DepartmentMembership(maxId, 1L, DepartmentMembershipKind.MEMBER, Instant.now(clock));
        final DepartmentMembership janeMembership = new DepartmentMembership(janeId, 1L, DepartmentMembershipKind.MEMBER, Instant.now(clock));
        final DepartmentMembership juleMembership = new DepartmentMembership(juleId, 1L, DepartmentMembershipKind.MEMBER, Instant.now(clock));
        final DepartmentMembership inactiveMembership = new DepartmentMembership(inactiveId, 1L, DepartmentMembershipKind.MEMBER, Instant.now(clock));

        when(departmentMembershipService.getActiveMemberships(personId)).thenReturn(List.of(personMembership));

        final DepartmentStaff staff = new DepartmentStaff(1L, List.of(maxMembership, inactiveMembership, janeMembership, juleMembership), List.of(personMembership), List.of());
        when(departmentMembershipService.getDepartmentStaff(Set.of(1L))).thenReturn(Map.of(1L, staff));

        when(personService.getAllPersonsByIds(Set.of(maxId, janeId, juleId, inactiveId)))
            // service returns persons in order of firstName, actually.
            // sut has to sort them by the given Sort, however.
            .thenReturn(List.of(max, jule, inactive, jane));

        final PageRequest pageRequest = PageRequest.of(1, 2, Sort.by("lastName").and(Sort.by("firstName")));
        final PageableSearchQuery pageableSearchQuery = new PageableSearchQuery(pageRequest, "");

        final Page<Person> actual = sut.getManagedMembersOfPerson(person, pageableSearchQuery);
        assertThat(actual.getTotalPages()).isEqualTo(2);
        assertThat(pageRequest.getPageNumber()).isEqualTo(1);
        assertThat(actual.getContent()).containsExactly(max);
    }

    @Test
    void ensureGetManagedInactiveMembersOfPersonReturnsPageSecond() {

        final PersonId personId = new PersonId(1L);
        final Person person = new Person();
        person.setId(personId.value());
        person.setPermissions(List.of(USER, DEPARTMENT_HEAD));

        final PersonId maxId = new PersonId(2L);
        final Person max = new Person();
        max.setId(maxId.value());
        max.setFirstName("Max");
        max.setLastName("Mustermann");
        max.setPermissions(List.of(INACTIVE));

        final PersonId janeId = new PersonId(3L);
        final Person jane = new Person();
        jane.setId(janeId.value());
        jane.setFirstName("Jane");
        jane.setLastName("Doe");
        jane.setPermissions(List.of(INACTIVE));

        final PersonId juleId = new PersonId(4L);
        final Person jule = new Person();
        jule.setId(juleId.value());
        jule.setFirstName("Jule");
        jule.setLastName("Doe");
        jule.setPermissions(List.of(INACTIVE));

        final DepartmentMembership personMembership = new DepartmentMembership(personId, 1L, DepartmentMembershipKind.DEPARTMENT_HEAD, Instant.now(clock));
        final DepartmentMembership maxMembership = new DepartmentMembership(maxId, 1L, DepartmentMembershipKind.MEMBER, Instant.now(clock));
        final DepartmentMembership janeMembership = new DepartmentMembership(janeId, 1L, DepartmentMembershipKind.MEMBER, Instant.now(clock));
        final DepartmentMembership juleMembership = new DepartmentMembership(juleId, 1L, DepartmentMembershipKind.MEMBER, Instant.now(clock));

        when(departmentMembershipService.getActiveMemberships(personId)).thenReturn(List.of(personMembership));

        final DepartmentStaff staff = new DepartmentStaff(1L, List.of(maxMembership, janeMembership, juleMembership), List.of(personMembership), List.of());
        when(departmentMembershipService.getDepartmentStaff(Set.of(1L))).thenReturn(Map.of(1L, staff));

        when(personService.getAllPersonsByIds(Set.of(maxId, janeId, juleId)))
            // service returns persons in order of firstName, actually.
            // sut has to sort them by the given Sort, however.
            .thenReturn(List.of(max, jule, jane));

        final PageRequest pageRequest = PageRequest.of(1, 2, Sort.by("lastName").and(Sort.by("firstName")));
        final PageableSearchQuery pageableSearchQuery = new PageableSearchQuery(pageRequest, "");

        final Page<Person> actual = sut.getManagedInactiveMembersOfPerson(person, pageableSearchQuery);
        assertThat(actual.getTotalPages()).isEqualTo(2);
        assertThat(pageRequest.getPageNumber()).isEqualTo(1);
        assertThat(actual.getContent()).containsExactly(max);
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"BOSS", "OFFICE"})
    void ensureGetManagedMembersOfPersonAndDepartmentForRole(Role role) {

        final PersonId personId = new PersonId(1L);
        final Person person = new Person();
        person.setId(personId.value());
        person.setPermissions(List.of(USER, role));

        final PersonId memberId = new PersonId(2L);
        final Person member = new Person();
        member.setId(memberId.value());
        member.setPermissions(List.of(USER));

        final PersonId inactiveId = new PersonId(3L);
        final Person inactive = new Person();
        inactive.setId(inactiveId.value());
        inactive.setPermissions(List.of(INACTIVE));

        final DepartmentEntity departmentEntity = new DepartmentEntity();
        departmentEntity.setId(1L);

        final DepartmentMembership membership = new DepartmentMembership(memberId, 1L, DepartmentMembershipKind.MEMBER, Instant.now(clock));
        final DepartmentMembership inactiveMembership = new DepartmentMembership(inactiveId, 1L, DepartmentMembershipKind.MEMBER, Instant.now(clock));
        final DepartmentStaff staff = new DepartmentStaff(1L, List.of(membership, inactiveMembership), List.of(), List.of());

        when(departmentMembershipService.getDepartmentStaff(1L)).thenReturn(staff);

        when(personService.getAllPersonsByIds(Set.of(memberId, inactiveId))).thenReturn(List.of(member, inactive));

        final PageRequest pageRequest = PageRequest.of(0, 10);
        final PageableSearchQuery pageableSearchQuery = new PageableSearchQuery(pageRequest, "");

        final Page<Person> actual = sut.getManagedMembersOfPersonAndDepartment(person, 1L, pageableSearchQuery);

        assertThat(actual.getTotalPages()).isEqualTo(1);
        assertThat(actual.getPageable().getPageNumber()).isZero();
        assertThat(actual.getContent()).containsExactly(member);
    }

    @Test
    void ensureGetManagedInactiveMembersOfPersonAndDepartmentForDepartmentHead() {

        final PersonId personId = new PersonId(1L);
        final Person person = new Person();
        person.setId(personId.value());
        person.setPermissions(List.of(USER, DEPARTMENT_HEAD));

        final PersonId memberId = new PersonId(2L);
        final Person member = new Person();
        member.setId(memberId.value());
        member.setPermissions(List.of(USER));

        final PersonId inactiveId = new PersonId(3L);
        final Person inactive = new Person();
        inactive.setId(inactiveId.value());
        inactive.setPermissions(List.of(INACTIVE));

        final DepartmentEntity departmentEntity = new DepartmentEntity();
        departmentEntity.setId(1L);

        final DepartmentMembership departmenHeadMembership = new DepartmentMembership(personId, 1L, DepartmentMembershipKind.DEPARTMENT_HEAD, Instant.now(clock));
        final DepartmentMembership membership = new DepartmentMembership(memberId, 1L, DepartmentMembershipKind.MEMBER, Instant.now(clock));
        final DepartmentMembership inactiveMembership = new DepartmentMembership(inactiveId, 1L, DepartmentMembershipKind.MEMBER, Instant.now(clock));
        final DepartmentStaff staff = new DepartmentStaff(1L, List.of(membership, inactiveMembership), List.of(departmenHeadMembership), List.of());

        when(departmentMembershipService.getDepartmentStaff(1L)).thenReturn(staff);

        when(personService.getAllPersonsByIds(Set.of(memberId, inactiveId))).thenReturn(List.of(member, inactive));

        final PageRequest pageRequest = PageRequest.of(0, 10);
        final PageableSearchQuery pageableSearchQuery = new PageableSearchQuery(pageRequest, "");

        final Page<Person> actual = sut.getManagedInactiveMembersOfPersonAndDepartment(person, 1L, pageableSearchQuery);

        assertThat(actual.getTotalPages()).isEqualTo(1);
        assertThat(actual.getPageable().getPageNumber()).isZero();
        assertThat(actual.getContent()).containsExactly(inactive);
    }

    @Test
    void ensureGetManagedInactiveMembersOfPersonAndDepartmentForSecondStageAuthority() {

        final PersonId personId = new PersonId(1L);
        final Person person = new Person();
        person.setId(personId.value());
        person.setPermissions(List.of(USER, SECOND_STAGE_AUTHORITY));

        final PersonId memberId = new PersonId(2L);
        final Person member = new Person();
        member.setId(memberId.value());
        member.setPermissions(List.of(USER));

        final PersonId inactiveId = new PersonId(3L);
        final Person inactive = new Person();
        inactive.setId(inactiveId.value());
        inactive.setPermissions(List.of(INACTIVE));

        final DepartmentEntity departmentEntity = new DepartmentEntity();
        departmentEntity.setId(1L);

        final DepartmentMembership personMembership = new DepartmentMembership(personId, 1L, DepartmentMembershipKind.SECOND_STAGE_AUTHORITY, Instant.now(clock));
        final DepartmentMembership membership = new DepartmentMembership(memberId, 1L, DepartmentMembershipKind.MEMBER, Instant.now(clock));
        final DepartmentMembership inactiveMembership = new DepartmentMembership(inactiveId, 1L, DepartmentMembershipKind.MEMBER, Instant.now(clock));
        final DepartmentStaff staff = new DepartmentStaff(1L, List.of(membership, inactiveMembership), List.of(), List.of(personMembership));

        when(departmentMembershipService.getDepartmentStaff(1L)).thenReturn(staff);

        when(personService.getAllPersonsByIds(Set.of(memberId, inactiveId))).thenReturn(List.of(member, inactive));

        final PageRequest pageRequest = PageRequest.of(0, 10);
        final PageableSearchQuery pageableSearchQuery = new PageableSearchQuery(pageRequest, "");

        final Page<Person> actual = sut.getManagedInactiveMembersOfPersonAndDepartment(person, 1L, pageableSearchQuery);

        assertThat(actual.getTotalPages()).isEqualTo(1);
        assertThat(actual.getPageable().getPageNumber()).isZero();
        assertThat(actual.getContent()).containsExactly(inactive);
    }

    @Test
    void ensureGetManagedInactiveMembersOfPersonAndDepartmentForMember() {

        final PersonId personId = new PersonId(1L);
        final Person person = new Person();
        person.setId(personId.value());
        person.setPermissions(List.of(USER, SECOND_STAGE_AUTHORITY));

        final PersonId memberId = new PersonId(2L);
        final Person member = new Person();
        member.setId(memberId.value());
        member.setPermissions(List.of(USER));

        final PersonId inactiveId = new PersonId(3L);
        final Person inactive = new Person();
        inactive.setId(inactiveId.value());
        inactive.setPermissions(List.of(INACTIVE));

        final DepartmentEntity departmentEntity = new DepartmentEntity();
        departmentEntity.setId(1L);

        final DepartmentMembership personMembership = new DepartmentMembership(personId, 1L, DepartmentMembershipKind.SECOND_STAGE_AUTHORITY, Instant.now(clock));
        final DepartmentMembership membership = new DepartmentMembership(memberId, 1L, DepartmentMembershipKind.MEMBER, Instant.now(clock));
        final DepartmentMembership inactiveMembership = new DepartmentMembership(inactiveId, 1L, DepartmentMembershipKind.MEMBER, Instant.now(clock));
        final DepartmentStaff staff = new DepartmentStaff(1L, List.of(membership, inactiveMembership), List.of(), List.of(personMembership));

        when(departmentMembershipService.getDepartmentStaff(1L)).thenReturn(staff);

        final PageRequest pageRequest = PageRequest.of(0, 10);
        final PageableSearchQuery pageableSearchQuery = new PageableSearchQuery(pageRequest, "");

        final Page<Person> actual = sut.getManagedInactiveMembersOfPersonAndDepartment(member, 1L, pageableSearchQuery);
        assertThat(actual).isEqualTo(Page.empty());

        verify(personService, times(0)).getAllPersonsByIds(anySet());
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"BOSS", "OFFICE"})
    void ensureGetManagedMembersOfPersonAndDepartmentReturnsPageSecond(Role role) {

        final PersonId personId = new PersonId(1L);
        final Person person = new Person();
        person.setId(personId.value());
        person.setPermissions(List.of(USER, role));

        final PersonId maxId = new PersonId(2L);
        final Person max = new Person();
        max.setId(maxId.value());
        max.setFirstName("Max");
        max.setLastName("Mustermann");

        final PersonId janeId = new PersonId(3L);
        final Person jane = new Person();
        jane.setId(janeId.value());
        jane.setFirstName("Jane");
        jane.setLastName("Doe");

        final PersonId juleId = new PersonId(4L);
        final Person jule = new Person();
        jule.setId(juleId.value());
        jule.setFirstName("Jule");
        jule.setLastName("Doe");

        final PersonId inactiveId = new PersonId(5L);
        final Person inactive = new Person();
        inactive.setId(inactiveId.value());
        inactive.setPermissions(List.of(INACTIVE));

        final DepartmentMembership maxMembership = new DepartmentMembership(maxId, 1L, DepartmentMembershipKind.MEMBER, Instant.now(clock));
        final DepartmentMembership janeMembership = new DepartmentMembership(janeId, 1L, DepartmentMembershipKind.MEMBER, Instant.now(clock));
        final DepartmentMembership juleMembership = new DepartmentMembership(juleId, 1L, DepartmentMembershipKind.MEMBER, Instant.now(clock));
        final DepartmentMembership inactiveMembership = new DepartmentMembership(inactiveId, 1L, DepartmentMembershipKind.MEMBER, Instant.now(clock));
        final DepartmentStaff staff = new DepartmentStaff(1L, List.of(maxMembership, inactiveMembership, janeMembership, juleMembership), List.of(), List.of());

        when(departmentMembershipService.getDepartmentStaff(1L)).thenReturn(staff);

        when(personService.getAllPersonsByIds(Set.of(maxId, janeId, juleId, inactiveId)))
            // service returns persons in order of firstName, actually.
            // sut has to sort them by the given Sort, however.
            .thenReturn(List.of(max, jule, inactive, jane));

        final PageRequest pageRequest = PageRequest.of(1, 2, Sort.by("lastName").and(Sort.by("firstName")));
        final PageableSearchQuery pageableSearchQuery = new PageableSearchQuery(pageRequest, "");

        final Page<Person> actual = sut.getManagedMembersOfPersonAndDepartment(person, 1L, pageableSearchQuery);

        assertThat(actual.getTotalPages()).isEqualTo(2);
        assertThat(actual.getPageable().getPageNumber()).isEqualTo(1);
        assertThat(actual.getContent()).containsExactly(max);
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"DEPARTMENT_HEAD", "SECOND_STAGE_AUTHORITY"})
    void ensureGetManagedMembersOfPersonAndDepartmentReturnsEmptyPageWhenIsNotResponsible(Role role) {

        final PersonId personId = new PersonId(1L);
        final Person person = new Person();
        person.setId(personId.value());
        person.setPermissions(List.of(USER, role));

        final PersonId maxId = new PersonId(2L);
        final Person max = new Person();
        max.setId(maxId.value());
        max.setFirstName("Max");
        max.setLastName("Mustermann");

        final PersonId inactiveId = new PersonId(5L);
        final Person inactive = new Person();
        inactive.setId(inactiveId.value());
        inactive.setPermissions(List.of(INACTIVE));

        final DepartmentMembership personMembership = new DepartmentMembership(personId, 1L, DepartmentMembershipKind.MEMBER, Instant.now(clock));
        final DepartmentMembership maxMembership = new DepartmentMembership(maxId, 1L, DepartmentMembershipKind.MEMBER, Instant.now(clock));
        final DepartmentMembership inactiveMembership = new DepartmentMembership(inactiveId, 1L, DepartmentMembershipKind.MEMBER, Instant.now(clock));
        final DepartmentStaff staff = new DepartmentStaff(1L, List.of(personMembership, maxMembership, inactiveMembership), List.of(), List.of());

        when(departmentMembershipService.getDepartmentStaff(1L)).thenReturn(staff);

        final PageRequest pageRequest = PageRequest.of(1, 10);
        final PageableSearchQuery pageableSearchQuery = new PageableSearchQuery(pageRequest, "");

        final Page<Person> actual = sut.getManagedMembersOfPersonAndDepartment(person, 1L, pageableSearchQuery);

        // membership is missing (neither department head nor second stage authority)
        assertThat(actual).isEqualTo(Page.empty());
        verifyNoInteractions(personService);
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"BOSS", "OFFICE"})
    void ensureGetManagedInactiveMembersOfPersonAndDepartmentForRole(Role role) {

        final PersonId personId = new PersonId(1L);
        final Person person = new Person();
        person.setId(personId.value());
        person.setPermissions(List.of(USER, role));

        final PersonId maxId = new PersonId(2L);
        final Person max = new Person();
        max.setId(maxId.value());
        max.setFirstName("Max");
        max.setLastName("Mustermann");

        final PersonId inactiveId = new PersonId(5L);
        final Person inactive = new Person();
        inactive.setId(inactiveId.value());
        inactive.setPermissions(List.of(INACTIVE));

        final DepartmentMembership maxMembership = new DepartmentMembership(maxId, 1L, DepartmentMembershipKind.MEMBER, Instant.now(clock));
        final DepartmentMembership inactiveMembership = new DepartmentMembership(inactiveId, 1L, DepartmentMembershipKind.MEMBER, Instant.now(clock));
        final DepartmentStaff staff = new DepartmentStaff(1L, List.of(maxMembership, inactiveMembership), List.of(), List.of());

        when(departmentMembershipService.getDepartmentStaff(1L)).thenReturn(staff);

        when(personService.getAllPersonsByIds(Set.of(maxId, inactiveId)))
            .thenReturn(List.of(max, inactive));

        final PageRequest pageRequest = PageRequest.of(0, 10);
        final PageableSearchQuery pageableSearchQuery = new PageableSearchQuery(pageRequest, "");

        final Page<Person> actual = sut.getManagedInactiveMembersOfPersonAndDepartment(person, 1L, pageableSearchQuery);

        assertThat(actual.getTotalPages()).isEqualTo(1);
        assertThat(actual.getPageable().getPageNumber()).isZero();
        assertThat(actual.getContent()).containsExactly(inactive);
    }

    @Test
    void ensureGetManagedMembersOfPersonAndDepartmentForDepartmentHead() {

        final PersonId personId = new PersonId(1L);
        final Person person = new Person();
        person.setId(personId.value());
        person.setPermissions(List.of(USER, DEPARTMENT_HEAD));

        final PersonId maxId = new PersonId(2L);
        final Person max = new Person();
        max.setId(maxId.value());
        max.setFirstName("Max");
        max.setLastName("Mustermann");

        final PersonId inactiveId = new PersonId(3L);
        final Person inactive = new Person();
        inactive.setId(inactiveId.value());
        inactive.setPermissions(List.of(INACTIVE));

        final DepartmentMembership personMembership = new DepartmentMembership(personId, 1L, DepartmentMembershipKind.DEPARTMENT_HEAD, Instant.now(clock));
        final DepartmentMembership maxMembership = new DepartmentMembership(maxId, 1L, DepartmentMembershipKind.MEMBER, Instant.now(clock));
        final DepartmentMembership inactiveMembership = new DepartmentMembership(inactiveId, 1L, DepartmentMembershipKind.MEMBER, Instant.now(clock));
        final DepartmentStaff staff = new DepartmentStaff(1L, List.of(maxMembership, inactiveMembership), List.of(personMembership), List.of());

        when(departmentMembershipService.getDepartmentStaff(1L)).thenReturn(staff);

        when(personService.getAllPersonsByIds(Set.of(maxId, inactiveId)))
            .thenReturn(List.of(max, inactive));

        final PageRequest pageRequest = PageRequest.of(0, 10);
        final PageableSearchQuery pageableSearchQuery = new PageableSearchQuery(pageRequest, "");

        final Page<Person> actual = sut.getManagedMembersOfPersonAndDepartment(person, 1L, pageableSearchQuery);

        assertThat(actual.getTotalPages()).isEqualTo(1);
        assertThat(actual.getPageable().getPageNumber()).isZero();
        assertThat(actual.getContent()).containsExactly(max);
    }

    @Test
    void ensureGetManagedMembersOfPersonAndDepartmentForSecondStageAuthority() {

        final PersonId personId = new PersonId(1L);
        final Person person = new Person();
        person.setId(personId.value());
        person.setPermissions(List.of(USER, SECOND_STAGE_AUTHORITY));

        final PersonId maxId = new PersonId(2L);
        final Person max = new Person();
        max.setId(maxId.value());
        max.setFirstName("Max");
        max.setLastName("Mustermann");

        final PersonId inactiveId = new PersonId(3L);
        final Person inactive = new Person();
        inactive.setId(inactiveId.value());
        inactive.setPermissions(List.of(INACTIVE));

        final DepartmentMembership personMembership = new DepartmentMembership(personId, 1L, DepartmentMembershipKind.SECOND_STAGE_AUTHORITY, Instant.now(clock));
        final DepartmentMembership maxMembership = new DepartmentMembership(maxId, 1L, DepartmentMembershipKind.MEMBER, Instant.now(clock));
        final DepartmentMembership inactiveMembership = new DepartmentMembership(inactiveId, 1L, DepartmentMembershipKind.MEMBER, Instant.now(clock));
        final DepartmentStaff staff = new DepartmentStaff(1L, List.of(maxMembership, inactiveMembership), List.of(), List.of(personMembership));

        when(departmentMembershipService.getDepartmentStaff(1L)).thenReturn(staff);

        when(personService.getAllPersonsByIds(Set.of(maxId, inactiveId)))
            .thenReturn(List.of(max, inactive));

        final PageRequest pageRequest = PageRequest.of(0, 10);
        final PageableSearchQuery pageableSearchQuery = new PageableSearchQuery(pageRequest, "");

        final Page<Person> actual = sut.getManagedMembersOfPersonAndDepartment(person, 1L, pageableSearchQuery);

        assertThat(actual.getTotalPages()).isEqualTo(1);
        assertThat(actual.getPageable().getPageNumber()).isZero();
        assertThat(actual.getContent()).containsExactly(max);
    }

    @Test
    void ensureGetManagedMembersOfPersonAndDepartmentForMember() {

        final PersonId personId = new PersonId(1L);
        final Person person = new Person();
        person.setId(personId.value());
        person.setPermissions(List.of(USER));

        final PersonId maxId = new PersonId(2L);
        final Person max = new Person();
        max.setId(maxId.value());
        max.setFirstName("Max");
        max.setLastName("Mustermann");

        final DepartmentMembership personMembership = new DepartmentMembership(personId, 1L, DepartmentMembershipKind.MEMBER, Instant.now(clock));
        final DepartmentMembership maxMembership = new DepartmentMembership(maxId, 1L, DepartmentMembershipKind.MEMBER, Instant.now(clock));
        final DepartmentStaff staff = new DepartmentStaff(1L, List.of(maxMembership, personMembership), List.of(), List.of());

        when(departmentMembershipService.getDepartmentStaff(1L)).thenReturn(staff);

        final PageRequest pageRequest = PageRequest.of(0, 10);
        final PageableSearchQuery pageableSearchQuery = new PageableSearchQuery(pageRequest, "");

        final Page<Person> actual = sut.getManagedMembersOfPersonAndDepartment(person, 1L, pageableSearchQuery);

        assertThat(actual).isEqualTo(Page.empty());
        verifyNoInteractions(personService);
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"BOSS", "OFFICE"})
    void ensureGetManagedInactiveMembersOfPersonAndDepartmentReturnsPageSecond(Role role) {

        final PersonId personId = new PersonId(1L);
        final Person person = new Person();
        person.setId(personId.value());
        person.setPermissions(List.of(USER, role));

        final PersonId maxId = new PersonId(2L);
        final Person max = new Person();
        max.setId(maxId.value());
        max.setFirstName("Max");
        max.setLastName("Mustermann");
        max.setPermissions(List.of(USER, INACTIVE));

        final PersonId janeId = new PersonId(3L);
        final Person jane = new Person();
        jane.setId(janeId.value());
        jane.setFirstName("Jane");
        jane.setLastName("Doe");
        jane.setPermissions(List.of(USER, INACTIVE));

        final PersonId juleId = new PersonId(4L);
        final Person jule = new Person();
        jule.setId(juleId.value());
        jule.setFirstName("Jule");
        jule.setLastName("Doe");
        jule.setPermissions(List.of(USER, INACTIVE));

        final DepartmentMembership maxMembership = new DepartmentMembership(maxId, 1L, DepartmentMembershipKind.MEMBER, Instant.now(clock));
        final DepartmentMembership janeMembership = new DepartmentMembership(janeId, 1L, DepartmentMembershipKind.MEMBER, Instant.now(clock));
        final DepartmentMembership juleMembership = new DepartmentMembership(juleId, 1L, DepartmentMembershipKind.MEMBER, Instant.now(clock));
        final DepartmentStaff staff = new DepartmentStaff(1L, List.of(maxMembership, janeMembership, juleMembership), List.of(), List.of());

        when(departmentMembershipService.getDepartmentStaff(1L)).thenReturn(staff);

        when(personService.getAllPersonsByIds(Set.of(maxId, janeId, juleId)))
            // service returns persons in order of firstName, actually.
            // sut has to sort them by the given Sort, however.
            .thenReturn(List.of(max, jule, jane));

        final PageRequest pageRequest = PageRequest.of(1, 2, Sort.by("lastName").and(Sort.by("firstName")));
        final PageableSearchQuery pageableSearchQuery = new PageableSearchQuery(pageRequest, "");

        final Page<Person> actual = sut.getManagedInactiveMembersOfPersonAndDepartment(person, 1L, pageableSearchQuery);

        assertThat(actual.getTotalPages()).isEqualTo(2);
        assertThat(actual.getPageable().getPageNumber()).isEqualTo(1);
        assertThat(actual.getContent()).containsExactly(max);
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"DEPARTMENT_HEAD", "SECOND_STAGE_AUTHORITY"})
    void ensureGetManagedInactiveMembersOfPersonAndDepartmentReturnsEmptyPageWhenDepartmentHeadIsNotResponsible(Role role) {

        final PersonId personId = new PersonId(1L);
        final Person person = new Person();
        person.setId(personId.value());
        person.setPermissions(List.of(USER, role));

        final PersonId maxId = new PersonId(2L);
        final Person max = new Person();
        max.setId(maxId.value());
        max.setFirstName("Max");
        max.setLastName("Mustermann");

        final PersonId inactiveId = new PersonId(5L);
        final Person inactive = new Person();
        inactive.setId(inactiveId.value());
        inactive.setPermissions(List.of(INACTIVE));

        final DepartmentMembership personMembership = new DepartmentMembership(personId, 1L, DepartmentMembershipKind.MEMBER, Instant.now(clock));
        final DepartmentMembership maxMembership = new DepartmentMembership(maxId, 1L, DepartmentMembershipKind.MEMBER, Instant.now(clock));
        final DepartmentMembership inactiveMembership = new DepartmentMembership(inactiveId, 1L, DepartmentMembershipKind.MEMBER, Instant.now(clock));
        final DepartmentStaff staff = new DepartmentStaff(1L, List.of(personMembership, maxMembership, inactiveMembership), List.of(), List.of());

        when(departmentMembershipService.getDepartmentStaff(1L)).thenReturn(staff);

        final PageRequest pageRequest = PageRequest.of(1, 10);
        final PageableSearchQuery pageableSearchQuery = new PageableSearchQuery(pageRequest, "");

        final Page<Person> actual = sut.getManagedInactiveMembersOfPersonAndDepartment(person, 1L, pageableSearchQuery);

        // membership is missing (neither department head nor second stage authority)
        assertThat(actual).isEqualTo(Page.empty());
        verifyNoInteractions(personService);
    }

    @Test
    void ensureGetManagedInactiveMembersOfPersonAndDepartmentReturnsEmptyPageWhenGivenUserIsNotAMember() {

        final PersonId personId = new PersonId(1L);
        final Person person = new Person();
        person.setId(personId.value());
        person.setPermissions(List.of(USER));

        final PersonId maxId = new PersonId(2L);
        final Person max = new Person();
        max.setId(maxId.value());
        max.setFirstName("Max");
        max.setLastName("Mustermann");

        final PersonId inactiveId = new PersonId(5L);
        final Person inactive = new Person();
        inactive.setId(inactiveId.value());
        inactive.setPermissions(List.of(INACTIVE));

        final DepartmentMembership personMembership = new DepartmentMembership(personId, 1L, DepartmentMembershipKind.MEMBER, Instant.now(clock));
        final DepartmentMembership maxMembership = new DepartmentMembership(maxId, 1L, DepartmentMembershipKind.MEMBER, Instant.now(clock));
        final DepartmentMembership inactiveMembership = new DepartmentMembership(inactiveId, 1L, DepartmentMembershipKind.MEMBER, Instant.now(clock));
        final DepartmentStaff staff = new DepartmentStaff(1L, List.of(personMembership, maxMembership, inactiveMembership), List.of(), List.of());

        when(departmentMembershipService.getDepartmentStaff(1L)).thenReturn(staff);

        final PageRequest pageRequest = PageRequest.of(1, 10);
        final PageableSearchQuery pageableSearchQuery = new PageableSearchQuery(pageRequest, "");

        final Page<Person> actual = sut.getManagedInactiveMembersOfPersonAndDepartment(person, 1L, pageableSearchQuery);

        assertThat(actual).isEqualTo(Page.empty());
        verifyNoInteractions(personService);
    }

    @Test
    void ensureNewDepartmentCreation() {
        final Department department = new Department();
        department.setName("department");

        final DepartmentEntity savedDepartmentEntity = new DepartmentEntity();
        savedDepartmentEntity.setId(42L);
        savedDepartmentEntity.setName("department");

        when(departmentRepository.save(any())).thenReturn(savedDepartmentEntity);

        when(departmentMembershipService.createInitialMemberships(42L, List.of(), List.of(), List.of()))
            .thenReturn(new DepartmentStaff(42L, List.of(), List.of(), List.of()));

        final Department createdDepartment = sut.create(department);

        assertThat(createdDepartment).isNotSameAs(department);
        assertThat(createdDepartment.getId()).isEqualTo(42);
        assertThat(createdDepartment.getName()).isEqualTo("department");
        assertThat(createdDepartment.getLastModification()).isEqualTo(LocalDate.now(clock));
    }

    @Test
    void ensureCreatedDateIsSetForNewDepartment() {

        final Department department = new Department();
        department.setName("department");

        final DepartmentEntity entity = new DepartmentEntity();
        entity.setId(42L);

        when(departmentRepository.save(any())).thenReturn(entity);

        when(departmentMembershipService.createInitialMemberships(42L, List.of(), List.of(), List.of()))
            .thenReturn(new DepartmentStaff(42L, List.of(), List.of(), List.of()));

        sut.create(department);

        final ArgumentCaptor<DepartmentEntity> departmentEntityArgumentCaptor = ArgumentCaptor.forClass(DepartmentEntity.class);
        verify(departmentRepository).save(departmentEntityArgumentCaptor.capture());

        final DepartmentEntity savedDepartmentEntity = departmentEntityArgumentCaptor.getValue();
        assertThat(savedDepartmentEntity.getCreatedAt()).isEqualTo(LocalDate.now(clock));
    }

    @Test
    void ensureCallDepartmentRepositoryFindById() {

        sut.getDepartmentById(42L);
        verify(departmentRepository).findById(42L);
    }

    @Test
    void ensureUpdateDepartmentFailsWhenDepartmentDoesNotExistYet() {
        final Department department = new Department();
        department.setId(42L);
        department.setName("department");

        when(departmentRepository.findById(42L)).thenReturn(Optional.empty());

        assertThatIllegalStateException()
            .isThrownBy(() -> sut.update(department));
    }

    @Test
    void ensureUpdateCallDepartmentDAOUpdate() {

        final Department department = new Department();
        department.setId(42L);
        department.setName("department");

        final DepartmentEntity oldEntity = new DepartmentEntity();
        oldEntity.setId(42L);
        when(departmentRepository.findById(42L)).thenReturn(Optional.of(oldEntity));

        final DepartmentEntity updatedDepartmentEntity = new DepartmentEntity();
        updatedDepartmentEntity.setId(42L);
        updatedDepartmentEntity.setName("department");
        when(departmentRepository.save(any())).thenReturn(updatedDepartmentEntity);

        final DepartmentStaff staff = new DepartmentStaff(42L, List.of(), List.of(), List.of());
        when(departmentMembershipService.getDepartmentStaff(42L))
            .thenReturn(staff);
        when(departmentMembershipService.updateDepartmentMemberships(any(DepartmentStaff.class), any(DepartmentStaff.class)))
            .thenReturn(staff);

        final Department updatedDepartment = sut.update(department);

        assertThat(updatedDepartment).isNotSameAs(department);
        assertThat(updatedDepartment.getName()).isEqualTo("department");

        final ArgumentCaptor<DepartmentEntity> departmentEntityArgumentCaptor = ArgumentCaptor.forClass(DepartmentEntity.class);
        verify(departmentRepository).save(departmentEntityArgumentCaptor.capture());

        final DepartmentEntity departmentEntityToUpdate = departmentEntityArgumentCaptor.getValue();
        assertThat(departmentEntityToUpdate.getId()).isEqualTo(42);
        assertThat(departmentEntityToUpdate.getName()).isEqualTo("department");
    }

    @Test
    void ensureUpdateDoesNotChangeTheCreatedAtDate() {

        final Department department = new Department();
        department.setId(1L);
        department.setName("department");

        final DepartmentEntity departmentEntity = new DepartmentEntity();
        departmentEntity.setId(1L);
        departmentEntity.setCreatedAt(LocalDate.of(2020, DECEMBER, 4));
        departmentEntity.setLastModification(LocalDate.of(2020, DECEMBER, 4));

        when(departmentRepository.findById(1L)).thenReturn(Optional.of(departmentEntity));
        when(departmentRepository.save(any())).thenReturn(departmentEntity);

        final DepartmentStaff staff = new DepartmentStaff(1L, List.of(), List.of(), List.of());
        when(departmentMembershipService.getDepartmentStaff(1L))
            .thenReturn(staff);
        when(departmentMembershipService.updateDepartmentMemberships(any(DepartmentStaff.class), any(DepartmentStaff.class)))
            .thenReturn(staff);

        sut.update(department);

        final ArgumentCaptor<DepartmentEntity> departmentEntityArgumentCaptor = ArgumentCaptor.forClass(DepartmentEntity.class);
        verify(departmentRepository).save(departmentEntityArgumentCaptor.capture());

        final DepartmentEntity savedDepartmentEntity = departmentEntityArgumentCaptor.getValue();
        assertThat(savedDepartmentEntity.getCreatedAt()).isEqualTo(LocalDate.of(2020, DECEMBER, 4));
        assertThat(savedDepartmentEntity.getLastModification()).isEqualTo(LocalDate.now(clock));
    }

    @Test
    void ensureRemovingMembersInDepartmentAlsoSentDepartmentLeftEvent() {

        final PersonId personId = new PersonId(1L);
        final Person person = new Person();
        person.setId(personId.value());

        final PersonId personThatWillLeftId = new PersonId(2L);
        final Person personThatWillLeft = new Person();
        personThatWillLeft.setId(personThatWillLeftId.value());

        final Department department = new Department();
        department.setId(42L);
        department.setName("department");
        department.setMembers(List.of(person));

        final DepartmentEntity departmentEntity = new DepartmentEntity();
        departmentEntity.setId(42L);

        when(departmentRepository.findById(42L)).thenReturn(Optional.of(departmentEntity));

        final DepartmentMembership personMembership = new DepartmentMembership(personId, 42L, DepartmentMembershipKind.MEMBER, Instant.now(clock));
        final DepartmentMembership personLeftMembership = new DepartmentMembership(personThatWillLeftId, 42L, DepartmentMembershipKind.MEMBER, Instant.now(clock));
        final DepartmentStaff currentStaff = new DepartmentStaff(42L, List.of(personMembership, personLeftMembership), List.of(), List.of());
        when(departmentMembershipService.getDepartmentStaff(42L)).thenReturn(currentStaff);

        when(personService.getAllPersonsByIds(Set.of(personId, personThatWillLeftId))).thenReturn(List.of(person, personThatWillLeft));

        final DepartmentStaff updatedStaff = new DepartmentStaff(42L, List.of(personMembership), List.of(), List.of());

        when(departmentMembershipService.updateDepartmentMemberships(any(DepartmentStaff.class), any(DepartmentStaff.class)))
            .thenReturn(updatedStaff);

        when(departmentRepository.save(any(DepartmentEntity.class))).thenReturn(departmentEntity);

        final Department updatedDepartment = sut.update(department);
        assertThat(updatedDepartment.getMembers()).containsExactly(person);

        final ArgumentCaptor<PersonLeftDepartmentEvent> captor = ArgumentCaptor.forClass(PersonLeftDepartmentEvent.class);
        verify(applicationEventPublisher).publishEvent(captor.capture());

        final PersonLeftDepartmentEvent departmentLeftEvent = captor.getValue();
        assertThat(departmentLeftEvent.getDepartmentId()).isEqualTo(42);
        assertThat(departmentLeftEvent.getPersonId()).isEqualTo(1);
    }

    @Test
    void ensureGetAllDepartmentSorted() {

        final DepartmentEntity departmentEntity1 = new DepartmentEntity();
        departmentEntity1.setId(1L);
        departmentEntity1.setName("Department B");

        final DepartmentEntity departmentEntity2 = new DepartmentEntity();
        departmentEntity2.setId(2L);
        departmentEntity2.setName("Department A");

        final Department department1 = new Department();
        department1.setId(1L);

        final Department department2 = new Department();
        department2.setId(2L);

        when(departmentRepository.findAll()).thenReturn(List.of(departmentEntity1, departmentEntity2));

        when(departmentMembershipService.getDepartmentStaff(Set.of(1L, 2L)))
            .thenReturn(Map.of(
                1L, new DepartmentStaff(1L, List.of(), List.of(), List.of()),
                2L, new DepartmentStaff(2L, List.of(), List.of(), List.of())
            ));

        final List<Department> actual = sut.getAllDepartments();
        assertThat(actual).containsExactly(department2, department1);
    }

    @Test
    void ensureGetManagedDepartmentsOfDepartmentHeadDepartmentSorted() {

        final PersonId personId = new PersonId(1L);
        final Person person = new Person();
        person.setId(personId.value());
        person.setPermissions(List.of(USER, DEPARTMENT_HEAD));

        final PersonId memberId = new PersonId(2L);
        final Person member = new Person();
        member.setId(memberId.value());
        person.setPermissions(List.of(USER));

        final DepartmentEntity departmentEntity1 = new DepartmentEntity();
        departmentEntity1.setId(1L);
        departmentEntity1.setName("Department A");
        final DepartmentEntity departmentEntity3 = new DepartmentEntity();
        departmentEntity3.setId(3L);
        departmentEntity3.setName("Department B");

        final DepartmentMembership departmentHeadMembership1 = new DepartmentMembership(personId, 1L, DepartmentMembershipKind.DEPARTMENT_HEAD, Instant.now(clock));
        final DepartmentMembership membership1 = new DepartmentMembership(memberId, 1L, DepartmentMembershipKind.MEMBER, Instant.now(clock));
        final DepartmentMembership departmentHeadMembership3 = new DepartmentMembership(personId, 3L, DepartmentMembershipKind.DEPARTMENT_HEAD, Instant.now(clock));

        when(departmentMembershipService.getActiveMemberships(personId))
            .thenReturn(List.of(departmentHeadMembership1, departmentHeadMembership3));

        when(departmentRepository.findAllById(Set.of(1L, 3L)))
            .thenReturn(List.of(departmentEntity1, departmentEntity3));

        final DepartmentStaff staff1 = new DepartmentStaff(1L, List.of(membership1), List.of(departmentHeadMembership1), List.of());
        final DepartmentStaff staff3 = new DepartmentStaff(3L, List.of(), List.of(departmentHeadMembership3), List.of());

        when(departmentMembershipService.getDepartmentStaff(Set.of(1L, 3L)))
            .thenReturn(Map.of(1L, staff1, 3L, staff3));

        when(personService.getAllPersonsByIds(Set.of(personId, memberId))).thenReturn(List.of(person, member));

        final List<Department> actual = sut.getManagedDepartmentsOfDepartmentHead(person);
        assertThat(actual).satisfiesExactly(
            department -> {
                assertThat(department.getId()).isEqualTo(1L);
                assertThat(department.getName()).isEqualTo("Department A");
                assertThat(department.getMembers()).containsExactly(member);
                assertThat(department.getDepartmentHeads()).containsExactly(person);
                assertThat(department.getSecondStageAuthorities()).isEmpty();
            },
            department -> {
                assertThat(department.getId()).isEqualTo(3L);
                assertThat(department.getName()).isEqualTo("Department B");
                assertThat(department.getMembers()).isEmpty();
                assertThat(department.getDepartmentHeads()).containsExactly(person);
                assertThat(department.getSecondStageAuthorities()).isEmpty();
            }
        );
    }

    @Test
    void ensureGetManagedDepartmentsOfSecondStageAuthorityDepartmentSorted() {

        final PersonId personId = new PersonId(1L);
        final Person person = new Person();
        person.setId(personId.value());
        person.setPermissions(List.of(USER, SECOND_STAGE_AUTHORITY));

        final PersonId memberId = new PersonId(2L);
        final Person member = new Person();
        member.setId(memberId.value());
        person.setPermissions(List.of(USER));

        final DepartmentEntity departmentEntity1 = new DepartmentEntity();
        departmentEntity1.setId(1L);
        departmentEntity1.setName("Department A");
        final DepartmentEntity departmentEntity3 = new DepartmentEntity();
        departmentEntity3.setId(3L);
        departmentEntity3.setName("Department B");

        final DepartmentMembership secondStageMembership1 = new DepartmentMembership(personId, 1L, DepartmentMembershipKind.SECOND_STAGE_AUTHORITY, Instant.now(clock));
        final DepartmentMembership membership1 = new DepartmentMembership(memberId, 1L, DepartmentMembershipKind.MEMBER, Instant.now(clock));
        final DepartmentMembership secondStageMembership3 = new DepartmentMembership(personId, 3L, DepartmentMembershipKind.SECOND_STAGE_AUTHORITY, Instant.now(clock));

        when(departmentMembershipService.getActiveMemberships(personId))
            .thenReturn(List.of(secondStageMembership1, secondStageMembership3));

        when(departmentRepository.findAllById(Set.of(1L, 3L)))
            .thenReturn(List.of(departmentEntity1, departmentEntity3));

        final DepartmentStaff staff1 = new DepartmentStaff(1L, List.of(membership1), List.of(), List.of(secondStageMembership1));
        final DepartmentStaff staff3 = new DepartmentStaff(3L, List.of(), List.of(), List.of(secondStageMembership3));

        when(departmentMembershipService.getDepartmentStaff(Set.of(1L, 3L)))
            .thenReturn(Map.of(1L, staff1, 3L, staff3));

        when(personService.getAllPersonsByIds(Set.of(personId, memberId))).thenReturn(List.of(person, member));

        final List<Department> actual = sut.getManagedDepartmentsOfSecondStageAuthority(person);
        assertThat(actual).satisfiesExactly(
            department -> {
                assertThat(department.getId()).isEqualTo(1L);
                assertThat(department.getName()).isEqualTo("Department A");
                assertThat(department.getMembers()).containsExactly(member);
                assertThat(department.getDepartmentHeads()).isEmpty();
                assertThat(department.getSecondStageAuthorities()).containsExactly(person);
            },
            department -> {
                assertThat(department.getId()).isEqualTo(3L);
                assertThat(department.getName()).isEqualTo("Department B");
                assertThat(department.getMembers()).isEmpty();
                assertThat(department.getDepartmentHeads()).isEmpty();
                assertThat(department.getSecondStageAuthorities()).containsExactly(person);
            }
        );
    }

    @Test
    void ensureGetAssignedDepartmentsOfMemberDepartmentSorted() {

        final PersonId personId = new PersonId(1L);
        final Person person = new Person();
        person.setId(personId.value());
        person.setPermissions(List.of(USER));

        final PersonId departmentHeadId = new PersonId(2L);
        final Person departmentHead = new Person();
        departmentHead.setId(departmentHeadId.value());
        departmentHead.setPermissions(List.of(USER, DEPARTMENT_HEAD));

        final DepartmentEntity departmentEntity1 = new DepartmentEntity();
        departmentEntity1.setId(1L);
        departmentEntity1.setName("Department A");
        final DepartmentEntity departmentEntity3 = new DepartmentEntity();
        departmentEntity3.setId(3L);
        departmentEntity3.setName("Department B");

        final DepartmentMembership membership1 = new DepartmentMembership(personId, 1L, DepartmentMembershipKind.MEMBER, Instant.now(clock));
        final DepartmentMembership membership3 = new DepartmentMembership(personId, 3L, DepartmentMembershipKind.MEMBER, Instant.now(clock));
        final DepartmentMembership departmentHeadMembership3 = new DepartmentMembership(departmentHeadId, 3L, DepartmentMembershipKind.DEPARTMENT_HEAD, Instant.now(clock));

        when(departmentMembershipService.getActiveMemberships(personId))
            .thenReturn(List.of(membership1, membership3));

        when(departmentRepository.findAllById(Set.of(1L, 3L)))
            .thenReturn(List.of(departmentEntity1, departmentEntity3));

        final DepartmentStaff staff1 = new DepartmentStaff(1L, List.of(membership1), List.of(), List.of());
        final DepartmentStaff staff3 = new DepartmentStaff(3L, List.of(membership3), List.of(departmentHeadMembership3), List.of());

        when(departmentMembershipService.getDepartmentStaff(Set.of(1L, 3L)))
            .thenReturn(Map.of(1L, staff1, 3L, staff3));

        when(personService.getAllPersonsByIds(Set.of(personId, departmentHeadId))).thenReturn(List.of(person, departmentHead));

        final List<Department> actual = sut.getAssignedDepartmentsOfMember(person);
        assertThat(actual).satisfiesExactly(
            department -> {
                assertThat(department.getId()).isEqualTo(1L);
                assertThat(department.getName()).isEqualTo("Department A");
                assertThat(department.getMembers()).containsExactly(person);
                assertThat(department.getDepartmentHeads()).isEmpty();
                assertThat(department.getSecondStageAuthorities()).isEmpty();
            },
            department -> {
                assertThat(department.getId()).isEqualTo(3L);
                assertThat(department.getName()).isEqualTo("Department B");
                assertThat(department.getMembers()).containsExactly(person);
                assertThat(department.getDepartmentHeads()).containsExactly(departmentHead);
                assertThat(department.getSecondStageAuthorities()).isEmpty();
            }
        );
    }

    @Test
    void ensureDeletionIsNotExecutedIfDepartmentWithGivenIDDoesNotExist() {

        sut.delete(0L);

        verify(departmentRepository, never()).deleteById(anyLong());
    }

    @Test
    void ensureDeleteCallFindOneAndDelete() {

        final DepartmentEntity departmentEntity = new DepartmentEntity();
        departmentEntity.setName("department");

        when(departmentRepository.existsById(0L)).thenReturn(true);

        sut.delete(0L);

        verify(departmentRepository).existsById(0L);
        verify(departmentRepository).deleteById(0L);
    }

    @Test
    void ensureSetLastModificationOnUpdate() {

        final Department department = new Department();
        department.setId(1L);

        final DepartmentEntity departmentEntity = new DepartmentEntity();
        departmentEntity.setId(1L);

        final LocalDate expectedModificationDate = LocalDate.of(2020, Month.JANUARY, 1);
        departmentEntity.setLastModification(expectedModificationDate);

        when(departmentRepository.findById(1L)).thenReturn(Optional.of(departmentEntity));
        when(departmentRepository.save(departmentEntity)).thenReturn(departmentEntity);

        final DepartmentStaff staff = new DepartmentStaff(1L, List.of(), List.of(), List.of());
        when(departmentMembershipService.getDepartmentStaff(1L)).thenReturn(staff);
        when(departmentMembershipService.updateDepartmentMemberships(any(DepartmentStaff.class), any(DepartmentStaff.class))).thenReturn(staff);

        final Department updatedDepartment = sut.update(department);

        assertThat(department.getLastModification()).isEqualTo(LocalDate.now(clock)); // department constructor currently sets the modification date
        assertThat(updatedDepartment.getLastModification()).isEqualTo(expectedModificationDate);
    }

    @Test
    void getMembersForDepartmentHead() {

        final PersonId personId = new PersonId(1L);
        final Person person = new Person();
        person.setId(personId.value());
        person.setPermissions(List.of(USER, DEPARTMENT_HEAD));

        final PersonId memberId1 = new PersonId(2L);
        final Person member1 = new Person();
        member1.setId(memberId1.value());
        member1.setPermissions(List.of(USER));

        final PersonId memberId2 = new PersonId(3L);
        final Person member2 = new Person();
        member2.setId(memberId2.value());
        member2.setPermissions(List.of(USER));

        final DepartmentEntity departmentEntity1 = new DepartmentEntity();
        departmentEntity1.setId(1L);
        departmentEntity1.setName("Department B");

        final DepartmentEntity departmentEntity2 = new DepartmentEntity();
        departmentEntity2.setId(2L);
        departmentEntity2.setName("Department A");

        final DepartmentMembership headMembership1 = new DepartmentMembership(personId, 1L, DepartmentMembershipKind.DEPARTMENT_HEAD, Instant.now(clock));
        final DepartmentMembership membership1 = new DepartmentMembership(memberId1, 1L, DepartmentMembershipKind.MEMBER, Instant.now(clock));
        final DepartmentMembership headMembership2 = new DepartmentMembership(personId, 2L, DepartmentMembershipKind.DEPARTMENT_HEAD, Instant.now(clock));
        final DepartmentMembership membership2 = new DepartmentMembership(memberId2, 2L, DepartmentMembershipKind.MEMBER, Instant.now(clock));
        final DepartmentMembership membership3 = new DepartmentMembership(personId, 3L, DepartmentMembershipKind.MEMBER, Instant.now(clock));

        when(departmentMembershipService.getActiveMemberships(personId)).thenReturn(List.of(headMembership1, headMembership2, membership3));

        when(departmentRepository.findAllById(Set.of(1L, 2L))).thenReturn(List.of(departmentEntity1, departmentEntity2));

        final DepartmentStaff staff1 = new DepartmentStaff(1L, List.of(membership1), List.of(headMembership1), List.of());
        final DepartmentStaff staff2 = new DepartmentStaff(2L, List.of(membership2), List.of(headMembership1), List.of());

        when(departmentMembershipService.getDepartmentStaff(Set.of(1L, 2L)))
            .thenReturn(Map.of(1L, staff1, 2L, staff2));

        when(personService.getAllPersonsByIds(Set.of(personId, memberId1, memberId2)))
            .thenReturn(List.of(person, member1, member2));

        final List<Person> actual = sut.getMembersForDepartmentHead(person);
        assertThat(actual).containsExactlyInAnyOrder(member1, member2);
    }

    @Test
    void getMembersForSecondStageAuthority() {

        final PersonId personId = new PersonId(1L);
        final Person person = new Person();
        person.setId(personId.value());
        person.setPermissions(List.of(USER, SECOND_STAGE_AUTHORITY));

        final PersonId memberId1 = new PersonId(2L);
        final Person member1 = new Person();
        member1.setId(memberId1.value());
        member1.setPermissions(List.of(USER));

        final PersonId memberId2 = new PersonId(3L);
        final Person member2 = new Person();
        member2.setId(memberId2.value());
        member2.setPermissions(List.of(USER));

        final DepartmentEntity departmentEntity1 = new DepartmentEntity();
        departmentEntity1.setId(1L);
        departmentEntity1.setName("Department B");

        final DepartmentEntity departmentEntity2 = new DepartmentEntity();
        departmentEntity2.setId(2L);
        departmentEntity2.setName("Department A");

        final DepartmentMembership secondStageMembership1 = new DepartmentMembership(personId, 1L, DepartmentMembershipKind.SECOND_STAGE_AUTHORITY, Instant.now(clock));
        final DepartmentMembership membership1 = new DepartmentMembership(memberId1, 1L, DepartmentMembershipKind.MEMBER, Instant.now(clock));
        final DepartmentMembership secondStageMembership2 = new DepartmentMembership(personId, 2L, DepartmentMembershipKind.SECOND_STAGE_AUTHORITY, Instant.now(clock));
        final DepartmentMembership membership2 = new DepartmentMembership(memberId2, 2L, DepartmentMembershipKind.MEMBER, Instant.now(clock));
        final DepartmentMembership membership3 = new DepartmentMembership(personId, 3L, DepartmentMembershipKind.MEMBER, Instant.now(clock));

        when(departmentMembershipService.getActiveMemberships(personId)).thenReturn(List.of(secondStageMembership1, secondStageMembership2, membership3));

        when(departmentRepository.findAllById(Set.of(1L, 2L))).thenReturn(List.of(departmentEntity1, departmentEntity2));

        final DepartmentStaff staff1 = new DepartmentStaff(1L, List.of(membership1), List.of(secondStageMembership1), List.of());
        final DepartmentStaff staff2 = new DepartmentStaff(2L, List.of(membership2), List.of(secondStageMembership1), List.of());

        when(departmentMembershipService.getDepartmentStaff(Set.of(1L, 2L)))
            .thenReturn(Map.of(1L, staff1, 2L, staff2));

        when(personService.getAllPersonsByIds(Set.of(personId, memberId1, memberId2)))
            .thenReturn(List.of(person, member1, member2));

        final List<Person> actual = sut.getMembersForSecondStageAuthority(person);
        assertThat(actual).containsExactlyInAnyOrder(member1, member2);
    }

    @Test
    void ensureReturnsTrueIfIsDepartmentHeadOfTheGivenPerson() {

        final PersonId departmentHeadId = new PersonId(1L);
        final Person departmentHead = new Person();
        departmentHead.setId(departmentHeadId.value());
        departmentHead.setPermissions(List.of(USER, DEPARTMENT_HEAD));

        final PersonId personId = new PersonId(2L);
        final Person person = new Person();
        person.setId(personId.value());
        person.setPermissions(List.of(USER));

        final DepartmentMembership headMembership = new DepartmentMembership(departmentHeadId, 1L, DepartmentMembershipKind.DEPARTMENT_HEAD, Instant.now(clock));
        final DepartmentMembership membership = new DepartmentMembership(personId, 1L, DepartmentMembershipKind.MEMBER, Instant.now(clock));

        when(departmentMembershipService.getActiveMembershipsOfPersons(List.of(departmentHeadId, personId)))
            .thenReturn(Map.of(departmentHeadId, List.of(headMembership), personId, List.of(membership)));

        boolean actual = sut.isDepartmentHeadAllowedToManagePerson(departmentHead, person);
        assertThat(actual).isTrue();
    }

    @Test
    void ensureReturnsFalseIfIsNotDepartmentHeadOfTheGivenPerson() {

        final PersonId departmentHeadId = new PersonId(1L);
        final Person departmentHead = new Person();
        departmentHead.setId(departmentHeadId.value());
        departmentHead.setPermissions(List.of(USER, DEPARTMENT_HEAD));

        final PersonId otherPersonId = new PersonId(2L);
        final Person otherPerson = new Person();
        otherPerson.setId(otherPersonId.value());
        otherPerson.setPermissions(List.of(USER));

        final DepartmentMembership headMembership = new DepartmentMembership(departmentHeadId, 1L, DepartmentMembershipKind.DEPARTMENT_HEAD, Instant.now(clock));
        final DepartmentMembership otherMembership = new DepartmentMembership(otherPersonId, 2L, DepartmentMembershipKind.MEMBER, Instant.now(clock));

        when(departmentMembershipService.getActiveMembershipsOfPersons(List.of(departmentHeadId, otherPersonId)))
            .thenReturn(Map.of(departmentHeadId, List.of(headMembership), otherPersonId, List.of(otherMembership)));

        boolean actual = sut.isDepartmentHeadAllowedToManagePerson(departmentHead, otherPerson);
        assertThat(actual).isFalse();
    }

    @Test
    void ensureReturnsFalseIfIsInTheSameDepartmentButHasNotDepartmentHeadRole() {

        final PersonId personId = new PersonId(1L);
        final Person person = new Person();
        person.setId(personId.value());
        person.setPermissions(List.of(USER));

        final Person otherPerson = new Person();

        boolean actual = sut.isDepartmentHeadAllowedToManagePerson(person, otherPerson);
        assertThat(actual).isFalse();

        verifyNoInteractions(departmentMembershipService);
    }

    @Test
    void ensureGetApplicationsFromColleaguesOfReturnsEmptyApplicationsBecauseNoDepartmentsAssigned() {

        when(departmentRepository.count()).thenReturn(1L);

        final PersonId personId = new PersonId(1L);
        final Person person = new Person();
        person.setId(personId.value());
        person.setPermissions(List.of(USER));

        when(departmentMembershipService.getActiveMemberships(personId)).thenReturn(List.of());

        final LocalDate date = LocalDate.now(clock);

        final List<Application> applications = sut.getApplicationsFromColleaguesOf(person, date, date);
        assertThat(applications).isEmpty();

        verifyNoInteractions(applicationService);
    }

    @Test
    void ensureGetApplicationsFromColleaguesOfReturnsEmptyApplications() {

        when(departmentRepository.count()).thenReturn(1L);

        final PersonId personId = new PersonId(1L);
        final Person person = new Person();
        person.setId(personId.value());
        person.setPermissions(List.of(USER));

        final PersonId personId2 = new PersonId(2L);
        final Person person2 = new Person();
        person2.setId(personId2.value());
        person2.setPermissions(List.of(USER));

        final PersonId personId3 = new PersonId(3L);
        final Person person3 = new Person();
        person3.setId(personId3.value());
        person3.setPermissions(List.of(USER));

        final DepartmentMembership membership11 = new DepartmentMembership(personId, 1L, DepartmentMembershipKind.MEMBER, Instant.now(clock));
        final DepartmentMembership membership12 = new DepartmentMembership(personId2, 1L, DepartmentMembershipKind.MEMBER, Instant.now(clock));
        final DepartmentMembership membership21 = new DepartmentMembership(personId, 2L, DepartmentMembershipKind.MEMBER, Instant.now(clock));
        final DepartmentMembership membership23 = new DepartmentMembership(personId3, 2L, DepartmentMembershipKind.MEMBER, Instant.now(clock));

        when(departmentMembershipService.getActiveMemberships(personId)).thenReturn(List.of(membership11, membership21));

        final DepartmentEntity departmentEntity1 = new DepartmentEntity();
        departmentEntity1.setId(1L);
        departmentEntity1.setName("Department B");

        final DepartmentEntity departmentEntity2 = new DepartmentEntity();
        departmentEntity2.setId(2L);
        departmentEntity2.setName("Department A");

        when(departmentRepository.findAllById(Set.of(1L, 2L))).thenReturn(List.of(departmentEntity1, departmentEntity2));

        final DepartmentStaff staff1 = new DepartmentStaff(1L, List.of(membership11, membership12), List.of(), List.of());
        final DepartmentStaff staff2 = new DepartmentStaff(2L, List.of(membership21, membership23), List.of(), List.of());

        when(departmentMembershipService.getDepartmentStaff(Set.of(1L, 2L)))
            .thenReturn(Map.of(1L, staff1, 2L, staff2));

        when(personService.getAllPersonsByIds(Set.of(personId, personId2, personId3)))
            .thenReturn(List.of(person, person2, person3));

        final LocalDate date = LocalDate.now(clock);

        when(applicationService.getForStatesAndPerson(ApplicationStatus.activeStatuses(), List.of(person3, person2), date, date))
            .thenReturn(List.of());

        final List<Application> applications = sut.getApplicationsFromColleaguesOf(person, date, date);
        assertThat(applications).isEmpty();
    }

    @Test
    void ensureGetApplicationsFromColleaguesOfReturnsApplicationsSortedByDate() {

        when(departmentRepository.count()).thenReturn(1L);

        final PersonId personId = new PersonId(1L);
        final Person person = new Person();
        person.setId(personId.value());
        person.setPermissions(List.of(USER));

        final PersonId personId2 = new PersonId(2L);
        final Person person2 = new Person();
        person2.setId(personId2.value());
        person2.setPermissions(List.of(USER));

        final PersonId personId3 = new PersonId(3L);
        final Person person3 = new Person();
        person3.setId(personId3.value());
        person3.setPermissions(List.of(USER));

        final DepartmentMembership membership11 = new DepartmentMembership(personId, 1L, DepartmentMembershipKind.MEMBER, Instant.now(clock));
        final DepartmentMembership membership12 = new DepartmentMembership(personId2, 1L, DepartmentMembershipKind.MEMBER, Instant.now(clock));
        final DepartmentMembership membership21 = new DepartmentMembership(personId, 2L, DepartmentMembershipKind.MEMBER, Instant.now(clock));
        final DepartmentMembership membership23 = new DepartmentMembership(personId3, 2L, DepartmentMembershipKind.MEMBER, Instant.now(clock));

        when(departmentMembershipService.getActiveMemberships(personId)).thenReturn(List.of(membership11, membership21));

        final DepartmentEntity departmentEntity1 = new DepartmentEntity();
        departmentEntity1.setId(1L);
        departmentEntity1.setName("Department B");

        final DepartmentEntity departmentEntity2 = new DepartmentEntity();
        departmentEntity2.setId(2L);
        departmentEntity2.setName("Department A");

        when(departmentRepository.findAllById(Set.of(1L, 2L))).thenReturn(List.of(departmentEntity1, departmentEntity2));

        final DepartmentStaff staff1 = new DepartmentStaff(1L, List.of(membership11, membership12), List.of(), List.of());
        final DepartmentStaff staff2 = new DepartmentStaff(2L, List.of(membership21, membership23), List.of(), List.of());

        when(departmentMembershipService.getDepartmentStaff(Set.of(1L, 2L)))
            .thenReturn(Map.of(1L, staff1, 2L, staff2));

        when(personService.getAllPersonsByIds(Set.of(personId, personId2, personId3)))
            .thenReturn(List.of(person, person2, person3));

        final LocalDate date = LocalDate.now(clock);

        final Application application1 = new Application();
        application1.setId(1L);
        application1.setStartDate(date.minusDays(1));

        final Application application2 = new Application();
        application2.setId(2L);
        application2.setStartDate(date.minusDays(2));

        when(applicationService.getForStatesAndPerson(ApplicationStatus.activeStatuses(), List.of(person3, person2), date, date))
            .thenReturn(List.of(application1, application2));

        final List<Application> applications = sut.getApplicationsFromColleaguesOf(person, date, date);
        assertThat(applications).containsExactly(application2, application1);
    }

    @Test
    void ensureSignedInOfficeUserCanAccessPersonData() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setId(1L);
        person.setPermissions(List.of(USER));

        final Person office = new Person("muster", "Muster", "Marlene", "muster@example.org");
        office.setId(2L);
        office.setPermissions(List.of(USER, OFFICE));

        boolean isAllowed = sut.isSignedInUserAllowedToAccessPersonData(office, person);
        assertThat(isAllowed).isTrue();
    }

    @Test
    void ensureSignedInBossUserCanAccessPersonData() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setId(1L);
        person.setPermissions(List.of(USER));

        final Person boss = new Person("muster", "Muster", "Marlene", "muster@example.org");
        boss.setId(2L);
        boss.setPermissions(List.of(USER, BOSS));

        boolean isAllowed = sut.isSignedInUserAllowedToAccessPersonData(boss, person);
        assertThat(isAllowed).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"BOSS", "OFFICE"})
    void ensureIsPersonAllowedToManageDepartment(Role givenRole) {

        final Department department = new Department();
        department.setId(1L);

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setId(2L);
        person.setPermissions(List.of(USER, givenRole));

        boolean isAllowed = sut.isPersonAllowedToManageDepartment(person, department);
        assertThat(isAllowed).isTrue();
    }

    @Test
    void ensureIsPersonAllowedToManageDepartmentWhenDepartmentHead() {

        final Person departmentHead = new Person("muster", "Muster", "Marlene", "muster@example.org");
        departmentHead.setId(2L);
        departmentHead.setPermissions(List.of(USER, DEPARTMENT_HEAD));

        final Department department = new Department();
        department.setId(1L);
        department.setDepartmentHeads(List.of(departmentHead));

        boolean isAllowed = sut.isPersonAllowedToManageDepartment(departmentHead, department);
        assertThat(isAllowed).isTrue();
    }

    @Test
    void ensureIsPersonAllowedToManageDepartmentWhenSecondStageAuthority() {

        final Person secondStageuthority = new Person("muster", "Muster", "Marlene", "muster@example.org");
        secondStageuthority.setId(2L);
        secondStageuthority.setPermissions(List.of(USER, SECOND_STAGE_AUTHORITY));

        final Department department = new Department();
        department.setId(1L);
        department.setSecondStageAuthorities(List.of(secondStageuthority));

        boolean isAllowed = sut.isPersonAllowedToManageDepartment(secondStageuthority, department);
        assertThat(isAllowed).isTrue();
    }

    @Test
    void ensureIsPersonAllowedToManageDepartmentIsFalseForUser() {

        final Person user = new Person("muster", "Muster", "Marlene", "muster@example.org");
        user.setId(2L);
        user.setPermissions(List.of(USER));

        final Department department = new Department();
        department.setId(1L);

        boolean isAllowed = sut.isPersonAllowedToManageDepartment(user, department);
        assertThat(isAllowed).isFalse();
    }

    @Test
    void ensureIsPersonAllowedToManageDepartmentIsFalseForNormalDepartmentMember() {

        final Person user = new Person("muster", "Muster", "Marlene", "muster@example.org");
        user.setId(2L);
        user.setPermissions(List.of(USER));

        final Department department = new Department();
        department.setId(1L);
        department.setMembers(List.of(user));

        boolean isAllowed = sut.isPersonAllowedToManageDepartment(user, department);
        assertThat(isAllowed).isFalse();
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"DEPARTMENT_HEAD", "SECOND_STAGE_AUTHORITY"})
    void ensureIsPersonAllowedToManageDepartmentIsFalseForRoleDepartmentHeadButNotThisDepartment(Role givenRole) {

        final Person user = new Person("muster", "Muster", "Marlene", "muster@example.org");
        user.setId(2L);
        user.setPermissions(List.of(USER, givenRole));

        final Department department = new Department();
        department.setId(1L);

        boolean isAllowed = sut.isPersonAllowedToManageDepartment(user, department);
        assertThat(isAllowed).isFalse();
    }

    @Test
    void ensureSignedInDepartmentHeadOfPersonCanAccessPersonData() {

        final PersonId departmentHeadId = new PersonId(1L);
        final Person departmentHead = new Person();
        departmentHead.setId(departmentHeadId.value());
        departmentHead.setPermissions(List.of(USER, DEPARTMENT_HEAD));

        final PersonId personId = new PersonId(2L);
        final Person person = new Person();
        person.setId(personId.value());
        person.setPermissions(List.of(USER));

        final DepartmentMembership headMembership = new DepartmentMembership(departmentHeadId, 1L, DepartmentMembershipKind.DEPARTMENT_HEAD, Instant.now(clock));
        final DepartmentMembership membership = new DepartmentMembership(personId, 1L, DepartmentMembershipKind.MEMBER, Instant.now(clock));

        when(departmentMembershipService.getActiveMemberships(departmentHeadId))
            .thenReturn(List.of(headMembership));

        final DepartmentEntity departmentEntity = new DepartmentEntity();
        departmentEntity.setId(1L);

        when(departmentRepository.findAllById(Set.of(1L))).thenReturn(List.of(departmentEntity));

        final DepartmentStaff staff = new DepartmentStaff(1L, List.of(membership), List.of(headMembership), List.of());
        when(departmentMembershipService.getDepartmentStaff(Set.of(1L))).thenReturn(Map.of(1L, staff));

        when(personService.getAllPersonsByIds(Set.of(departmentHeadId, personId))).thenReturn(List.of(departmentHead, person));

        boolean actual = sut.isSignedInUserAllowedToAccessPersonData(departmentHead, person);
        assertThat(actual).isTrue();
    }

    @Test
    void ensureSignedInDepartmentHeadThatIsNotDepartmentHeadOfPersonCanNotAccessPersonData() {

        final PersonId departmentHeadId = new PersonId(1L);
        final Person departmentHead = new Person();
        departmentHead.setId(departmentHeadId.value());
        departmentHead.setPermissions(List.of(USER, DEPARTMENT_HEAD));

        final PersonId personId = new PersonId(2L);
        final Person person = new Person();
        person.setId(personId.value());
        person.setPermissions(List.of(USER));

        final DepartmentMembership headMembership = new DepartmentMembership(departmentHeadId, 1L, DepartmentMembershipKind.DEPARTMENT_HEAD, Instant.now(clock));

        when(departmentMembershipService.getActiveMemberships(departmentHeadId))
            .thenReturn(List.of(headMembership));

        final DepartmentEntity departmentEntity = new DepartmentEntity();
        departmentEntity.setId(1L);

        when(departmentRepository.findAllById(Set.of(1L))).thenReturn(List.of(departmentEntity));

        final DepartmentStaff staff = new DepartmentStaff(1L, List.of(), List.of(headMembership), List.of());
        when(departmentMembershipService.getDepartmentStaff(Set.of(1L))).thenReturn(Map.of(1L, staff));

        boolean actual = sut.isSignedInUserAllowedToAccessPersonData(departmentHead, person);
        assertThat(actual).isFalse();
    }

    @Test
    void ensureSignedInDepartmentHeadCannotAccessSecondStageAuthorityBecauseNoMember() {

        final PersonId secondStageId = new PersonId(1L);
        final Person secondStageAuthority = new Person();
        secondStageAuthority.setId(secondStageId.value());
        secondStageAuthority.setPermissions(List.of(USER, SECOND_STAGE_AUTHORITY));

        final PersonId departmentHeadId = new PersonId(2L);
        final Person departmentHead = new Person();
        departmentHead.setId(departmentHeadId.value());
        departmentHead.setPermissions(List.of(USER, DEPARTMENT_HEAD));

        final DepartmentMembership headMembership = new DepartmentMembership(departmentHeadId, 1L, DepartmentMembershipKind.DEPARTMENT_HEAD, Instant.now(clock));
        final DepartmentMembership secondMembership = new DepartmentMembership(secondStageId, 1L, DepartmentMembershipKind.SECOND_STAGE_AUTHORITY, Instant.now(clock));

        when(departmentMembershipService.getActiveMemberships(departmentHeadId)).thenReturn(List.of(headMembership));

        final DepartmentEntity departmentEntity = new DepartmentEntity();
        departmentEntity.setId(1L);
        departmentEntity.setName("Department");

        when(departmentRepository.findAllById(Set.of(1L))).thenReturn(List.of(departmentEntity));

        final DepartmentStaff staff = new DepartmentStaff(1L, List.of(), List.of(headMembership), List.of(secondMembership));
        when(departmentMembershipService.getDepartmentStaff(Set.of(1L))).thenReturn(Map.of(1L, staff));

        when(personService.getAllPersonsByIds(Set.of(departmentHeadId, secondStageId))).thenReturn(List.of(secondStageAuthority, departmentHead));

        boolean isAllowed = sut.isSignedInUserAllowedToAccessPersonData(departmentHead, secondStageAuthority);
        assertThat(isAllowed).isFalse();
    }

    @Test
    void ensureNotPrivilegedUserCanNotAccessPersonData() {

        Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setId(1L);
        person.setPermissions(List.of(USER));

        Person user = new Person("muster", "Muster", "Marlene", "muster@example.org");
        user.setId(2L);
        user.setPermissions(List.of(USER));

        boolean isAllowed = sut.isSignedInUserAllowedToAccessPersonData(user, person);
        assertThat(isAllowed).isFalse();
    }

    @Test
    void ensureNotPrivilegedUserCanAccessOwnPersonData() {

        Person user = new Person("muster", "Muster", "Marlene", "muster@example.org");
        user.setId(1L);
        user.setPermissions(List.of(USER));

        boolean isAllowed = sut.isSignedInUserAllowedToAccessPersonData(user, user);
        assertThat(isAllowed).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"OFFICE", "BOSS"})
    void ensureOfficeHasAccessToAllDepartments(Role officeOrBossRole) {

        final PersonId personId = new PersonId(1L);
        final Person person = new Person();
        person.setId(personId.value());
        person.setPermissions(List.of(USER, officeOrBossRole));

        final PersonId memberId = new PersonId(2L);
        final Person member = new Person();
        member.setId(memberId.value());
        member.setPermissions(List.of(USER));

        final DepartmentEntity departmentEntity1 = new DepartmentEntity();
        departmentEntity1.setId(1L);
        departmentEntity1.setName("Department B");
        final DepartmentEntity departmentEntity2 = new DepartmentEntity();
        departmentEntity2.setId(2L);
        departmentEntity2.setName("Department A");

        when(departmentRepository.findAll()).thenReturn(List.of(departmentEntity1, departmentEntity2));

        final DepartmentMembership membership1 = new DepartmentMembership(memberId, 1L, DepartmentMembershipKind.MEMBER, Instant.now(clock));

        final DepartmentStaff staff1 = new DepartmentStaff(1L, List.of(membership1), List.of(), List.of());
        final DepartmentStaff staff2 = new DepartmentStaff(2L, List.of(), List.of(), List.of());

        when(departmentMembershipService.getDepartmentStaff(Set.of(1L, 2L)))
            .thenReturn(Map.of(1L, staff1, 2L, staff2));

        when(personService.getAllPersonsByIds(Set.of(memberId))).thenReturn(List.of(member));

        final List<Department> actual = sut.getDepartmentsPersonHasAccessTo(person);
        assertThat(actual).satisfiesExactly(
            department -> {
                assertThat(department.getId()).isEqualTo(2L);
                assertThat(department.getName()).isEqualTo("Department A");
                assertThat(department.getMembers()).isEmpty();
                assertThat(department.getDepartmentHeads()).isEmpty();
                assertThat(department.getSecondStageAuthorities()).isEmpty();
            },
            department -> {
                assertThat(department.getId()).isEqualTo(1L);
                assertThat(department.getName()).isEqualTo("Department B");
                assertThat(department.getMembers()).containsExactly(member);
                assertThat(department.getDepartmentHeads()).isEmpty();
                assertThat(department.getSecondStageAuthorities()).isEmpty();
            }
        );
    }

    @Test
    void ensureSecondStageAuthorityHasAccessToAllowedDepartments() {
        final PersonId personId = new PersonId(1L);
        final Person person = new Person();
        person.setId(personId.value());
        person.setPermissions(List.of(USER, SECOND_STAGE_AUTHORITY));

        final PersonId memberId = new PersonId(2L);
        final Person member = new Person();
        member.setId(memberId.value());
        member.setPermissions(List.of(USER));

        final DepartmentEntity departmentEntity1 = new DepartmentEntity();
        departmentEntity1.setId(1L);
        departmentEntity1.setName("Department B");
        final DepartmentEntity departmentEntity2 = new DepartmentEntity();
        departmentEntity2.setId(2L);
        departmentEntity2.setName("Department A");

        when(departmentRepository.findAllById(Set.of(1L, 2L))).thenReturn(List.of(departmentEntity1, departmentEntity2));

        final DepartmentMembership secondStageMembership1 = new DepartmentMembership(personId, 1L, DepartmentMembershipKind.SECOND_STAGE_AUTHORITY, Instant.now(clock));
        final DepartmentMembership membership1 = new DepartmentMembership(memberId, 1L, DepartmentMembershipKind.MEMBER, Instant.now(clock));
        final DepartmentMembership secondStageMembership2 = new DepartmentMembership(personId, 2L, DepartmentMembershipKind.SECOND_STAGE_AUTHORITY, Instant.now(clock));

        final DepartmentStaff staff1 = new DepartmentStaff(1L, List.of(membership1), List.of(), List.of(secondStageMembership1));
        final DepartmentStaff staff2 = new DepartmentStaff(2L, List.of(), List.of(), List.of(secondStageMembership2));

        when(departmentMembershipService.getActiveMemberships(personId))
            .thenReturn(List.of(secondStageMembership1, secondStageMembership2));

        when(departmentMembershipService.getDepartmentStaff(Set.of(1L, 2L)))
            .thenReturn(Map.of(1L, staff1, 2L, staff2));

        when(personService.getAllPersonsByIds(Set.of(personId, memberId))).thenReturn(List.of(person, member));

        final List<Department> actual = sut.getDepartmentsPersonHasAccessTo(person);
        assertThat(actual).satisfiesExactly(
            department -> {
                assertThat(department.getId()).isEqualTo(2L);
                assertThat(department.getName()).isEqualTo("Department A");
                assertThat(department.getMembers()).isEmpty();
                assertThat(department.getDepartmentHeads()).isEmpty();
                assertThat(department.getSecondStageAuthorities()).containsExactly(person);
            },
            department -> {
                assertThat(department.getId()).isEqualTo(1L);
                assertThat(department.getName()).isEqualTo("Department B");
                assertThat(department.getMembers()).containsExactly(member);
                assertThat(department.getDepartmentHeads()).isEmpty();
                assertThat(department.getSecondStageAuthorities()).containsExactly(person);
            }
        );
    }

    @Test
    void ensureDepartmentHeadHasAccessToAllowedDepartments() {
        final PersonId personId = new PersonId(1L);
        final Person person = new Person();
        person.setId(personId.value());
        person.setPermissions(List.of(USER, DEPARTMENT_HEAD));

        final PersonId memberId = new PersonId(2L);
        final Person member = new Person();
        member.setId(memberId.value());
        member.setPermissions(List.of(USER));

        final DepartmentEntity departmentEntity1 = new DepartmentEntity();
        departmentEntity1.setId(1L);
        departmentEntity1.setName("Department B");
        final DepartmentEntity departmentEntity2 = new DepartmentEntity();
        departmentEntity2.setId(2L);
        departmentEntity2.setName("Department A");

        when(departmentRepository.findAllById(Set.of(1L, 2L))).thenReturn(List.of(departmentEntity1, departmentEntity2));

        final DepartmentMembership headMembership1 = new DepartmentMembership(personId, 1L, DepartmentMembershipKind.DEPARTMENT_HEAD, Instant.now(clock));
        final DepartmentMembership membership1 = new DepartmentMembership(memberId, 1L, DepartmentMembershipKind.MEMBER, Instant.now(clock));
        final DepartmentMembership headMembership2 = new DepartmentMembership(personId, 2L, DepartmentMembershipKind.DEPARTMENT_HEAD, Instant.now(clock));

        final DepartmentStaff staff1 = new DepartmentStaff(1L, List.of(membership1), List.of(headMembership1), List.of());
        final DepartmentStaff staff2 = new DepartmentStaff(2L, List.of(), List.of(headMembership2), List.of());

        when(departmentMembershipService.getActiveMemberships(personId))
            .thenReturn(List.of(headMembership1, headMembership2));

        when(departmentMembershipService.getDepartmentStaff(Set.of(1L, 2L)))
            .thenReturn(Map.of(1L, staff1, 2L, staff2));

        when(personService.getAllPersonsByIds(Set.of(personId, memberId))).thenReturn(List.of(person, member));

        final List<Department> actual = sut.getDepartmentsPersonHasAccessTo(person);
        assertThat(actual).satisfiesExactly(
            department -> {
                assertThat(department.getId()).isEqualTo(2L);
                assertThat(department.getName()).isEqualTo("Department A");
                assertThat(department.getMembers()).isEmpty();
                assertThat(department.getDepartmentHeads()).containsExactly(person);
                assertThat(department.getSecondStageAuthorities()).isEmpty();
            },
            department -> {
                assertThat(department.getId()).isEqualTo(1L);
                assertThat(department.getName()).isEqualTo("Department B");
                assertThat(department.getMembers()).containsExactly(member);
                assertThat(department.getDepartmentHeads()).containsExactly(person);
                assertThat(department.getSecondStageAuthorities()).isEmpty();
            }
        );
    }

    @Test
    void ensurePersonWithSecondStageAuthorityAndDepartmentHeadHasAccessToAllowedDepartments() {
        final PersonId personId = new PersonId(1L);
        final Person person = new Person();
        person.setId(personId.value());
        person.setPermissions(List.of(USER, DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY));

        final PersonId memberId = new PersonId(2L);
        final Person member = new Person();
        member.setId(memberId.value());
        member.setPermissions(List.of(USER));

        final DepartmentEntity departmentEntity1 = new DepartmentEntity();
        departmentEntity1.setId(1L);
        departmentEntity1.setName("Department B");
        final DepartmentEntity departmentEntity2 = new DepartmentEntity();
        departmentEntity2.setId(2L);
        departmentEntity2.setName("Department A");

        when(departmentRepository.findAllById(Set.of(2L))).thenReturn(List.of(departmentEntity2));
        when(departmentRepository.findAllById(Set.of(1L))).thenReturn(List.of(departmentEntity1));

        final DepartmentMembership headMembership1 = new DepartmentMembership(personId, 1L, DepartmentMembershipKind.DEPARTMENT_HEAD, Instant.now(clock));
        final DepartmentMembership membership1 = new DepartmentMembership(memberId, 1L, DepartmentMembershipKind.MEMBER, Instant.now(clock));
        final DepartmentMembership secondMembership2 = new DepartmentMembership(personId, 2L, DepartmentMembershipKind.SECOND_STAGE_AUTHORITY, Instant.now(clock));

        final DepartmentStaff staff1 = new DepartmentStaff(1L, List.of(membership1), List.of(headMembership1), List.of());
        final DepartmentStaff staff2 = new DepartmentStaff(2L, List.of(), List.of(), List.of(secondMembership2));

        when(departmentMembershipService.getActiveMemberships(personId))
            .thenReturn(List.of(headMembership1, secondMembership2));

        when(departmentMembershipService.getDepartmentStaff(Set.of(1L)))
            .thenReturn(Map.of(1L, staff1));
        when(departmentMembershipService.getDepartmentStaff(Set.of(2L)))
            .thenReturn(Map.of(2L, staff2));

        when(personService.getAllPersonsByIds(Set.of(personId))).thenReturn(List.of(person));
        when(personService.getAllPersonsByIds(Set.of(personId, memberId))).thenReturn(List.of(person, member));

        final List<Department> actual = sut.getDepartmentsPersonHasAccessTo(person);
        assertThat(actual).satisfiesExactly(
            department -> {
                assertThat(department.getId()).isEqualTo(2L);
                assertThat(department.getName()).isEqualTo("Department A");
                assertThat(department.getMembers()).isEmpty();
                assertThat(department.getDepartmentHeads()).isEmpty();
                assertThat(department.getSecondStageAuthorities()).containsExactly(person);
            },
            department -> {
                assertThat(department.getId()).isEqualTo(1L);
                assertThat(department.getName()).isEqualTo("Department B");
                assertThat(department.getMembers()).containsExactly(member);
                assertThat(department.getDepartmentHeads()).containsExactly(person);
                assertThat(department.getSecondStageAuthorities()).isEmpty();
            }
        );
    }

    @Test
    void getNumberOfDepartment() {

        when(departmentRepository.count()).thenReturn(10L);

        final long numberOfDepartments = sut.getNumberOfDepartments();
        assertThat(numberOfDepartments).isEqualTo(10);
    }

    @Test
    void getDepartmentsByMembers() {

        final PersonId personId = new PersonId(1L);
        final Person person = new Person();
        person.setId(personId.value());

        final DepartmentEntity departmentEntity1 = new DepartmentEntity();
        departmentEntity1.setName("Department B");
        departmentEntity1.setId(1L);

        final DepartmentEntity departmentEntity2 = new DepartmentEntity();
        departmentEntity2.setName("Department A");
        departmentEntity2.setId(2L);

        final DepartmentEntity departmentEntity3 = new DepartmentEntity();
        departmentEntity3.setName("Department AA");
        departmentEntity3.setId(3L);

        when(departmentRepository.findAllById(Set.of(1L, 3L))).thenReturn(List.of(departmentEntity1, departmentEntity3));

        final DepartmentMembership membership1 = new DepartmentMembership(personId, 1L, DepartmentMembershipKind.MEMBER, Instant.now(clock));
        final DepartmentMembership secondMembership2 = new DepartmentMembership(personId, 2L, DepartmentMembershipKind.SECOND_STAGE_AUTHORITY, Instant.now(clock));
        final DepartmentMembership headMembership3 = new DepartmentMembership(personId, 3L, DepartmentMembershipKind.DEPARTMENT_HEAD, Instant.now(clock));
        final DepartmentMembership membership3 = new DepartmentMembership(personId, 3L, DepartmentMembershipKind.MEMBER, Instant.now(clock));

        when(departmentMembershipService.getActiveMembershipsOfPersons(List.of(personId)))
            .thenReturn(Map.of(personId, List.of(membership1, secondMembership2, headMembership3, membership3)));

        final DepartmentStaff staff1 = new DepartmentStaff(1L, List.of(membership1), List.of(), List.of());
        final DepartmentStaff staff3 = new DepartmentStaff(3L, List.of(membership1), List.of(headMembership3), List.of());

        when(departmentMembershipService.getDepartmentStaff(Set.of(1L, 3L)))
            .thenReturn(Map.of(1L, staff1, 3L, staff3));

        final Map<PersonId, List<String>> departmentsByMembers = sut.getDepartmentNamesByMembers(List.of(person));
        assertThat(departmentsByMembers).containsEntry(personId, List.of("Department AA", "Department B"));
    }

    @Test
    void getDepartmentsByMembersForDifferentDepartmentsAndPersons() {

        final PersonId personId = new PersonId(1L);
        final Person person = new Person();
        person.setId(personId.value());

        final PersonId personId2 = new PersonId(2L);
        final Person person2 = new Person();
        person2.setId(personId2.value());

        final DepartmentEntity departmentEntity1 = new DepartmentEntity();
        departmentEntity1.setName("Department B");
        departmentEntity1.setId(1L);

        final DepartmentEntity departmentEntity2 = new DepartmentEntity();
        departmentEntity2.setName("Department A");
        departmentEntity2.setId(2L);

        final DepartmentEntity departmentEntity3 = new DepartmentEntity();
        departmentEntity3.setName("Department AA");
        departmentEntity3.setId(3L);

        when(departmentRepository.findAllById(Set.of(1L, 2L, 3L))).thenReturn(List.of(departmentEntity1, departmentEntity2, departmentEntity3));

        final DepartmentMembership membership1 = new DepartmentMembership(personId, 1L, DepartmentMembershipKind.MEMBER, Instant.now(clock));
        final DepartmentMembership secondMembership2 = new DepartmentMembership(personId2, 2L, DepartmentMembershipKind.SECOND_STAGE_AUTHORITY, Instant.now(clock));
        final DepartmentMembership headMembership2 = new DepartmentMembership(personId, 2L, DepartmentMembershipKind.DEPARTMENT_HEAD, Instant.now(clock));
        final DepartmentMembership membership2 = new DepartmentMembership(personId2, 2L, DepartmentMembershipKind.MEMBER, Instant.now(clock));
        final DepartmentMembership membership3 = new DepartmentMembership(personId2, 3L, DepartmentMembershipKind.MEMBER, Instant.now(clock));

        when(departmentMembershipService.getActiveMembershipsOfPersons(List.of(personId, personId2)))
            .thenReturn(Map.of(
                personId, List.of(membership1, headMembership2),
                personId2, List.of(secondMembership2, membership2, membership3)
            ));

        final DepartmentStaff staff1 = new DepartmentStaff(1L, List.of(membership1), List.of(), List.of());
        final DepartmentStaff staff2 = new DepartmentStaff(2L, List.of(membership2), List.of(headMembership2), List.of(secondMembership2));
        final DepartmentStaff staff3 = new DepartmentStaff(3L, List.of(membership3), List.of(), List.of());

        when(departmentMembershipService.getDepartmentStaff(Set.of(1L, 2L, 3L)))
            .thenReturn(Map.of(1L, staff1, 2L, staff2, 3L, staff3));

        final Map<PersonId, List<String>> departmentsByMembers = sut.getDepartmentNamesByMembers(List.of(person, person2));
        assertThat(departmentsByMembers).containsEntry(personId, List.of("Department B"));
        assertThat(departmentsByMembers).containsEntry(personId2, List.of("Department A", "Department AA"));
    }

    @Test
    void ensureDepartmentMatchFalse() {

        final PersonId personId = new PersonId(1L);
        final Person person = new Person();
        person.setId(personId.value());
        person.setPermissions(List.of(USER));

        final PersonId otherId = new PersonId(2L);
        final Person other = new Person();
        other.setId(otherId.value());
        other.setPermissions(List.of(USER));

        final DepartmentMembership personMembership = new DepartmentMembership(personId, 1L, DepartmentMembershipKind.MEMBER, Instant.now(clock));
        final DepartmentMembership otherMembership = new DepartmentMembership(otherId, 2L, DepartmentMembershipKind.MEMBER, Instant.now(clock));

        when(departmentMembershipService.getActiveMembershipsOfPersons(List.of(personId, otherId)))
            .thenReturn(Map.of(personId, List.of(personMembership), otherId, List.of(otherMembership)));

        final boolean actual = sut.hasDepartmentMatch(person, other);
        assertThat(actual).isFalse();
    }

    @Test
    void ensureDepartmentMatchWhenBothAreMembers() {

        final PersonId personId = new PersonId(1L);
        final Person person = new Person();
        person.setId(personId.value());
        person.setPermissions(List.of(USER));

        final PersonId otherId = new PersonId(2L);
        final Person other = new Person();
        other.setId(otherId.value());
        other.setPermissions(List.of(USER));

        final DepartmentMembership personMembership = new DepartmentMembership(personId, 1L, DepartmentMembershipKind.MEMBER, Instant.now(clock));
        final DepartmentMembership otherMembership = new DepartmentMembership(otherId, 1L, DepartmentMembershipKind.MEMBER, Instant.now(clock));

        when(departmentMembershipService.getActiveMembershipsOfPersons(List.of(personId, otherId)))
            .thenReturn(Map.of(personId, List.of(personMembership), otherId, List.of(otherMembership)));

        final boolean actual = sut.hasDepartmentMatch(person, other);
        assertThat(actual).isTrue();
    }

    @Test
    void ensureDepartmentMatchWhenPersonIsDepartmentHeadOfOtherPersonButNotMember() {

        final PersonId personId = new PersonId(1L);
        final Person person = new Person();
        person.setId(personId.value());
        person.setPermissions(List.of(USER, DEPARTMENT_HEAD));

        final PersonId otherId = new PersonId(2L);
        final Person other = new Person();
        other.setId(otherId.value());
        other.setPermissions(List.of(USER));

        final DepartmentMembership personMembership = new DepartmentMembership(personId, 1L, DepartmentMembershipKind.DEPARTMENT_HEAD, Instant.now(clock));
        final DepartmentMembership otherMembership = new DepartmentMembership(otherId, 1L, DepartmentMembershipKind.MEMBER, Instant.now(clock));

        when(departmentMembershipService.getActiveMembershipsOfPersons(List.of(personId, otherId)))
            .thenReturn(Map.of(personId, List.of(personMembership), otherId, List.of(otherMembership)));

        final boolean actual = sut.hasDepartmentMatch(person, other);
        assertThat(actual).isTrue();
    }

    @Test
    void ensureDepartmentMatchWhenPersonIsSecondStageAuthorityOfOtherPersonButNotMember() {

        final PersonId personId = new PersonId(1L);
        final Person person = new Person();
        person.setId(personId.value());
        person.setPermissions(List.of(USER, SECOND_STAGE_AUTHORITY));

        final PersonId otherId = new PersonId(2L);
        final Person other = new Person();
        other.setId(otherId.value());
        other.setPermissions(List.of(USER));

        final DepartmentMembership personMembership = new DepartmentMembership(personId, 1L, DepartmentMembershipKind.SECOND_STAGE_AUTHORITY, Instant.now(clock));
        final DepartmentMembership otherMembership = new DepartmentMembership(otherId, 1L, DepartmentMembershipKind.MEMBER, Instant.now(clock));

        when(departmentMembershipService.getActiveMembershipsOfPersons(List.of(personId, otherId)))
            .thenReturn(Map.of(personId, List.of(personMembership), otherId, List.of(otherMembership)));

        final boolean actual = sut.hasDepartmentMatch(person, other);
        assertThat(actual).isTrue();
    }

    @Test
    void ensureDepartmentMatchWhenOtherPersonIsDepartmentHeadOfPersonButNotMember() {

        final PersonId personId = new PersonId(1L);
        final Person person = new Person();
        person.setId(personId.value());
        person.setPermissions(List.of(USER));

        final PersonId otherId = new PersonId(2L);
        final Person other = new Person();
        other.setId(otherId.value());
        other.setPermissions(List.of(USER, DEPARTMENT_HEAD));

        final DepartmentMembership personMembership = new DepartmentMembership(personId, 1L, DepartmentMembershipKind.MEMBER, Instant.now(clock));
        final DepartmentMembership otherMembership = new DepartmentMembership(otherId, 1L, DepartmentMembershipKind.DEPARTMENT_HEAD, Instant.now(clock));

        when(departmentMembershipService.getActiveMembershipsOfPersons(List.of(personId, otherId)))
            .thenReturn(Map.of(personId, List.of(personMembership), otherId, List.of(otherMembership)));

        final boolean actual = sut.hasDepartmentMatch(person, other);
        assertThat(actual).isTrue();
    }

    @Test
    void ensureDepartmentMatchWhenOtherPersonIsSecondStageAuthorityOfPersonButNotMember() {

        final PersonId personId = new PersonId(1L);
        final Person person = new Person();
        person.setId(personId.value());
        person.setPermissions(List.of(USER));

        final PersonId otherId = new PersonId(2L);
        final Person other = new Person();
        other.setId(otherId.value());
        other.setPermissions(List.of(USER, SECOND_STAGE_AUTHORITY));

        final DepartmentMembership personMembership = new DepartmentMembership(personId, 1L, DepartmentMembershipKind.MEMBER, Instant.now(clock));
        final DepartmentMembership otherMembership = new DepartmentMembership(otherId, 1L, DepartmentMembershipKind.SECOND_STAGE_AUTHORITY, Instant.now(clock));

        when(departmentMembershipService.getActiveMembershipsOfPersons(List.of(personId, otherId)))
            .thenReturn(Map.of(personId, List.of(personMembership), otherId, List.of(otherMembership)));

        final boolean actual = sut.hasDepartmentMatch(person, other);
        assertThat(actual).isTrue();
    }

    private static PageableSearchQuery defaultPersonSearchQuery() {
        return new PageableSearchQuery(defaultPageRequest(), "");
    }

    private static PageRequest defaultPageRequest() {
        return PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "firstName"));
    }

    private static Person anyPerson(int id) {
        final Person person = new Person();
        person.setId((long) id);
        return person;
    }

    private static List<Person> anyPersons(int size, int firstPersonId) {
        final List<Integer> personIds = IntStream.range(firstPersonId, firstPersonId + size).boxed().toList();
        return IntStream.range(0, size).boxed().map(index -> anyPerson(personIds.get(index))).toList();
    }
}
