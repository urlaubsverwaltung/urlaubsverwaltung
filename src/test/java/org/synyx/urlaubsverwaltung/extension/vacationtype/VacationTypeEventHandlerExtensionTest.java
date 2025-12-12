package org.synyx.urlaubsverwaltung.extension.vacationtype;


import de.focus_shift.urlaubsverwaltung.extension.api.tenancy.TenantSupplier;
import de.focus_shift.urlaubsverwaltung.extension.api.vacationtype.VacationTypeUpdatedEventDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.support.StaticMessageSource;
import org.synyx.urlaubsverwaltung.application.vacationtype.CustomVacationType;
import org.synyx.urlaubsverwaltung.application.vacationtype.ProvidedVacationType;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationType;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeColor;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeLabel;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeUpdatedEvent;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import static java.util.Locale.forLanguageTag;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.HOLIDAY;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeColor.YELLOW;

@ExtendWith(MockitoExtension.class)
class VacationTypeEventHandlerExtensionTest {

    @Mock
    private TenantSupplier tenantSupplier;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Captor
    private ArgumentCaptor<VacationTypeUpdatedEventDTO> eventCaptor;

    private VacationTypeEventHandlerExtension vacationTypeEventHandlerExtension;

    @BeforeEach
    void setUp() {
        vacationTypeEventHandlerExtension = new VacationTypeEventHandlerExtension(tenantSupplier, applicationEventPublisher);
    }

    @Test
    void handlesCreatedEvent() {

        final String tenant = "default";
        when(tenantSupplier.get()).thenReturn(tenant);

        final CustomVacationType vacationType = CustomVacationType.builder(new StaticMessageSource())
            .color(YELLOW)
            .id(42L)
            .active(true)
            .requiresApprovalToApply(false)
            .requiresApprovalToCancel(false)
            .visibleToEveryone(true)
            .labels(List.of(
                new VacationTypeLabel(Locale.GERMAN, "vacation-type-DE"),
                new VacationTypeLabel(Locale.ENGLISH, "vacation-type-EN")
            ))
            .category(HOLIDAY)
            .build();

        vacationTypeEventHandlerExtension.onVacationTypeUpdated(VacationTypeUpdatedEvent.of(vacationType));

        verify(applicationEventPublisher).publishEvent(eventCaptor.capture());
        final VacationTypeUpdatedEventDTO actualEvent = eventCaptor.getValue();

        assertThat(actualEvent.id()).isNotNull();
        assertThat(actualEvent.tenantId()).isEqualTo(tenant);
        assertThat(actualEvent.category()).isEqualTo("HOLIDAY");
        assertThat(actualEvent.requiresApprovalToApply()).isFalse();
        assertThat(actualEvent.requiresApprovalToCancel()).isFalse();
        assertThat(actualEvent.color()).isEqualTo("YELLOW");
        assertThat(actualEvent.visibleToEveryone()).isTrue();
    }

