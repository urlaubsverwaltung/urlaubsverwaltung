package org.synyx.urlaubsverwaltung.extension.calendar;

import de.focus_shift.urlaubsverwaltung.extension.api.calendar.CompanyCalendarDisabledEventDTO;
import de.focus_shift.urlaubsverwaltung.extension.api.calendar.CompanyCalendarEnabledEventDTO;
import de.focus_shift.urlaubsverwaltung.extension.api.calendar.PersonalCalendarCreatedEventDTO;
import de.focus_shift.urlaubsverwaltung.extension.api.calendar.PersonalCalendarDeletedEventDTO;
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
import org.synyx.urlaubsverwaltung.calendar.CompanyCalendarDisabledEvent;
import org.synyx.urlaubsverwaltung.calendar.CompanyCalendarEnabledEvent;
import org.synyx.urlaubsverwaltung.calendar.PersonalCalendarCreatedEvent;
import org.synyx.urlaubsverwaltung.calendar.PersonalCalendarDeletedEvent;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CalendarEventHandlerExtensionTest {

    @Mock
    private TenantSupplier tenantSupplier;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private CalendarEventHandlerExtension sut;

    @Nested
    class PersonalCalendarCreatedEventTest {

        @Captor
        private ArgumentCaptor<PersonalCalendarCreatedEventDTO> argumentCaptor;

        @Test
        void ensureEventIsConvertedToDTO() {

            final PersonalCalendarCreatedEvent event = PersonalCalendarCreatedEvent.of("muster");

            when(tenantSupplier.get()).thenReturn("default");

            sut.on(event);

            verify(tenantSupplier).get();
            verify(applicationEventPublisher).publishEvent(argumentCaptor.capture());

            final PersonalCalendarCreatedEventDTO result = argumentCaptor.getValue();

            assertThat(result).isNotNull();
            assertThat(result.id()).isNotNull();
            assertThat(result.createdAt()).isBeforeOrEqualTo(Instant.now());
            assertThat(result.tenantId()).isEqualTo("default");
            assertThat(result.username()).isEqualTo("muster");
        }
    }

    @Nested
    class PersonalCalendarDeletedEventTest {

        @Captor
        private ArgumentCaptor<PersonalCalendarDeletedEventDTO> argumentCaptor;

        @Test
        void ensureEventIsConvertedToDTO() {

            final PersonalCalendarDeletedEvent event = PersonalCalendarDeletedEvent.of("muster");

            when(tenantSupplier.get()).thenReturn("default");

            sut.on(event);

            verify(tenantSupplier).get();
            verify(applicationEventPublisher).publishEvent(argumentCaptor.capture());

            final PersonalCalendarDeletedEventDTO result = argumentCaptor.getValue();

            assertThat(result).isNotNull();
            assertThat(result.id()).isNotNull();
            assertThat(result.createdAt()).isBeforeOrEqualTo(Instant.now());
            assertThat(result.tenantId()).isEqualTo("default");
            assertThat(result.username()).isEqualTo("muster");
        }
    }

    @Nested
    class CompanyCalendarEnabledEventTest {

        @Captor
        private ArgumentCaptor<CompanyCalendarEnabledEventDTO> argumentCaptor;

        @Test
        void ensureEventIsConvertedToDTO() {

            final CompanyCalendarEnabledEvent event = CompanyCalendarEnabledEvent.of();

            when(tenantSupplier.get()).thenReturn("default");

            sut.on(event);

            verify(tenantSupplier).get();
            verify(applicationEventPublisher).publishEvent(argumentCaptor.capture());

            final CompanyCalendarEnabledEventDTO result = argumentCaptor.getValue();

            assertThat(result).isNotNull();
            assertThat(result.id()).isNotNull();
            assertThat(result.createdAt()).isBeforeOrEqualTo(Instant.now());
            assertThat(result.tenantId()).isEqualTo("default");
        }
    }

    @Nested
    class CompanyCalendarDisabledEventTest {

        @Captor
        private ArgumentCaptor<CompanyCalendarDisabledEventDTO> argumentCaptor;

        @Test
        void ensureEventIsConvertedToDTO() {

            final CompanyCalendarDisabledEvent event = CompanyCalendarDisabledEvent.of();

            when(tenantSupplier.get()).thenReturn("default");

            sut.on(event);

            verify(tenantSupplier).get();
            verify(applicationEventPublisher).publishEvent(argumentCaptor.capture());

            final CompanyCalendarDisabledEventDTO result = argumentCaptor.getValue();

            assertThat(result).isNotNull();
            assertThat(result.id()).isNotNull();
            assertThat(result.createdAt()).isBeforeOrEqualTo(Instant.now());
            assertThat(result.tenantId()).isEqualTo("default");
        }
    }
}
