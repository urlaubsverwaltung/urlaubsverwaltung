package org.synyx.urlaubsverwaltung.person;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.application.application.Application;
import org.synyx.urlaubsverwaltung.application.application.ApplicationAllowedEvent;
import org.synyx.urlaubsverwaltung.application.application.ApplicationAppliedEvent;
import org.synyx.urlaubsverwaltung.overtime.OvertimeCreatedEvent;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteCreatedEvent;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeConfiguredEvent;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.person.Role.INACTIVE;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

@ExtendWith(MockitoExtension.class)
class PersonActivePeriodEventListenerTest {

    @InjectMocks
    private PersonActivePeriodEventListener sut;

    @Mock
    private PersonActivePeriodServiceImpl personActivePeriodService;

    @Mock
    private PersonService personService;

    @Nested
    class OnPersonCreatedEvent {

        @Test
        void ensureOpensPeriodWhenPersonIsCreatedActive() {

            final Instant createdAt = Instant.parse("2021-01-01T00:00:00Z");
            final PersonCreatedEvent event = new PersonCreatedEvent(this, 1L, "niceName", "username", "email", true, createdAt);

            sut.on(event);

            verify(personActivePeriodService).openPeriod(new PersonId(1L), createdAt);
        }

        @Test
        void ensureDoesNotOpenPeriodWhenPersonIsCreatedInactive() {

            final Instant createdAt = Instant.parse("2021-01-01T00:00:00Z");
            final PersonCreatedEvent event = new PersonCreatedEvent(this, 1L, "niceName", "username", "email", false, createdAt);

            sut.on(event);

            verify(personActivePeriodService, never()).openPeriod(new PersonId(1L), createdAt);
        }
    }

    @Nested
    class OnPersonPermissionsChangedEvent {

        @Test
        void ensureClosesOpenPeriodWhenInactiveRoleIsGranted() {

            final PersonPermissionsChangedEvent event = PersonPermissionsChangedEvent.of(
                person(1L), List.of(USER), List.of(USER, INACTIVE)
            );

            sut.on(event);

            verify(personActivePeriodService).closeOpenPeriod(new PersonId(1L), event.createdAt());
        }

        @Test
        void ensureOpensNewPeriodWhenInactiveRoleIsRevoked() {

            final PersonPermissionsChangedEvent event = PersonPermissionsChangedEvent.of(
                person(1L), List.of(USER, INACTIVE), List.of(USER)
            );

            sut.on(event);

            verify(personActivePeriodService).openPeriod(new PersonId(1L), event.createdAt());
        }

        @Test
        void ensureDoesNothingWhenInactiveRoleIsUnaffected() {

            final PersonPermissionsChangedEvent event = PersonPermissionsChangedEvent.of(
                person(1L), List.of(USER), List.of(USER, Role.OFFICE)
            );

            sut.on(event);

            verify(personActivePeriodService, never()).openPeriod(new PersonId(1L), event.createdAt());
            verify(personActivePeriodService, never()).closeOpenPeriod(new PersonId(1L), event.createdAt());
        }

        private Person person(long id) {
            final Person person = new Person("username", "lastName", "firstName", "email@example.org");
            person.setId(id);
            return person;
        }
    }

    @Nested
    class OnOvertimeCreatedEvent {

        @Test
        void ensureInsertsGapFillingPeriodWhenActivityPredatesEarliestKnownPeriod() {

            final Instant earliestValidFrom = Instant.parse("2026-06-01T00:00:00Z");
            final Instant createdAt = Instant.parse("2026-01-15T00:00:00Z");
            final LocalDate startDate = LocalDate.of(2026, 1, 1);
            final Instant expectedValidFrom = Instant.parse("2026-01-01T00:00:00Z");

            when(personService.getPersonByUsername("username")).thenReturn(Optional.of(person(1L)));
            when(personActivePeriodService.getActivePeriods(new PersonId(1L)))
                .thenReturn(List.of(new PersonActivePeriod(new PersonId(1L), earliestValidFrom)));

            final OvertimeCreatedEvent event = new OvertimeCreatedEvent(
                UUID.randomUUID(), createdAt, 42L, "username", startDate, LocalDate.of(2026, 1, 2), Duration.ofHours(5)
            );

            sut.on(event);

            verify(personActivePeriodService).insertPeriod(new PersonId(1L), expectedValidFrom, earliestValidFrom);
        }

