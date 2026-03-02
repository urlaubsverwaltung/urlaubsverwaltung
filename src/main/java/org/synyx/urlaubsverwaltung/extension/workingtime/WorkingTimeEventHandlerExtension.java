package org.synyx.urlaubsverwaltung.extension.workingtime;

import de.focus_shift.urlaubsverwaltung.extension.api.tenancy.TenantSupplier;
import de.focus_shift.urlaubsverwaltung.extension.api.workingtime.WorkingTimeConfiguredEventDTO;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.synyx.urlaubsverwaltung.extension.ConditionalOnExtensionsEnabled;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeConfiguredEvent;

import java.time.DayOfWeek;
import java.util.List;

@Component
@ConditionalOnExtensionsEnabled
class WorkingTimeEventHandlerExtension {

    private final TenantSupplier tenantSupplier;
    private final ApplicationEventPublisher applicationEventPublisher;

    WorkingTimeEventHandlerExtension(TenantSupplier tenantSupplier,
                                     ApplicationEventPublisher applicationEventPublisher) {
        this.tenantSupplier = tenantSupplier;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @EventListener
    void on(WorkingTimeConfiguredEvent event) {

        final List<String> workingDayNames = event.workingDays().stream()
            .map(DayOfWeek::of)
            .map(DayOfWeek::name)
            .toList();

        applicationEventPublisher.publishEvent(
            WorkingTimeConfiguredEventDTO.create(tenantSupplier.get(), event.username(),
                event.validFrom(), workingDayNames, event.federalState())
        );
    }
}
