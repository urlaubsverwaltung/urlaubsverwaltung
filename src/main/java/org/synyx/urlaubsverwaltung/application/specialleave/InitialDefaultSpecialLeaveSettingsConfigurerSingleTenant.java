package org.synyx.urlaubsverwaltung.application.specialleave;


import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.synyx.urlaubsverwaltung.tenancy.configuration.single.IsSingleTenantMode;

@Component
@Conditional(IsSingleTenantMode.class)
public class InitialDefaultSpecialLeaveSettingsConfigurerSingleTenant {

    private final SpecialLeaveSettingsService specialLeaveSettingsService;

    public InitialDefaultSpecialLeaveSettingsConfigurerSingleTenant(SpecialLeaveSettingsService specialLeaveSettingsService) {
        this.specialLeaveSettingsService = specialLeaveSettingsService;
    }

    @EventListener(ApplicationStartedEvent.class)
    void insertDefaultSpecialLeaveSettings() {
        this.specialLeaveSettingsService.insertDefaultSpecialLeaveSettings();
    }

}
