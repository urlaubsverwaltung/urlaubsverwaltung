package org.synyx.urlaubsverwaltung.extension.vacationtype;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.synyx.urlaubsverwaltung.tenancy.configuration.single.ConditionalOnSingleTenantMode;

@Component
@ConditionalOnProperty(value = "uv.extensions.vacationtype.republish.enabled", havingValue = "true")
@ConditionalOnBean(VacationTypeEventHandlerExtension.class)
@ConditionalOnSingleTenantMode
class VacationTypeEventRepublisherSingleTenant {

    private final VacationTypeEventRepublisher vacationTypeEventRepublisher;

    VacationTypeEventRepublisherSingleTenant(VacationTypeEventRepublisher vacationTypeEventRepublisher) {
        this.vacationTypeEventRepublisher = vacationTypeEventRepublisher;
    }

    @Async
    @EventListener(ApplicationStartedEvent.class)
    public void republishEvents() {
        vacationTypeEventRepublisher.republishEvents();
    }
}
