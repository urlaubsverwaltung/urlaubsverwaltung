package org.synyx.urlaubsverwaltung.extension.overtime;

import de.focus_shift.urlaubsverwaltung.extension.api.overtime.OvertimeCreatedEventDTO;
import de.focus_shift.urlaubsverwaltung.extension.api.overtime.OvertimeSettingsActivatedEventDTO;
import de.focus_shift.urlaubsverwaltung.extension.api.overtime.OvertimeSettingsDeactivatedEventDTO;
import de.focus_shift.urlaubsverwaltung.extension.api.overtime.OvertimeUpdatedEventDTO;
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
import org.synyx.urlaubsverwaltung.overtime.OvertimeCreatedEvent;
import org.synyx.urlaubsverwaltung.overtime.OvertimeSettingsActivatedEvent;
import org.synyx.urlaubsverwaltung.overtime.OvertimeSettingsDeactivatedEvent;
import org.synyx.urlaubsverwaltung.overtime.OvertimeUpdatedEvent;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OvertimeEventHandlerExtensionTest {

    @Mock
    private TenantSupplier tenantSupplier;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private OvertimeEventHandlerExtension sut;

    @Nested
    class OvertimeCreatedEventTest {

        @Captor
        private ArgumentCaptor<OvertimeCreatedEventDTO> argumentCaptor;

        @Test
        void ensureEventIsConvertedToDTO() {

            final LocalDate startDate = LocalDate.of(2025, 3, 1);
            final LocalDate endDate = LocalDate.of(2025, 3, 1);
            final Duration duration = Duration.ofHours(2);

            final OvertimeCreatedEvent event = OvertimeCreatedEvent.of(42L, "muster", startDate, endDate, duration);

            when(tenantSupplier.get()).thenReturn("default");

            sut.on(event);

            verify(tenantSupplier).get();
            verify(applicationEventPublisher).publishEvent(argumentCaptor.capture());

            final OvertimeCreatedEventDTO result = argumentCaptor.getValue();

            assertThat(result).isNotNull();
            assertThat(result.id()).isNotNull();
            assertThat(result.sourceId()).isEqualTo(42L);
            assertThat(result.createdAt()).isBeforeOrEqualTo(Instant.now());
            assertThat(result.tenantId()).isEqualTo("default");
            assertThat(result.username()).isEqualTo("muster");
            assertThat(result.startDate()).isEqualTo(startDate);
            assertThat(result.endDate()).isEqualTo(endDate);
            assertThat(result.duration()).isEqualTo(duration);
        }
    }

    @Nested
    class OvertimeUpdatedEventTest {

        @Captor
        private ArgumentCaptor<OvertimeUpdatedEventDTO> argumentCaptor;

        @Test
        void ensureEventIsConvertedToDTO() {

            final LocalDate startDate = LocalDate.of(2025, 3, 1);
            final LocalDate endDate = LocalDate.of(2025, 3, 5);
            final Duration duration = Duration.ofHours(8);

            final OvertimeUpdatedEvent event = OvertimeUpdatedEvent.of(99L, "muster", startDate, endDate, duration);

            when(tenantSupplier.get()).thenReturn("default");

            sut.on(event);

            verify(tenantSupplier).get();
            verify(applicationEventPublisher).publishEvent(argumentCaptor.capture());

            final OvertimeUpdatedEventDTO result = argumentCaptor.getValue();

            assertThat(result).isNotNull();
            assertThat(result.id()).isNotNull();
            assertThat(result.sourceId()).isEqualTo(99L);
            assertThat(result.createdAt()).isBeforeOrEqualTo(Instant.now());
            assertThat(result.tenantId()).isEqualTo("default");
            assertThat(result.username()).isEqualTo("muster");
            assertThat(result.startDate()).isEqualTo(startDate);
            assertThat(result.endDate()).isEqualTo(endDate);
            assertThat(result.duration()).isEqualTo(duration);
        }
    }

    @Nested
    class OvertimeSettingsActivatedEventTest {

        @Captor
        private ArgumentCaptor<OvertimeSettingsActivatedEventDTO> argumentCaptor;

        @Test
        void ensureEventIsConvertedToDTO() {

            final OvertimeSettingsActivatedEvent event = OvertimeSettingsActivatedEvent.of();

            when(tenantSupplier.get()).thenReturn("default");

            sut.on(event);

            verify(tenantSupplier).get();
            verify(applicationEventPublisher).publishEvent(argumentCaptor.capture());

            final OvertimeSettingsActivatedEventDTO result = argumentCaptor.getValue();

            assertThat(result).isNotNull();
            assertThat(result.id()).isNotNull();
            assertThat(result.createdAt()).isBeforeOrEqualTo(Instant.now());
            assertThat(result.tenantId()).isEqualTo("default");
        }
    }

    @Nested
    class OvertimeSettingsDeactivatedEventTest {

        @Captor
        private ArgumentCaptor<OvertimeSettingsDeactivatedEventDTO> argumentCaptor;

        @Test
        void ensureEventIsConvertedToDTO() {

            final OvertimeSettingsDeactivatedEvent event = OvertimeSettingsDeactivatedEvent.of();

            when(tenantSupplier.get()).thenReturn("default");

            sut.on(event);

            verify(tenantSupplier).get();
            verify(applicationEventPublisher).publishEvent(argumentCaptor.capture());

            final OvertimeSettingsDeactivatedEventDTO result = argumentCaptor.getValue();

            assertThat(result).isNotNull();
            assertThat(result.id()).isNotNull();
            assertThat(result.createdAt()).isBeforeOrEqualTo(Instant.now());
            assertThat(result.tenantId()).isEqualTo("default");
        }
    }
}
