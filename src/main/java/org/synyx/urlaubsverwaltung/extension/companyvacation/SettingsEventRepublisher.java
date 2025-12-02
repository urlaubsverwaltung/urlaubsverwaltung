package org.synyx.urlaubsverwaltung.extension.companyvacation;

import org.slf4j.Logger;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.synyx.urlaubsverwaltung.publicholiday.PublicHolidaysSettings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;
import org.synyx.urlaubsverwaltung.settings.WorkingDurationForChristmasEveUpdatedEvent;
import org.synyx.urlaubsverwaltung.settings.WorkingDurationForNewYearsEveUpdatedEvent;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Used for different use cases
 * - republishing events on application start (single tenant)
 * - republishing events on turn of year
 */
@Component
public class SettingsEventRepublisher {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final SettingsService settingsService;
    private final ApplicationEventPublisher applicationEventPublisher;

    SettingsEventRepublisher(
        SettingsService settingsService,
        ApplicationEventPublisher applicationEventPublisher
    ) {
        this.settingsService = settingsService;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public void republishEvents() {
        LOG.info("Republishing all company vacation events based on public holiday settings (working duration Christmas and new years eve)");

        final PublicHolidaysSettings publicHolidaysSettings = settingsService.getSettings().getPublicHolidaysSettings();
        applicationEventPublisher.publishEvent(new WorkingDurationForChristmasEveUpdatedEvent(publicHolidaysSettings.getWorkingDurationForChristmasEve()));
        applicationEventPublisher.publishEvent(new WorkingDurationForNewYearsEveUpdatedEvent(publicHolidaysSettings.getWorkingDurationForNewYearsEve()));

        LOG.info("Republished all company vacation events based on public holiday settings (working duration Christmas and new years eve)");
    }
}
