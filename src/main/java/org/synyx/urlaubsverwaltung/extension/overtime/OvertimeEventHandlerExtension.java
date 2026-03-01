package org.synyx.urlaubsverwaltung.extension.overtime;

import de.focus_shift.urlaubsverwaltung.extension.api.overtime.OvertimeCreatedEventDTO;
import de.focus_shift.urlaubsverwaltung.extension.api.overtime.OvertimeSettingsActivatedEventDTO;
import de.focus_shift.urlaubsverwaltung.extension.api.overtime.OvertimeSettingsDeactivatedEventDTO;
import de.focus_shift.urlaubsverwaltung.extension.api.overtime.OvertimeUpdatedEventDTO;
import de.focus_shift.urlaubsverwaltung.extension.api.tenancy.TenantSupplier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.synyx.urlaubsverwaltung.extension.ConditionalOnExtensionsEnabled;
import org.synyx.urlaubsverwaltung.overtime.OvertimeCreatedEvent;
import org.synyx.urlaubsverwaltung.overtime.OvertimeSettingsActivatedEvent;
import org.synyx.urlaubsverwaltung.overtime.OvertimeSettingsDeactivatedEvent;
import org.synyx.urlaubsverwaltung.overtime.OvertimeUpdatedEvent;

@Component
@ConditionalOnExtensionsEnabled
class OvertimeEventHandlerExtension {

    private final TenantSupplier tenantSupplier;
    private final ApplicationEventPublisher applicationEventPublisher;

    OvertimeEventHandlerExtension(TenantSupplier tenantSupplier,
                                  ApplicationEventPublisher applicationEventPublisher) {
        this.tenantSupplier = tenantSupplier;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @EventListener
    void on(OvertimeCreatedEvent event) {
        applicationEventPublisher.publishEvent(
            OvertimeCreatedEventDTO.create(event.overtimeId(), tenantSupplier.get(), event.username(),
                event.startDate(), event.endDate(), event.duration())
        );
    }

    @EventListener
    void on(OvertimeUpdatedEvent event) {
        applicationEventPublisher.publishEvent(
            OvertimeUpdatedEventDTO.create(event.overtimeId(), tenantSupplier.get(), event.username(),
                event.startDate(), event.endDate(), event.duration())
        );
    }

    @EventListener
    void on(OvertimeSettingsActivatedEvent event) {
        applicationEventPublisher.publishEvent(
            OvertimeSettingsActivatedEventDTO.create(tenantSupplier.get())
        );
    }

    @EventListener
    void on(OvertimeSettingsDeactivatedEvent event) {
        applicationEventPublisher.publishEvent(
            OvertimeSettingsDeactivatedEventDTO.create(tenantSupplier.get())
        );
    }
}