        @Test
        void ensureDoesNotInsertPeriodWhenActivityIsWithinKnownPeriod() {

            final Instant earliestValidFrom = Instant.parse("2026-01-01T00:00:00Z");
            final Instant createdAt = Instant.parse("2025-01-01T00:00:00Z");
            final LocalDate startDate = LocalDate.of(2026, 6, 1);

            when(personService.getPersonByUsername("username")).thenReturn(Optional.of(person(1L)));
            when(personActivePeriodService.getActivePeriods(new PersonId(1L)))
                .thenReturn(List.of(new PersonActivePeriod(new PersonId(1L), earliestValidFrom)));

            final OvertimeCreatedEvent event = new OvertimeCreatedEvent(
                UUID.randomUUID(), createdAt, 42L, "username", startDate, LocalDate.of(2026, 6, 2), Duration.ofHours(5)
            );

            sut.on(event);

            verify(personActivePeriodService, never()).insertPeriod(any(), any(), any());
        }

        @Test
        void ensureDoesNotInsertPeriodWhenPersonHasNoActivePeriod() {

            when(personService.getPersonByUsername("username")).thenReturn(Optional.of(person(1L)));
            when(personActivePeriodService.getActivePeriods(new PersonId(1L))).thenReturn(List.of());

            final OvertimeCreatedEvent event = new OvertimeCreatedEvent(
                UUID.randomUUID(), Instant.parse("2026-07-31T00:00:00Z"), 42L, "username", LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 2), Duration.ofHours(5)
            );

            sut.on(event);
            verify(personActivePeriodService, never()).insertPeriod(any(), any(), any());
        }

        @Test
        void ensureThrowsWhenPersonIsNotFoundByUsername() {

            when(personService.getPersonByUsername("username")).thenReturn(Optional.empty());

            final OvertimeCreatedEvent event = new OvertimeCreatedEvent(
                UUID.randomUUID(), Instant.parse("2026-07-31T00:00:00Z"), 42L, "username", LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 2), Duration.ofHours(5)
            );

