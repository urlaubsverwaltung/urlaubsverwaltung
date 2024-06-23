package org.synyx.urlaubsverwaltung.settings;


import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.synyx.urlaubsverwaltung.tenancy.configuration.single.IsSingleTenantMode;

@Component
@Conditional(IsSingleTenantMode.class)
class InitialDefaultSettingsConfigurerSingleTenant {


    private final SettingsService settingsService;

    InitialDefaultSettingsConfigurerSingleTenant(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    @EventListener(ApplicationStartedEvent.class)
    void insertDefaultSettings() {
        settingsService.insertDefaultSettings();
    }
}
