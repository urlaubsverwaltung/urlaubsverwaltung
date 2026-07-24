package org.synyx.urlaubsverwaltung.person;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.synyx.urlaubsverwaltung.person.Role.INACTIVE;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

@ExtendWith(MockitoExtension.class)
class PersonActivePeriodEventListenerTest {

    @InjectMocks
    private PersonActivePeriodEventListener sut;

    @Mock
    private PersonActivePeriodServiceImpl personActivePeriodService;

    @Nested
    class OnPersonCreatedEvent {

        @Test
        void ensureOpensPeriodWhenPersonIsCreatedActive() {

            final PersonCreatedEvent event = new PersonCreatedEvent(this, 1L, "niceName", "username", "email", true);

            sut.on(event);

            verify(personActivePeriodService).openPeriod(new PersonId(1L), Instant.ofEpochMilli(event.getTimestamp()));
        }

        @Test
        void ensureDoesNotOpenPeriodWhenPersonIsCreatedInactive() {

            final PersonCreatedEvent event = new PersonCreatedEvent(this, 1L, "niceName", "username", "email", false);

            sut.on(event);

            verify(personActivePeriodService, never()).openPeriod(new PersonId(1L), Instant.ofEpochMilli(event.getTimestamp()));
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
}
