package org.synyx.urlaubsverwaltung.extension.companyvacation;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.synyx.urlaubsverwaltung.tenancy.configuration.single.ConditionalOnSingleTenantMode;

@Component
@ConditionalOnProperty(value = "uv.extensions.companyvacation.republish.enabled", havingValue = "true")
@ConditionalOnBean(CompanyVacationEventHandlerExtension.class)
@ConditionalOnSingleTenantMode
class CompanyVacationEventRepublisherSingleTenant {

    private final CompanyVacationEventRepublisher companyVacationEventRepublisher;

    CompanyVacationEventRepublisherSingleTenant(CompanyVacationEventRepublisher companyVacationEventRepublisher) {
        this.companyVacationEventRepublisher = companyVacationEventRepublisher;
    }

    @Async
    @EventListener(ApplicationStartedEvent.class)
    public void republishEvents() {
        companyVacationEventRepublisher.republishEvents();
    }
}