            assertThatThrownBy(() -> sut.on(event)).isInstanceOf(IllegalStateException.class);
        }

        private Person person(long id) {
            final Person person = new Person("username", "lastName", "firstName", "email@example.org");
            person.setId(id);
            return person;
        }
    }

    @Nested
    class OnWorkingTimeConfiguredEvent {

        @Test
        void ensureInsertsGapFillingPeriodWhenActivityPredatesEarliestKnownPeriod() {

            final Instant earliestValidFrom = Instant.parse("2026-06-01T00:00:00Z");
            final Instant createdAt = Instant.parse("2026-01-15T00:00:00Z");
            final LocalDate validFrom = LocalDate.of(2026, 1, 1);
            final Instant expectedValidFrom = Instant.parse("2026-01-01T00:00:00Z");

            when(personService.getPersonByUsername("username")).thenReturn(Optional.of(person(1L)));
            when(personActivePeriodService.getActivePeriods(new PersonId(1L)))
                .thenReturn(List.of(new PersonActivePeriod(new PersonId(1L), earliestValidFrom)));

            final WorkingTimeConfiguredEvent event = new WorkingTimeConfiguredEvent(
                UUID.randomUUID(), createdAt, "username", validFrom, List.of(1, 2, 3, 4, 5), "GERMANY_BADEN_WUERTTEMBERG"
            );

            sut.on(event);

            verify(personActivePeriodService).insertPeriod(new PersonId(1L), expectedValidFrom, earliestValidFrom);
        }

        @Test
        void ensureDoesNotInsertPeriodWhenActivityIsWithinKnownPeriod() {

            final Instant earliestValidFrom = Instant.parse("2026-01-01T00:00:00Z");
            final Instant createdAt = Instant.parse("2025-01-01T00:00:00Z");
            final LocalDate validFrom = LocalDate.of(2026, 6, 1);

            when(personService.getPersonByUsername("username")).thenReturn(Optional.of(person(1L)));
            when(personActivePeriodService.getActivePeriods(new PersonId(1L)))
                .thenReturn(List.of(new PersonActivePeriod(new PersonId(1L), earliestValidFrom)));

            final WorkingTimeConfiguredEvent event = new WorkingTimeConfiguredEvent(
                UUID.randomUUID(), createdAt, "username", validFrom, List.of(1, 2, 3, 4, 5), "GERMANY_BADEN_WUERTTEMBERG"
            );

            sut.on(event);

            verify(personActivePeriodService, never()).insertPeriod(any(), any(), any());
        }

        @Test
        void ensureDoesNotInsertWhenPersonHasNoActivePeriod() {

            when(personService.getPersonByUsername("username")).thenReturn(Optional.of(person(1L)));
            when(personActivePeriodService.getActivePeriods(new PersonId(1L))).thenReturn(List.of());

            final WorkingTimeConfiguredEvent event = new WorkingTimeConfiguredEvent(
                UUID.randomUUID(), Instant.parse("2026-07-31T00:00:00Z"), "username",
                LocalDate.of(2026, 6, 1), List.of(1, 2, 3, 4, 5), "GERMANY_BADEN_WUERTTEMBERG"
            );

            sut.on(event);
            verify(personActivePeriodService, never()).insertPeriod(any(), any(), any());
        }

        @Test
        void ensureThrowsWhenPersonIsNotFoundByUsername() {

            when(personService.getPersonByUsername("username")).thenReturn(Optional.empty());

            final WorkingTimeConfiguredEvent event = new WorkingTimeConfiguredEvent(
                UUID.randomUUID(), Instant.parse("2026-07-31T00:00:00Z"), "username",
                LocalDate.of(2026, 6, 1), List.of(1, 2, 3, 4, 5), "GERMANY_BADEN_WUERTTEMBERG"
            );

            assertThatThrownBy(() -> sut.on(event)).isInstanceOf(IllegalStateException.class);
        }

        private Person person(long id) {
            final Person person = new Person("username", "lastName", "firstName", "email@example.org");
            person.setId(id);
            return person;
        }
    }

    @Nested
    class OnSickNoteCreatedEvent {

        @Test
        void ensureInsertsGapFillingPeriodWhenActivityPredatesEarliestKnownPeriod() {

            final Instant earliestValidFrom = Instant.parse("2026-06-01T00:00:00Z");
            final Instant createdAt = Instant.parse("2026-01-15T00:00:00Z");
            final LocalDate startDate = LocalDate.of(2026, 1, 1);
            final Instant expectedValidFrom = Instant.parse("2026-01-01T00:00:00Z");

            final Person person = person(1L);
            final SickNote sickNote = SickNote.builder().person(person).startDate(startDate).build();

            when(personActivePeriodService.getActivePeriods(new PersonId(1L)))
                .thenReturn(List.of(new PersonActivePeriod(new PersonId(1L), earliestValidFrom)));

            final SickNoteCreatedEvent event = new SickNoteCreatedEvent(UUID.randomUUID(), createdAt, sickNote);

            sut.on(event);

            verify(personActivePeriodService).insertPeriod(new PersonId(1L), expectedValidFrom, earliestValidFrom);
        }

        @Test
        void ensureDoesNotInsertPeriodWhenActivityIsWithinKnownPeriod() {

            final Instant earliestValidFrom = Instant.parse("2026-01-01T00:00:00Z");
            final Instant createdAt = Instant.parse("2025-01-01T00:00:00Z");
            final LocalDate startDate = LocalDate.of(2026, 6, 1);

            final Person person = person(1L);
            final SickNote sickNote = SickNote.builder().person(person).startDate(startDate).build();

            when(personActivePeriodService.getActivePeriods(new PersonId(1L)))
                .thenReturn(List.of(new PersonActivePeriod(new PersonId(1L), earliestValidFrom)));

            final SickNoteCreatedEvent event = new SickNoteCreatedEvent(UUID.randomUUID(), createdAt, sickNote);

            sut.on(event);

            verify(personActivePeriodService, never()).insertPeriod(any(), any(), any());
        }

        @Test
        void ensureDoesNotInsertPeroidWhenPersonHasNoActivePeriod() {

            final Person person = person(1L);
            final SickNote sickNote = SickNote.builder().person(person).startDate(LocalDate.of(2026, 6, 1)).build();

            when(personActivePeriodService.getActivePeriods(new PersonId(1L))).thenReturn(List.of());

            final SickNoteCreatedEvent event = new SickNoteCreatedEvent(UUID.randomUUID(), Instant.parse("2026-07-31T00:00:00Z"), sickNote);

            sut.on(event);
            verify(personActivePeriodService, never()).insertPeriod(any(), any(), any());
        }

        private Person person(long id) {
            final Person person = new Person("username", "lastName", "firstName", "email@example.org");
            person.setId(id);
            return person;
        }
    }

    @Nested
    class OnApplicationAppliedEvent {

        @Test
        void ensureInsertsGapFillingPeriodWhenActivityPredatesEarliestKnownPeriod() {

            final Instant earliestValidFrom = Instant.parse("2026-06-01T00:00:00Z");
            final Instant createdAt = Instant.parse("2026-01-15T00:00:00Z");
            final LocalDate startDate = LocalDate.of(2026, 1, 1);
            final Instant expectedValidFrom = Instant.parse("2026-01-01T00:00:00Z");

            final Person person = person(1L);
            final Application application = new Application();
            application.setPerson(person);
            application.setStartDate(startDate);

            when(personActivePeriodService.getActivePeriods(new PersonId(1L)))
                .thenReturn(List.of(new PersonActivePeriod(new PersonId(1L), earliestValidFrom)));

            final ApplicationAppliedEvent event = new ApplicationAppliedEvent(UUID.randomUUID(), createdAt, application);

            sut.on(event);

            verify(personActivePeriodService).insertPeriod(new PersonId(1L), expectedValidFrom, earliestValidFrom);
        }

        @Test
        void ensureDoesNotInsertPeriodWhenActivityIsWithinKnownPeriod() {

            final Instant earliestValidFrom = Instant.parse("2026-01-01T00:00:00Z");
            final Instant createdAt = Instant.parse("2025-01-01T00:00:00Z");
            final LocalDate startDate = LocalDate.of(2026, 6, 1);

            final Person person = person(1L);
            final Application application = new Application();
            application.setPerson(person);
            application.setStartDate(startDate);

            when(personActivePeriodService.getActivePeriods(new PersonId(1L)))
                .thenReturn(List.of(new PersonActivePeriod(new PersonId(1L), earliestValidFrom)));

            final ApplicationAppliedEvent event = new ApplicationAppliedEvent(UUID.randomUUID(), createdAt, application);

            sut.on(event);

            verify(personActivePeriodService, never()).insertPeriod(any(), any(), any());
        }

        @Test
        void ensureDoesNotInsertPeriodWhenPersonHasNoActivePeriod() {

            final Person person = person(1L);
            final Application application = new Application();
            application.setPerson(person);
            application.setStartDate(LocalDate.of(2026, 6, 1));

            when(personActivePeriodService.getActivePeriods(new PersonId(1L))).thenReturn(List.of());

            final ApplicationAppliedEvent event = new ApplicationAppliedEvent(UUID.randomUUID(), Instant.parse("2026-07-31T00:00:00Z"), application);

            sut.on(event);

            verify(personActivePeriodService, never()).insertPeriod(any(), any(), any());
        }

        private Person person(long id) {
            final Person person = new Person("username", "lastName", "firstName", "email@example.org");
            person.setId(id);
            return person;
        }
    }

    @Nested
    class OnApplicationAllowedEvent {

        @Test
        void ensureInsertsGapFillingPeriodWhenActivityPredatesEarliestKnownPeriod() {

            final Instant earliestValidFrom = Instant.parse("2026-06-01T00:00:00Z");
            final Instant createdAt = Instant.parse("2026-01-15T00:00:00Z");
            final LocalDate startDate = LocalDate.of(2026, 1, 1);
            final Instant expectedValidFrom = Instant.parse("2026-01-01T00:00:00Z");

            final Person person = person(1L);
            final Application application = new Application();
            application.setPerson(person);
            application.setStartDate(startDate);

            when(personActivePeriodService.getActivePeriods(new PersonId(1L)))
                .thenReturn(List.of(new PersonActivePeriod(new PersonId(1L), earliestValidFrom)));

            final ApplicationAllowedEvent event = new ApplicationAllowedEvent(UUID.randomUUID(), createdAt, application);

            sut.on(event);

            verify(personActivePeriodService).insertPeriod(new PersonId(1L), expectedValidFrom, earliestValidFrom);
        }

        @Test
        void ensureDoesNotInsertPeriodWhenActivityIsWithinKnownPeriod() {

            final Instant earliestValidFrom = Instant.parse("2026-01-01T00:00:00Z");
            final Instant createdAt = Instant.parse("2025-01-01T00:00:00Z");
            final LocalDate startDate = LocalDate.of(2026, 6, 1);

            final Person person = person(1L);
            final Application application = new Application();
            application.setPerson(person);
            application.setStartDate(startDate);

            when(personActivePeriodService.getActivePeriods(new PersonId(1L)))
                .thenReturn(List.of(new PersonActivePeriod(new PersonId(1L), earliestValidFrom)));

            final ApplicationAllowedEvent event = new ApplicationAllowedEvent(UUID.randomUUID(), createdAt, application);

            sut.on(event);

            verify(personActivePeriodService, never()).insertPeriod(any(), any(), any());
        }

        @Test
        void ensureDoesNotInsertPeriodWhenPersonHasNoActivePeriod() {

            final Person person = person(1L);
            final Application application = new Application();
            application.setPerson(person);
            application.setStartDate(LocalDate.of(2026, 6, 1));

            when(personActivePeriodService.getActivePeriods(new PersonId(1L))).thenReturn(List.of());

            final ApplicationAllowedEvent event = new ApplicationAllowedEvent(UUID.randomUUID(), Instant.parse("2026-07-31T00:00:00Z"), application);

            sut.on(event);

            verify(personActivePeriodService, never()).insertPeriod(any(), any(), any());
        }

        private Person person(long id) {
            final Person person = new Person("username", "lastName", "firstName", "email@example.org");
            person.setId(id);
            return person;
        }
    }
}
