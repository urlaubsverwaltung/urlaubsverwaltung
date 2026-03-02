package org.synyx.urlaubsverwaltung.extension.department;

import de.focus_shift.urlaubsverwaltung.extension.api.department.DepartmentCreatedEventDTO;
import de.focus_shift.urlaubsverwaltung.extension.api.department.DepartmentDeletedEventDTO;
import de.focus_shift.urlaubsverwaltung.extension.api.department.DepartmentHeadAssignedEventDTO;
import de.focus_shift.urlaubsverwaltung.extension.api.department.DepartmentHeadUnassignedEventDTO;
import de.focus_shift.urlaubsverwaltung.extension.api.department.DepartmentMemberAssignedEventDTO;
import de.focus_shift.urlaubsverwaltung.extension.api.department.DepartmentMemberUnassignedEventDTO;
import de.focus_shift.urlaubsverwaltung.extension.api.department.DepartmentUpdatedEventDTO;
import de.focus_shift.urlaubsverwaltung.extension.api.tenancy.TenantSupplier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.synyx.urlaubsverwaltung.department.DepartmentCreatedEvent;
import org.synyx.urlaubsverwaltung.extension.ConditionalOnExtensionsEnabled;
import org.synyx.urlaubsverwaltung.department.DepartmentDeletedEvent;
import org.synyx.urlaubsverwaltung.department.DepartmentHeadAssignedEvent;
import org.synyx.urlaubsverwaltung.department.DepartmentHeadUnassignedEvent;
import org.synyx.urlaubsverwaltung.department.DepartmentMemberAssignedEvent;
import org.synyx.urlaubsverwaltung.department.DepartmentMemberUnassignedEvent;
import org.synyx.urlaubsverwaltung.department.DepartmentUpdatedEvent;

@Component
@ConditionalOnExtensionsEnabled
class DepartmentEventHandlerExtension {

    private final TenantSupplier tenantSupplier;
    private final ApplicationEventPublisher applicationEventPublisher;

    DepartmentEventHandlerExtension(TenantSupplier tenantSupplier,
                                    ApplicationEventPublisher applicationEventPublisher) {
        this.tenantSupplier = tenantSupplier;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @EventListener
    void on(DepartmentCreatedEvent event) {
        applicationEventPublisher.publishEvent(
            DepartmentCreatedEventDTO.create(
                event.departmentId(), tenantSupplier.get(), event.departmentName(),
                event.memberCount()
            )
        );
    }

    @EventListener
    void on(DepartmentUpdatedEvent event) {
        applicationEventPublisher.publishEvent(
            DepartmentUpdatedEventDTO.create(
                event.departmentId(), tenantSupplier.get(), event.departmentName(),
                event.memberCount()
            )
        );
    }

    @EventListener
    void on(DepartmentDeletedEvent event) {
        applicationEventPublisher.publishEvent(
            DepartmentDeletedEventDTO.create(event.departmentId(), tenantSupplier.get())
        );
    }

    @EventListener
    void on(DepartmentMemberAssignedEvent event) {
        applicationEventPublisher.publishEvent(
            DepartmentMemberAssignedEventDTO.create(
                event.departmentId(), tenantSupplier.get(), event.username()
            )
        );
    }

    @EventListener
    void on(DepartmentMemberUnassignedEvent event) {
        applicationEventPublisher.publishEvent(
            DepartmentMemberUnassignedEventDTO.create(
                event.departmentId(), tenantSupplier.get(), event.username()
            )
        );
    }

    @EventListener
    void on(DepartmentHeadAssignedEvent event) {
        applicationEventPublisher.publishEvent(
            DepartmentHeadAssignedEventDTO.create(
                event.departmentId(), tenantSupplier.get(), event.departmentHeadUsername()
            )
        );
    }

    @EventListener
    void on(DepartmentHeadUnassignedEvent event) {
        applicationEventPublisher.publishEvent(
            DepartmentHeadUnassignedEventDTO.create(
                event.departmentId(), tenantSupplier.get(), event.departmentHeadUsername()
            )
        );
    }
}
