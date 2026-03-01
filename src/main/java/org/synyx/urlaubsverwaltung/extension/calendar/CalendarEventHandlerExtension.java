package org.synyx.urlaubsverwaltung.extension.calendar;

import de.focus_shift.urlaubsverwaltung.extension.api.calendar.CompanyCalendarDisabledEventDTO;
import de.focus_shift.urlaubsverwaltung.extension.api.calendar.CompanyCalendarEnabledEventDTO;
import de.focus_shift.urlaubsverwaltung.extension.api.calendar.PersonalCalendarCreatedEventDTO;
import de.focus_shift.urlaubsverwaltung.extension.api.calendar.PersonalCalendarDeletedEventDTO;
import de.focus_shift.urlaubsverwaltung.extension.api.tenancy.TenantSupplier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.synyx.urlaubsverwaltung.calendar.CompanyCalendarDisabledEvent;
import org.synyx.urlaubsverwaltung.calendar.CompanyCalendarEnabledEvent;
import org.synyx.urlaubsverwaltung.calendar.PersonalCalendarCreatedEvent;
import org.synyx.urlaubsverwaltung.calendar.PersonalCalendarDeletedEvent;
import org.synyx.urlaubsverwaltung.extension.ConditionalOnExtensionsEnabled;

@Component
@ConditionalOnExtensionsEnabled
class CalendarEventHandlerExtension {

    private final TenantSupplier tenantSupplier;
    private final ApplicationEventPublisher applicationEventPublisher;

    CalendarEventHandlerExtension(TenantSupplier tenantSupplier,
                                  ApplicationEventPublisher applicationEventPublisher) {
        this.tenantSupplier = tenantSupplier;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @EventListener
    void on(PersonalCalendarCreatedEvent event) {
        applicationEventPublisher.publishEvent(
            PersonalCalendarCreatedEventDTO.create(tenantSupplier.get(), event.username())
        );
    }

    @EventListener
    void on(PersonalCalendarDeletedEvent event) {
        applicationEventPublisher.publishEvent(
            PersonalCalendarDeletedEventDTO.create(tenantSupplier.get(), event.username())
        );
    }

    @EventListener
    void on(CompanyCalendarEnabledEvent event) {
        applicationEventPublisher.publishEvent(
            CompanyCalendarEnabledEventDTO.create(tenantSupplier.get())
        );
    }

    @EventListener
    void on(CompanyCalendarDisabledEvent event) {
        applicationEventPublisher.publishEvent(
            CompanyCalendarDisabledEventDTO.create(tenantSupplier.get())
        );
    }
}
