package org.synyx.urlaubsverwaltung.extension.department;

import de.focus_shift.urlaubsverwaltung.extension.api.department.DepartmentCreatedEventDTO;
import de.focus_shift.urlaubsverwaltung.extension.api.department.DepartmentDeletedEventDTO;
import de.focus_shift.urlaubsverwaltung.extension.api.department.DepartmentHeadAssignedEventDTO;
import de.focus_shift.urlaubsverwaltung.extension.api.department.DepartmentHeadUnassignedEventDTO;
import de.focus_shift.urlaubsverwaltung.extension.api.department.DepartmentMemberAssignedEventDTO;
import de.focus_shift.urlaubsverwaltung.extension.api.department.DepartmentMemberUnassignedEventDTO;
import de.focus_shift.urlaubsverwaltung.extension.api.department.DepartmentUpdatedEventDTO;
import de.focus_shift.urlaubsverwaltung.extension.api.tenancy.TenantSupplier;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.synyx.urlaubsverwaltung.department.DepartmentCreatedEvent;
import org.synyx.urlaubsverwaltung.department.DepartmentDeletedEvent;
import org.synyx.urlaubsverwaltung.department.DepartmentHeadAssignedEvent;
import org.synyx.urlaubsverwaltung.department.DepartmentHeadUnassignedEvent;
import org.synyx.urlaubsverwaltung.department.DepartmentMemberAssignedEvent;
import org.synyx.urlaubsverwaltung.department.DepartmentMemberUnassignedEvent;
import org.synyx.urlaubsverwaltung.department.DepartmentUpdatedEvent;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DepartmentEventHandlerExtensionTest {

    @Mock
    private TenantSupplier tenantSupplier;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private DepartmentEventHandlerExtension sut;

    @Nested
    class DepartmentCreatedEventTest {

        @Captor
        private ArgumentCaptor<DepartmentCreatedEventDTO> argumentCaptor;

        @Test
        void ensureEventIsConvertedToDTO() {

            final DepartmentCreatedEvent event = new DepartmentCreatedEvent(
                UUID.randomUUID(), Instant.now(), 42L, "Engineering", 5
            );

            when(tenantSupplier.get()).thenReturn("default");

            sut.on(event);

            verify(tenantSupplier).get();
            verify(applicationEventPublisher).publishEvent(argumentCaptor.capture());

            final DepartmentCreatedEventDTO result = argumentCaptor.getValue();

            assertThat(result).isNotNull();
            assertThat(result.id()).isNotNull();
            assertThat(result.createdAt()).isBeforeOrEqualTo(Instant.now());
            assertThat(result.tenantId()).isEqualTo("default");
            assertThat(result.sourceId()).isEqualTo(42L);
            assertThat(result.departmentName()).isEqualTo("Engineering");
            assertThat(result.memberCount()).isEqualTo(5);
        }
    }

    @Nested
    class DepartmentUpdatedEventTest {

        @Captor
        private ArgumentCaptor<DepartmentUpdatedEventDTO> argumentCaptor;

        @Test
        void ensureEventIsConvertedToDTO() {

            final DepartmentUpdatedEvent event = new DepartmentUpdatedEvent(
                UUID.randomUUID(), Instant.now(), 42L, "Engineering", 10
            );

            when(tenantSupplier.get()).thenReturn("default");

            sut.on(event);

            verify(tenantSupplier).get();
            verify(applicationEventPublisher).publishEvent(argumentCaptor.capture());

            final DepartmentUpdatedEventDTO result = argumentCaptor.getValue();

            assertThat(result).isNotNull();
            assertThat(result.id()).isNotNull();
            assertThat(result.createdAt()).isBeforeOrEqualTo(Instant.now());
            assertThat(result.tenantId()).isEqualTo("default");
            assertThat(result.sourceId()).isEqualTo(42L);
            assertThat(result.departmentName()).isEqualTo("Engineering");
            assertThat(result.memberCount()).isEqualTo(10);
        }
    }

    @Nested
    class DepartmentDeletedEventTest {

        @Captor
        private ArgumentCaptor<DepartmentDeletedEventDTO> argumentCaptor;

        @Test
        void ensureEventIsConvertedToDTO() {

            final DepartmentDeletedEvent event = DepartmentDeletedEvent.of(42L);

            when(tenantSupplier.get()).thenReturn("default");

            sut.on(event);

            verify(tenantSupplier).get();
            verify(applicationEventPublisher).publishEvent(argumentCaptor.capture());

            final DepartmentDeletedEventDTO result = argumentCaptor.getValue();

            assertThat(result).isNotNull();
            assertThat(result.id()).isNotNull();
            assertThat(result.createdAt()).isBeforeOrEqualTo(Instant.now());
            assertThat(result.tenantId()).isEqualTo("default");
            assertThat(result.sourceId()).isEqualTo(42L);
        }
    }

    @Nested
    class DepartmentMemberAssignedEventTest {

        @Captor
        private ArgumentCaptor<DepartmentMemberAssignedEventDTO> argumentCaptor;

        @Test
        void ensureEventIsConvertedToDTO() {

            final DepartmentMemberAssignedEvent event = DepartmentMemberAssignedEvent.of(42L, "muster");

            when(tenantSupplier.get()).thenReturn("default");

            sut.on(event);

            verify(tenantSupplier).get();
            verify(applicationEventPublisher).publishEvent(argumentCaptor.capture());

            final DepartmentMemberAssignedEventDTO result = argumentCaptor.getValue();

            assertThat(result).isNotNull();
            assertThat(result.id()).isNotNull();
            assertThat(result.createdAt()).isBeforeOrEqualTo(Instant.now());
            assertThat(result.tenantId()).isEqualTo("default");
            assertThat(result.sourceId()).isEqualTo(42L);
            assertThat(result.username()).isEqualTo("muster");
        }
    }

    @Nested
    class DepartmentMemberUnassignedEventTest {

        @Captor
        private ArgumentCaptor<DepartmentMemberUnassignedEventDTO> argumentCaptor;

        @Test
        void ensureEventIsConvertedToDTO() {

            final DepartmentMemberUnassignedEvent event = DepartmentMemberUnassignedEvent.of(42L, "muster");

            when(tenantSupplier.get()).thenReturn("default");

            sut.on(event);

            verify(tenantSupplier).get();
            verify(applicationEventPublisher).publishEvent(argumentCaptor.capture());

            final DepartmentMemberUnassignedEventDTO result = argumentCaptor.getValue();

            assertThat(result).isNotNull();
            assertThat(result.id()).isNotNull();
            assertThat(result.createdAt()).isBeforeOrEqualTo(Instant.now());
            assertThat(result.tenantId()).isEqualTo("default");
            assertThat(result.sourceId()).isEqualTo(42L);
            assertThat(result.username()).isEqualTo("muster");
        }
    }

    @Nested
    class DepartmentHeadAssignedEventTest {

        @Captor
        private ArgumentCaptor<DepartmentHeadAssignedEventDTO> argumentCaptor;

        @Test
        void ensureEventIsConvertedToDTO() {

            final DepartmentHeadAssignedEvent event = DepartmentHeadAssignedEvent.of(42L, "boss");

            when(tenantSupplier.get()).thenReturn("default");

            sut.on(event);

            verify(tenantSupplier).get();
            verify(applicationEventPublisher).publishEvent(argumentCaptor.capture());

            final DepartmentHeadAssignedEventDTO result = argumentCaptor.getValue();

            assertThat(result).isNotNull();
            assertThat(result.id()).isNotNull();
            assertThat(result.createdAt()).isBeforeOrEqualTo(Instant.now());
            assertThat(result.tenantId()).isEqualTo("default");
            assertThat(result.sourceId()).isEqualTo(42L);
            assertThat(result.departmentHeadUsername()).isEqualTo("boss");
        }
    }

    @Nested
    class DepartmentHeadUnassignedEventTest {

        @Captor
        private ArgumentCaptor<DepartmentHeadUnassignedEventDTO> argumentCaptor;

        @Test
        void ensureEventIsConvertedToDTO() {

            final DepartmentHeadUnassignedEvent event = DepartmentHeadUnassignedEvent.of(42L, "boss");

            when(tenantSupplier.get()).thenReturn("default");

            sut.on(event);

            verify(tenantSupplier).get();
            verify(applicationEventPublisher).publishEvent(argumentCaptor.capture());

            final DepartmentHeadUnassignedEventDTO result = argumentCaptor.getValue();

            assertThat(result).isNotNull();
            assertThat(result.id()).isNotNull();
            assertThat(result.createdAt()).isBeforeOrEqualTo(Instant.now());
            assertThat(result.tenantId()).isEqualTo("default");
            assertThat(result.sourceId()).isEqualTo(42L);
            assertThat(result.departmentHeadUsername()).isEqualTo("boss");
        }
    }
}
