package org.synyx.urlaubsverwaltung.calendarintegration;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.absence.Absence;
import org.synyx.urlaubsverwaltung.settings.CalendarSettings;
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

        LOG.info("The following calendar provider is configured: {}", calendarService.getCalendarProvider().getClass());
    }

    @Override
    public Optional<String> addAbsence(Absence absence) {
        CalendarSettings calendarSettings = this.settingsService.getSettings().getCalendarSettings();

        return calendarService.getCalendarProvider().add(absence, calendarSettings);
    }


    @Override
    public void update(Absence absence, String eventId) {
        CalendarSettings calendarSettings = this.settingsService.getSettings().getCalendarSettings();

        calendarService.getCalendarProvider().update(absence, eventId, calendarSettings);
    }


    @Override
    public void deleteAbsence(String eventId) {
        CalendarSettings calendarSettings = this.settingsService.getSettings().getCalendarSettings();

        calendarService.getCalendarProvider().delete(eventId, calendarSettings);
    }

    @Override
    public void checkCalendarSyncSettings() {
        CalendarSettings calendarSettings = this.settingsService.getSettings().getCalendarSettings();

        calendarService.getCalendarProvider().checkCalendarSyncSettings(calendarSettings);
    }
}
