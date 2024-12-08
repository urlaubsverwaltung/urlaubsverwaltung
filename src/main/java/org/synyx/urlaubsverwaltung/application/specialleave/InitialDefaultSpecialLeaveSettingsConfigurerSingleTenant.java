package org.synyx.urlaubsverwaltung.application.specialleave;


import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.synyx.urlaubsverwaltung.tenancy.configuration.single.ConditionalOnSingleTenantMode;

@Component
@ConditionalOnSingleTenantMode
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
