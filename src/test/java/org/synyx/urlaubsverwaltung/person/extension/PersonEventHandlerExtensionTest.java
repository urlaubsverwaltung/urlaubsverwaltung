package org.synyx.urlaubsverwaltung.person.extension;

import de.focus_shift.urlaubsverwaltung.extension.api.person.PersonCreatedEventDTO;
import de.focus_shift.urlaubsverwaltung.extension.api.person.PersonDeletedEventDTO;
import de.focus_shift.urlaubsverwaltung.extension.api.person.PersonDisabledEventDTO;
import de.focus_shift.urlaubsverwaltung.extension.api.person.PersonUpdatedEventDTO;
import de.focus_shift.urlaubsverwaltung.extension.api.tenancy.TenantSupplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonCreatedEvent;
import org.synyx.urlaubsverwaltung.person.PersonDeletedEvent;
import org.synyx.urlaubsverwaltung.person.PersonDisabledEvent;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.PersonUpdatedEvent;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

@ExtendWith(MockitoExtension.class)
class PersonEventHandlerExtensionTest {

    @Mock
    private TenantSupplier tenantSupplier;

    @Mock
    private PersonService personService;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private PersonEventHandlerExtension sut;

    private static Person anyPerson() {
        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setId(1L);
        person.setPermissions(Set.of(USER));

        return person;
    }

    @BeforeEach
    void setup() {
        clearInvocations(tenantSupplier);
        clearInvocations(personService);
        clearInvocations(applicationEventPublisher);
    }

    @Nested
    class PersonCreatedEventTest {

        @Captor
        private ArgumentCaptor<PersonCreatedEventDTO> argumentCaptor;

        @Test
        void happyPath() {

            final Person person = anyPerson();
            final PersonCreatedEvent event = new PersonCreatedEvent("source?", person.getId(), person.getNiceName(), person.getUsername(), person.getEmail(), person.isActive());

            when(personService.getPersonByUsername(any())).thenReturn(Optional.of(person));
            when(tenantSupplier.get()).thenReturn("default");

            sut.on(event);

            verify(personService).getPersonByUsername(person.getUsername());
            verify(applicationEventPublisher).publishEvent(argumentCaptor.capture());
            verify(tenantSupplier).get();

            final PersonCreatedEventDTO result = argumentCaptor.getValue();

            assertThat(result).isNotNull();
            assertThat(result.getId()).isNotNull();
            assertThat(result.getCreatedAt()).isBeforeOrEqualTo(Instant.now());
            assertThat(result.getTenantId()).isEqualTo("default");
            assertThat(result.getPersonId()).isEqualTo(1L);
            assertThat(result.getUsername()).isEqualTo(person.getUsername());
            assertThat(result.getLastName()).isEqualTo(person.getLastName());
            assertThat(result.getFirstName()).isEqualTo(person.getFirstName());
            assertThat(result.getEmail()).isEqualTo(person.getEmail());
            assertThat(result.isEnabled()).isTrue();
        }

        @Test
        void ensureNoEventPublishedForUnknownPerson() {

            final PersonCreatedEvent event = new PersonCreatedEvent("source?", 1L, "Marlene Muster", "muster", "muster@example.org", true);

            sut.on(event);

            verify(personService).getPersonByUsername("muster");
            verifyNoInteractions(applicationEventPublisher);
            verifyNoInteractions(tenantSupplier);
        }
    }

    @Nested
    class PersonUpdatedEventTest {

        @Captor
        private ArgumentCaptor<PersonUpdatedEventDTO> argumentCaptor;

        @Test
        void happyPath() {

            final Person person = anyPerson();
            final PersonUpdatedEvent event = new PersonUpdatedEvent("source?", person.getId(), person.getNiceName(), person.getUsername(), person.getEmail(), person.isActive());

            when(personService.getPersonByUsername(any())).thenReturn(Optional.of(person));
            when(tenantSupplier.get()).thenReturn("default");

            sut.on(event);

            verify(personService).getPersonByUsername(person.getUsername());
            verify(tenantSupplier).get();
            verify(applicationEventPublisher).publishEvent(argumentCaptor.capture());

            final PersonUpdatedEventDTO result = argumentCaptor.getValue();

            assertThat(result).isNotNull();
            assertThat(result.getId()).isNotNull();
            assertThat(result.getCreatedAt()).isBeforeOrEqualTo(Instant.now());
            assertThat(result.getTenantId()).isEqualTo("default");
            assertThat(result.getPersonId()).isEqualTo(1L);
            assertThat(result.getUsername()).isEqualTo(person.getUsername());
            assertThat(result.getLastName()).isEqualTo(person.getLastName());
            assertThat(result.getFirstName()).isEqualTo(person.getFirstName());
            assertThat(result.getEmail()).isEqualTo(person.getEmail());
            assertThat(result.isEnabled()).isTrue();
        }

