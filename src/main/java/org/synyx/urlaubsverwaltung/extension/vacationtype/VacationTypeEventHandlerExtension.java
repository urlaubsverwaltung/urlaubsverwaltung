package org.synyx.urlaubsverwaltung.extension.vacationtype;

import de.focus_shift.urlaubsverwaltung.extension.api.tenancy.TenantSupplier;
import de.focus_shift.urlaubsverwaltung.extension.api.vacationtype.VacationTypeUpdatedEventDTO;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.synyx.urlaubsverwaltung.application.vacationtype.CustomVacationType;
import org.synyx.urlaubsverwaltung.application.vacationtype.ProvidedVacationType;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationType;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeCreatedEvent;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeLabel;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeUpdatedEvent;
import org.synyx.urlaubsverwaltung.settings.SupportedLanguages;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

@Component
@ConditionalOnProperty(value = "uv.extensions.enabled", havingValue = "true")
class VacationTypeEventHandlerExtension {

    private final TenantSupplier tenantSupplier;
    private final ApplicationEventPublisher applicationEventPublisher;


    VacationTypeEventHandlerExtension(
        TenantSupplier tenantSupplier,
        ApplicationEventPublisher applicationEventPublisher
    ) {
        this.tenantSupplier = tenantSupplier;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @EventListener
    @Async
    void onVacationTypeCreated(VacationTypeCreatedEvent event) {
        publishVacationType(event.vacationType());
    }

    @EventListener
    @Async
    void onVacationTypeUpdated(VacationTypeUpdatedEvent event) {
        publishVacationType(event.updatedVacationType());
    }

    private void publishVacationType(VacationType<?> vacationType) {
        final String tenant = tenantSupplier.get();
        final VacationTypeUpdatedEventDTO updatedEventDTO = toVacationTypeDTO(tenant, vacationType);
        applicationEventPublisher.publishEvent(updatedEventDTO);
    }

    private VacationTypeUpdatedEventDTO toVacationTypeDTO(String tenantId, VacationType<?> vacationType) {
        return VacationTypeUpdatedEventDTO.builder()
            .id(UUID.randomUUID())
            .tenantId(tenantId)
            .sourceId(vacationType.getId())
            .category(vacationType.getCategory().name())
            .requiresApprovalToApply(vacationType.isRequiresApprovalToApply())
            .requiresApprovalToCancel(vacationType.isRequiresApprovalToCancel())
            .color(vacationType.getColor().name())
            .visibleToEveryone(vacationType.isVisibleToEveryone())
            .label(toLabels(vacationType))
            .build();
    }

    private Map<Locale, String> toLabels(VacationType<?> vacationType) {
        return switch (vacationType) {
            case ProvidedVacationType providedVacationType -> toLabels(providedVacationType);
            case CustomVacationType customVacationType -> toLabels(customVacationType);
            default -> throw new IllegalArgumentException("Unsupported vacation type: " + vacationType);
        };
    }

    private Map<Locale, String> toLabels(ProvidedVacationType providedVacationType) {
        return Arrays.stream(SupportedLanguages.values())
            .map(SupportedLanguages::getLocale)
            .collect(toMap(identity(), providedVacationType::getLabel));
    }

    private Map<Locale, String> toLabels(CustomVacationType customVacationType) {
        return customVacationType.labelsByLocale().values()
            .stream()
            .collect(toMap(VacationTypeLabel::locale, VacationTypeLabel::label));
    }
}
