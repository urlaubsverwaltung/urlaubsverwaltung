package org.synyx.urlaubsverwaltung.extension.companyvacation;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.synyx.urlaubsverwaltung.extension.application.ApplicationEventHandlerExtension;
import org.synyx.urlaubsverwaltung.tenancy.configuration.single.ConditionalOnSingleTenantMode;

@Component
@ConditionalOnProperty(value = "uv.extensions.settings.republish.enabled", havingValue = "true")
@ConditionalOnBean(ApplicationEventHandlerExtension.class)
@ConditionalOnSingleTenantMode
class SettingsEventRepublisherSingleTenant {

    private final SettingsEventRepublisher settingsEventRepublisher;

    SettingsEventRepublisherSingleTenant(SettingsEventRepublisher settingsEventRepublisher) {
        this.settingsEventRepublisher = settingsEventRepublisher;
    }

    @Async
    @EventListener(ApplicationStartedEvent.class)
    public void republishEvents() {
        settingsEventRepublisher.republishEvents();
    }
}