        @Test
        void ensureNoEventPublishedForUnknownPerson() {

            final PersonUpdatedEvent event = new PersonUpdatedEvent("source?", 1L, "Marlene Muster", "muster", "muster@example.org", true);

            sut.on(event);

            verify(personService).getPersonByUsername("muster");
            verifyNoInteractions(applicationEventPublisher);
            verifyNoInteractions(tenantSupplier);
        }
    }

    @Nested
    class PersonDisabledEventTest {

        @Captor
        private ArgumentCaptor<PersonDisabledEventDTO> argumentCaptor;

        @Test
        void happyPath() {

            final Person person = anyPerson();
            final PersonDisabledEvent event = new PersonDisabledEvent("source?", person.getId(), person.getNiceName(), person.getUsername(), person.getEmail());

            when(personService.getPersonByUsername(any())).thenReturn(Optional.of(person));
            when(tenantSupplier.get()).thenReturn("default");

            sut.on(event);

            verify(personService).getPersonByUsername(person.getUsername());
            verify(tenantSupplier).get();
            verify(applicationEventPublisher).publishEvent(argumentCaptor.capture());

            final PersonDisabledEventDTO result = argumentCaptor.getValue();

            assertThat(result).isNotNull();
            assertThat(result.getId()).isNotNull();
            assertThat(result.getCreatedAt()).isBeforeOrEqualTo(Instant.now());
            assertThat(result.getTenantId()).isEqualTo("default");
            assertThat(result.getPersonId()).isEqualTo(1L);
            assertThat(result.getUsername()).isEqualTo(person.getUsername());
            assertThat(result.getLastName()).isEqualTo(person.getLastName());
            assertThat(result.getFirstName()).isEqualTo(person.getFirstName());
            assertThat(result.getEmail()).isEqualTo(person.getEmail());
            assertThat(result.isEnabled()).isFalse();
        }

        @Test
        void ensureNoEventPublishedForUnknownPerson() {

            final PersonDisabledEvent event = new PersonDisabledEvent("source?", 1L, "Marlene Muster", "muster", "muster@example.org");

            sut.on(event);

            verify(personService).getPersonByUsername("muster");
            verifyNoInteractions(applicationEventPublisher);
            verifyNoInteractions(tenantSupplier);
        }
    }

    @Nested
    class PersonDeletedEventTest {

        @Captor
        private ArgumentCaptor<PersonDeletedEventDTO> argumentCaptor;

        @Test
        void happyPath() {

            final Person person = anyPerson();
            final PersonDeletedEvent event = new PersonDeletedEvent(person);

            when(tenantSupplier.get()).thenReturn("default");

            sut.on(event);

            verifyNoInteractions(personService);
            verify(tenantSupplier).get();
            verify(applicationEventPublisher).publishEvent(argumentCaptor.capture());

            final PersonDeletedEventDTO result = argumentCaptor.getValue();

            assertThat(result).isNotNull();
            assertThat(result.getId()).isNotNull();
            assertThat(result.getCreatedAt()).isBeforeOrEqualTo(Instant.now());
            assertThat(result.getTenantId()).isEqualTo("default");
            assertThat(result.getPersonId()).isEqualTo(1L);
            assertThat(result.getUsername()).isEqualTo(person.getUsername());
            assertThat(result.getLastName()).isEqualTo(person.getLastName());
            assertThat(result.getFirstName()).isEqualTo(person.getFirstName());
            assertThat(result.getEmail()).isEqualTo(person.getEmail());
            assertThat(result.isEnabled()).isTrue();
        }
    }
}
