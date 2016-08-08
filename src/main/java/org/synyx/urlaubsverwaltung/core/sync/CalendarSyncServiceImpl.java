package org.synyx.urlaubsverwaltung.core.sync;

import org.apache.log4j.Logger;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import org.synyx.urlaubsverwaltung.core.settings.CalendarSettings;
import org.synyx.urlaubsverwaltung.core.settings.ExchangeCalendarSettings;
import org.synyx.urlaubsverwaltung.core.settings.SettingsService;
import org.synyx.urlaubsverwaltung.core.sync.absence.Absence;
import org.synyx.urlaubsverwaltung.core.sync.providers.exchange.ExchangeCalendarProviderService;
import org.synyx.urlaubsverwaltung.core.sync.providers.google.GoogleCalendarSyncProviderService;

import java.util.Optional;


/**
 * Implementation of {@link CalendarSyncService}.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Service
public class CalendarSyncServiceImpl implements CalendarSyncService {

    private static final Logger LOG = Logger.getLogger(CalendarSyncServiceImpl.class);

    private final SettingsService settingsService;
    private final ExchangeCalendarProviderService exchangeCalendarProviderService;
    private GoogleCalendarSyncProviderService googleCalendarSyncProviderService;

    @Autowired
    public CalendarSyncServiceImpl(SettingsService settingsService,
                                   ExchangeCalendarProviderService exchangeCalendarProviderService,
                                   GoogleCalendarSyncProviderService googleCalendarSyncProviderService) {

        this.settingsService = settingsService;
        this.exchangeCalendarProviderService = exchangeCalendarProviderService;
        this.googleCalendarSyncProviderService = googleCalendarSyncProviderService;
    }

    @Override
    public Optional<String> addAbsence(Absence absence) {

        CalendarSettings calendarSettings = settingsService.getSettings().getCalendarSettings();
        ExchangeCalendarSettings exchangeCalendarSettings = calendarSettings.getExchangeCalendarSettings();

        googleCalendarSyncProviderService.add(absence, calendarSettings);

        if (exchangeCalendarSettings.isActive()) {
            return exchangeCalendarProviderService.add(absence, calendarSettings);
        }

        LOG.info(String.format("No calendar provider configured to add event: %s", absence));

        return Optional.empty();
    }


    @Override
    public void update(Absence absence, String eventId) {

        CalendarSettings calendarSettings = settingsService.getSettings().getCalendarSettings();
        ExchangeCalendarSettings exchangeCalendarSettings = calendarSettings.getExchangeCalendarSettings();

        if (exchangeCalendarSettings.isActive()) {
            exchangeCalendarProviderService.update(absence, eventId, calendarSettings);

            return;
        }

        LOG.info(String.format("No calendar provider configured to update event: %s, eventId %s", absence, eventId));
    }


    @Override
    public void deleteAbsence(String eventId) {

        CalendarSettings calendarSettings = settingsService.getSettings().getCalendarSettings();
        ExchangeCalendarSettings exchangeCalendarSettings = calendarSettings.getExchangeCalendarSettings();

        if (exchangeCalendarSettings.isActive()) {
            exchangeCalendarProviderService.delete(eventId, calendarSettings);

            return;
        }

        LOG.info(String.format("No calendar provider configured to delete event '%s'", eventId));
    }

    @Override
    public void checkCalendarSyncSettings() {
        
    }
}