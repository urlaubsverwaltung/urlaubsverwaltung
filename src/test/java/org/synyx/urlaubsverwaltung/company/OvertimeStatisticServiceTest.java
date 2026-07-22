package org.synyx.urlaubsverwaltung.company;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.absence.DateRange;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.overtime.Overtime;
import org.synyx.urlaubsverwaltung.overtime.OvertimeId;
import org.synyx.urlaubsverwaltung.overtime.OvertimeService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonId;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.TestDataCreator.createPerson;
import static org.synyx.urlaubsverwaltung.overtime.OvertimeType.UV_INTERNAL;
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.SECOND_STAGE_AUTHORITY;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

@ExtendWith(MockitoExtension.class)
class OvertimeStatisticServiceTest {

    private OvertimeStatisticService sut;

    @Mock
    private OvertimeService overtimeService;
    @Mock
    private PersonService personService;
    @Mock
    private DepartmentService departmentService;

    @BeforeEach
    void setUp() {
        sut = new OvertimeStatisticService(overtimeService, personService, departmentService);
    }

    @Nested
    class GetOvertimeStatistics {

        @Test
        void ensureOfficeRoleGetsStatisticsForAllPersons() {

            final Person viewer = createPerson("viewer", OFFICE);
            viewer.setId(1L);

            final Person otherPerson = createPerson("other");
            otherPerson.setId(2L);

            when(personService.getActivePersons()).thenReturn(List.of(viewer, otherPerson));

            final Instant from = Instant.parse("2026-01-01T00:00:00.00Z");
            final Instant to = Instant.parse("2026-01-31T00:00:00.00Z");

            final PersonId viewerId = new PersonId(1L);
            final PersonId otherPersonId = new PersonId(2L);
            final Overtime overtime = overtime(viewerId, Duration.ofHours(2));

            when(overtimeService.getOvertimeForPersonsInDateRange(List.of(viewerId, otherPersonId), from, to))
                .thenReturn(Map.of(viewerId, List.of(overtime)));

            final OvertimeStatistic actual = sut.getOvertimeStatistics(viewer, from, to);

            assertThat(actual).isEqualTo(new OvertimeStatistic(Map.of(viewerId, List.of(overtime))));

            verify(departmentService, never()).getManagedActiveMembersOfPerson(viewer);
        }

        @Test
        void ensureBossRoleGetsStatisticsForAllPersons() {

            final Person viewer = createPerson("viewer", BOSS);
            viewer.setId(1L);

            when(personService.getActivePersons()).thenReturn(List.of(viewer));

            final Instant from = Instant.parse("2026-01-01T00:00:00.00Z");
            final Instant to = Instant.parse("2026-01-31T00:00:00.00Z");

            final PersonId viewerId = new PersonId(1L);

            when(overtimeService.getOvertimeForPersonsInDateRange(List.of(viewerId), from, to))
                .thenReturn(Map.of());

            final OvertimeStatistic actual = sut.getOvertimeStatistics(viewer, from, to);

            assertThat(actual).isEqualTo(new OvertimeStatistic(Map.of()));
        }

        @Test
        void ensureDepartmentPrivilegedPersonGetsStatisticsForManagedMembersAndSelf() {

            final Person viewer = createPerson("viewer", DEPARTMENT_HEAD);
            viewer.setId(1L);

            final Person managedMember = createPerson("member");
            managedMember.setId(2L);

            when(departmentService.getManagedActiveMembersOfPerson(viewer)).thenReturn(List.of(managedMember));

            final Instant from = Instant.parse("2026-01-01T00:00:00.00Z");
            final Instant to = Instant.parse("2026-01-31T00:00:00.00Z");

            final PersonId viewerId = new PersonId(1L);
            final PersonId managedMemberId = new PersonId(2L);

            when(overtimeService.getOvertimeForPersonsInDateRange(List.of(managedMemberId, viewerId), from, to))
                .thenReturn(Map.of());

            final OvertimeStatistic actual = sut.getOvertimeStatistics(viewer, from, to);

            assertThat(actual).isEqualTo(new OvertimeStatistic(Map.of()));

            verify(personService, never()).getAllPersons();
        }

        @Test
        void ensureDepartmentPrivilegedPersonManagingSelfIsNotDuplicated() {

            final Person viewer = createPerson("viewer", SECOND_STAGE_AUTHORITY);
            viewer.setId(1L);

            when(departmentService.getManagedActiveMembersOfPerson(viewer)).thenReturn(List.of(viewer));

            final Instant from = Instant.parse("2026-01-01T00:00:00.00Z");
            final Instant to = Instant.parse("2026-01-31T00:00:00.00Z");

            final PersonId viewerId = new PersonId(1L);

            when(overtimeService.getOvertimeForPersonsInDateRange(List.of(viewerId), from, to))
                .thenReturn(Map.of());

            sut.getOvertimeStatistics(viewer, from, to);

            verify(overtimeService).getOvertimeForPersonsInDateRange(List.of(viewerId), from, to);
        }

        @Test
        void ensurePlainPersonGetsStatisticsForSelfOnly() {

            final Person viewer = createPerson("viewer", USER);
            viewer.setId(1L);

            final Instant from = Instant.parse("2026-01-01T00:00:00.00Z");
            final Instant to = Instant.parse("2026-01-31T00:00:00.00Z");

            final PersonId viewerId = new PersonId(1L);
            final Overtime overtime = overtime(viewerId, Duration.ofHours(5));

            when(overtimeService.getOvertimeForPersonsInDateRange(List.of(viewerId), from, to))
                .thenReturn(Map.of(viewerId, List.of(overtime)));

            final OvertimeStatistic actual = sut.getOvertimeStatistics(viewer, from, to);

            assertThat(actual).isEqualTo(new OvertimeStatistic(Map.of(viewerId, List.of(overtime))));

            verify(personService, never()).getAllPersons();
            verify(departmentService, never()).getManagedActiveMembersOfPerson(viewer);
        }
    }

    private static Overtime overtime(PersonId personId, Duration duration) {
        return new Overtime(new OvertimeId(1L), personId,
            new DateRange(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 2)),
            duration, UV_INTERNAL, Instant.parse("2026-01-01T00:00:00.00Z"));
    }
}