    @Test
    void handlesUpdatedEventOfProvidedVacationType() {
        String tenant = "default";
        when(tenantSupplier.get()).thenReturn(tenant);

        ProvidedVacationType providedVacationType = mock(ProvidedVacationType.class);
        when(providedVacationType.getId()).thenReturn(42L);
        when(providedVacationType.getCategory()).thenReturn(VacationCategory.HOLIDAY);
        when(providedVacationType.isRequiresApprovalToApply()).thenReturn(true);
        when(providedVacationType.isRequiresApprovalToCancel()).thenReturn(true);
        when(providedVacationType.getColor()).thenReturn(VacationTypeColor.YELLOW);
        when(providedVacationType.isVisibleToEveryone()).thenReturn(true);
        when(providedVacationType.getLabel(Locale.GERMAN)).thenReturn("Urlaub");
        when(providedVacationType.getLabel(Locale.ENGLISH)).thenReturn("Holiday");
        when(providedVacationType.getLabel(forLanguageTag("de-AT"))).thenReturn("Urlaub");
        when(providedVacationType.getLabel(forLanguageTag("el"))).thenReturn("Holiday");
        VacationTypeUpdatedEvent event = VacationTypeUpdatedEvent.of(providedVacationType);

        vacationTypeEventHandlerExtension.onVacationTypeUpdated(event);

        verify(applicationEventPublisher).publishEvent(eventCaptor.capture());
        VacationTypeUpdatedEventDTO capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent.id()).isNotNull();
        assertThat(capturedEvent.tenantId()).isEqualTo(tenant);
        assertThat(capturedEvent.sourceId()).isEqualTo(providedVacationType.getId());
        assertThat(capturedEvent.category()).isEqualTo(providedVacationType.getCategory().name());
        assertThat(capturedEvent.requiresApprovalToApply()).isEqualTo(providedVacationType.isRequiresApprovalToApply());
        assertThat(capturedEvent.requiresApprovalToCancel()).isEqualTo(providedVacationType.isRequiresApprovalToCancel());
        assertThat(capturedEvent.color()).isEqualTo(providedVacationType.getColor().name());
        assertThat(capturedEvent.visibleToEveryone()).isEqualTo(providedVacationType.isVisibleToEveryone());
        assertThat(capturedEvent.label()).containsEntry(Locale.GERMAN, "Urlaub");
        assertThat(capturedEvent.label()).containsEntry(Locale.ENGLISH, "Holiday");
        assertThat(capturedEvent.label()).containsEntry(forLanguageTag("de-AT"), "Urlaub");
        assertThat(capturedEvent.label()).containsEntry(forLanguageTag("el"), "Holiday");

    }

    @Test
    void handlesUpdatedEventOfCustomVacationType() {
        String tenant = "default";
        when(tenantSupplier.get()).thenReturn(tenant);

        CustomVacationType customVacationType = mock(CustomVacationType.class);

        when(customVacationType.getId()).thenReturn(42L);
        when(customVacationType.getCategory()).thenReturn(VacationCategory.UNPAIDLEAVE);
        when(customVacationType.isRequiresApprovalToApply()).thenReturn(false);
        when(customVacationType.isRequiresApprovalToCancel()).thenReturn(false);
        when(customVacationType.getColor()).thenReturn(VacationTypeColor.YELLOW);
        when(customVacationType.isVisibleToEveryone()).thenReturn(true);
        when(customVacationType.labelsByLocale()).thenReturn(Map.of(Locale.GERMAN, new VacationTypeLabel(Locale.GERMAN, "Gammeln")));

        VacationTypeUpdatedEvent event = VacationTypeUpdatedEvent.of(customVacationType);

        vacationTypeEventHandlerExtension.onVacationTypeUpdated(event);

        verify(applicationEventPublisher).publishEvent(eventCaptor.capture());
        VacationTypeUpdatedEventDTO capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent.id()).isNotNull();
        assertThat(capturedEvent.tenantId()).isEqualTo(tenant);
        assertThat(capturedEvent.sourceId()).isEqualTo(customVacationType.getId());
        assertThat(capturedEvent.category()).isEqualTo(customVacationType.getCategory().name());
        assertThat(capturedEvent.requiresApprovalToApply()).isEqualTo(customVacationType.isRequiresApprovalToApply());
        assertThat(capturedEvent.requiresApprovalToCancel()).isEqualTo(customVacationType.isRequiresApprovalToCancel());
        assertThat(capturedEvent.color()).isEqualTo(customVacationType.getColor().name());
        assertThat(capturedEvent.visibleToEveryone()).isEqualTo(customVacationType.isVisibleToEveryone());
        assertThat(capturedEvent.label()).containsEntry(Locale.GERMAN, "Gammeln");

    }

    @Test
    void detectsUnsupportedVacationType() {

        when(tenantSupplier.get()).thenReturn("default");

        VacationType<?> unsupportedVacationType = mock(VacationType.class);
        when(unsupportedVacationType.getId()).thenReturn(42L);
        when(unsupportedVacationType.getCategory()).thenReturn(VacationCategory.UNPAIDLEAVE);
        when(unsupportedVacationType.isRequiresApprovalToApply()).thenReturn(false);
        when(unsupportedVacationType.isRequiresApprovalToCancel()).thenReturn(false);
        when(unsupportedVacationType.getColor()).thenReturn(VacationTypeColor.YELLOW);
        when(unsupportedVacationType.isVisibleToEveryone()).thenReturn(true);

        VacationTypeUpdatedEvent event = VacationTypeUpdatedEvent.of(unsupportedVacationType);

        assertThatThrownBy(() -> vacationTypeEventHandlerExtension.onVacationTypeUpdated(event))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Unsupported vacation type: " + unsupportedVacationType);
    }
}
