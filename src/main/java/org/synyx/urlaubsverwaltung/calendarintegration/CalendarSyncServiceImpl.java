package org.synyx.urlaubsverwaltung.calendarintegration;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.absence.Absence;
import org.synyx.urlaubsverwaltung.settings.SettingsService;

import java.util.Optional;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;


/**
 * Implementation of {@link CalendarSyncService}.
 */
@Deprecated(since = "4.0.0", forRemoval = true)
@Service
public class CalendarSyncServiceImpl implements CalendarSyncService {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final SettingsService settingsService;
    private final CalendarService calendarService;

    @Autowired
    public CalendarSyncServiceImpl(SettingsService settingsService, CalendarService calendarService) {
        this.settingsService = settingsService;
        this.calendarService = calendarService;
        LOG.debug("The following calendar provider is configured: {}", calendarService.getCalendarProvider().getClass());
    }

    @Override
    public boolean isRealProviderConfigured() {
        return calendarService.getCalendarProvider().isRealProviderConfigured();
    }

    @Override
    public Optional<String> addAbsence(Absence absence) {
        return calendarService.getCalendarProvider().add(absence, getCalendarSettings());
    }

    @Override
    public void update(Absence absence, String eventId) {
        calendarService.getCalendarProvider().update(absence, eventId, getCalendarSettings());
    }

    @Override
    public void deleteAbsence(String eventId) {
        calendarService.getCalendarProvider().delete(eventId, getCalendarSettings());
    }

    @Override
    public void checkCalendarSyncSettings() {
        calendarService.getCalendarProvider().checkCalendarSyncSettings(getCalendarSettings());
    }

    private CalendarSettings getCalendarSettings() {
        return this.settingsService.getSettings().getCalendarSettings();
    }
}
