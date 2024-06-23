package org.synyx.urlaubsverwaltung.application.vacationtype;

import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.synyx.urlaubsverwaltung.tenancy.configuration.single.IsSingleTenantMode;

@Component
@Conditional(IsSingleTenantMode.class)
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
