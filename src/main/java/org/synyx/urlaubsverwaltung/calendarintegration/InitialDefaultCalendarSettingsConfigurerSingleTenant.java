package org.synyx.urlaubsverwaltung.calendarintegration;


import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.synyx.urlaubsverwaltung.tenancy.configuration.single.ConditionalOnSingleTenantMode;

@Component
@ConditionalOnSingleTenantMode
class InitialDefaultCalendarSettingsConfigurerSingleTenant {


    private final CalendarSettingsService settingsService;

    InitialDefaultCalendarSettingsConfigurerSingleTenant(CalendarSettingsService settingsService) {
        this.settingsService = settingsService;
    }

    @EventListener(ApplicationStartedEvent.class)
    void insertDefaultSettings() {
        settingsService.insertDefaultCalendarSettings();
    }
}
