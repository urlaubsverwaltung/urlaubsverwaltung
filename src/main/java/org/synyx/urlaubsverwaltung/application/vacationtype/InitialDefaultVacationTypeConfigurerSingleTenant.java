package org.synyx.urlaubsverwaltung.application.vacationtype;

import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.synyx.urlaubsverwaltung.tenancy.configuration.single.ConditionalOnSingleTenantMode;

@Component
@ConditionalOnSingleTenantMode
class InitialDefaultVacationTypeConfigurerSingleTenant {

    private final VacationTypeService vacationTypeService;

    InitialDefaultVacationTypeConfigurerSingleTenant(VacationTypeService vacationTypeService) {
        this.vacationTypeService = vacationTypeService;
    }

    @EventListener(ApplicationStartedEvent.class)
    void insertDefaultVacationTypes() {
        vacationTypeService.insertDefaultVacationTypes();
    }
}
