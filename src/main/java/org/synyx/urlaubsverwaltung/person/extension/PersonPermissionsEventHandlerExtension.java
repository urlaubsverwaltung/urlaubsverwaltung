package org.synyx.urlaubsverwaltung.person.extension;

import de.focus_shift.urlaubsverwaltung.extension.api.person.PersonPermissionsChangedEventDTO;
import de.focus_shift.urlaubsverwaltung.extension.api.tenancy.TenantSupplier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.synyx.urlaubsverwaltung.extension.ConditionalOnExtensionsEnabled;
import org.synyx.urlaubsverwaltung.person.PersonPermissionsChangedEvent;
import org.synyx.urlaubsverwaltung.person.Role;

import java.util.Collection;
import java.util.List;

@Component
@ConditionalOnExtensionsEnabled
class PersonPermissionsEventHandlerExtension {

    private final TenantSupplier tenantSupplier;
    private final ApplicationEventPublisher applicationEventPublisher;

    PersonPermissionsEventHandlerExtension(TenantSupplier tenantSupplier,
                                           ApplicationEventPublisher applicationEventPublisher) {
        this.tenantSupplier = tenantSupplier;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @EventListener
    void on(PersonPermissionsChangedEvent event) {
        applicationEventPublisher.publishEvent(
            PersonPermissionsChangedEventDTO.create(
                tenantSupplier.get(),
                event.username(),
                toRoleStrings(event.currentPermissions()),
                toRoleStrings(event.grantedPermissions()),
                toRoleStrings(event.revokedPermissions())
            )
        );
    }

    private static List<String> toRoleStrings(Collection<Role> roles) {
        return roles.stream()
            .filter(role -> !Role.INACTIVE.equals(role))
            .map(Role::name)
            .toList();
    }
}
