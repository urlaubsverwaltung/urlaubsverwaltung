package org.synyx.urlaubsverwaltung.extension.workingtime;

import de.focus_shift.urlaubsverwaltung.extension.api.tenancy.TenantSupplier;
import de.focus_shift.urlaubsverwaltung.extension.api.workingtime.WorkingTimeConfiguredEventDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeConfiguredEvent;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkingTimeEventHandlerExtensionTest {

    @Mock
    private TenantSupplier tenantSupplier;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private WorkingTimeEventHandlerExtension sut;

    @Captor
    private ArgumentCaptor<WorkingTimeConfiguredEventDTO> argumentCaptor;

    @Test
    void ensureWorkingTimeConfiguredEventIsConvertedToDTO() {

        final LocalDate validFrom = LocalDate.of(2025, 1, 1);
        final List<Integer> workingDays = List.of(1, 2, 3, 4, 5);

        final WorkingTimeConfiguredEvent event = WorkingTimeConfiguredEvent.of("muster", validFrom, workingDays, "GERMANY_BADEN_WUERTTEMBERG");

        when(tenantSupplier.get()).thenReturn("default");

        sut.on(event);

        verify(tenantSupplier).get();
        verify(applicationEventPublisher).publishEvent(argumentCaptor.capture());

        final WorkingTimeConfiguredEventDTO result = argumentCaptor.getValue();

        assertThat(result).isNotNull();
        assertThat(result.id()).isNotNull();
        assertThat(result.createdAt()).isBeforeOrEqualTo(Instant.now());
        assertThat(result.tenantId()).isEqualTo("default");
        assertThat(result.username()).isEqualTo("muster");
        assertThat(result.validFrom()).isEqualTo(validFrom);
        assertThat(result.workingDays()).containsExactly("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY");
        assertThat(result.federalState()).isEqualTo("GERMANY_BADEN_WUERTTEMBERG");
    }

    @Test
    void ensureWorkingTimeConfiguredEventWithNullFederalStateIsConvertedToDTO() {

        final LocalDate validFrom = LocalDate.of(2025, 1, 1);
        final List<Integer> workingDays = List.of(1, 2, 3, 4, 5);

        final WorkingTimeConfiguredEvent event = WorkingTimeConfiguredEvent.of("muster", validFrom, workingDays, null);

        when(tenantSupplier.get()).thenReturn("default");

        sut.on(event);

        verify(applicationEventPublisher).publishEvent(argumentCaptor.capture());

        final WorkingTimeConfiguredEventDTO result = argumentCaptor.getValue();

        assertThat(result.federalState()).isNull();
    }
}
